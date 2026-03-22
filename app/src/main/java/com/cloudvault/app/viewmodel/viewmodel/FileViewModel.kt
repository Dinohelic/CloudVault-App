package com.cloudvault.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cloudvault.app.data.model.FileModel
import com.cloudvault.app.data.repository.FileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class FileViewModel : ViewModel() {

    private val repo = FileRepository()

    private val _files = MutableStateFlow<List<FileModel>>(emptyList())
    val files: StateFlow<List<FileModel>> = _files

    fun loadFiles() {
        viewModelScope.launch {
            try {
                _files.value = repo.getUserFiles()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}