package com.example.altong_v2.ui.medicine

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.altong_v2.databinding.ItemMedicineBinding
import com.example.altong_v2.data.model.PrescriptionMedicine

/**
 * 전문의약품 리스트 Adapter (임시 버전)
 */
class PrescriptionMedicineAdapter(
    private val onItemClick: (PrescriptionMedicine) -> Unit,
    private val onFavoriteClick: (PrescriptionMedicine) -> Unit
) : ListAdapter<PrescriptionMedicine, PrescriptionMedicineAdapter.ViewHolder>(PrescriptionMedicineDiffCallback()) {

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
            binding.medicineEfficacy.text = medicine.efficacy ?: ""

            binding.root.setOnClickListener {
                onItemClick(medicine)
            }
        }
    }

    class PrescriptionMedicineDiffCallback : DiffUtil.ItemCallback<PrescriptionMedicine>() {
        override fun areItemsTheSame(oldItem: PrescriptionMedicine, newItem: PrescriptionMedicine) =
            oldItem.medicine_id == newItem.medicine_id
        override fun areContentsTheSame(oldItem: PrescriptionMedicine, newItem: PrescriptionMedicine) =
            oldItem == newItem
    }
}