package com.cloudvault.cloudvault.viewmodel
import androidx.lifecycle.LiveData import androidx.lifecycle.MutableLiveData import androidx.lifecycle.ViewModel import androidx.lifecycle.viewModelScope import com.cloudvault.cloudvault.model.FileModel import com.cloudvault.cloudvault.repository.FileRepository import kotlinx.coroutines.flow.catch import kotlinx.coroutines.launch
class TrashViewModel : ViewModel() {
    private val fileRepository = FileRepository()

    private val _fileListState = MutableLiveData<FileListState>(FileListState.Loading)
    val fileListState: LiveData<FileListState> = _fileListState

    private val _fileActionState = MutableLiveData<FileActionState>(FileActionState.Idle)
    val fileActionState: LiveData<FileActionState> = _fileActionState

    init {
        fetchTrashedFiles()
    }
    private fun fetchTrashedFiles() {
        viewModelScope.launch {
            _fileListState.value = FileListState.Loading
            fileRepository.getFiles(fetchFromTrash = true)
                .catch { e ->
                    _fileListState.value = FileListState.Error(e.message ?: "Failed to fetch files")
                }
                .collect { files ->
                    _fileListState.value = FileListState.Success(files)
                }
        }
    }

    fun restoreFile(fileId: String) {
        viewModelScope.launch {
            try {
                fileRepository.setTrashStatus(fileId, false)
                _fileActionState.value = FileActionState.Success("File restored")
            } catch (e: Exception) {
                _fileActionState.value = FileActionState.Error(e.message ?: "Failed to restore file")
            }
        }
    }
    fun deletePermanently(file: FileModel) {
        viewModelScope.launch {
            try {
                fileRepository.deleteFilePermanently(file)
            } catch (e: Exception) {
                _fileActionState.value = FileActionState.Error(e.message ?: "Failed to delete file")
            }
        }
    }

    fun clearActionState() {
        _fileActionState.value = FileActionState.Idle
    }
}