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
import com.example.altong_v2.data.model.PrescriptionMedicine

class PrescriptionMedicineAdapter(
    private val onItemClick: (PrescriptionMedicine) -> Unit,
    private val onFavoriteClick: (PrescriptionMedicine) -> Unit
) : ListAdapter<PrescriptionMedicine, PrescriptionMedicineAdapter.ViewHolder>(
    PrescriptionMedicineDiffCallback()
) {

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
        private val onItemClick: (PrescriptionMedicine) -> Unit,
        private val onFavoriteClick: (PrescriptionMedicine) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(medicine: PrescriptionMedicine) {
            binding.medicineName.text = medicine.medicine_name
            binding.medicineCompany.text = medicine.manufacturer

            if (medicine.efficacy.isNullOrBlank()) {
                binding.medicineEfficacy.visibility = View.GONE
            } else {
                binding.medicineEfficacy.visibility = View.VISIBLE
                binding.medicineEfficacy.text = medicine.efficacy
            }

            if (!medicine.image_url.isNullOrBlank()) {
                Glide.with(binding.root.context)
                    .load(medicine.image_url)
                    .placeholder(R.drawable.medicine_image_placeholder)
                    .error(R.drawable.medicine_image_placeholder)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .centerCrop()
                    .into(binding.medicineImage)
            } else {
                binding.medicineImage.setImageResource(R.drawable.medicine_image_placeholder)
            }

            binding.root.setOnClickListener {
                onItemClick(medicine)
            }
        }
    }

    class PrescriptionMedicineDiffCallback : DiffUtil.ItemCallback<PrescriptionMedicine>() {
        override fun areItemsTheSame(
            oldItem: PrescriptionMedicine,
            newItem: PrescriptionMedicine
        ) = oldItem.medicine_id == newItem.medicine_id

        override fun areContentsTheSame(
            oldItem: PrescriptionMedicine,
            newItem: PrescriptionMedicine
        ) = oldItem == newItem
    }
}