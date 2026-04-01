package com.cloudvault.cloudvault.ui.fragments

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.cloudvault.cloudvault.adapter.FileAdapter
import com.cloudvault.cloudvault.databinding.FragmentHomeBinding
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
    }

    private fun setupRecyclerView() {
        fileAdapter = FileAdapter(
            onItemClick = { file ->
                Toast.makeText(context, "${file.name} clicked", Toast.LENGTH_SHORT).show()
            },
            onItemLongClick = { file ->
                Toast.makeText(context, "${file.name} long-pressed", Toast.LENGTH_SHORT).show()
            }
        )
        binding.recyclerView.apply {
            adapter = fileAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun setupFab() {
        binding.fabAddFile.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply { type = "*/*" }
            filePickerLauncher.launch(intent)
        }
    }

    private fun observeFileList() {
        fileViewModel.fileListState.observe(viewLifecycleOwner) { state ->
            // Temporarily disable progress bar from here to let upload state control it
            // binding.progressBar.isVisible = state is FileListState.Loading
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
            binding.progressBar.isVisible = state is UploadState.Uploading
            binding.fabAddFile.isEnabled = state !is UploadState.Uploading

            when (state) {
                is UploadState.Success -> {
                    Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                }
                is UploadState.Error -> {
                    Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                }
                else -> {} // Idle or Uploading
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
