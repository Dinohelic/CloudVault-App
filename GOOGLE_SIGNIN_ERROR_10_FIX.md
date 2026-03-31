# Google Sign-In Error 10 - Root Cause Analysis & Solution

## Error Details
**Error Message:** "Google sign in failed: 10"
**Location:** LoginActivity.kt - Google Sign-In Launcher

---

## Root Cause Analysis

### What is Error 10?
Error code **10** is a Google OAuth configuration error that occurs when:
- The SHA-1 fingerprint of your app's signing certificate is not registered in Firebase Console
- There's a mismatch between your local development keystore and the Firebase project settings
- Google's OAuth validation fails to verify your app's identity

### Why It's Happening:
1. **Missing SHA-1 Registration**: Your app's debug keystore SHA-1 fingerprint is not added to Firebase Console
2. **Security Validation**: Google verifies that the app requesting authentication is the legitimate owner of the package name
3. **Certificate Mismatch**: The certificate used to sign your APK doesn't match what Firebase expects

---

## Solution Summary

### Step 1: Extract SHA-1 Fingerprint ✅
Run this command in the project directory:
```bash
./gradlew signingReport
```

Copy the **SHA1** value (format: `XX:XX:XX:XX:...`)

### Step 2: Register SHA-1 in Firebase Console ⚡ **CRITICAL**
1. Open [Firebase Console](https://console.firebase.google.com/)
2. Go to Project: **self-drive-17ee0**
3. Go to **Project Settings** → **Your apps**
4. Select **com.cloudvault.cloudvault** (Android app)
5. Scroll to **SHA certificate fingerprints**
6. Click **Add fingerprint**
7. Paste the SHA-1 value
8. Click **Save**
9. **Wait 10-15 minutes** for Firebase to sync the changes

### Step 3: Verify Firebase Authentication Setup
Ensure in Firebase Console:
- ✅ **Authentication** tab → **Sign-in method**
- ✅ **Google** provider is **Enabled**
- ✅ Support email is set
- ✅ OAuth consent screen is configured

### Step 4: Verify App Configuration
Check your local `strings.xml`:
```xml
<string name="default_web_client_id">1062568562602-0mu6lcuguc0v4qp9k8tsgnk9elg5qv7r.apps.googleusercontent.com</string>
```
This must match the Web Client ID from Firebase Console.

### Step 5: Rebuild App
```bash
./gradlew clean build
adb uninstall com.cloudvault.cloudvault
./gradlew installDebug
```

Then test Google Sign-In again.

---

## Code Changes Made to Fix This

### 1. **AndroidManifest.xml** - Added Network Permissions
```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```
**Why:** Google Sign-In requires network connectivity to authenticate.

### 2. **LoginActivity.kt** - Enhanced Error Handling
```kotlin
private val googleSignInLauncher = registerForActivityResult(...) { result ->
    val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
    try {
        val account = task.getResult(ApiException::class.java)
        if (account != null && account.idToken != null) {
            val credential = GoogleAuthProvider.getCredential(account.idToken!!, null)
            authViewModel.signInWithGoogle(credential)
        } else {
            Log.e(TAG, "Account or ID token is null")
            Toast.makeText(this, "Failed to get Google account credentials", Toast.LENGTH_SHORT).show()
        }
    } catch (e: ApiException) {
        Log.e(TAG, "Google sign in failed with error code: ${e.statusCode}", e)
        val errorMessage = when (e.statusCode) {
            12500 -> "Google Play Services is not available or is too old"
            12501 -> "The user cancelled the sign-in flow"
            12502 -> "One of the API calls returned an invalid result"
            12503 -> "The requestIdToken or requestScopes params are not provided or invalid"
            12504 -> "Google Sign-In configuration error"
            10 -> "There was an error with OAuth consent. Check SHA-1 fingerprint in Firebase Console"
            else -> "Error code: ${e.statusCode}"
        }
        Log.e(TAG, "Error message: $errorMessage")
        Toast.makeText(this, "Google sign in failed: $errorMessage", Toast.LENGTH_LONG).show()
    }
}
```
**Why:** Provides better debugging information and handles edge cases.

### 3. **build.gradle.kts** - Updated Dependencies
```kotlin
implementation("com.google.android.gms:play-services-base:18.5.0")
```
**Why:** Ensures Google Play Services is properly initialized.

---

## Debugging Tips

### 1. Check Logcat for Detailed Errors
Filter by "GoogleSignIn" or "FirebaseAuth" to see detailed error messages.

### 2. Verify SHA-1 Was Added
```bash
./gradlew signingReport
```
Compare with Firebase Console settings.

### 3. Test Incrementally
- First test basic Firebase Authentication (email/password)
- Then test Google Sign-In
- This helps isolate whether it's a Google or Firebase issue

### 4. Clear App Data
```bash
adb shell pm clear com.cloudvault.cloudvault
```

---

## Success Indicators

After applying the fix, you should see:
- ✅ No error code 10 when clicking "Sign in with Google"
- ✅ Google Sign-In screen appears
- ✅ User can select their Google account
- ✅ App successfully authenticates and logs user in

---

## Timeline

| Event | Time |
|-------|------|
| Add SHA-1 to Firebase | Now |
| Firebase syncs changes | 10-15 minutes |
| App can authenticate | After rebuild |

---

## References

- [Firebase Google Sign-In Troubleshooting](https://firebase.google.com/docs/auth/android/google-signin)
- [Android GoogleSignIn Error Codes](https://developers.google.com/android/reference/com/google/android/gms/auth/api/signin)
- [Firebase Console](https://console.firebase.google.com/)

