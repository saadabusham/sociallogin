package com.sedo.contextmenu.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.sedo.contextmenu.data.models.Menu
import com.sedo.contextmenu.databinding.RowContextItemBinding
import com.sedo.contextmenu.ui.base.BaseBindingRecyclerViewAdapter
import com.sedo.contextmenu.ui.base.BaseViewHolder


class ContextMenuRecyclerAdapter constructor(
    context: Context
) : BaseBindingRecyclerViewAdapter<Menu>(context) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ViewHolder(
            RowContextItemBinding.inflate(
                LayoutInflater.from(context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ViewHolder) {
            holder.bind(items[position])
        }
    }

    inner class ViewHolder(private val binding: RowContextItemBinding) :
        BaseViewHolder<Menu>(binding.root) {

        override fun bind(item: Menu) {
            binding.data = item
            binding.root.setOnClickListener {
                itemClickListener?.onItemClick(it, adapterPosition, item)
            }
        }
    }
}