package com.cloudvault.cloudvault.ui.fragments

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.cloudvault.cloudvault.R
import com.cloudvault.cloudvault.databinding.FragmentProfileBinding
import com.cloudvault.cloudvault.viewmodel.ProfileViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.userProfileChangeRequest

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val profileViewModel: ProfileViewModel by viewModels()

    private val imagePickerLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri != null) {
            try {
                requireContext().contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                binding.profileImage.setImageURI(uri)
                val user = FirebaseAuth.getInstance().currentUser
                val uid = user?.uid ?: "default"
                val prefs = requireContext().getSharedPreferences("profile_prefs", Context.MODE_PRIVATE)
                prefs.edit().putString("profile_image_uri_$uid", uri.toString()).apply()
            } catch (e: Exception) {
                binding.profileImage.setImageResource(R.drawable.ic_default_profile)
                Toast.makeText(requireContext(), "Failed to set profile image", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun openImagePicker() {
        imagePickerLauncher.launch(arrayOf("image/*"))
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    private fun setupUserDetails() {
        val user = FirebaseAuth.getInstance().currentUser
        user?.let {
            binding.userEmail.text = it.email
            
            val displayName = it.displayName
            if (!displayName.isNullOrBlank()) {
                binding.userName.text = displayName
            } else {
                // Extract from email
                val emailName = it.email?.substringBefore("@") ?: "User"
                binding.userName.text = emailName
            }
        }
    }

    private fun setupStorageStats() {
        profileViewModel.storageInfo.observe(viewLifecycleOwner) { stats ->
            binding.totalFilesText.text = stats.totalFiles.toString()
            binding.totalSizeText.text = android.text.format.Formatter.formatFileSize(context, stats.totalSizeInBytes)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUserDetails()
        setupStorageStats()
        val user = FirebaseAuth.getInstance().currentUser
        val uid = user?.uid ?: "default"
        val prefs = requireContext().getSharedPreferences("profile_prefs", Context.MODE_PRIVATE)
        val savedUri = prefs.getString("profile_image_uri_$uid", null)
        if (!savedUri.isNullOrEmpty()) {
            try {
                val uri = Uri.parse(savedUri)
                // Check persisted permission
                val persistedUris = requireContext().contentResolver.persistedUriPermissions
                val hasPermission = persistedUris.any { it.uri == uri && it.isReadPermission }
                if (hasPermission) {
                    binding.profileImage.setImageURI(uri)
                } else {
                    throw Exception("No persisted permission for URI")
                }
            } catch (e: Exception) {
                binding.profileImage.setImageResource(R.drawable.ic_default_profile)
            }
        } else {
            // Always use ic_default_profile if no user photo is set
            binding.profileImage.setImageResource(R.drawable.ic_default_profile)
        }

        binding.btnChangeImage.setOnClickListener {
            openImagePicker()
        }

        binding.btnEditName.setOnClickListener {
            showEditNameDialog(binding.userName)
        }
    }

    private fun showEditNameDialog(tvUsername: TextView) {
        val editText = EditText(requireContext())
        editText.hint = "Enter New Name"
        editText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_WORDS

        AlertDialog.Builder(requireContext())
            .setTitle("Edit Profile Name")
            .setView(editText)
            .setPositiveButton("Save") { _, _ ->
                val newName = editText.text.toString().trim()

                if (newName.isNotEmpty()) {
                    val profileUpdates = userProfileChangeRequest {
                        displayName = newName
                    }

                    FirebaseAuth.getInstance().currentUser
                        ?.updateProfile(profileUpdates)
                        ?.addOnSuccessListener {
                            tvUsername.text = newName
                            Toast.makeText(
                                requireContext(),
                                "Name Updated",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        ?.addOnFailureListener { e ->
                            Toast.makeText(
                                requireContext(),
                                "Failed to update name: ${e.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
