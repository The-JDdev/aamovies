# Aamovies — Native Android App

A native Android (Kotlin) movie streaming app with WebView UI and full native bridge architecture.

## Architecture

HTML files serve as the **UI layer only**. All logic runs natively in Kotlin:

| Bridge | Registered Name | Responsibility |
|--------|----------------|----------------|
| AuthBridge | `AndroidAuth` | Firebase Email/Password, Google, Anonymous sign-in |
| FCMBridge | `AndroidFCM` | FCM token, topic subscribe/unsubscribe |
| AdBridge | `AndroidAd` | Native ad bypass (AdBlocker-immune double-click) |
| DownloadBridge | `AndroidDownload` | Android DownloadManager |
| AppBridge | `AndroidApp` | Toast, Share, Clipboard, platform info |

## Setup

1. Clone repo
2. Add your `google-services.json` to `app/` (get from Firebase Console)
3. Create your keystore and set env vars:
   ```
   KEYSTORE_FILE=your.keystore
   KEYSTORE_PASSWORD=yourpassword
   KEY_ALIAS=youralias
   KEY_PASSWORD=yourpassword
   ```
4. Open in Android Studio and build

## Key Features

- **Native Firebase Auth** — no JS SDK; login/logout fully in Kotlin
- **Native FCM** — push notifications, silent topic subscription on first launch
- **Ad-blocker immune ads** — double-click logic in native code, opens in system browser
- **Persistent downloads** — Android DownloadManager with notification bar progress
- **Netflix-style splash** — programmatic animation (no XML animators)

## Package

`com.aamovies.aamovies` | Min SDK 24 | Target SDK 34
