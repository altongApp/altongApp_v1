package com.example.altong_v2.ui.prescription

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.altong_v2.databinding.ItemDrugEditBinding

/* * 약품 수정 어댑터*/

class DrugEditAdapter(
    private val onEditClick: (Int) -> Unit,
    private val onDeleteClick: (Int) -> Unit
) : ListAdapter<TempDrugData, DrugEditAdapter.DrugViewHolder>(DrugDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DrugViewHolder {
        val binding = ItemDrugEditBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return DrugViewHolder(binding, onEditClick, onDeleteClick)
    }
    override fun onBindViewHolder(holder: DrugViewHolder, position: Int) {
        holder.bind(getItem(position), position)
    }

    class DrugViewHolder(
        private val binding: ItemDrugEditBinding,
        private val onEditClick: (Int) -> Unit,
        private val onDeleteClick: (Int) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(drug: TempDrugData, position: Int) {
            // 약품명
            binding.tvDrugName.text = drug.name
            // 복용 정보
            binding.tvDosageInfo.text = "1회 ${drug.dosage}, 1일 ${drug.frequency}"
            // 총 일수
            binding.tvDays.text = "총 ${drug.days}일분"
            // 알림 시간대
            binding.tvTimeSlots.text = "⏰ ${drug.timeSlots.joinToString(", ")}"
            // 수정
            binding.btnEdit.setOnClickListener {
                onEditClick(position)
            }
            // 삭제
            binding.btnDelete.setOnClickListener {
                onDeleteClick(position)
            }
        }
    }

    class DrugDiffCallback : DiffUtil.ItemCallback<TempDrugData>() {
        override fun areItemsTheSame(oldItem: TempDrugData, newItem: TempDrugData): Boolean {
            return oldItem.name == newItem.name
        }
        override fun areContentsTheSame(oldItem: TempDrugData, newItem: TempDrugData): Boolean {
            return oldItem == newItem
        }
    }
}