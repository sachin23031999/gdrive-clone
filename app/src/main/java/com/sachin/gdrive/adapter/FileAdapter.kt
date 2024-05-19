package com.sachin.gdrive.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.sachin.gdrive.R
import com.sachin.gdrive.databinding.ItemNodeBinding
import com.sachin.gdrive.model.DriveEntity

class FileAdapter(
    private val context: Context,
    private val listener: ItemClickListener
) :
    RecyclerView.Adapter<FileAdapter.FileViewHolder>() {

    private var files: MutableList<DriveEntity> = mutableListOf()

    inner class FileViewHolder(private val binding: ItemNodeBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(node: DriveEntity) {
            when (node) {
                is DriveEntity.File -> {
                    binding.icon.setImageDrawable(
                        ContextCompat.getDrawable(context, R.drawable.ic_file)
                    )
                    binding.name.text = node.name
                    binding.root.setOnClickListener {
                        listener.onFileClick(node)
                        binding.root.setBackgroundColor(
                            ContextCompat.getColor(context, R.color.light_gray)
                        )
                    }
                }

                is DriveEntity.Folder -> {
                    binding.icon.setImageDrawable(
                        ContextCompat.getDrawable(context, R.drawable.ic_folder)
                    )
                    binding.name.text = node.name
                    binding.root.setOnClickListener {
                        listener.onFolderClick(node)
                        binding.root.setBackgroundColor(
                            ContextCompat.getColor(context, R.color.light_gray)
                        )
                    }
                }
            }
            binding.root.setOnLongClickListener {
                listener.onItemLongClick(node)
                binding.root.setBackgroundColor(Color.GRAY)
                return@setOnLongClickListener true
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