package com.cloudvault.cloudvault.network

import com.cloudvault.cloudvault.BuildConfig
import retrofit2.Retrofit

object SupabaseClient {
    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BuildConfig.SUPABASE_URL)
            .build()
    }

    val service: SupabaseApiService by lazy {
        retrofit.create(SupabaseApiService::class.java)
    }
}
