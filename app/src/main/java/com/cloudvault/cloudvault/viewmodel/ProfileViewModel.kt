package com.cloudvault.cloudvault.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cloudvault.cloudvault.model.FileModel
import com.cloudvault.cloudvault.repository.FileRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

data class StorageInfo(
    val totalFiles: Int,
    val totalSizeInBytes: Long
)

class ProfileViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    
    private val _storageInfo = MutableLiveData<StorageInfo>()
    val storageInfo: LiveData<StorageInfo> = _storageInfo

    val currentUser = auth.currentUser

    init {
        fetchStorageInfo()
    }

    fun fetchStorageInfo() {
        val userId = currentUser?.uid ?: return
        
        viewModelScope.launch {
            firestore.collection("files")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener { result ->
                    val files = result.toObjects(FileModel::class.java)
                    val totalFiles = files.size
                    val totalSize = files.sumOf { it.sizeInBytes }
                    _storageInfo.value = StorageInfo(totalFiles, totalSize)
                }
                .addOnFailureListener {
                    // Handle failure
                }
        }
    }
}
