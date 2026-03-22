package com.cloudvault.app.data.model

data class FileModel(
    val id: String = "",
    val name: String = "",
    val type: String = "",
    val size: Long = 0,
    val ownerId: String = "",
    val downloadURL: String = "",
    val isFavorite: Boolean = false,
    val isTrashed: Boolean = false,
    val createdAt: Long = 0
)