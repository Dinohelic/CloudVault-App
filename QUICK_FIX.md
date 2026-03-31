# Quick Fix Steps - Google Sign-In Error 10

## 🚀 What You Need to Do (5 Minutes)

### Step 1: Get Your App's SHA-1 Fingerprint
Open Terminal and run:
```bash
cd /Users/deepakanish/AndroidStudioProjects/cloudvault
./gradlew signingReport
```

**Look for output like:**
```
Config 'debug':
    SHA1: AB:CD:EF:12:34:56:78:90:AB:CD:EF:12:34:56:78:90:AB:CD:EF:12
```
**Copy this SHA1 value** (the one with colons)

---

### Step 2: Add to Firebase Console
1. Go to https://console.firebase.google.com/
2. Click on project **"self-drive-17ee0"**
3. Click ⚙️ **Project Settings** (top left, gear icon)
4. Click **"Your apps"** tab
5. Find and click **"com.cloudvault.cloudvault"** (Android app)
6. Scroll down to **"SHA certificate fingerprints"**
7. Click **"Add fingerprint"**
8. Paste the SHA1 value you copied
9. Click **"Save"**
10. ⏳ **Wait 10-15 minutes for changes to sync**

---

### Step 3: Enable Google Sign-In Method (if not done)
In Firebase Console:
1. Go to **"Authentication"** tab (left menu)
2. Click **"Sign-in method"** tab
3. Look for **"Google"** in the list
4. If it shows a switch, make sure it's **turned ON** (blue)
5. A dialog might appear - just click **"Save"**

---

### Step 4: Rebuild Your App
```bash
cd /Users/deepakanish/AndroidStudioProjects/cloudvault
./gradlew clean build

# On your device/emulator:
adb uninstall com.cloudvault.cloudvault
./gradlew installDebug
```

---

### Step 5: Test Google Sign-In
1. Open the app on your device
2. Go to login screen
3. Click **"SIGN IN WITH GOOGLE"** button
4. Select your Google account
5. ✅ Should work now!

---

## 🔍 If It Still Doesn't Work

### Check These:
```bash
# 1. Verify SHA-1 was added
./gradlew signingReport

# 2. Verify package name matches
grep applicationId app/build.gradle.kts

# 3. Clear app cache
adb shell pm clear com.cloudvault.cloudvault

# 4. Check Logcat for detailed errors
adb logcat | grep -i "googlesignin\|firebase"
```

### Check in Firebase Console:
- ✅ SHA1 is showing in Firebase Settings
- ✅ Google Sign-In method is ENABLED
- ✅ Support email is set in OAuth Consent Screen
- ✅ Project ID matches: `self-drive-17ee0`
- ✅ Package name matches: `com.cloudvault.cloudvault`

---

## 📝 What Changed in Your Code

1. **AndroidManifest.xml** - Added INTERNET permission
2. **LoginActivity.kt** - Better error messages for debugging
3. **build.gradle.kts** - Added play-services-base dependency

These are all done. You just need to add the SHA-1 to Firebase! ⬆️

---

## ⏱️ Expected Timeline
- Add SHA-1 to Firebase: **Now**
- Firebase sync time: **10-15 minutes**
- Rebuild app: **5 minutes**
- Total: **~20 minutes**

---

## 💡 Why This Happens
Google needs to verify that:
1. Your app is authentic (not a fake)
2. Your certificate matches what's registered
3. Your Firebase project ID is correct

The SHA-1 fingerprint proves you own the app!

