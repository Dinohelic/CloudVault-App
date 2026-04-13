package com.cloudvault.cloudvault.ui.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.cloudvault.cloudvault.R
import com.cloudvault.cloudvault.databinding.ActivityLoginBinding
import com.cloudvault.cloudvault.utils.SharedPrefsManager
import com.cloudvault.cloudvault.viewmodel.AuthState
import com.cloudvault.cloudvault.viewmodel.AuthViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.GoogleAuthProvider

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val authViewModel: AuthViewModel by viewModels()
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var prefsManager: SharedPrefsManager
    
    private val TAG = "LoginActivity"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        prefsManager = SharedPrefsManager(this)


        // Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        setupClickListeners()
        observeViewModel()
    }

    private fun setupClickListeners() {
        binding.loginButton.setOnClickListener {
            val email = binding.email.text.toString().trim()
            val password = binding.password.text.toString().trim()
            if (validateInput(email, password)) {
                authViewModel.loginWithEmail(email, password)
            }
        }

        binding.signupButton.setOnClickListener {
            val email = binding.email.text.toString().trim()
            val password = binding.password.text.toString().trim()
            if (validateInput(email, password)) {
                authViewModel.signUpWithEmail(email, password)
            }
        }

        binding.googleSignInButton.setOnClickListener {
            val signInIntent = googleSignInClient.signInIntent
            googleSignInLauncher.launch(signInIntent)
        }
    }

    private fun observeViewModel() {
        authViewModel.authState.observe(this) { state ->
            when (state) {
                is AuthState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    setButtonsEnabled(false)
                }
                is AuthState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    setButtonsEnabled(true)
                    prefsManager.isLoggedIn = true
                    Toast.makeText(this, "Authentication successful", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
                is AuthState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    setButtonsEnabled(true)
                    Toast.makeText(this, "Error: ${state.message}", Toast.LENGTH_LONG).show()
                }
                is AuthState.Idle -> {
                    binding.progressBar.visibility = View.GONE
                    setButtonsEnabled(true)
                }
            }
        }
    }

    private fun validateInput(email: String, pass: String): Boolean {
        if (email.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, "Email and password cannot be empty", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }
    
    private fun setButtonsEnabled(enabled: Boolean) {
        binding.loginButton.isEnabled = enabled
        binding.signupButton.isEnabled = enabled
        binding.googleSignInButton.isEnabled = enabled
    }

    private val googleSignInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            if (account != null && account.idToken != null) {
                val credential = GoogleAuthProvider.getCredential(account.idToken!!, null)
                authViewModel.signInWithGoogle(credential)
            } else {
                Log.e(TAG, "Account or ID token is null")
                Toast.makeText(this, "Failed to get Google account credentials", Toast.LENGTH_SHORT).show()
            }
        } catch (e: ApiException) {
            Log.e(TAG, "Google sign in failed with error code: ${e.statusCode}", e)
            val errorMessage = when (e.statusCode) {
                12500 -> "Google Play Services is not available or is too old"
                12501 -> "The user cancelled the sign-in flow"
                12502 -> "One of the API calls returned an invalid result"
                12503 -> "The requestIdToken or requestScopes params are not provided or invalid"
                12504 -> "Google Sign-In configuration error"
                10 -> "There was an error with OAuth consent. Check SHA-1 fingerprint in Firebase Console"
                else -> "Error code: ${e.statusCode}"
            }
            Log.e(TAG, "Error message: $errorMessage")
            Toast.makeText(this, "Google sign in failed: $errorMessage", Toast.LENGTH_LONG).show()
        }
    }
}
