package com.cloudvault.cloudvault.ui.fragments

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.cloudvault.cloudvault.adapter.FileAdapter
import com.cloudvault.cloudvault.databinding.FragmentHomeBinding
import com.cloudvault.cloudvault.model.FileModel
import com.cloudvault.cloudvault.ui.activities.FileViewerActivity
import com.cloudvault.cloudvault.viewmodel.FileActionState
import com.cloudvault.cloudvault.viewmodel.FileListState
import com.cloudvault.cloudvault.viewmodel.FileViewModel
import com.cloudvault.cloudvault.viewmodel.UploadState

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val fileViewModel: FileViewModel by viewModels()
    private lateinit var fileAdapter: FileAdapter

    private val filePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                fileViewModel.uploadFile(requireContext(), uri)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupFab()
        observeFileList()
        observeUploadState()
        observeFileActionState()
    }

    private fun setupRecyclerView() {
        fileAdapter = FileAdapter(
            onItemClick = { file ->
                val intent = Intent(requireContext(), FileViewerActivity::class.java).apply {
                    putExtra("FILE_NAME", file.name)
                    putExtra("FILE_URL", file.url)
                    putExtra("FILE_TYPE", file.type)
                }
                startActivity(intent)
            },
            onItemLongClick = { file ->
                showOptionsDialog(file)
            }
        )
        binding.recyclerView.apply {
            adapter = fileAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun showOptionsDialog(file: FileModel) {
        val options = arrayOf("Move to Vault", "Rename", "Share URL", "Download", "Delete")
        AlertDialog.Builder(requireContext())
            .setTitle(file.name)
            .setItems(options) { dialog, which ->
                when (which) {
                    0 -> fileViewModel.moveToVault(file.id)
                    1 -> showRenameDialog(file)
                    2 -> shareFile(file)
                    3 -> downloadFile(file)
                    4 -> showDeleteConfirmationDialog(file)
                }
                dialog.dismiss()
            }
            .show()
    }
    
    private fun showRenameDialog(file: FileModel) {
        val editText = EditText(requireContext()).apply {
            setText(file.name)
            inputType = InputType.TYPE_CLASS_TEXT
        }
        AlertDialog.Builder(requireContext())
            .setTitle("Rename File")
            .setView(editText)
            .setPositiveButton("Rename") { dialog, _ ->
                val newName = editText.text.toString()
                fileViewModel.renameFile(file.id, newName)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun showDeleteConfirmationDialog(file: FileModel) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete File")
            .setMessage("Are you sure you want to delete '${file.name}'? This cannot be undone.")
            .setPositiveButton("Delete") { dialog, _ ->
                fileViewModel.deleteFile(file)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun shareFile(file: FileModel) {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, "Here's the file from VaultShare: ${file.url}")
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Share File URL")
        startActivity(shareIntent)
    }

    private fun downloadFile(file: FileModel) {
        fileViewModel.downloadFile(requireContext(), file)
        Toast.makeText(context, "Download started...", Toast.LENGTH_SHORT).show()
    }

    private fun setupFab() {
        binding.fabAddFile.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply { type = "*/*" }
            filePickerLauncher.launch(intent)
        }
    }

    private fun observeFileList() {
        fileViewModel.fileListState.observe(viewLifecycleOwner) { state ->
            if (fileViewModel.uploadState.value !is UploadState.Uploading) {
                 binding.progressBar.isVisible = state is FileListState.Loading
            }
            binding.emptyView.isVisible = state is FileListState.Success && state.files.isEmpty()
            binding.recyclerView.isVisible = state is FileListState.Success && state.files.isNotEmpty()

            if (state is FileListState.Success) {
                fileAdapter.submitList(state.files)
            } else if (state is FileListState.Error) {
                Toast.makeText(context, "Error: ${state.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun observeUploadState() {
        fileViewModel.uploadState.observe(viewLifecycleOwner) { state ->
            val isUploading = state is UploadState.Uploading
            binding.progressBar.isVisible = isUploading
            binding.fabAddFile.isEnabled = !isUploading

            when (state) {
                is UploadState.Success -> {
                    Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                }
                is UploadState.Error -> {
                    Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                }
                else -> {} // Idle
            }
        }
    }
    
    private fun observeFileActionState() {
        fileViewModel.fileActionState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is FileActionState.Success -> {
                    Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                    fileViewModel.clearActionState()
                }
                is FileActionState.Error -> {
                    Toast.makeText(context, "Error: ${state.message}", Toast.LENGTH_LONG).show()
                    fileViewModel.clearActionState()
                }
                is FileActionState.Idle -> { }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
