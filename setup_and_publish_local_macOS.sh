@echo off

echo 📦 Downloading Zoom SDK...
curl -o zoom_sdk.zip https://media.testpress.in/static/android/zoom_sdk.zip

echo 📂 Extracting Zoom SDK...
tar -xf zoom_sdk.zip

echo 🧹 Cleaning up archive...
del zoom_sdk.zip

echo 🛠️ Building and publishing...

IF EXIST gradlew (
    echo Using gradlew
    call gradlew clean build -x test
    call gradlew publishToMavenLocal
) ELSE (
    echo gradlew not found, using system gradle
    call gradle clean build -x test
    call gradle publishToMavenLocal
)

echo ✅ Done! Library published to local Maven and Zoom SDK is ready.