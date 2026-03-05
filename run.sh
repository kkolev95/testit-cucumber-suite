#!/bin/bash

###############################################################################
# Cucumber Test Runner - Runs tests and archives the HTML report
# Usage: ./run.sh [description]
###############################################################################

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPORT_DIR="$SCRIPT_DIR/reports"
TIMESTAMP=$(date +"%Y-%m-%d_%H-%M-%S")
DESCRIPTION="${1:-test-run}"
SAFE_DESCRIPTION=$(echo "$DESCRIPTION" | tr ' ' '-' | tr -cd '[:alnum:]-_')

REPORT_FILE="$REPORT_DIR/report-$TIMESTAMP-$SAFE_DESCRIPTION.html"
LOG_FILE="$REPORT_DIR/log-$TIMESTAMP-$SAFE_DESCRIPTION.log"

JAVA_HOME="${JAVA_HOME:-/home/kolev95/tools/jdk-21.0.2}"
MVN="/home/kolev95/tools/apache-maven-3.9.6/bin/mvn"

mkdir -p "$REPORT_DIR"

echo "=========================================" | tee "$LOG_FILE"
echo "Test Run: $TIMESTAMP"                      | tee -a "$LOG_FILE"
echo "Description: $DESCRIPTION"                | tee -a "$LOG_FILE"
echo "=========================================" | tee -a "$LOG_FILE"
echo ""                                          | tee -a "$LOG_FILE"

cd "$SCRIPT_DIR" || exit 1

echo "[$(date +%H:%M:%S)] Running tests..." | tee -a "$LOG_FILE"

JAVA_HOME="$JAVA_HOME" "$MVN" test 2>&1 | tee -a "$LOG_FILE"

TEST_EXIT_CODE=${PIPESTATUS[0]}

echo "" | tee -a "$LOG_FILE"

# Cucumber generates the HTML report automatically at target/cucumber-reports/report.html
CUCUMBER_REPORT="$SCRIPT_DIR/target/cucumber-reports/report.html"

if [ -f "$CUCUMBER_REPORT" ]; then
    cp "$CUCUMBER_REPORT" "$REPORT_FILE"
    echo "[$(date +%H:%M:%S)] ✓ Report: $REPORT_FILE" | tee -a "$LOG_FILE"
else
    echo "[$(date +%H:%M:%S)] ✗ Report not found at $CUCUMBER_REPORT" | tee -a "$LOG_FILE"
fi

echo "" | tee -a "$LOG_FILE"
echo "=========================================" | tee -a "$LOG_FILE"
if [ $TEST_EXIT_CODE -eq 0 ]; then
    echo "✓ PASSED" | tee -a "$LOG_FILE"
else
    echo "✗ FAILED" | tee -a "$LOG_FILE"
fi
echo "=========================================" | tee -a "$LOG_FILE"

ln -sf "$(basename "$REPORT_FILE")" "$REPORT_DIR/latest-report.html"
ln -sf "$(basename "$LOG_FILE")"    "$REPORT_DIR/latest-log.log"
cp "$REPORT_FILE" "$REPORT_DIR/current-report.html"
cp "$LOG_FILE"    "$REPORT_DIR/current-log.log"

echo ""
echo "Report: reports/latest-report.html"
echo ""

exit $TEST_EXIT_CODE
