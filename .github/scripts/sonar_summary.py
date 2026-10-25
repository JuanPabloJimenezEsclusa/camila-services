#!/usr/bin/env python3
"""Render a Markdown SonarQube summary from the SonarCloud Web API.

Runs after ``sonar:sonar``. Waits for the Compute Engine task reported in
``target/sonar/report-task.txt``, then prints the quality gate status and the
main metrics for the analysed branch.

Environment:
    SONAR_TOKEN        Token with ``Browse`` permission (required).
    SONAR_HOST_URL     API base URL, defaults to ``https://sonarcloud.io``.
    SONAR_PROJECT_KEY  Project key, defaults to the one declared in ``pom.xml``.
    SONAR_BRANCH       Branch name, defaults to ``develop``.
"""

from __future__ import annotations

import base64
import glob
import json
import os
import sys
import time
import urllib.error
import urllib.parse
import urllib.request

HOST = os.environ.get("SONAR_HOST_URL", "https://sonarcloud.io").rstrip("/")
PROJECT = os.environ.get("SONAR_PROJECT_KEY", "JuanPabloJimenezEsclusa_camila-services")
BRANCH = os.environ.get("SONAR_BRANCH", "develop")
TOKEN = os.environ.get("SONAR_TOKEN", "")
DASHBOARD = f"{HOST}/dashboard?id={PROJECT}&branch={BRANCH}"
TASK_TIMEOUT = int(os.environ.get("SONAR_TASK_TIMEOUT", "180"))

METRICS = (
  "bugs",
  "vulnerabilities",
  "code_smells",
  "coverage",
  "duplicated_lines_density",
  "sqale_index",
  "reliability_rating",
  "security_rating",
  "sqale_rating",
  "security_hotspots",
)
LABELS = {
  "bugs": "🐛 Bugs",
  "vulnerabilities": "🔓 Vulnerabilities",
  "code_smells": "🧹 Code smells",
  "coverage": "🧪 Coverage",
  "duplicated_lines_density": "📑 Duplications",
  "sqale_index": "⏳ Technical debt",
  "security_hotspots": "🔥 Security hotspots",
  "reliability_rating": "🛡️ Reliability",
  "security_rating": "🔒 Security",
  "sqale_rating": "🔧 Maintainability",
}
SUFFIX = {"coverage": "%", "duplicated_lines_density": "%"}
RATINGS = {"1.0": "A", "1.1": "A", "2.0": "B", "2.1": "B",
           "3.0": "C", "3.1": "C", "4.0": "D", "4.1": "D", "5.0": "E", "5.1": "E"}
RATINGS_METRICS = {"reliability_rating", "security_rating", "sqale_rating"}

class ApiError(RuntimeError):
  """Raised when the SonarCloud API cannot be queried."""

def api(path: str, params: dict[str, str]) -> dict:
  """Perform an authenticated GET against the SonarCloud Web API."""
  url = f"{HOST}{path}?{urllib.parse.urlencode(params)}"
  credentials = base64.b64encode(f"{TOKEN}:".encode()).decode()
  request = urllib.request.Request(url, headers={"Authorization": f"Basic {credentials}"})
  try:
    with urllib.request.urlopen(request, timeout=30) as response:
      return json.load(response)
  except urllib.error.HTTPError as error:
    raise ApiError(f"{path} returned HTTP {error.code}") from error
  except (urllib.error.URLError, json.JSONDecodeError) as error:
    raise ApiError(f"{path} failed: {error}") from error

def wait_for_analysis() -> str | None:
  """Wait until the Compute Engine task finishes. Returns an error message on failure."""
  reports = sorted(glob.glob("**/target/sonar/report-task.txt", recursive=True))
  if not reports:
    return None

  task_id = None
  for line in open(reports[0], encoding="utf-8"):
    if line.startswith("ceTaskId="):
      task_id = line.split("=", 1)[1].strip()
      break
  if not task_id:
    return None

  deadline = time.time() + TASK_TIMEOUT
  while time.time() < deadline:
    task = api("/api/ce/task", {"id": task_id}).get("task", {})
    status = task.get("status")
    if status == "SUCCESS":
      return None
    if status in {"FAILED", "CANCELED"}:
      message = (task.get("errorMessage") or "").strip()
      return f"{status}: {message}" if message else str(status)
    time.sleep(5)
  return f"not finished after {TASK_TIMEOUT}s"

def render() -> str:
  """Build the Markdown document."""
  lines = ["## 📊 SonarQube Code Analysis", ""]

  if not TOKEN:
    lines.append("### ⚠️ `SONAR_TOKEN` is not available")
    lines.append("")
    lines.append("Set the token to publish quality gate and metric results.")
    return "\n".join(lines) + "\n"

  lines.append(f"**Branch:** `{BRANCH}` · **Project:** [`{PROJECT}`]({DASHBOARD})")
  lines.append("")

  outcome = os.environ.get("SONAR_ANALYSIS_OUTCOME", "success")
  if outcome != "success":
    lines.append(
      f"> ⚠️ The analysis step ended with `{outcome}`. "
      "The values below are the last analysis available for this branch, not from this run."
    )
    lines.append("")

  try:
    failure = wait_for_analysis()
    if failure:
      lines.append(f"### ⚠️ Analysis task did not succeed: {failure}")
      return "\n".join(lines) + "\n"

    gate = api("/api/qualitygates/project_status", {"projectKey": PROJECT, "branch": BRANCH})
    status = gate.get("projectStatus", {}).get("status", "UNKNOWN")
    icon = {"OK": "✅ Passed", "ERROR": "❌ Failed", "WARN": "⚠️ Passed with warnings"}.get(
      status, status.title()
    )
    lines.append(f"**Quality gate:** {icon}")
    lines.append("")

    conditions = gate.get("projectStatus", {}).get("conditions", [])
    if conditions:
      lines.append("| Condition | Status | Threshold | Actual |")
      lines.append("|---|---|---|---|")
      for condition in conditions:
        mark = "✅" if condition.get("status") == "OK" else "❌"
        metric = condition.get("metricKey", "")
        lines.append(
          f"| `{metric}` | {mark} {condition.get('status', '')} | "
          f"{condition.get('errorThreshold', '')} | {condition.get('actualValue', 'n/a')} |"
        )
      lines.append("")

    measures = api(
      "/api/measures/component",
      {"component": PROJECT, "branch": BRANCH, "metricKeys": ",".join(METRICS)},
    )
    values = {m["metric"]: m.get("value") for m in measures.get("component", {}).get("measures", [])}

    lines.append("| Metric | Value |")
    lines.append("|---|---|")
    for metric in METRICS:
      value = values.get(metric)
      if value is None:
        continue
      if metric in RATINGS_METRICS:
        value = RATINGS.get(value, value)
      elif metric == "sqale_index":
        value = debt(value)
      else:
        value = f"{value}{SUFFIX.get(metric, '')}"
      lines.append(f"| {LABELS[metric]} | {value} |")

  except ApiError as error:
    lines.append(f"### ⚠️ Could not read the SonarCloud API")
    lines.append("")
    lines.append(f"```\n{error}\n```")

  return "\n".join(lines) + "\n"

def debt(minutes: str) -> str:
  """Format SonarQube technical debt (minutes) as a human-readable value."""
  total = int(float(minutes))
  days, rest = divmod(total, 480)
  hours, mins = divmod(rest, 60)
  parts = []
  if days:
    parts.append(f"{days}d")
  if hours:
    parts.append(f"{hours}h")
  if mins or not parts:
    parts.append(f"{mins}min")
  return " ".join(parts)

def main() -> int:
  """Print the summary to stdout so the caller can tee it to the job summary."""
  sys.stdout.write(render())
  return 0

if __name__ == "__main__":
  sys.exit(main())
