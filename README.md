# Testpress Android SDK

## Setup Instructions

### 1. Clone the Repository
Clone the Testpress Android SDK repository to your local machine using SSH:

```bash
git clone git@github.com:testpress/android-sdk.git
cd android-sdk
```

*(Optionally, using HTTPS)*:
```bash
git clone https://github.com/testpress/android-sdk.git
```

### 2. Run the Zoom SDK Setup Script
Navigate to the project directory and execute the setup script for your operating system to download and extract the required Zoom SDK dependencies:

- **macOS / Linux:**
  ```bash
  chmod +x setup_zoom_sdk_macOS.sh
  ./setup_zoom_sdk_macOS.sh
  ```

- **Windows:**
  ```cmd
  setup_zoom_sdk_windows.bat
  ```

### 3. Open the Project in Android Studio
1. Launch the latest version of **Android Studio**.
2. Click **Open** (or **File > Open**) and select the cloned `android-sdk` directory.
3. Wait for Android Studio to finish the Gradle sync and build indexing.

