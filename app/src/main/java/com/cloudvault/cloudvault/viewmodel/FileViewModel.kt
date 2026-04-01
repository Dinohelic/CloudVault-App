package com.cloudvault.cloudvault.viewmodel

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.OpenableColumns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cloudvault.cloudvault.model.FileModel
import com.cloudvault.cloudvault.repository.FileRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

sealed class FileListState {
    object Loading : FileListState()
    data class Success(val files: List<FileModel>) : FileListState()
    data class Error(val message: String) : FileListState()
}

sealed class UploadState {
    object Idle : UploadState()
    object Uploading : UploadState()
    data class Success(val message: String) : UploadState()
    data class Error(val message: String) : UploadState()
}

sealed class FileActionState {
    object Idle : FileActionState()
    data class Success(val message: String) : FileActionState()
    data class Error(val message: String) : FileActionState()
}

class FileViewModel : ViewModel() {

    private val fileRepository = FileRepository()
    private val auth = FirebaseAuth.getInstance()

    private val _fileListState = MutableLiveData<FileListState>(FileListState.Loading)
    val fileListState: LiveData<FileListState> = _fileListState

    private val _uploadState = MutableLiveData<UploadState>(UploadState.Idle)
    val uploadState: LiveData<UploadState> = _uploadState
    
    private val _fileActionState = MutableLiveData<FileActionState>(FileActionState.Idle)
    val fileActionState: LiveData<FileActionState> = _fileActionState

    init {
        fetchFiles()
    }

    private fun fetchFiles() {
        viewModelScope.launch {
            _fileListState.value = FileListState.Loading
            fileRepository.getFiles()
                .catch { e ->
                    _fileListState.value = FileListState.Error(e.message ?: "Failed to fetch files")
                }
                .collect { files ->
                    _fileListState.value = FileListState.Success(files)
                }
        }
    }

    fun uploadFile(context: Context, fileUri: Uri) {
        viewModelScope.launch {
            _uploadState.value = UploadState.Uploading
            try {
                val fileName = getFileName(context, fileUri)
                val uniqueFileName = "${System.currentTimeMillis()}_${fileName}"
                val fileUrl = fileRepository.uploadFile(context, fileUri, uniqueFileName)
                
                val fileMetadata = FileModel(
                    name = fileName,
                    size = getFileSize(context, fileUri),
                    type = context.contentResolver.getType(fileUri) ?: "application/octet-stream",
                    url = fileUrl,
                    timestamp = System.currentTimeMillis(),
                    userId = auth.currentUser!!.uid
                )
                fileRepository.saveMetadata(fileMetadata)
                _uploadState.value = UploadState.Success("File uploaded successfully!")
            } catch (e: Exception) {
                _uploadState.value = UploadState.Error(e.message ?: "Upload failed")
            }
        }
    }

    fun downloadFile(context: Context, file: FileModel) {
        try {
            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val request = DownloadManager.Request(Uri.parse(file.url))
                .setTitle(file.name)
                .setDescription("Downloading file...")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, file.name)
            
            downloadManager.enqueue(request)
        } catch (e: Exception) {
            _fileActionState.value = FileActionState.Error("Download failed: ${e.message}")
        }
    }
    
    fun renameFile(fileId: String, newName: String) {
        viewModelScope.launch {
            try {
                if (newName.isBlank()) {
                    throw IllegalArgumentException("File name cannot be empty.")
                }
                fileRepository.renameFile(fileId, newName)
                _fileActionState.value = FileActionState.Success("File renamed successfully")
            } catch (e: Exception) {
                _fileActionState.value = FileActionState.Error(e.message ?: "Failed to rename file")
            }
        }
    }

    fun deleteFile(file: FileModel) {
        viewModelScope.launch {
            try {
                fileRepository.deleteFile(file)
                // Success message is not needed, list will update automatically
            } catch (e: Exception) {
                _fileActionState.value = FileActionState.Error(e.message ?: "Failed to delete file")
            }
        }
    }

    fun clearActionState() {
        _fileActionState.value = FileActionState.Idle
    }

    private fun getFileName(context: Context, uri: Uri): String {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    val columnIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (columnIndex != -1) {
                       result = cursor.getString(columnIndex)
                    }
                }
            } finally {
                cursor?.close()
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result!!.lastIndexOf('/')
            if (cut != -1) {
                result = result.substring(cut + 1)
            }
        }
        return result
    }
    
    private fun getFileSize(context: Context, uri: Uri): String {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        val sizeIndex = cursor!!.getColumnIndex(OpenableColumns.SIZE)
        cursor.moveToFirst()
        val size = cursor.getLong(sizeIndex)
        cursor.close()
        return android.text.format.Formatter.formatFileSize(context, size)
    }
}
