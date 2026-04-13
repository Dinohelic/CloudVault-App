package com.cloudvault.cloudvault.adapter

import android.text.format.Formatter
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.cloudvault.cloudvault.R
import com.cloudvault.cloudvault.databinding.ItemFileBinding
import com.cloudvault.cloudvault.model.FileModel

class FileAdapter(
    private val onItemClick: (FileModel) -> Unit,
    private val onItemLongClick: (FileModel) -> Unit
) : ListAdapter<FileModel, FileAdapter.FileViewHolder>(FileDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
        val binding = ItemFileBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FileViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
        val file = getItem(position)
        holder.bind(file)
        holder.itemView.setOnClickListener { onItemClick(file) }
        holder.itemView.setOnLongClickListener {
            onItemLongClick(file)
            true
        }
        // Smooth fade-in animation
        holder.itemView.alpha = 0f
        holder.itemView.animate()
            .alpha(1f)
            .setDuration(300)
            .start()
    }

    class FileViewHolder(private val binding: ItemFileBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(file: FileModel) {
            binding.tvFileName.text = file.name
            binding.tvFileSize.text = Formatter.formatFileSize(itemView.context, file.sizeInBytes)

            val iconRes = when (file.type.substringBefore('/')) {
                "image" -> R.drawable.ic_file_image
                "application" -> if (file.type.contains("pdf")) R.drawable.ic_file_pdf else R.drawable.ic_file_generic
                else -> R.drawable.ic_file_generic
            }
            binding.fileIcon.setImageResource(iconRes)
        }
    }

    class FileDiffCallback : DiffUtil.ItemCallback<FileModel>() {
        override fun areItemsTheSame(oldItem: FileModel, newItem: FileModel): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: FileModel, newItem: FileModel): Boolean {
            return oldItem == newItem
        }
    }
}
