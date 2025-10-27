#!/bin/bash
set -e  # Exit on any error

# ===== CONFIGURATION =====
SERVICE_NAME="ds-inventory"

# ===== STEP 1: RESTART THE SYSTEMD SERVICE =====
echo "🔄 Restarting service: $SERVICE_NAME"
sudo systemctl stop "$SERVICE_NAME"
sudo systemctl start "$SERVICE_NAME"

# ===== STEP 2: SHOW STATUS =====
echo "📋 Service status:"
sudo systemctl status "$SERVICE_NAME" --no-pager

echo "✅ Restarting completed."
