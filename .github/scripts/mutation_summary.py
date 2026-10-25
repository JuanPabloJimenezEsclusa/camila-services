#!/usr/bin/env python3
"""Render a Markdown mutation-testing summary from PIT report files.

Reads every ``**/target/pit-reports/mutations.xml`` produced by the ``pitest``
profile and prints a Markdown table with the mutation score and test strength
of each module, followed by the project total.
"""

from __future__ import annotations

import glob
import os
import sys
import xml.etree.ElementTree as ET

REPORT_GLOB = os.environ.get("PIT_REPORT_GLOB", "**/target/pit-reports/mutations.xml")
STATUSES = (
  "KILLED",
  "SURVIVED",
  "NO_COVERAGE",
  "TIMED_OUT",
  "NON_VIABLE",
  "RUN_ERROR",
  "MEMORY_ERROR",
)

def module_of(report: str) -> str:
  """Return the Maven module name owning the report."""
  depth = report.split(os.sep)[-4]
  return depth

def read_counts(report: str) -> dict[str, int]:
  """Count mutations per PIT status for a single report file."""
  counts: dict[str, int] = dict.fromkeys(STATUSES, 0)
  for mutation in ET.parse(report).getroot().iter("mutation"):
    status = (mutation.get("status") or "UNKNOWN").upper()
    counts[status] = counts.get(status, 0) + 1
  return counts

def ratio(numerator: int, denominator: int) -> str:
  """Format a percentage, or ``n/a`` when the denominator is not positive."""
  return f"{100.0 * numerator / denominator:.1f}%" if denominator > 0 else "n/a"

def mutation_score(counts: dict[str, int]) -> str:
  """Percentage of detectable mutations that the test suite killed."""
  denominator = sum(counts.values()) - counts["NON_VIABLE"]
  return ratio(counts["KILLED"] + counts["TIMED_OUT"], denominator)

def test_strength(counts: dict[str, int]) -> str:
  """Percentage of mutations killed among those reached by at least one test."""
  denominator = sum(counts.values()) - counts["NON_VIABLE"] - counts["NO_COVERAGE"]
  return ratio(counts["KILLED"] + counts["TIMED_OUT"], denominator)

def find_reports() -> list[str]:
  """Return report paths, skipping the aggregate copy to avoid double counting."""
  reports = []
  for report in sorted(glob.glob(REPORT_GLOB, recursive=True)):
    if os.path.basename(os.path.dirname(report)) == "aggregate":
      continue
    reports.append(report)
  return reports

def render(reports: list[str]) -> str:
  """Build the Markdown document."""
  lines = ["## 🧬 Mutation Testing Report", ""]

  if not reports:
    lines.extend([
      "### ⚠️ No mutation reports were generated",
      "",
      "PIT did not produce any `mutations.xml`. Review the *Run Mutation Tests* step."])
    return "\n".join(lines) + "\n"

  issued: dict[str, int] = dict.fromkeys(STATUSES, 0)
  rows = []

  for report in reports:
    counts = read_counts(report)
    if not any(counts.values()):
      continue
    for status, value in counts.items():
      issued[status] = issued.get(status, 0) + value
    rows.append(
      f"| `{module_of(report)}` | {sum(counts.values())} | {counts['KILLED']} | "
      f"{counts['SURVIVED']} | {counts['NO_COVERAGE']} | "
      f"{mutation_score(counts)} | {test_strength(counts)} |"
    )

  total = sum(issued.values())
  lines.extend([
    "| Module | Generated | Killed | Survived | No coverage | Score | Test strength |",
    "|---|---:|---:|---:|---:|---:|---:|"])
  lines.extend(rows)
  lines.extend([
    f"| **Total** | **{total}** | **{issued['KILLED']}** | **{issued['SURVIVED']}** | "
    f"**{issued['NO_COVERAGE']}** | **{mutation_score(issued)}** | **{test_strength(issued)}** |",
    "",
    f"🕐 {issued['TIMED_OUT']} timed out · ⚠️ {issued['RUN_ERROR']} run errors · "
    f"💀 {issued['MEMORY_ERROR']} memory errors · 🚫 {issued['NON_VIABLE']} non-viable"])
  return "\n".join(lines) + "\n"

def main() -> int:
  """Print the summary to stdout so the caller can tee it to the job summary."""
  sys.stdout.write(render(find_reports()))
  return 0

if __name__ == "__main__":
  sys.exit(main())
