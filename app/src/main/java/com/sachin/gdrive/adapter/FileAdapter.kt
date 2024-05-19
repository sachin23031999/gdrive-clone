package com.sachin.gdrive.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.sachin.gdrive.databinding.ItemNodeBinding
import com.sachin.gdrive.model.DriveEntity

class FileAdapter(private val listener: ItemClickListener) :
    RecyclerView.Adapter<FileAdapter.FileViewHolder>() {

    private var files: MutableList<DriveEntity> = mutableListOf()

    inner class FileViewHolder(private val binding: ItemNodeBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(node: DriveEntity) {
            when (node) {
                // TODO: set the icon based on file/folder
                is DriveEntity.File -> {
                    binding.name.text = node.name
                    binding.root.setOnClickListener {
                        listener.onFileClick(node)
                    }
                }

                is DriveEntity.Folder -> {
                    binding.name.text = node.name
                    binding.root.setOnClickListener {
                        listener.onFolderClick(node)
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
        val view = ItemNodeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FileViewHolder(view)
    }

    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
        val file = files[position]
        holder.bind(file)
    }

    override fun getItemCount(): Int {
        return files.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateEntities(nodes: List<DriveEntity>) {
        this.files = nodes.toMutableList()
        notifyDataSetChanged()
    }
}