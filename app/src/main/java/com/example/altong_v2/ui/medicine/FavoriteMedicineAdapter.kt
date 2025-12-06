package com.example.altong_v2.ui.medicine

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.altong_v2.R
import com.example.altong_v2.databinding.ItemFavoriteMedicineBinding
import com.example.altong_v2.data.local.entity.FavoriteMedicineEntity

/**
 * 찜 목록 Adapter
 */
class FavoriteMedicineAdapter(
    private val onItemClick: (FavoriteMedicineEntity) -> Unit,
    private val onDeleteClick: (FavoriteMedicineEntity) -> Unit
) : ListAdapter<FavoriteMedicineEntity, FavoriteMedicineAdapter.ViewHolder>(FavoriteDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemFavoriteMedicineBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding, onItemClick, onDeleteClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(
        private val binding: ItemFavoriteMedicineBinding,
        private val onItemClick: (FavoriteMedicineEntity) -> Unit,
        private val onDeleteClick: (FavoriteMedicineEntity) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(favorite: FavoriteMedicineEntity) {
            // 약품명
            binding.medicineName.text = favorite.medicineName

            // 제조사
            binding.medicineCompany.text = favorite.manufacturer

            // 타입 표시
            binding.medicineType.text = if (favorite.medicineType == "otc") "💙 약국약" else "❤️ 병원약"

            // 이미지 로딩
            if (favorite.imageUrl.isNotBlank()) {
                Glide.with(binding.root.context)
                    .load(favorite.imageUrl)
                    .placeholder(R.drawable.medicine_image_placeholder)
                    .error(R.drawable.medicine_image_placeholder)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .centerCrop()
                    .into(binding.medicineImage)
            } else {
                binding.medicineImage.setImageResource(R.drawable.medicine_image_placeholder)
            }

            // 클릭 이벤트
            binding.root.setOnClickListener {
                onItemClick(favorite)
            }

            // 삭제 버튼
            binding.deleteButton.setOnClickListener {
                onDeleteClick(favorite)
            }
        }
    }

    class FavoriteDiffCallback : DiffUtil.ItemCallback<FavoriteMedicineEntity>() {
        override fun areItemsTheSame(
            oldItem: FavoriteMedicineEntity,
            newItem: FavoriteMedicineEntity
        ) = oldItem.id == newItem.id

        override fun areContentsTheSame(
            oldItem: FavoriteMedicineEntity,
            newItem: FavoriteMedicineEntity
        ) = oldItem == newItem
    }
}