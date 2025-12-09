package com.example.altong_v2.ui.medicine

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.altong_v2.R
import com.example.altong_v2.data.local.entity.FavoriteMedicineEntity
import com.example.altong_v2.databinding.ItemFavoriteMedicineBinding

/**
 * 찜한 약품 목록 Adapter
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
            binding.medicineName.text = favorite.medicineName
            binding.medicineCompany.text = favorite.manufacturer

            // 메모 표시
            if (!favorite.memo.isNullOrBlank()) {
                binding.memoText.visibility = View.VISIBLE
                binding.memoText.text = "📝 ${favorite.memo}"
            } else {
                binding.memoText.visibility = View.GONE
            }

            // 이미지
            if (favorite.imageUrl.isNotBlank()) {
                Glide.with(binding.root.context)
                    .load(favorite.imageUrl)
                    .placeholder(R.drawable.medicine_image_placeholder)
                    .error(R.drawable.medicine_image_placeholder)
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