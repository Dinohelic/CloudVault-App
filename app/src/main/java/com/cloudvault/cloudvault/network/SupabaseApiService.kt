package com.cloudvault.cloudvault.network

import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.PUT
import retrofit2.http.Path

interface SupabaseApiService {
    @PUT("storage/v1/object/vault-files/{fileName}")
    suspend fun uploadFile(
        @Header("Authorization") token: String,
        @Path("fileName") fileName: String,
        @Body file: RequestBody
    ): Response<Unit>
}
