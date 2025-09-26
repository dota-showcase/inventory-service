#!/bin/bash
set -e  # Exit on any error

# ===== CONFIGURATION =====
SERVICE_NAME="ds-inventory"
GRADLE_CMD="./gradlew"

# ===== STEP 1: BUILD THE JAR =====
echo "🚀 Building application..."
$GRADLE_CMD build -x test

# ===== STEP 2: RESTART THE SYSTEMD SERVICE =====
echo "🔄 Restarting service: $SERVICE_NAME"
sudo systemctl stop "$SERVICE_NAME"
sudo systemctl start "$SERVICE_NAME"

# ===== STEP 3: SHOW STATUS =====
echo "📋 Service status:"
sudo systemctl status "$SERVICE_NAME" --no-pager

echo "✅ Deployment completed."
