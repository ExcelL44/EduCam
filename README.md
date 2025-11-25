# EduCam

## Codespaces Setup

This repository is configured for GitHub Codespaces.

1.  Click the **Code** button on GitHub.
2.  Select **Codespaces**.
3.  Click **Create codespace on main**.

The environment includes:
- Java 17
- Android SDK (Platform 34, Build Tools 34.0.0)
- Gradle

### Build Instructions
To build the APK in Codespaces:
```bash
./gradlew assembleDebug
```
The APK will be located in `app/build/outputs/apk/debug/`.
