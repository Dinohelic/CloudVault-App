package com.cloudvault.cloudvault.ui.activities

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cloudvault.cloudvault.databinding.ActivityPinEntryBinding
import com.cloudvault.cloudvault.utils.PinManager
import com.google.firebase.auth.FirebaseAuth

class PinEntryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPinEntryBinding
    private lateinit var pinManager: PinManager
    private var isSetupMode: Boolean = false
    private var firstPin: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPinEntryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            Toast.makeText(this, "Error: User not logged in. Please restart the app.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        pinManager = PinManager(this, userId) // <-- Pass the user's ID
        isSetupMode = !pinManager.isPinSet()

        setupUI()
        setupClickListener()
    }

    private fun setupUI() {
        if (isSetupMode) {
            binding.titleTextView.text = "Create a New PIN"
        } else {
            binding.titleTextView.text = "Enter Vault PIN"
        }
    }

    private fun setupClickListener() {
        binding.confirmButton.setOnClickListener {
            val pin = binding.pinEditText.text.toString()
            if (pin.length != 4) {
                Toast.makeText(this, "PIN must be 4 digits", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (isSetupMode) {
                handlePinSetup(pin)
            } else {
                handlePinVerification(pin)
            }
        }
    }

    private fun handlePinSetup(pin: String) {
        if (firstPin == null) {
            firstPin = pin
            binding.titleTextView.text = "Confirm Your PIN"
            binding.pinEditText.text.clear()
        } else {
            if (firstPin == pin) {
                pinManager.savePin(pin)
                Toast.makeText(this, "PIN set successfully!", Toast.LENGTH_SHORT).show()
                setResult(Activity.RESULT_OK)
                finish()
            } else {
                Toast.makeText(this, "PINs do not match. Try again.", Toast.LENGTH_LONG).show()
                firstPin = null
                binding.titleTextView.text = "Create a New PIN"
                binding.pinEditText.text.clear()
            }
        }
    }

    private fun handlePinVerification(pin: String) {
        if (pinManager.verifyPin(pin)) {
            setResult(Activity.RESULT_OK)
            finish()
        } else {
            Toast.makeText(this, "Incorrect PIN", Toast.LENGTH_SHORT).show()
            binding.pinEditText.text.clear()
        }
    }
}
