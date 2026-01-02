#!/bin/bash

echo "╔════════════════════════════════════════════════════════════════╗"
echo "║     GraalVM Native Image - Quick Reference                    ║"
echo "╚════════════════════════════════════════════════════════════════╝"
echo ""

cat << 'EOF'
📋 MAIN COMMANDS
══════════════════════════════════════════════════════════════════
1️⃣  CAPTURE CONFIGURATIONS WITH AGENT (RECOMMENDED)
   ./run-with-agent.sh

   👉 Exercise all endpoints while it runs
   👉 Press Ctrl+C when you're done
   👉 Configurations are saved automatically
──────────────────────────────────────────────────────────────────
2️⃣  BUILD NATIVE IMAGE
   ./../build-image-native.sh

   ⏱️  Approximate duration: 3-5 minutes
   💾 Required RAM: 8GB+
──────────────────────────────────────────────────────────────────
3️⃣  ANALYZE BUILD ERRORS
   ./extract-missing-classes.sh

   📊 Generates: analysis-report.txt with missing classes
   📋 Generates: XML ready to copy to pom.xml
──────────────────────────────────────────────────────────────────
4️⃣  RUN NATIVE IMAGE
   ./target/camila-product-api-infrastructure-boot

   🚀 Startup in ~0.05 seconds (vs ~3 seconds on JVM)
   💾 Memory: ~1/4 of JVM usage
══════════════════════════════════════════════════════════════════

💡 PRO TIPS
══════════════════════════════════════════════════════════════════
✓ Use the agent BEFORE attempting manual fixes
✓ Exercise ALL endpoints while the agent is running
✓ Prefer package-level initialization (org.springframework.util)
  over individual classes
✓ Keep JSON files in Git to track changes
✓ Update dependencies - better support in newer versions
✓ Test the native image thoroughly - it may behave differently
  than JVM
══════════════════════════════════════════════════════════════════
EOF
