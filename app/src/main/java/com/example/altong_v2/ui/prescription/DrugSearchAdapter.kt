package com.example.altong_v2.ui.prescription

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.altong_v2.R
import com.example.altong_v2.databinding.ItemDrugSearchResultBinding


/* * 약 검색 결과 어댑터*/
class DrugSearchAdapter(
    private val onItemClick: (DrugSearchResult, Int) -> Unit
) : ListAdapter<DrugSearchResult, DrugSearchAdapter.DrugViewHolder>(DrugDiffCallback()) {
    private var selectedPosition: Int = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DrugViewHolder {
        val binding = ItemDrugSearchResultBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return DrugViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: DrugViewHolder, position: Int) {
        holder.bind(getItem(position),position, position == selectedPosition)
    }

    // 약 선택한거 상태 업뎃
    fun setSelectedPosition(position: Int) {
        val oldPosition = selectedPosition
        selectedPosition = position
        // 이전 선택 해제
        if (oldPosition != -1) {
            notifyItemChanged(oldPosition)
        }
        // 새로운 선택 표시
        notifyItemChanged(position)
    }

    class DrugViewHolder(
        private val binding: ItemDrugSearchResultBinding,
        private val onItemClick: (DrugSearchResult, Int) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(drug: DrugSearchResult, position: Int, isSelected: Boolean) {
            binding.tvDrugName.text = drug.name
            binding.tvDrugDescription.text = drug.description
            // 선택 상태에 따라 배경색 변경
            if (isSelected) {
                binding.root.strokeColor = ContextCompat.getColor(
                    binding.root.context,
                    R.color.primary_green_dark
                )
                binding.root.strokeWidth = 4
                binding.root.setCardBackgroundColor(
                    ContextCompat.getColor(
                        binding.root.context,
                        R.color.primary_green_light
                    )
                )
            } else {
                binding.root.strokeColor = ContextCompat.getColor(
                    binding.root.context,
                    R.color.border_default
                )
                binding.root.strokeWidth = 1
                binding.root.setCardBackgroundColor(
                    ContextCompat.getColor(
                        binding.root.context,
                        R.color.white
                    )
                )
            }
            binding.root.setOnClickListener {
                onItemClick(drug, position)
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