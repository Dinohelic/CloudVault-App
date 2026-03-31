# Google Sign-In Error 10 - Fix Guide

## What Causes Error 10?
Error 10 occurs when there's a mismatch between your app's SHA-1 fingerprint and what's configured in Firebase Console. This is a **security validation error** from Google's OAuth service.

## Root Causes:
1. **Missing SHA-1 Fingerprint** in Firebase Console
2. **Debug vs Release Keystore** mismatch
3. **Package name mismatch** between app and Firebase
4. **Incorrect Web Client ID** configuration

---

## Solutions (Follow These Steps):

### Step 1: Get Your App's SHA-1 Fingerprint

Run this command in terminal from your project root:

```bash
cd /Users/deepakanish/AndroidStudioProjects/cloudvault
./gradlew signingReport
```

Look for the `SHA1` value in the output. Example:
```
SHA1: AB:CD:EF:12:34:56:78:90:AB:CD:EF:12:34:56:78:90:AB:CD:EF:12
```

### Step 2: Add SHA-1 to Firebase Console

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Select your project: `self-drive-17ee0`
3. Go to **Project Settings** (gear icon)
4. Click **Your apps** tab
5. Click on your Android app: `com.cloudvault.cloudvault`
6. Scroll down to **SHA certificate fingerprints**
7. Click **Add fingerprint**
8. Paste the SHA1 value from Step 1 (in format: `AB:CD:EF:...`)
9. Click **Save**

### Step 3: Verify Firebase Configuration

Make sure your `google-services.json` contains:
- ✅ Correct `package_name`: `com.cloudvault.cloudvault`
- ✅ OAuth client with `client_type: 3` (Web client for backend)
- ✅ Valid `client_id` and `api_key`

Current config is correct:
```json
{
  "project_id": "self-drive-17ee0",
  "package_name": "com.cloudvault.cloudvault",
  "client_id": "1062568562602-0mu6lcuguc0v4qp9k8tsgnk9elg5qv7r.apps.googleusercontent.com"
}
```

### Step 4: Enable Google Sign-In in Firebase

1. In Firebase Console, go to **Authentication**
2. Click **Sign-in method** tab
3. Enable **Google** provider
4. Make sure a project is selected in the **Web SDK configuration**
5. Click **Save**

### Step 5: Rebuild and Test

```bash
cd /Users/deepakanish/AndroidStudioProjects/cloudvault
./gradlew clean
./gradlew build
```

Then run on device/emulator and test Google Sign-In again.

---

## Code Changes Made:

### 1. Added Required Permissions (AndroidManifest.xml)
✅ `android.permission.INTERNET` - Required for network calls
✅ `android.permission.ACCESS_NETWORK_STATE` - Required for connectivity check

### 2. Improved Error Handling (LoginActivity.kt)
- Added detailed error logging with error code explanations
- Better null safety checks
- Specific error messages for each Google Sign-In status code

### 3. Updated Dependencies (build.gradle.kts)
- Added `play-services-base` for better Google Play Services support

---

## Common Error Codes Reference:

| Code | Meaning | Solution |
|------|---------|----------|
| 10 | OAuth configuration error | Add SHA-1 fingerprint to Firebase |
| 12500 | Google Play Services unavailable | Update Google Play Services on device |
| 12501 | User cancelled sign-in | Expected behavior, retry |
| 12502 | Invalid API result | Check configuration |
| 12503 | Missing requestIdToken | Check strings.xml has web_client_id |
| 12504 | Google Sign-In config error | Verify GoogleSignInOptions |

---

## Troubleshooting Checklist:

- [ ] SHA-1 fingerprint added to Firebase Console
- [ ] Google Sign-In method enabled in Firebase Authentication
- [ ] INTERNET permission added to AndroidManifest.xml
- [ ] google-services.json is in app/ directory
- [ ] Package name in gradle matches Firebase
- [ ] Web client ID in strings.xml matches Firebase config
- [ ] Device has Google Play Services installed
- [ ] Firebase project rules allow authentication

---

## Testing Steps:

1. **Clean build:**
   ```bash
   ./gradlew clean build
   ```

2. **Uninstall from device:**
   ```bash
   adb uninstall com.cloudvault.cloudvault
   ```

3. **Rebuild and install:**
   ```bash
   ./gradlew installDebug
   ```

4. **Test Google Sign-In button**

If still failing, check Android Studio Logcat for detailed Firebase error messages.

