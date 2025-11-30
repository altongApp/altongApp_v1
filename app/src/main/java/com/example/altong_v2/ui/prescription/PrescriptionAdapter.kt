package com.example.altong_v2.ui.prescription

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.altong_v2.databinding.ItemPrescriptionCardBinding
import com.example.altong_v2.data.local.entity.PrescriptionEntity
import com.google.android.material.chip.Chip

/* * 처방전 RecyclerView Adapter
 * DiffUtil 사용으로 효율적인 리스트 업데이트*/

class PrescriptionAdapter(
    private val onItemClick: (PrescriptionEntity) -> Unit,
    private val onAddDrugClick: (Long) -> Unit  // 약품 추가 버튼 클릭
) : RecyclerView.Adapter<PrescriptionAdapter.PrescriptionViewHolder>() {

    private var prescriptionList: List<PrescriptionWithDrugs> = emptyList()

    // 처방전 + 약품 개수를 담는 데이터클래스임
    data class PrescriptionWithDrugs(
        val prescription: PrescriptionEntity,
        val drugCount: Int = 0,
        val drugNames: List<String> = emptyList()
    )

    fun submitList(newList: List<PrescriptionWithDrugs>) {
        val diffCallback = PrescriptionDiffCallback(prescriptionList, newList)
        val diffResult = DiffUtil.calculateDiff(diffCallback)

        prescriptionList = newList
        diffResult.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PrescriptionViewHolder {
        val binding = ItemPrescriptionCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PrescriptionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PrescriptionViewHolder, position: Int) {
        holder.bind(prescriptionList[position])
    }

    override fun getItemCount(): Int = prescriptionList.size

    inner class PrescriptionViewHolder(
        private val binding: ItemPrescriptionCardBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: PrescriptionWithDrugs) {
            val prescription = item.prescription

            binding.apply {
                tvPrescriptionDate.text = prescription.date
                tvHospital.text = prescription.hospital ?: "병원명 없음"
                tvDiagnosis.text = prescription.diagnosis

                // 약품 뱃지 표시
                chipGroupDrugs.removeAllViews()
                if (item.drugNames.isNotEmpty()) {
                    item.drugNames.forEach { drugName ->
                        val chip = Chip(binding.root.context).apply {
                            text = "💊 $drugName"
                            isClickable = false
                            isCheckable = false
                            setTextColor(binding.root.context.getColor(android.R.color.white))
                            setChipBackgroundColorResource(com.example.altong_v2.R.color.primary_green_dark)
                        }
                        chipGroupDrugs.addView(chip)
                    }
                }

                // 약품 미등록 경고
                if (item.drugCount == 0) {
                    warningContainer.visibility = View.VISIBLE
                    btnAddDrug.setOnClickListener {
                        onAddDrugClick(prescription.id)
                    }
                } else {
                    warningContainer.visibility = View.GONE
                }

                root.setOnClickListener {
                    onItemClick(prescription)
                }
            }
        }
    }

    class PrescriptionDiffCallback(
        private val oldList: List<PrescriptionWithDrugs>,
        private val newList: List<PrescriptionWithDrugs>
    ) : DiffUtil.Callback() {

        override fun getOldListSize() = oldList.size
        override fun getNewListSize() = newList.size

        override fun areItemsTheSame(oldPos: Int, newPos: Int): Boolean {
            return oldList[oldPos].prescription.id == newList[newPos].prescription.id
        }

        override fun areContentsTheSame(oldPos: Int, newPos: Int): Boolean {
            return oldList[oldPos] == newList[newPos]
        }
    }
}