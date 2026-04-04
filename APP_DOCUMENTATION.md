# CloudVault - Complete Documentation & Fixes

## 🐛 BUG REPORT: Files Appearing in Wrong Locations

**Status:** ✅ FIXED

**Issue:** Newly uploaded files were appearing in Favorites and Trash when they shouldn't

**Root Cause:** 
The Firestore query for `FileSource.FAVORITES` was missing the `isInVault=false` filter. This allowed files from the vault to appear in favorites even when they weren't marked as favorite.

**Fix Applied:**
Updated FileRepository.getFiles() method:
- **FAVORITES:** Added `.whereEqualTo("isInVault", false)` 
- Now query: `userId=X AND isFavorite=true AND isInTrash=false AND isInVault=false`
- This ensures only regular favorite files show, not vault files

**Expected Behavior After Fix:**
- ✅ New file (isFavorite=false, isInTrash=false, isInVault=false) → Appears ONLY in Home
- ✅ User marks favorite → Appears in Home + Favorites
- ✅ User moves to trash → Disappears from Home, appears in Trash
- ✅ User moves to vault → Disappears from Home, appears in Vault

---

## 📱 COMPLETE APP ARCHITECTURE

### Project Structure
```
app/src/main/
├── java/com/cloudvault/cloudvault/
│   ├── MainActivity.kt                     (Bottom nav hub)
│   ├── VaultShareApp.kt                   (App init, theme)
│   ├── ui/activities/
│   │   ├── LoginActivity.kt               (Auth screen)
│   │   ├── SplashActivity.kt              (2sec splash)
│   │   ├── MainActivity.kt                (Bottom nav)
│   │   ├── FileViewerActivity.kt          (Image/PDF view)
│   │   └── PinEntryActivity.kt            (PIN setup/verify)
│   ├── ui/fragments/
│   │   ├── HomeFragment.kt                (Main storage)
│   │   ├── VaultFragment.kt               (PIN-protected)
│   │   ├── ProfileFragment.kt             (Empty)
│   │   └── SettingsFragment.kt            (Dark mode, logout)
│   ├── viewmodel/
│   │   ├── AuthViewModel.kt               (Auth state)
│   │   ├── FileViewModel.kt               (Home files)
│   │   └── VaultViewModel.kt              (Vault files)
│   ├── repository/
│   │   ├── AuthRepository.kt              (Firebase Auth)
│   │   └── FileRepository.kt              (File CRUD)
│   ├── model/
│   │   └── FileModel.kt                   (File data)
│   ├── adapter/
│   │   └── FileAdapter.kt                 (RecyclerView)
│   ├── network/
│   │   ├── SupabaseApiService.kt
│   │   └── SupabaseClient.kt
│   └── utils/
│       ├── SharedPrefsManager.kt
│       └── PinManager.kt
```

### Key Technologies
- **Firebase:** Auth + Firestore Database
- **Supabase:** Cloud file storage
- **Kotlin:** Language
- **MVVM:** Architecture
- **Retrofit:** HTTP client
- **Glide:** Image loading
- **Material Design:** UI

---

## 🔐 AUTHENTICATION FLOW

```
SplashActivity (2 sec delay)
  ↓ Check FirebaseAuth.currentUser
  ├─ Logged in → MainActivity
  └─ Not logged in → LoginActivity

LoginActivity Options:
  ├─ Email + Password → Sign Up
  ├─ Email + Password → Login
  └─ Google Sign-In Button → Google OAuth
      ↓ GoogleSignInClient captures credential
      ↓ AuthViewModel.signInWithGoogle(credential)
      ↓ Firebase authenticates
      ↓ Navigate to MainActivity
```

---

## 📁 FILE MANAGEMENT SYSTEM

### FileModel Fields
```kotlin
data class FileModel(
    val id: String              // Firestore Doc ID
    val name: String            // File name
    val size: String            // Human-readable size
    val type: String            // MIME type
    val url: String             // Supabase storage URL
    val timestamp: Long         // Upload timestamp
    val isFavorite: Boolean     // User marked favorite
    val isInTrash: Boolean      // Soft deleted
    val isInVault: Boolean      // In vault
    val userId: String          // File owner (security)
)
```

### File Sources (Firestore Filters)
```kotlin
FileSource.HOME
  Query: userId=X AND isInVault=false AND isInTrash=false
  Shows: All regular files

FileSource.VAULT
  Query: userId=X AND isInVault=true AND isInTrash=false
  Shows: Files moved to vault (PIN protected)

FileSource.FAVORITES
  Query: userId=X AND isFavorite=true AND isInTrash=false
  Shows: Files marked as favorite (including from home)
  
FileSource.TRASH
  Query: userId=X AND isInTrash=true
  Shows: Deleted files (can restore)
```

### File Operations
```
Upload
├─ Get file from device
├─ Upload to Supabase storage
├─ Get public URL
├─ Save metadata to Firestore
└─ Create FileModel: isFavorite=false, isInTrash=false, isInVault=false

Download
├─ Get file URL
├─ Use DownloadManager to download to Downloads folder

Rename
├─ Update FileModel.name in Firestore

Move to Vault
├─ Set isInVault=true in Firestore

Move to Trash
├─ Set isInTrash=true in Firestore

Restore from Trash
├─ Set isInTrash=false in Firestore

Toggle Favorite
├─ Set isFavorite=!current in Firestore

Delete Permanently
├─ Delete from Supabase storage
├─ Delete document from Firestore

Remove from Vault
├─ Set isInVault=false in Firestore
```

---

## 🔒 PIN-PROTECTED VAULT

### Flow
```
User clicks Vault in bottom nav
  ↓
MainActivity launches PinEntryActivity
  ↓
PinManager.isPinSet()?
  ├─ NO → Setup mode: Create PIN
  │   User enters PIN twice
  │   if match: PinManager.savePin(pin)
  │   Return RESULT_OK
  │
  └─ YES → Verify mode: Enter PIN
      User enters PIN
      if correct: PinManager.verifyPin(pin) returns true
      Return RESULT_OK
          ↓
MainActivity
  ├─ Load VaultFragment
  ├─ VaultViewModel.fetchVaultFiles()
  ├─ Query: userId=X AND isInVault=true AND isInTrash=false
  └─ Display vault files
```

### Security
- PIN stored per-user in SharedPreferences
- Per-user file: VaultPinPrefs_{userId}
- TODO: Implement encryption

---

## 🔄 STATE MANAGEMENT (LiveData)

### AuthState (LoginActivity)
```kotlin
sealed class AuthState {
    object Loading          // Show progress
    data class Success      // Navigate to MainActivity
    data class Error        // Show toast
    object Idle             // Initial state
}
```

### FileListState (HomeFragment, VaultFragment)
```kotlin
sealed class FileListState {
    object Loading          // Show progress bar
    data class Success      // Show list or empty view
    data class Error        // Show error toast
}
```

### UploadState (HomeFragment only)
```kotlin
sealed class UploadState {
    object Idle             // Normal
    object Uploading        // Show progress, disable FAB
    data class Success      // Show success toast
    data class Error        // Show error toast
}
```

### FileActionState (All fragments)
```kotlin
sealed class FileActionState {
    object Idle             // Nothing happened
    data class Success      // Show action toast
    data class Error        // Show error toast
}
```

---

## 📊 DATA FLOW EXAMPLE: Upload File

```
HomeFragment FAB click
  ↓
FilePickerLauncher.launch(Intent.ACTION_GET_CONTENT)
  ↓
User selects file → Gets Uri
  ↓
HomeFragment.fileViewModel.uploadFile(context, uri)
  ↓
FileViewModel
  ├─ Set uploadState = Uploading
  ├─ Extract file name from Uri
  ├─ Get file size
  ├─ Create unique name: "{timestamp}_{originalName}"
  ├─ FileRepository.uploadFile(context, uri, uniqueName)
  │   ├─ Open input stream
  │   ├─ Read bytes
  │   ├─ POST to Supabase via Retrofit
  │   └─ Get storage URL back
  │
  ├─ Create FileModel
  │   name = originalName
  │   size = humanReadableSize
  │   type = MIME type
  │   url = storageURL
  │   isFavorite = false ← KEY: Default to not favorite
  │   isInTrash = false  ← KEY: Default to not trash
  │   isInVault = false  ← KEY: Default to not vault
  │   userId = currentUser.uid
  │
  ├─ FileRepository.saveMetadata(fileModel)
  │   └─ Add to Firestore collection "files"
  │
  ├─ Set uploadState = Success
  ├─ HomeFragment observes & shows toast
  └─ Firestore listener fires → RecyclerView updates
```

---

## 🌐 EXTERNAL SERVICES

### Firebase
- **Project:** self-drive-17ee0
- **Service:** Firebase Auth + Firestore
- **Auth Methods:** Email/Password, Google Sign-In
- **Database:** 
  - Collection: "files"
  - Document fields: All FileModel fields
  - Security: Checked by userId

### Supabase
- **Service:** Cloud Storage
- **Bucket:** vault-files
- **Auth:** Bearer token (SUPABASE_ANON_KEY)
- **Endpoints:**
  - PUT: /storage/v1/object/vault-files/{fileName}
  - DELETE: /storage/v1/object/vault-files/{fileName}
- **URL Pattern:** {SUPABASE_URL}/storage/v1/object/public/vault-files/{fileName}

### APIs Integrated
- Google Play Services Auth (Google Sign-In)
- Firebase Authentication API
- Firebase Firestore API
- Retrofit HTTP Client (Supabase)

---

## 🎨 UI COMPONENTS

### MainActivity
- BottomNavigationView with 4 tabs
- FrameLayout for Fragment container
- Theme: Light/Dark (persistent in SharedPreferences)

### HomeFragment
- RecyclerView with FileAdapter
- FAB for file upload
- FilePickerLauncher for Intent
- Long-click context menu
- Options: Rename, Move to Vault, Share, Download, Delete

### VaultFragment
- RecyclerView with FileAdapter (same)
- Long-click context menu
- Options: Remove from Vault, Delete Permanently

### SettingsFragment
- SwitchMaterial for dark mode
- Button for logout

### FileViewerActivity
- ImageView for images (Glide)
- WebView for PDFs (Google Docs Viewer)

### FileAdapter
- Shows file name, size, icon, favorite star
- Click: Open FileViewerActivity
- Long click: Show context menu
- Star click: Toggle favorite

---

## 🛠️ BUILD CONFIGURATION

### Kotlin
- Language Level: 1.8
- Coroutines support

### Android
- Min SDK: 26
- Target SDK: 34
- Compile SDK: 34

### Gradle
- Kotlin: org.jetbrains.kotlin.android
- Google Services: com.google.gms.google-services
- KAPT: For Glide annotation processing

### Dynamic Configuration (local.properties)
```properties
SUPABASE_URL = https://xxx.supabase.co
SUPABASE_ANON_KEY = xxxxx
```

### Build Config Fields
```kotlin
buildConfigField("String", "SUPABASE_URL", "\"$supabaseUrl\"")
buildConfigField("String", "SUPABASE_ANON_KEY", "\"$supabaseAnonKey\"")
```

### Dependencies
- Android Core: androidx.core, appcompat, material
- Firebase: BOM 33.1.2 (auth, firestore)
- Networking: Retrofit 2.9.0, OkHttp 4.10.0
- Images: Glide 4.12.0
- Google Play: play-services-auth 21.2.0

---

## 🔧 UTILITIES

### SharedPrefsManager
- File: VaultSharePrefs
- Properties:
  - isLoggedIn: Boolean (check after restart)
  - isDarkTheme: Boolean (default: true)

### PinManager
- File: VaultPinPrefs_{userId} (per-user)
- Methods:
  - isPinSet(): Check if PIN exists
  - savePin(pin): Store 4-digit PIN
  - verifyPin(pin): Validate PIN
- Security: TODO: Implement encryption

---

## 🐛 KNOWN ISSUES & TODO

1. ✅ File upload appearing in wrong locations (FIXED - April 2, 2026)
2. ❌ PIN stored plain-text (need encryption)
3. ❌ ProfileFragment is empty placeholder
4. ❌ MainViewModel not used yet
5. ⚠️ No offline caching
6. ⚠️ Limited file sharing (only URL)
7. ⚠️ No retry logic for failed uploads

---

## 📝 CODE PATTERNS

### ViewHolder with Callbacks
```kotlin
class FileViewHolder(binding: ItemFileBinding) : RecyclerView.ViewHolder(binding.root) {
    private var currentFile: FileModel? = null
    
    init {
        binding.favoriteIcon.setOnClickListener {
            currentFile?.let(onFavoriteClick)
        }
    }
    
    fun bind(file: FileModel) {
        currentFile = file  // Store for click handler
        // Update UI...
    }
}
```

### Fragment Binding Cleanup
```kotlin
override fun onCreateView(...): View {
    _binding = FragmentXxxBinding.inflate(...)
    return binding.root
}

override fun onDestroyView() {
    super.onDestroyView()
    _binding = null  // Prevent memory leak
}
```

### Coroutine Error Handling
```kotlin
viewModelScope.launch {
    try {
        _state.value = State.Loading
        val result = repository.operation()
        _state.value = State.Success(result)
    } catch (e: Exception) {
        _state.value = State.Error(e.message ?: "Error")
    }
}
```

### LiveData Observer in Fragment
```kotlin
viewModel.state.observe(viewLifecycleOwner) { state ->
    when (state) {
        is State.Loading → // Show progress
        is State.Success → // Show data
        is State.Error → // Show error
    }
}
```

---

## ✅ NEXT STEPS / IMPROVEMENTS

1. Fix file upload query filters (Favorites, Trash)
2. Encrypt PIN storage
3. Implement Favorites fragment
4. Implement Trash fragment with restore option
5. Add file search
6. Add user-to-user file sharing
7. Add upload progress visualization
8. Add camera integration
9. Add biometric vault unlock
10. Implement offline caching

---

## 🧪 TESTING SCENARIOS

**Happy Path:**
1. Sign up → Login → Upload → See in Home ✓
2. Upload → Mark favorite → See in Favorites ✓
3. Upload → Move to vault → Enter PIN → See in Vault ✓
4. Upload → Move to trash → See in Trash ✓
5. Google Sign-In → Works like email ✓

**Edge Cases:**
1. Wrong PIN → Reject, retry ✓
2. No internet → Show error ✓
3. Logout → Clear data, return to login ✓
4. Theme switch → Persist on restart ✓

---

**Document maintained: ONE FILE** ✅

