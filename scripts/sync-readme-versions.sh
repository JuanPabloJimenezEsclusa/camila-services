#!/usr/bin/env bash
# Sync the versions documented in the Readmes and AGENTS.md with pom.xml.
set -euo pipefail

root=$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")/.." && pwd)
pom="$root/pom.xml"
docs=(
  "$root/Readme.md"
  "$root/camila-product-api/Readme.md"
  "$root/camila-gateway/Readme.md"
  "$root/camila-admin/Readme.md"
  "$root/AGENTS.md"
)

# prop <name>: value of a <properties> entry.
prop() { sed -n "s:.*<$1>\([^<]*\)</$1>.*:\1:p" "$pom" | head -1; }
# minor <version>: major.minor, the only precision the docs claim ("6.0.x").
minor() { cut -d. -f1-2 <<<"$1"; }

# The Spring Boot version is the parent version, not a property.
boot_version=$(awk '/<parent>/{p=1} p && /<version>/{print; exit}' "$pom" |
  sed 's:.*<version>\([^<]*\)</version>.*:\1:')

# No Maven wrapper here, so the enforcer range is the version contract.
maven_min=$(awk '/<requireMavenVersion>/{p=1} p && /<version>/{print; exit}' "$pom" |
  sed 's:.*\[\([0-9][0-9.]*\),.*:\1:')

java_version=$(prop java.version)

# shortcut: no Gradle wrapper in this repo, so this literal is the only source;
# bump it here (or read from the wrapper if one is added).
gradle_version=9.8

# An empty extraction would blank the docs; fail loudly instead.
for v in "$java_version" "$boot_version" "$maven_min" \
  "$(prop spring-cloud.version)" "$(prop junit-platform-suite.version)" \
  "$(prop cucumber.version)" "$(prop pitest-maven.version)" \
  "$(prop chaos-monkey-spring-boot.version)" "$(prop archunit-junit5.version)" \
  "$(prop testcontainers.version)"; do
  [[ -n $v ]] || { echo "cannot read documented versions from $pom" >&2; exit 1; }
done

# Replace only the number right after the anchor, keeping a trailing ".x".
# The anchor keeps the rewrite away from unrelated numbers on the same line.
sync() {
  local file=$1 anchor=$2 value=$3
  perl -0777 -pi -e "s/\Q$anchor\E[0-9]+(?:\.[0-9]+)*/$anchor$value/g" "$file"
}

for file in "${docs[@]}"; do
  before=$(cksum <"$file")
  # Root Readme badge anchors ("%3E%3D" is ">="); service Readmes use "~= [x.y]".
  sync "$file" 'OpenJDK-%3E%3D' "$java_version"
  sync "$file" 'GraalVM-%3E%3D' "$java_version"
  sync "$file" 'GraalVM ~= [' "$java_version"
  sync "$file" 'JDK ~= [' "$java_version"
  sync "$file" 'Spring%20Boot-%3E%3D' "$(minor "$boot_version")"
  sync "$file" 'Spring-boot ~= [' "$(minor "$boot_version")"
  sync "$file" 'Spring%20Cloud-%3E%3D' "$(minor "$(prop spring-cloud.version)")"
  sync "$file" 'Spring-cloud ~= [' "$(minor "$(prop spring-cloud.version)")"
  sync "$file" 'Maven-%3E%3D' "$(minor "$maven_min")"
  sync "$file" 'Maven ~= [' "$(minor "$maven_min")"
  sync "$file" 'Gradle-%3E%3D' "$gradle_version"
  sync "$file" 'Gradle ~= [' "$gradle_version"
  sync "$file" 'JUnit5-%3E%3D' "$(minor "$(prop junit-platform-suite.version)")"
  sync "$file" 'Cucumber-%3E%3D' "$(minor "$(prop cucumber.version)")"
  sync "$file" 'Pitest-%3E%3D' "$(minor "$(prop pitest-maven.version)")"
  sync "$file" 'ChaosMonkey-%3E%3D' "$(minor "$(prop chaos-monkey-spring-boot.version)")"
  sync "$file" 'ArchUnit-%3E%3D' "$(minor "$(prop archunit-junit5.version)")"
  sync "$file" 'Testcontainers-%3E%3D' "$(minor "$(prop testcontainers.version)")"
  # Plain-text forms used by AGENTS.md ("Java 25, Spring Boot 4.0.x").
  sync "$file" 'Java ' "$java_version"
  sync "$file" 'JDK ' "$java_version"
  sync "$file" 'Spring Boot ' "$(minor "$boot_version")"
  sync "$file" 'Maven ' "$(minor "$maven_min")"
  sync "$file" 'Gradle ' "$gradle_version"
  if [[ $before == "$(cksum <"$file")" ]]; then
    echo "${file#"$root"/}: already up to date"
  else
    echo "${file#"$root"/}: updated"
  fi
done

echo "pom: Java $java_version, Spring Boot $boot_version, Spring Cloud $(prop spring-cloud.version), Maven >= $maven_min, Gradle $gradle_version"
