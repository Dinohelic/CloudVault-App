# DETAILED COMPONENT BREAKDOWN

## 🎯 FILE MANIFEST WITH DESCRIPTIONS

### Activities (UI Screens)
```
LoginActivity.kt (142 lines)
├─ Purpose: Authentication screen
├─ Inputs: Email, Password, Google Sign-In
├─ Outputs: Navigate to MainActivity or show error
├─ Key Methods:
│  ├─ setupClickListeners() - Button handlers
│  ├─ observeViewModel() - Watch AuthState
│  └─ googleSignInLauncher - Handle Google Sign-In response
└─ Uses: AuthViewModel, FirebaseAuth, GoogleSignIn

SplashActivity.kt (26 lines)
├─ Purpose: App launcher screen
├─ Logic: 2-second delay, check login status
├─ Decision:
│  ├─ Logged in → MainActivity
│  └─ Not logged in → LoginActivity
└─ Uses: FirebaseAuth

MainActivity.kt (89 lines)
├─ Purpose: Main app hub with bottom navigation
├─ Fragments: Home, Vault (PIN), Profile, Settings
├─ Key Logic:
│  ├─ Bottom nav listener
│  ├─ Intercept Vault click → PIN verification
│  └─ Welcome notification on launch
└─ Uses: BottomNavigationView, PinEntryActivity

PinEntryActivity.kt (91 lines)
├─ Purpose: PIN setup or verification screen
├─ States: Setup mode (create PIN) vs Verify mode
├─ Logic:
│  ├─ First PIN entry → "Confirm your PIN"
│  ├─ Second PIN entry → Compare & save
│  ├─ Verify mode → Check against stored PIN
│  └─ Wrong PIN → Show error, retry
└─ Uses: PinManager, FirebaseAuth

FileViewerActivity.kt (57 lines)
├─ Purpose: Display file content
├─ Supports: Images (Glide), PDFs (WebView + Google Docs)
├─ Logic:
│  ├─ Get file name, URL, type from Intent
│  ├─ Detect file type
│  ├─ Show image or embed PDF viewer
│  └─ Show error for unsupported types
└─ Uses: Glide, WebView
```

### Fragments (UI Screens in MainActivity)
```
HomeFragment.kt (196 lines)
├─ Purpose: Main storage - browse & manage files
├─ Features:
│  ├─ RecyclerView of files
│  ├─ FAB to upload new files
│  ├─ File picker intent
│  ├─ Context menu (long click)
│  ├─ File actions: rename, move to vault, share, download, delete
│  └─ Favorite toggle
├─ Observers:
│  ├─ fileListState → Loading/Success/Error
│  ├─ uploadState → Show progress during upload
│  └─ fileActionState → Show action results
└─ Uses: FileViewModel, FileAdapter

VaultFragment.kt (111 lines)
├─ Purpose: PIN-protected secure storage
├─ Features:
│  ├─ RecyclerView of vault files only
│  ├─ Context menu (long click)
│  ├─ File actions: Remove from vault, Delete permanently
│  └─ Favorite toggle
├─ Observers:
│  ├─ fileListState → Loading/Success/Error
│  └─ fileActionState → Show action results
└─ Uses: VaultViewModel, FileAdapter

ProfileFragment.kt (20 lines)
├─ Purpose: User profile screen
├─ Status: EMPTY PLACEHOLDER
└─ TODO: Implement user profile display

SettingsFragment.kt (66 lines)
├─ Purpose: App settings & account management
├─ Features:
│  ├─ Dark mode toggle switch
│  ├─ Logout button
│  └─ Google Sign-Out
├─ Logic:
│  ├─ Toggle theme → Update preference → Restart app theme
│  ├─ Logout → Sign out Firebase → Sign out Google → Navigate to Login
└─ Uses: AuthViewModel, SharedPrefsManager, GoogleSignIn
```

### ViewModels (State Management)
```
AuthViewModel.kt (65 lines)
├─ State: AuthState (Loading/Success/Error/Idle)
├─ Methods:
│  ├─ loginWithEmail(email, password)
│  ├─ signUpWithEmail(email, password)
│  ├─ signInWithGoogle(credential)
│  └─ logout()
├─ Logic: Delegate to AuthRepository, update AuthState
└─ Uses: AuthRepository, Coroutines, viewModelScope

FileViewModel.kt (197 lines)
├─ States:
│  ├─ FileListState (Loading/Success/Error)
│  ├─ UploadState (Idle/Uploading/Success/Error)
│  └─ FileActionState (Idle/Success/Error)
├─ Methods:
│  ├─ fetchFiles() - Load HOME files
│  ├─ uploadFile(context, uri) - Upload & save metadata
│  ├─ downloadFile(context, file) - Download to Downloads
│  ├─ renameFile(fileId, newName)
│  ├─ moveToVault(fileId)
│  ├─ moveToTrash(fileId)
│  ├─ toggleFavorite(fileId, isFavorite)
│  └─ clearActionState()
├─ Logic: Get file name/size from URI, handle uploads
└─ Uses: FileRepository, Coroutines, DownloadManager

VaultViewModel.kt (77 lines)
├─ States:
│  ├─ FileListState (Loading/Success/Error)
│  └─ FileActionState (Idle/Success/Error)
├─ Methods:
│  ├─ fetchVaultFiles() - Load VAULT files
│  ├─ removeFromVault(fileId)
│  ├─ deleteFilePermanently(file) - Delete from storage + DB
│  ├─ toggleFavorite(fileId, isFavorite)
│  └─ clearActionState()
├─ Logic: Similar to FileViewModel but for vault
└─ Uses: FileRepository, Coroutines

MainViewModel.kt (8 lines)
├─ Status: EMPTY
├─ Purpose: Future main app logic
└─ TODO: Implement if needed
```

### Repositories (Data Access)
```
AuthRepository.kt (31 lines)
├─ Methods:
│  ├─ signInWithEmail(email, password) → AuthResult
│  ├─ createUserWithEmail(email, password) → AuthResult
│  ├─ signInWithGoogleCredential(credential) → AuthResult
│  ├─ signOut()
│  └─ getCurrentUser() → FirebaseUser?
├─ Logic: Direct Firebase calls
└─ Uses: FirebaseAuth

FileRepository.kt (126 lines)
├─ Methods:
│  ├─ getFiles(FileSource) → Flow<List<FileModel>>
│  ├─ uploadFile(context, uri, fileName) → String (URL)
│  ├─ saveMetadata(fileModel) → Unit
│  ├─ renameFile(fileId, newName) → Unit
│  ├─ setVaultStatus(fileId, isInVault) → Unit
│  ├─ setTrashStatus(fileId, isInTrash) → Unit
│  ├─ toggleFavorite(fileId, isFavorite) → Unit
│  └─ deleteFilePermanently(file) → Unit
├─ Logic:
│  ├─ Firestore queries with filters
│  ├─ Flow/callbackFlow for real-time updates
│  ├─ Supabase HTTP uploads/deletes
│  └─ Metadata CRUD in Firestore
└─ Uses: Firestore, Supabase API, Retrofit
```

### Models (Data Classes)
```
FileModel.kt (35 lines)
├─ Fields:
│  ├─ @DocumentId id: String
│  ├─ name, size, type: String
│  ├─ url: String (Supabase URL)
│  ├─ timestamp: Long
│  ├─ isFavorite, isInTrash, isInVault: Boolean
│  └─ userId: String
├─ Methods:
│  └─ toMap() - Convert to Firestore map
├─ Serialization: Firestore-compatible
└─ Annotations: @DocumentId, @Exclude
```

### Adapters (RecyclerView)
```
FileAdapter.kt (76 lines)
├─ Purpose: Display files in RecyclerView
├─ Callbacks:
│  ├─ onItemClick(file) → Open FileViewerActivity
│  ├─ onItemLongClick(file) → Show context menu
│  └─ onFavoriteClick(file) → Toggle favorite
├─ ViewHolder:
│  ├─ Display: File icon, name, size, favorite star
│  ├─ Logic: File type → Icon mapping
│  └─ State: currentFile for click handlers
├─ DiffCallback: Compare by ID
└─ Uses: ListAdapter, ViewBinding
```

### Network (API Integration)
```
SupabaseApiService.kt (25 lines)
├─ Interface: Retrofit service definition
├─ Endpoints:
│  ├─ @PUT uploadFile(token, fileName, file) → Response<Unit>
│  └─ @DELETE deleteFile(token, fileName) → Response<Unit>
├─ Headers: Authorization (Bearer token)
└─ BaseUrl: Supabase domain

SupabaseClient.kt (17 lines)
├─ Purpose: Retrofit singleton
├─ Initialization: Lazy retrofit builder
├─ BaseUrl: BuildConfig.SUPABASE_URL
├─ No JSON converter (binary uploads)
└─ Service: SupabaseApiService instance
```

### Utilities (Helper Classes)
```
SharedPrefsManager.kt (22 lines)
├─ File: "VaultSharePrefs"
├─ Keys:
│  ├─ IS_LOGGED_IN → Boolean
│  └─ IS_DARK_THEME → Boolean (default: true)
├─ Usage:
│  ├─ Check login state after app restart
│  └─ Persist theme preference
└─ Pattern: Property getter/setter with SharedPreferences

PinManager.kt (34 lines)
├─ File: "VaultPinPrefs_{userId}" (per-user)
├─ Key: VAULT_PIN_KEY → "vault_pin"
├─ Methods:
│  ├─ isPinSet() → Boolean
│  ├─ savePin(pin) → Unit (plain-text, TODO: encrypt)
│  └─ verifyPin(pin) → Boolean
├─ Security: Per-user file (Android encrypts in 5.0+)
└─ TODO: Implement encryption
```

### Application Class
```
VaultShareApp.kt (18 lines)
├─ Purpose: Application initialization
├─ Logic: Set theme on app start
├─ Flow:
│  ├─ Get isDarkTheme from SharedPrefsManager
│  ├─ Set AppCompatDelegate mode accordingly
│  └─ Theme persists throughout app lifecycle
└─ Uses: SharedPrefsManager, AppCompatDelegate
```

---

## 🔄 DATA FLOW DIAGRAMS

### File Upload Sequence
```
User → FAB Click
  ↓
FilePickerLauncher (gets Uri)
  ↓
HomeFragment.fileViewModel.uploadFile(context, uri)
  ↓
FileViewModel
  ├─ uploadState = Uploading
  ├─ Get file name (OpenableColumns)
  ├─ Get file size
  └─ Create unique name: "{timestamp}_{originalName}"
      ↓
  FileRepository.uploadFile(context, uri, uniqueName)
    ├─ Open input stream from Uri
    ├─ Read bytes
    ├─ Create RequestBody
    └─ POST to Supabase
        ├─ Authorization: Bearer SUPABASE_ANON_KEY
        ├─ Endpoint: storage/v1/object/vault-files/{uniqueName}
        └─ Response: 200 OK
            ↓
        Get URL: {SUPABASE_URL}/storage/v1/object/public/vault-files/{uniqueName}
            ↓
  FileRepository.saveMetadata(fileModel)
    └─ Add to Firestore "files" collection
        ↓
  uploadState = Success
    ↓
  Toast: "File uploaded successfully!"
    ↓
  HomeFragment observes fileListState
    ├─ Firestore listener fires
    └─ RecyclerView updates with new file
```

### File Download Sequence
```
User → Click download from menu
  ↓
HomeFragment → fileViewModel.downloadFile(context, file)
  ↓
FileViewModel
  └─ DownloadManager.enqueue(Request)
      ├─ URL: file.url (Supabase URL)
      ├─ Title: file.name
      ├─ Destination: DIRECTORY_DOWNLOADS
      └─ Notification: Visible when complete
          ↓
      System handles download in background
```

### Vault Access Sequence
```
User → Click Vault in bottom nav
  ↓
MainActivity.vaultPinLauncher.launch(PinEntryActivity)
  ↓
PinEntryActivity
  ├─ PinManager.isPinSet()?
  ├─ No → Show "Create a New PIN"
  │       User enters PIN twice
  │       PinManager.savePin(pin)
  │       Return RESULT_OK
  │   ↓
  └─ Yes → Show "Enter Vault PIN"
          User enters PIN
          PinManager.verifyPin(pin)?
          ├─ Correct → Return RESULT_OK
          └─ Wrong → Show error, retry
              ↓
MainActivity receives RESULT_OK
  ├─ Load VaultFragment
  ├─ VaultViewModel.fetchVaultFiles()
  │   └─ Query Firestore: isInVault=true, isInTrash=false
  └─ Show files
```

---

## 📊 STATE TRANSITION DIAGRAMS

### FileListState (Fragment Display)
```
Initial: Loading
  ↓
Firestore query starts
  ├─ Success: Show RecyclerView + files
  ├─ Empty: Show "No files" view
  └─ Error: Show error message, hide RecyclerView
```

### UploadState (Progress Display)
```
Initial: Idle (button enabled, no progress bar)
  ↓
FAB clicked: Uploading (button disabled, show progress bar)
  ├─ Success: Hide progress, show toast
  └─ Error: Hide progress, show error toast
      ↓
  Back to Idle
```

### AuthState (Navigation)
```
Initial: Idle
  ↓
User action (login/signup/google sign-in)
  ├─ Loading: Show progress
  ├─ Success: Navigate to MainActivity
  └─ Error: Show error toast, stay on LoginActivity
```

---

## 🔍 KEY LOGIC SPOTS

### 1. File Type Detection (FileViewerActivity)
```kotlin
when {
    fileType.contains("image", ignoreCase = true) → Show Glide ImageView
    fileType.contains("pdf", ignoreCase = true) → Show PDF via Google Docs
    else → Show unsupported error
}
```

### 2. File Filter Logic (FileRepository)
```kotlin
FileSource.HOME → isInVault=false, isInTrash=false
FileSource.VAULT → isInVault=true, isInTrash=false
FileSource.FAVORITES → isFavorite=true, isInTrash=false
FileSource.TRASH → isInTrash=true (all others)
```

### 3. PIN Setup vs Verify (PinEntryActivity)
```kotlin
if (isSetupMode) {
    if (firstPin == null) firstPin = pin  // Store first entry
    else if (firstPin == pin) savePin(pin)  // Confirmed
    else resetAndRetry()  // Mismatch
} else {
    if (verifyPin(pin)) finish()  // Correct
    else showError()  // Wrong
}
```

### 4. Unique File Naming (FileViewModel)
```kotlin
uniqueFileName = "${System.currentTimeMillis()}_${originalFileName}"
// Result: "1648490123456_document.pdf"
// Prevents collisions from duplicate uploads
```

---

## 🛡️ ERROR HANDLING

### Try-Catch in Coroutines
```kotlin
viewModelScope.launch {
    try {
        val result = repository.operation()
        _state.value = State.Success(result)
    } catch (e: Exception) {
        _state.value = State.Error(e.message ?: "Unknown error")
    }
}
```

### Fragment Observers
```kotlin
when (state) {
    is FileListState.Loading → Show progress bar
    is FileListState.Success → Show files or empty view
    is FileListState.Error → Show error toast
}
```

### Network Errors (Supabase)
```kotlin
if (response.isSuccessful) {
    // Success
} else if (response.code() == 404) {
    // Handle 404 (file not found)
} else {
    // Handle other errors
    throw Exception("API Error: ${response.errorBody()}")
}
```

---

## ✅ READY FOR MODIFICATIONS

With this complete understanding:
- ✅ Add new features easily
- ✅ Fix bugs with context
- ✅ Optimize performance
- ✅ Enhance security
- ✅ Improve UI/UX

**Please tell me what you need help with!** 🚀

