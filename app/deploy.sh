#!/bin/bash
set -e  # Exit on any error

# ===== CONFIGURATION =====
SERVICE_NAME="ds-inventory"
GRADLE_CMD="./gradlew"

# ===== STEP 1: BUILD THE JAR =====
# tests are skipped - RUN_TESTS=1 runs them
echo "🚀 Building application..."
if [ "${RUN_TESTS:-0}" = "1" ]; then
    $GRADLE_CMD build -x javadoc
else
    $GRADLE_CMD build -x test -x javadoc
fi

# ===== STEP 2: RESTART THE SYSTEMD SERVICE =====
echo "🔄 Restarting service: $SERVICE_NAME"
sudo systemctl stop "$SERVICE_NAME"
sudo systemctl start "$SERVICE_NAME"

# ===== STEP 3: SHOW STATUS =====
echo "📋 Service status:"
sudo systemctl status "$SERVICE_NAME" --no-pager

echo "✅ Deployment completed."
