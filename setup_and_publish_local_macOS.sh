#!/bin/bash

set -e

echo "📦 Downloading Zoom SDK..."
curl -o zoom_sdk.zip https://media.testpress.in/static/android/zoom_sdk.zip

echo "📂 Extracting Zoom SDK..."
tar -xf zoom_sdk.zip

echo "🧹 Cleaning up archive..."
rm zoom_sdk.zip

echo "🛠️ Building and publishing to Maven Local..."

if [ -f "./gradlew" ]; then
  echo "Using ./gradlew"
  chmod +x ./gradlew
  ./gradlew clean && ./gradlew assembleRelease && ./gradlew publishToMavenLocal
else
  echo "gradlew not found, using system gradle"
  gradle clean && gradle assembleRelease && gradle publishToMavenLocal
fi

echo "✅ Done! Library published to local Maven and Zoom SDK is ready."