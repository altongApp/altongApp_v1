package com.example.altong_v2.ui.prescription

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.altong_v2.databinding.ItemDrugSearchResultBinding


/* * 약품 검색 결과 어댑터*/
class DrugSearchAdapter(
    private val onItemClick: (DrugSearchResult) -> Unit
) : ListAdapter<DrugSearchResult, DrugSearchAdapter.DrugViewHolder>(DrugDiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DrugViewHolder {
        val binding = ItemDrugSearchResultBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return DrugViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: DrugViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DrugViewHolder(
        private val binding: ItemDrugSearchResultBinding,
        private val onItemClick: (DrugSearchResult) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(drug: DrugSearchResult) {
            binding.tvDrugName.text = drug.name
            binding.tvDrugDescription.text = drug.description

            binding.root.setOnClickListener {
                onItemClick(drug)
            }
        }
    }

    class DrugDiffCallback : DiffUtil.ItemCallback<DrugSearchResult>() {
        override fun areItemsTheSame(oldItem: DrugSearchResult, newItem: DrugSearchResult): Boolean {
            return oldItem.name == newItem.name
        }

        override fun areContentsTheSame(oldItem: DrugSearchResult, newItem: DrugSearchResult): Boolean {
            return oldItem == newItem
        }
    }
}