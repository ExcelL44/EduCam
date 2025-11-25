#!/usr/bin/env bash
set -euo pipefail

# -------------------------------------------------
# 1️⃣  Install Java 17 (OpenJDK)
# -------------------------------------------------
echo "=== Installing OpenJDK 17 ==="
sudo apt-get update -y
sudo apt-get install -y openjdk-17-jdk

# Verify installation
java -version

# -------------------------------------------------
# 2️⃣  Install Android command‑line tools
# -------------------------------------------------
ANDROID_SDK_ROOT=$HOME/Android/Sdk
CMDLINE_TOOLS_DIR=$ANDROID_SDK_ROOT/cmdline-tools/latest

echo "=== Creating Android SDK directories ==="
mkdir -p "$CMDLINE_TOOLS_DIR"

# Download the latest command‑line tools (Linux)
echo "=== Downloading Android command‑line tools ==="
TMP_ZIP=$(mktemp /tmp/android-cmdline-tools-XXXX.zip)
curl -L -o "$TMP_ZIP" "https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip"

# Extract
unzip -q "$TMP_ZIP" -d "$ANDROID_SDK_ROOT/cmdline-tools"
mv "$ANDROID_SDK_ROOT/cmdline-tools/cmdline-tools" "$CMDLINE_TOOLS_DIR"
rm "$TMP_ZIP"

# -------------------------------------------------
# 3️⃣  Add SDK tools to PATH & set env vars
# -------------------------------------------------
export PATH=$PATH:$CMDLINE_TOOLS_DIR/bin
export ANDROID_SDK_ROOT=$ANDROID_SDK_ROOT
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64

# Persist for future shells (Codespaces rebuilds the container, so also add to ~/.bashrc)
{
  echo ""
  echo "# Android SDK"
  echo "export ANDROID_SDK_ROOT=$ANDROID_SDK_ROOT"
  echo "export PATH=\$PATH:$CMDLINE_TOOLS_DIR/bin"
  echo "export JAVA_HOME=$JAVA_HOME"
} >> "$HOME/.bashrc"

# -------------------------------------------------
# 4️⃣  Accept licences & install required packages
# -------------------------------------------------
yes | sdkmanager --licenses

# Install the exact components you need (you can add more later)
sdkmanager "platform-tools" \
           "platforms;android-34" \
           "build-tools;34.0.0"

# Verify installation
sdkmanager --list | grep -E "platforms;android-34|build-tools;34.0.0|platform-tools"

echo "=== Android SDK and Java 17 installation complete ==="
