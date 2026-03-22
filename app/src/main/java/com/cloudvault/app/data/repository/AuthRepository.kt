package com.cloudvault.app.data.repository


import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AuthRepository {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    suspend fun signup(email: String, password: String, name: String): String {
        val result = auth.createUserWithEmailAndPassword(email, password).await()
        val user = result.user!!

        val userMap = hashMapOf(
            "uid" to user.uid,
            "email" to email,
            "displayName" to name,
            "photoURL" to null,
            "createdAt" to System.currentTimeMillis()
        )

        db.collection("users").document(user.uid).set(userMap).await()

        return "Signup Success"
    }

    suspend fun login(email: String, password: String): String {
        auth.signInWithEmailAndPassword(email, password).await()
        return "Login Success"
    }

    fun getCurrentUser() = auth.currentUser

    fun logout() = auth.signOut()
}