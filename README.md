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

### 4. Setup Zoom SDK & Publish to Maven Local

#### Step 1: Update Version Name
Update `VERSION_NAME` in `gradle.properties` before building and publishing:
```properties
VERSION_NAME=1.2.4
```

#### Step 2: Build and Publish
Download the Zoom SDK, build the release artifact, and publish it to Maven Local using:

- **macOS / Linux:**
  ```bash
  chmod +x setup_and_publish_local_macOS.sh
  ./setup_and_publish_local_macOS.sh
  ```

- **Windows:**
  ```cmd
  setup_and_publish_local_windows.bat
  ```

Alternatively, if the Zoom SDK is already set up, you can build and publish directly:
```bash
./gradlew clean && ./gradlew assembleRelease && ./gradlew publishToMavenLocal
```

#### Step 3: Consume Local SDK in Testpress Android Application
To use the locally published SDK in the [Testpress Android App](https://github.com/testpress/android):

1. **Add `mavenLocal()` to repository list**:
   In the application's root `build.gradle`, add `mavenLocal()` near the top of the repository list, before remote Maven repositories:

   ```groovy
   allprojects {
       repositories {
           mavenLocal()

           maven {
               url 'https://github.com/friberry/mvn-repo/raw/master/'
           }

           maven {
               name = 'GitHubPackages'
               url = uri('https://maven.pkg.github.com/testpress/android-sdk')

               credentials {
                   username = System.getenv('GITHUB_USERNAME')
                   password = System.getenv('GITHUB_ACCESS_KEY')
               }
           }

           maven { url 'https://jitpack.io' }

           google()
           mavenCentral()
           jcenter()
       }
   }
   ```

2. **Update the SDK Version in the Android App**:
   In the Android application, update `testpressSDK` to match the updated `VERSION_NAME`:

   ```groovy
   testpressSDK = '1.2.4'
   ```



