package com.cloudvault.app.viewmodel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cloudvault.app.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {

    private val repo = AuthRepository()

    private val _authState = MutableStateFlow("")
    val authState: StateFlow<String> = _authState

    fun signup(email: String, password: String, name: String) {
        viewModelScope.launch {
            try {
                _authState.value = repo.signup(email, password, name)
            } catch (e: Exception) {
                _authState.value = e.message ?: "Error"
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            try {
                _authState.value = repo.login(email, password)
            } catch (e: Exception) {
                _authState.value = e.message ?: "Error"
            }
        }
    }
}