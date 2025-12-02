package com.example.altong_v2.ui.prescription

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.altong_v2.data.local.entity.DrugEntity
import com.example.altong_v2.databinding.ItemDrugDetailBinding

/* * 약품 상세 정보 어댑터*/
class DrugDetailAdapter : ListAdapter<DrugEntity, DrugDetailAdapter.DrugViewHolder>(DrugDiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DrugViewHolder {
        val binding = ItemDrugDetailBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return DrugViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DrugViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    class DrugViewHolder(
        private val binding: ItemDrugDetailBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(drug: DrugEntity) {
            // 약품명
            binding.tvDrugName.text = drug.name
            // !~~~!~! 약품 이미지 처리 (추가해야함)
            // TODO: 나중에 약 DB에서 이미지 URL 가져올 때 처리
            // if (!drug.imageUrl.isNullOrEmpty()) {
            //     binding.tvDrugEmoji.visibility = View.GONE
            //     binding.ivDrugImage.visibility = View.VISIBLE
            //     // Glide 또는 Coil로 이미지 로드
            //     Glide.with(binding.root.context)
            //         .load(drug.imageUrl)
            //         .into(binding.ivDrugImage)
            // } else {
            //     binding.tvDrugEmoji.visibility = View.VISIBLE
            //     binding.ivDrugImage.visibility = View.GONE
            // }

            // 현재는 항상 이모지 표시
            binding.tvDrugEmoji.visibility = View.VISIBLE
            binding.ivDrugImage.visibility = View.GONE

            // 복용 정보
            binding.tvDosageInfo.text = "1회 ${drug.dosage}, 1일 ${drug.frequency}"
            // 총 일수
            binding.tvDays.text = "${drug.days}일분"
            // 복용 시점 (있을 경우만 표시)
            if (!drug.timing.isNullOrEmpty()) {
                binding.layoutTiming.visibility = View.VISIBLE
                binding.tvTiming.text = drug.timing
            } else {
                binding.layoutTiming.visibility = View.GONE
            }
            // 알림 시간대
            binding.tvTimeSlots.text = drug.timeSlots
            // 메모 (있을 경우만 표시)
            if (!drug.memo.isNullOrEmpty()) {
                binding.layoutMemo.visibility = View.VISIBLE
                binding.tvMemo.text = drug.memo
            } else {
                binding.layoutMemo.visibility = View.GONE
            }
        }
    }

    class DrugDiffCallback : DiffUtil.ItemCallback<DrugEntity>() {
        override fun areItemsTheSame(oldItem: DrugEntity, newItem: DrugEntity): Boolean {
            return oldItem.id == newItem.id
        }
        override fun areContentsTheSame(oldItem: DrugEntity, newItem: DrugEntity): Boolean {
            return oldItem == newItem
        }
    }
}