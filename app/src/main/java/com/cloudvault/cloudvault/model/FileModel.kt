package com.cloudvault.cloudvault.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.Exclude

data class FileModel(
    @DocumentId
    val id: String = "",
    val name: String = "",
    val size: String = "",
    val type: String = "",
    val url: String = "",
    val timestamp: Long = 0,
    val isFavorite: Boolean = false,
    val isInTrash: Boolean = false,
    val isInVault: Boolean = false,
    val userId: String = ""
) {
    // Manual converter to ensure all fields are included
    @Exclude
    fun toMap(): Map<String, Any> {
        return mapOf(
            "name" to name,
            "size" to size,
            "type" to type,
            "url" to url,
            "timestamp" to timestamp,
            "isFavorite" to isFavorite,
            "isInTrash" to isInTrash,
            "isInVault" to isInVault,
            "userId" to userId
        )
    }
}
