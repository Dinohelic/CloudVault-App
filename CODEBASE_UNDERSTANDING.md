# CloudVault - Complete Codebase Understanding

**Date:** April 2, 2026  
**App Name:** VaultShare (com.cloudvault.cloudvault)  
**Type:** Android Cloud Storage Application  
**Language:** Kotlin  
**Architecture:** MVVM + Repository Pattern

---

## 📱 PROJECT OVERVIEW

**CloudVault** is a secure cloud storage application that allows users to:
- Upload and manage files securely
- Store files in a secure vault (with PIN protection)
- Organize files with favorites, trash management
- Download and view files (images, PDFs)
- Authenticate via Firebase (Email/Password + Google Sign-In)
- Switch between light/dark themes

---

## 🏗️ ARCHITECTURE

### Layer Structure
```
UI Layer (Fragments/Activities)
    ↓
ViewModel Layer (MVVM)
    ↓
Repository Layer (Data Management)
    ↓
Data Layer (Firebase, Supabase)
```

### Key Dependencies
- **Firebase:** Authentication, Firestore Database
- **Supabase:** File Storage (bucket: vault-files)
- **Retrofit:** HTTP client for Supabase API
- **Glide:** Image loading and caching
- **Material Design:** UI components
- **Bottom Navigation View:** Fragment switching

---

## 📂 PROJECT STRUCTURE

```
cloudvault/app/src/main/
├── java/com/cloudvault/cloudvault/
│   ├── MainActivity.kt                      ← Main activity with bottom nav
│   ├── VaultShareApp.kt                    ← Application class (theme setup)
│   │
│   ├── ui/
│   │   ├── activities/
│   │   │   ├── LoginActivity.kt            ← Email/Password/Google Sign-In
│   │   │   ├── SplashActivity.kt           ← 2-second splash screen
│   │   │   ├── MainActivity.kt             ← Bottom nav + PIN vault access
│   │   │   ├── FileViewerActivity.kt       ← View images/PDFs
│   │   │   └── PinEntryActivity.kt         ← Create/Verify vault PIN
│   │   │
│   │   └── fragments/
│   │       ├── HomeFragment.kt             ← Main files + upload FAB
│   │       ├── VaultFragment.kt            ← PIN-protected vault
│   │       ├── ProfileFragment.kt          ← Empty (placeholder)
│   │       └── SettingsFragment.kt         ← Dark mode + logout
│   │
│   ├── viewmodel/
│   │   ├── AuthViewModel.kt                ← Email, Google Sign-In, Logout
│   │   ├── FileViewModel.kt                ← Home files management
│   │   ├── VaultViewModel.kt               ← Vault files management
│   │   └── MainViewModel.kt                ← Empty (future use)
│   │
│   ├── repository/
│   │   ├── AuthRepository.kt               ← Firebase Auth operations
│   │   └── FileRepository.kt               ← File CRUD + Supabase uploads
│   │
│   ├── model/
│   │   └── FileModel.kt                    ← Data class for files
│   │
│   ├── adapter/
│   │   └── FileAdapter.kt                  ← RecyclerView adapter for files
│   │
│   ├── network/
│   │   ├── SupabaseApiService.kt          ← Retrofit interface
│   │   └── SupabaseClient.kt              ← Retrofit singleton
│   │
│   └── utils/
│       ├── SharedPrefsManager.kt           ← Login state + theme preference
│       └── PinManager.kt                   ← PIN storage per user
│
└── res/layout/
    ├── activity_main.xml                   ← Bottom nav container
    ├── activity_login.xml                  ← Email, Password, Sign-In buttons
    ├── activity_splash.xml                 ← Splash screen
    ├── activity_pin_entry.xml             ← PIN entry UI
    ├── activity_file_viewer.xml           ← Image/PDF viewer
    ├── fragment_home.xml                   ← RecyclerView + FAB
    ├── fragment_vault.xml                  ← RecyclerView (vault files)
    ├── fragment_profile.xml                ← Profile placeholder
    ├── fragment_settings.xml               ← Theme switch + logout
    └── item_file.xml                       ← File list item (with favorite star)
```

---

## 🔐 AUTHENTICATION FLOW

### Flow
```
SplashActivity (2 sec delay)
    ↓
Check FirebaseAuth.currentUser
    ├─ Logged in → MainActivity
    └─ Not logged in → LoginActivity

LoginActivity
    ├─ Email/Password → AuthViewModel.loginWithEmail()
    ├─ Sign Up → AuthViewModel.signUpWithEmail()
    └─ Google Sign-In → GoogleSignInClient → AuthViewModel.signInWithGoogle()
        ↓
    Firebase Auth
        ↓
    MainActivity (Bottom Nav)
```

### Code
- **AuthRepository:** Handles Firebase Auth operations
- **AuthViewModel:** Observes AuthState (Loading, Success, Error, Idle)
- **AuthActivity:** Displays error messages and navigates

---

## 📁 FILE MANAGEMENT

### FileModel
```kotlin
data class FileModel(
    val id: String = "",                    // Firestore document ID
    val name: String = "",                  // File name
    val size: String = "",                  // Human-readable size
    val type: String = "",                  // MIME type
    val url: String = "",                   // Supabase storage URL
    val timestamp: Long = 0,                // Upload time
    val isFavorite: Boolean = false,        // User marked as favorite
    val isInTrash: Boolean = false,         // Soft delete flag
    val isInVault: Boolean = false,         // In PIN-protected vault
    val userId: String = ""                 // File owner
)
```

### File Operations

**HomeFragment:**
- ✅ Upload new files (FAB)
- ✅ View files (click → FileViewerActivity)
- ✅ Download files
- ✅ Move to Vault
- ✅ Move to Trash
- ✅ Rename file
- ✅ Toggle favorite

**VaultFragment:**
- ✅ View vault files (PIN-protected)
- ✅ Remove from vault
- ✅ Delete permanently
- ✅ Toggle favorite

**FileRepository:**
- `uploadFile()` → Supabase storage
- `saveMetadata()` → Firestore
- `getFiles(FileSource)` → Query with filters
- `renameFile()` → Update name
- `setVaultStatus()` → Move to/from vault
- `setTrashStatus()` → Soft delete
- `toggleFavorite()` → Mark favorite
- `deleteFilePermanently()` → Remove from storage + Firestore

### File Sources (Filters)
```kotlin
enum class FileSource {
    HOME,           // Not in vault, not in trash
    VAULT,          // In vault, not in trash
    FAVORITES,      // Marked as favorite, not in trash
    TRASH           // Marked as deleted
}
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
    ├─ No → Create new PIN (confirm twice)
    └─ Yes → Verify PIN
        ↓
    Correct PIN → Load VaultFragment
    Wrong PIN → Show error, try again
```

### Implementation
- **PinManager:** Stores PIN per user in SharedPreferences (user-specific file)
- **PinEntryActivity:** UI for PIN entry/creation
- **MainActivity:** Intercepts vault tab, shows PIN dialog first

### Security Note
- PINs stored plain-text (TODO: Implement encryption)
- Per-user SharedPreferences (encrypted in Android 5.0+)

---

## 📊 STATE MANAGEMENT

### FileListState (observed in fragments)
```kotlin
sealed class FileListState {
    object Loading : FileListState()
    data class Success(val files: List<FileModel>) : FileListState()
    data class Error(val message: String) : FileListState()
}
```

### UploadState (HomeFragment only)
```kotlin
sealed class UploadState {
    object Idle : UploadState()
    object Uploading : UploadState()
    data class Success(val message: String) : UploadState()
    data class Error(val message: String) : UploadState()
}
```

### FileActionState (rename, move, delete, etc.)
```kotlin
sealed class FileActionState {
    object Idle : FileActionState()
    data class Success(val message: String) : FileActionState()
    data class Error(val message: String) : FileActionState()
}
```

### AuthState
```kotlin
sealed class AuthState {
    object Loading : AuthState()
    data class Success(val user: FirebaseUser?) : AuthState()
    data class Error(val message: String) : AuthState()
    object Idle : AuthState()
}
```

---

## 🔄 DATA FLOW EXAMPLE: Upload File

```
User clicks FAB
    ↓
FilePickerLauncher (Activity Result API)
    ↓
User selects file → Gets Uri
    ↓
HomeFragment → fileViewModel.uploadFile(context, uri)
    ↓
FileViewModel
    ├─ Set UploadState.Uploading
    ├─ Get file name & size
    ├─ Create unique filename with timestamp
    ├─ Call fileRepository.uploadFile()
    │   ├─ Open input stream
    │   ├─ Read bytes
    │   └─ POST to Supabase via Retrofit
    │       ↓
    │       Response: Storage URL
    │
    ├─ Create FileModel with metadata
    ├─ Call fileRepository.saveMetadata()
    │   └─ Add to Firestore "files" collection
    │
    └─ Set UploadState.Success → Toast "Upload successful!"
        HomeFragment observes → Updates list (via Firestore listener)
```

---

## 🌐 EXTERNAL SERVICES

### Firebase (Authentication & Database)
- **Project:** self-drive-17ee0
- **Auth:** Email/Password + Google Sign-In
- **Database:** Firestore (collection: "files")
- **Rules:** Secured by userId field

### Supabase (File Storage)
- **Bucket:** vault-files
- **Auth:** API key via Authorization header
- **Endpoints:**
  - `PUT /storage/v1/object/vault-files/{fileName}` → Upload
  - `DELETE /storage/v1/object/vault-files/{fileName}` → Delete
- **URLs:** Built dynamically from SUPABASE_URL (from local.properties)

---

## 🎨 UI COMPONENTS

### MainActivity
- **BottomNavigationView** with 4 items:
  - Home (HomeFragment)
  - Vault (PinEntryActivity → VaultFragment)
  - Profile (ProfileFragment - empty)
  - Settings (SettingsFragment)
- **FrameLayout (nav_host_fragment)** for fragment replacement

### FileAdapter (RecyclerView)
- Shows file name, size, icon, favorite star
- Long click → Options dialog
- Click → FileViewerActivity
- Star click → Toggle favorite

### FileViewerActivity
- Shows images with Glide
- Shows PDFs via WebView + Google Docs Viewer
- Handles file type detection

---

## 🛠️ UTILITIES

### SharedPrefsManager
- `isLoggedIn` → Boolean flag
- `isDarkTheme` → Boolean flag
- Uses "VaultSharePrefs" file

### PinManager
- `isPinSet()` → Check if PIN exists
- `savePin(pin)` → Store 4-digit PIN
- `verifyPin(pin)` → Validate PIN
- Uses user-specific SharedPreferences file

---

## 🔧 BUILD CONFIGURATION

### BuildConfig Fields (from local.properties)
```
SUPABASE_URL = "https://xxx.supabase.co"
SUPABASE_ANON_KEY = "xxxxx"
```

### Dependencies Summary
- Android Core: API 26-34
- Firebase: Auth, Firestore (BOM 33.1.2)
- Retrofit: HTTP client
- Glide: Image loading
- Material Design: UI components
- Google Play Services: Auth, Base

### Kotlin & Tools
- Kotlin Language
- View Binding (enabled)
- KAPT (Glide annotation processing)

---

## 📡 NETWORK ARCHITECTURE

### SupabaseApiService (Retrofit Interface)
```kotlin
@PUT("storage/v1/object/vault-files/{fileName}")
suspend fun uploadFile(...): Response<Unit>

@DELETE("storage/v1/object/vault-files/{fileName}")
suspend fun deleteFile(...): Response<Unit>
```

### SupabaseClient (Retrofit Singleton)
- Lazy initializes Retrofit
- BaseUrl from BuildConfig.SUPABASE_URL
- No JSON converter (binary uploads)

### FileRepository Usage
- Wraps API calls in Dispatcher.IO coroutines
- Handles errors
- Returns URLs/status

---

## 🔄 OBSERVER PATTERN

### ViewModels expose LiveData
```kotlin
FileViewModel
├─ fileListState: LiveData<FileListState>    → Consumed by HomeFragment
├─ uploadState: LiveData<UploadState>        → Consumed by HomeFragment
├─ fileActionState: LiveData<FileActionState> → Consumed by HomeFragment

VaultViewModel
├─ fileListState: LiveData<FileListState>    → Consumed by VaultFragment
└─ fileActionState: LiveData<FileActionState> → Consumed by VaultFragment

AuthViewModel
└─ authState: LiveData<AuthState>            → Consumed by LoginActivity
```

### Fragment Observers
```kotlin
override fun onViewCreated(...) {
    viewModel.fileListState.observe(viewLifecycleOwner) { state ->
        // Handle Loading, Success, Error
    }
}
```

---

## 🚀 KEY FEATURES

✅ **Authentication**
- Email/Password signup & login
- Google Sign-In integration
- Logout with Google Sign-Out

✅ **File Management**
- Upload files to Supabase
- Store metadata in Firestore
- Download files
- Rename files
- Sort by recency

✅ **Organization**
- Home (all files)
- Vault (PIN-protected)
- Favorites (starred files)
- Trash (deleted files)

✅ **Vault Security**
- PIN protection (4 digits)
- Per-user PIN storage
- PIN setup on first access

✅ **File Viewing**
- View images (Glide)
- View PDFs (WebView + Google Docs)

✅ **Themes**
- Light mode
- Dark mode (default)
- Persistent preference

✅ **UI/UX**
- Bottom navigation
- RecyclerView lists
- FAB for upload
- Dialog menus
- Toast notifications
- Welcome notification

---

## 🐛 KNOWN ISSUES / TODO

1. **PIN Security:** Plain-text storage (need encryption)
2. **ProfileFragment:** Empty placeholder
3. **MainViewModel:** Empty (no logic yet)
4. **File Sharing:** Limited to URL sharing via Intent
5. **Offline Support:** No caching implemented
6. **Error Recovery:** Limited retry logic

---

## 📋 IMPORTANT CODE PATTERNS

### ViewHolder Pattern (FileAdapter)
```kotlin
class FileViewHolder(...) : RecyclerView.ViewHolder(...) {
    private var currentFile: FileModel? = null
    
    init {
        binding.favoriteIcon.setOnClickListener {
            currentFile?.let(onFavoriteClick)
        }
    }
    
    fun bind(file: FileModel) {
        currentFile = file  // Store for click handler
        // Update UI
    }
}
```

### Fragment Lifecycle Management
```kotlin
private var _binding: FragmentXxxBinding? = null
private val binding get() = _binding!!

override fun onCreateView(...): View {
    _binding = FragmentXxxBinding.inflate(...)
    return binding.root
}

override fun onDestroyView() {
    super.onDestroyView()
    _binding = null  // Prevent memory leak
}
```

### Coroutine Scope with Try-Catch
```kotlin
viewModelScope.launch {
    try {
        val result = repository.operation()
        _state.value = State.Success(result)
    } catch (e: Exception) {
        _state.value = State.Error(e.message ?: "Error")
    }
}
```

---

## 🎯 NEXT STEPS / IMPROVEMENTS

1. Implement real encryption for PIN
2. Add offline file caching
3. Implement favorites list view
4. Implement trash recovery
5. Add file search functionality
6. Implement file sharing with other users
7. Add progress bars for uploads
8. Add camera capture for images
9. Implement file preview during upload
10. Add Biometric unlock for vault

---

## 💡 TESTING POINTS

**Happy Path:**
1. Sign up with email → Login → Upload file → See in Home
2. Upload file → Move to Vault → Enter PIN → See in Vault
3. Upload file → Click favorite → See star updated
4. Upload file → Move to trash → See in empty home
5. Google Sign-In → Upload → Download → View

**Edge Cases:**
1. Wrong PIN entry → Should reject
2. Upload with no internet → Should show error
3. Large file upload → Should show progress
4. Switch theme → Should persist on restart
5. Logout → Should clear data & return to login

---

**Codebase fully analyzed and documented.** ✅

Ready to help with any specific improvements or features!

