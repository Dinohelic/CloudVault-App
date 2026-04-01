package com.cloudvault.cloudvault.model

import com.google.firebase.firestore.DocumentId

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
    val userId: String = ""
)
