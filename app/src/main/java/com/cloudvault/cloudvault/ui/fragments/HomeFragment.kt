package com.cloudvault.cloudvault.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.cloudvault.cloudvault.adapter.FileAdapter
import com.cloudvault.cloudvault.databinding.FragmentHomeBinding
import com.cloudvault.cloudvault.viewmodel.FileListState
import com.cloudvault.cloudvault.viewmodel.FileViewModel

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val fileViewModel: FileViewModel by viewModels()
    private lateinit var fileAdapter: FileAdapter

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
        observeViewModel()
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
            fileViewModel.addDummyFile()
        }
    }

    private fun observeViewModel() {
        fileViewModel.fileListState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is FileListState.Loading -> {
                    binding.progressBar.isVisible = true
                    binding.emptyView.isVisible = false
                    binding.recyclerView.isVisible = false
                }
                is FileListState.Success -> {
                    binding.progressBar.isVisible = false
                    if (state.files.isEmpty()) {
                        binding.emptyView.isVisible = true
                        binding.recyclerView.isVisible = false
                    } else {
                        binding.emptyView.isVisible = false

                        binding.recyclerView.isVisible = true
                        fileAdapter.submitList(state.files)
                    }
                }
                is FileListState.Error -> {
                    binding.progressBar.isVisible = false
                    binding.emptyView.isVisible = false
                    binding.recyclerView.isVisible = false
                    Toast.makeText(context, "Error: ${state.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
