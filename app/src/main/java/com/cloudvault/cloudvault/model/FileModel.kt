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
    val isInTrash: Boolean = false,
    val userId: String = ""
) {
    @Exclude
    fun toMap(): Map<String, Any> {
        return mapOf(
            "name" to name,
            "size" to size,
            "type" to type,
            "url" to url,
            "timestamp" to timestamp,
            "isInTrash" to isInTrash,
            "userId" to userId
        )
    }
}
