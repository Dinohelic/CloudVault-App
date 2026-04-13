package com.cloudvault.cloudvault.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.Exclude

data class FileModel(
    @DocumentId
    val id: String = "",
    val name: String = "",
    val sizeInBytes: Long = 0, // Changed from String to Long
    val type: String = "",
    val url: String = "",
    val timestamp: Long = 0,
    val isInTrash: Boolean = false,
    val userId: String = ""
) {
    @Exclude
    fun toMap(): Map<String, Any> {
        return mapOf(
            "name" to name,
            "sizeInBytes" to sizeInBytes, // Updated field
            "type" to type,
            "url" to url,
            "timestamp" to timestamp,
            "isInTrash" to isInTrash,
            "userId" to userId
        )
    }
}
