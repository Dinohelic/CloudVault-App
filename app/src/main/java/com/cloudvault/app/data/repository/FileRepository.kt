package com.cloudvault.app.data.repository

import com.cloudvault.app.data.model.FileModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FileRepository {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    suspend fun getUserFiles(): List<FileModel> {
        val userId = auth.currentUser?.uid ?: return emptyList()

        val snapshot = db.collection("files")
            .whereEqualTo("ownerId", userId)
            .get()
            .await()

        return snapshot.documents.map { doc ->
            FileModel(
                id = doc.id,
                name = doc.getString("name") ?: "",
                type = doc.getString("type") ?: "",
                size = doc.getLong("size") ?: 0,
                ownerId = doc.getString("ownerId") ?: "",
                downloadURL = doc.getString("downloadURL") ?: "",
                isFavorite = doc.getBoolean("isFavorite") ?: false,
                isTrashed = doc.getBoolean("isTrashed") ?: false,
                createdAt = doc.getLong("createdAt") ?: 0
            )
        }
    }
}