package com.cloudvault.cloudvault.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cloudvault.cloudvault.model.FileModel
import com.cloudvault.cloudvault.repository.FileRepository
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

sealed class FileListState {
    object Loading : FileListState()
    data class Success(val files: List<FileModel>) : FileListState()
    data class Error(val message: String) : FileListState()
}

class FileViewModel : ViewModel() {

    private val fileRepository = FileRepository()
    private val _fileListState = MutableLiveData<FileListState>(FileListState.Loading)
    val fileListState: LiveData<FileListState> = _fileListState

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

    fun addDummyFile() {
        viewModelScope.launch {
            try {
                fileRepository.addDummyFile()
            } catch (e: Exception) {
                // Optionally, handle the error with a separate LiveData event for UI feedback
            }
        }
    }
}
