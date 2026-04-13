package com.cloudvault.cloudvault.ui.fragments

import android.app.AlertDialog
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
import com.cloudvault.cloudvault.databinding.FragmentTrashBinding
import com.cloudvault.cloudvault.model.FileModel
import com.cloudvault.cloudvault.viewmodel.FileActionState
import com.cloudvault.cloudvault.viewmodel.FileListState
import com.cloudvault.cloudvault.viewmodel.TrashViewModel

class TrashFragment : Fragment() {

    private var _binding: FragmentTrashBinding? = null
    private val binding get() = _binding!!

    private val trashViewModel: TrashViewModel by viewModels()
    private lateinit var fileAdapter: FileAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTrashBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        fileAdapter = FileAdapter(
            onItemClick = {
                Toast.makeText(
                    context,
                    "Restore the file to perform actions.",
                    Toast.LENGTH_SHORT
                ).show()
            },
            onItemLongClick = { file ->
                showTrashOptionsDialog(file)
            }
        )

        binding.recyclerView.apply {
            adapter = fileAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun showTrashOptionsDialog(file: FileModel) {
        val options = arrayOf("Restore", "Delete Permanently")

        AlertDialog.Builder(requireContext())
            .setTitle(file.name)
            .setItems(options) { dialog, which ->
                when (which) {
                    0 -> trashViewModel.restoreFile(file.id)
                    1 -> showDeleteConfirmationDialog(file)
                }
                dialog.dismiss()
            }
            .show()
    }

    private fun showDeleteConfirmationDialog(file: FileModel) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Permanently")
            .setMessage("Are you sure you want to permanently delete '${file.name}'? This cannot be undone.")
            .setPositiveButton("Delete") { dialog, _ ->
                trashViewModel.deletePermanently(file)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun observeViewModel() {
        trashViewModel.fileListState.observe(viewLifecycleOwner) { state ->

            binding.progressBar.isVisible = state is FileListState.Loading

            when (state) {
                is FileListState.Success -> {
                    fileAdapter.submitList(state.files)

                    val isEmpty = state.files.isEmpty()

                    binding.emptyView.isVisible = isEmpty
                    binding.recyclerView.isVisible = !isEmpty
                }

                is FileListState.Error -> {
                    Toast.makeText(
                        context,
                        "Error: ${state.message}",
                        Toast.LENGTH_LONG
                    ).show()

                    binding.emptyView.isVisible = true
                    binding.recyclerView.isVisible = false
                }

                is FileListState.Loading -> Unit
            }
        }

        trashViewModel.fileActionState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is FileActionState.Success -> {
                    Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                    trashViewModel.clearActionState()
                }

                is FileActionState.Error -> {
                    Toast.makeText(
                        context,
                        "Error: ${state.message}",
                        Toast.LENGTH_LONG
                    ).show()
                    trashViewModel.clearActionState()
                }

                FileActionState.Idle -> Unit
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}