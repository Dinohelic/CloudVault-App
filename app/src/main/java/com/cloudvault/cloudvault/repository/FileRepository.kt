package com.cloudvault.cloudvault.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.cloudvault.cloudvault.BuildConfig
import com.cloudvault.cloudvault.model.FileModel
import com.cloudvault.cloudvault.network.SupabaseClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody

class FileRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val filesCollection = firestore.collection("files")
    private val supabaseService = SupabaseClient.service

    fun getFiles(fetchFromVault: Boolean): Flow<List<FileModel>> = callbackFlow {
        val userId = auth.currentUser?.uid ?: run {
            close(IllegalStateException("User not logged in"))
            return@callbackFlow
        }

        val listener = filesCollection
            .whereEqualTo("userId", userId)
            .whereEqualTo("isInVault", fetchFromVault)
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
    
    suspend fun setVaultStatus(fileId: String, isInVault: Boolean) {
        filesCollection.document(fileId).update("isInVault", isInVault).await()
    }
    
    suspend fun uploadFile(context: Context, fileUri: Uri, fileName: String): String {
        return withContext(Dispatchers.IO) {
            val bearerToken = "Bearer ${BuildConfig.SUPABASE_ANON_KEY}"
            val inputStream = context.contentResolver.openInputStream(fileUri)
            val fileBytes = inputStream!!.readBytes()
            inputStream.close()
            
            val requestBody = fileBytes.toRequestBody("application/octet-stream".toMediaTypeOrNull())
            
            val response = supabaseService.uploadFile(bearerToken, fileName, requestBody)

            if (response.isSuccessful) {
                "${BuildConfig.SUPABASE_URL}/storage/v1/object/public/vault-files/$fileName"
            } else {
                throw Exception("Supabase Upload Failed: ${response.errorBody()?.string()}")
            }
        }
    }

    suspend fun saveMetadata(fileModel: FileModel) {
        val fileMap = fileModel.toMap()
        Log.d("FileRepository", "Saving metadata to Firestore: $fileMap")
        filesCollection.add(fileMap).await()
    }
    
    suspend fun renameFile(fileId: String, newName: String) {
        filesCollection.document(fileId).update("name", newName).await()
    }

    suspend fun deleteFile(file: FileModel) {
        withContext(Dispatchers.IO) {
            val fileName = file.url.substringAfterLast("/")
            val bearerToken = "Bearer ${BuildConfig.SUPABASE_ANON_KEY}"
            
            Log.d("FileRepository", "Attempting to delete. Using code with 404 check.")

            val response = supabaseService.deleteFile(bearerToken, fileName)
            if (!response.isSuccessful && response.code() != 404) {
                throw Exception("Failed to delete file from storage: ${response.errorBody()?.string()}")
            }

            filesCollection.document(file.id).delete().await()
        }
    }
}
