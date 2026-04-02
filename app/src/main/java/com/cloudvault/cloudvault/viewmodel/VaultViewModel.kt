package com.cloudvault.cloudvault.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cloudvault.cloudvault.model.FileModel
import com.cloudvault.cloudvault.repository.FileRepository
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class VaultViewModel : ViewModel() {

    private val fileRepository = FileRepository()

    private val _fileListState = MutableLiveData<FileListState>(FileListState.Loading)
    val fileListState: LiveData<FileListState> = _fileListState
    
    private val _fileActionState = MutableLiveData<FileActionState>(FileActionState.Idle)
    val fileActionState: LiveData<FileActionState> = _fileActionState

    init {
        fetchVaultFiles()
    }

    private fun fetchVaultFiles() {
        viewModelScope.launch {
            _fileListState.value = FileListState.Loading
            fileRepository.getFiles(fetchFromVault = true) // <-- FETCHES VAULTED FILES
                .catch { e ->
                    _fileListState.value = FileListState.Error(e.message ?: "Failed to fetch files")
                }
                .collect { files ->
                    _fileListState.value = FileListState.Success(files)
                }
        }
    }
    
    fun removeFromVault(fileId: String) {
        viewModelScope.launch {
            try {
                fileRepository.setVaultStatus(fileId, false)
                _fileActionState.value = FileActionState.Success("Removed from Vault")
            } catch (e: Exception) {
                _fileActionState.value = FileActionState.Error(e.message ?: "Failed to remove file")
            }
        }
    }
    
    fun deleteFile(file: FileModel) {
        viewModelScope.launch {
            try {
                fileRepository.deleteFile(file)
            } catch (e: Exception) {
                _fileActionState.value = FileActionState.Error(e.message ?: "Failed to delete file")
            }
        }
    }

    fun clearActionState() {
        _fileActionState.value = FileActionState.Idle
    }
}
