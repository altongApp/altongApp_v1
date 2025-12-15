package com.example.altong_v2.ui.medicine

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.altong_v2.R
import com.example.altong_v2.databinding.ItemMedicineBinding
import com.example.altong_v2.data.model.Medicine

/**
 * 일반의약품 리스트 Adapter
 * Glide를 사용한 이미지 로딩 포함
 */
class MedicineAdapter(
    private val onItemClick: (Medicine) -> Unit,
    private val onFavoriteClick: (Medicine) -> Unit
) : ListAdapter<Medicine, MedicineAdapter.ViewHolder>(MedicineDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMedicineBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding, onItemClick, onFavoriteClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(
        private val binding: ItemMedicineBinding,
        private val onItemClick: (Medicine) -> Unit,
        private val onFavoriteClick: (Medicine) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(medicine: Medicine) {
            // 약품명
            binding.medicineName.text = medicine.medicine_name

            // 제조사
            binding.medicineCompany.text = medicine.manufacturer

            // 효능/효과 (최대 2줄)
            if (medicine.efficacy.isNullOrBlank()) {
                binding.medicineEfficacy.visibility = View.GONE
            } else {
                binding.medicineEfficacy.visibility = View.VISIBLE
                binding.medicineEfficacy.text = medicine.efficacy
            }

            // 이미지 로딩 (Glide)
            if (!medicine.image_url.isNullOrBlank()) {
                Glide.with(binding.root.context)
                    .load(medicine.image_url)
                    .placeholder(R.drawable.medicine_image_placeholder)
                    .error(R.drawable.medicine_image_placeholder)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .centerCrop()
                    .into(binding.medicineImage)
            } else {
                // 이미지 없으면 placeholder
                binding.medicineImage.setImageResource(R.drawable.medicine_image_placeholder)
            }

            // 클릭 이벤트
            binding.root.setOnClickListener {
                onItemClick(medicine)
            }
        }
    }

    class MedicineDiffCallback : DiffUtil.ItemCallback<Medicine>() {
        override fun areItemsTheSame(oldItem: Medicine, newItem: Medicine) =
            oldItem.medicine_id == newItem.medicine_id
        override fun areContentsTheSame(oldItem: Medicine, newItem: Medicine) =
            oldItem == newItem
    }
}