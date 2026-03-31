package com.cloudvault.cloudvault.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cloudvault.cloudvault.repository.AuthRepository
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.launch

sealed class AuthState {
    object Loading : AuthState()
    data class Success(val user: FirebaseUser?) : AuthState()
    data class Error(val message: String) : AuthState()
    object Idle : AuthState()
}

class AuthViewModel : ViewModel() {

    private val authRepository = AuthRepository()
    private val _authState = MutableLiveData<AuthState>(AuthState.Idle)
    val authState: LiveData<AuthState> = _authState

    fun loginWithEmail(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val result = authRepository.signInWithEmail(email, password)
                _authState.value = AuthState.Success(result.user)
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Login failed")
            }
        }
    }

    fun signUpWithEmail(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val result = authRepository.createUserWithEmail(email, password)
                _authState.value = AuthState.Success(result.user)
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Sign up failed")
            }
        }
    }
    
    fun signInWithGoogle(credential: AuthCredential) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val result = authRepository.signInWithGoogleCredential(credential)
                _authState.value = AuthState.Success(result.user)
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Google Sign-In failed")
            }
        }
    }

    fun logout() {
        authRepository.signOut()
    }
}
