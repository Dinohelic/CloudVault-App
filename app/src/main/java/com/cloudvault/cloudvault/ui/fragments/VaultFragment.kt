package com.cloudvault.cloudvault.ui.fragments

import android.app.AlertDialog
import android.content.Intent
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
import com.cloudvault.cloudvault.databinding.FragmentVaultBinding
import com.cloudvault.cloudvault.model.FileModel
import com.cloudvault.cloudvault.ui.activities.FileViewerActivity
import com.cloudvault.cloudvault.viewmodel.FileActionState
import com.cloudvault.cloudvault.viewmodel.FileListState
import com.cloudvault.cloudvault.viewmodel.VaultViewModel

class VaultFragment : Fragment() {

    private var _binding: FragmentVaultBinding? = null
    private val binding get() = _binding!!

    private val vaultViewModel: VaultViewModel by viewModels()
    private lateinit var fileAdapter: FileAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVaultBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeViewModel()
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
                showVaultOptionsDialog(file)
            }
        )
        binding.recyclerView.apply {
            adapter = fileAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }
    
    private fun showVaultOptionsDialog(file: FileModel) {
        val options = arrayOf("Remove from Vault", "Delete")
        AlertDialog.Builder(requireContext())
            .setTitle(file.name)
            .setItems(options) { dialog, which ->
                when (which) {
                    0 -> vaultViewModel.removeFromVault(file.id)
                    1 -> vaultViewModel.deleteFile(file)
                }
                dialog.dismiss()
            }
            .show()
    }

    private fun observeViewModel() {
        vaultViewModel.fileListState.observe(viewLifecycleOwner) { state ->
            binding.progressBar.isVisible = state is FileListState.Loading
            binding.emptyView.isVisible = state is FileListState.Success && state.files.isEmpty()
            binding.recyclerView.isVisible = state is FileListState.Success && state.files.isNotEmpty()

            if (state is FileListState.Success) {
                fileAdapter.submitList(state.files)
            } else if (state is FileListState.Error) {
                Toast.makeText(context, "Error: ${state.message}", Toast.LENGTH_LONG).show()
            }
        }
        
        vaultViewModel.fileActionState.observe(viewLifecycleOwner) { state ->
            when(state) {
                is FileActionState.Success -> {
                    Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                    vaultViewModel.clearActionState()
                }
                is FileActionState.Error -> {
                    Toast.makeText(context, "Error: ${state.message}", Toast.LENGTH_LONG).show()
                    vaultViewModel.clearActionState()
                }
                is FileActionState.Idle -> {}
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
