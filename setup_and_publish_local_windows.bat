#!/bin/bash

set -e

echo "📦 Downloading Zoom SDK..."
curl -o zoom_sdk.zip https://media.testpress.in/static/android/zoom_sdk.zip

echo "📂 Extracting Zoom SDK..."
unzip -q zoom_sdk.zip

echo "🧹 Cleaning up archive..."
rm zoom_sdk.zip

echo "🛠️ Building and publishing..."

if [ -f "./gradlew" ]; then
  echo "Using ./gradlew"
  chmod +x ./gradlew
  ./gradlew clean build -x test
  ./gradlew publishToMavenLocal
else
  echo "gradlew not found, using system gradle"
  gradle clean build -x test
  gradle publishToMavenLocal
fi

echo "✅ Done! Library published to ~/.m2 and Zoom SDK setup completed."