package com.cloudvault.cloudvault.repository

import com.cloudvault.cloudvault.model.FileModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FileRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val filesCollection = firestore.collection("files")

    fun getFiles(): Flow<List<FileModel>> = callbackFlow {
        val userId = auth.currentUser?.uid ?: run {
            close(IllegalStateException("User not logged in"))
            return@callbackFlow
        }

        val listener = filesCollection
            .whereEqualTo("userId", userId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val files = snapshot.toObjects(FileModel::class.java)
                    trySend(files)
                }
            }
        awaitClose { listener.remove() }
    }

    suspend fun addDummyFile() {
        val userId = auth.currentUser?.uid ?: return
        val dummyFile = FileModel(
            name = "Document_${System.currentTimeMillis()}.pdf",
            size = "1.5 MB",
            type = "pdf",
            url = "https://example.com/dummy.pdf",
            timestamp = System.currentTimeMillis(),
            userId = userId
        )
        filesCollection.add(dummyFile).await()
    }
}
