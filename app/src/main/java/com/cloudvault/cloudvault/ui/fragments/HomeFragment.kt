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
import androidx.appcompat.widget.SearchView
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
    private var allFiles = listOf<FileModel>()

    private val filePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                fileViewModel.uploadFile(requireContext(), uri)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupSearchView()
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
    
    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterFiles(newText)
                return true
            }
        })
    }

    private fun filterFiles(query: String?) {
        if (query.isNullOrBlank()) {
            fileAdapter.submitList(allFiles)
        } else {
            val filteredList = allFiles.filter {
                it.name.contains(query, ignoreCase = true)
            }
            fileAdapter.submitList(filteredList)
        }
    }

    private fun showOptionsDialog(file: FileModel) {
        val options = arrayOf("Rename", "Share URL", "Download", "Move to Trash")
        AlertDialog.Builder(requireContext())
            .setTitle(file.name)
            .setItems(options) { dialog, which ->
                when (which) {
                    0 -> showRenameDialog(file)
                    1 -> shareFile(file)
                    2 -> downloadFile(file)
                    3 -> fileViewModel.moveToTrash(file.id)
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
            
            if (state is FileListState.Success) {
                allFiles = state.files
                val query = binding.searchView.query.toString()
                if (query.isBlank()) {
                    binding.emptyView.isVisible = allFiles.isEmpty()
                    binding.recyclerView.isVisible = allFiles.isNotEmpty()
                    fileAdapter.submitList(allFiles)
                } else {
                    filterFiles(query)
                }
            } else if (state is FileListState.Error) {
                Toast.makeText(context, "Error: ${state.message}", Toast.LENGTH_LONG).show()
                binding.emptyView.isVisible = true
                binding.recyclerView.isVisible = false
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
                    Toast.makeText(context, "Error: ${state.message}", Toast.LENGTH_LONG).show()
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
