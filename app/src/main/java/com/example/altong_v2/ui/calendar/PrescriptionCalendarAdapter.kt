package com.example.altong_v2.ui.calendar

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.altong_v2.R
import com.example.altong_v2.data.model.PrescriptionWithDrugs
import com.example.altong_v2.databinding.ItemPrescriptionCalendarBinding

/**
 * 처방전 리스트 어댑터
 * 캘린더 화면에서 진단명별로 약을 그룹핑해서 표시
 */
class PrescriptionCalendarAdapter(
    private val onPrescriptionCheckChanged: (Long, Boolean) -> Unit  // 진단명 체크박스 클릭 콜백
) : RecyclerView.Adapter<PrescriptionCalendarAdapter.PrescriptionViewHolder>() {

    // 표시할 처방전 리스트
    private var prescriptions = listOf<PrescriptionWithDrugs>()

    // 각 처방전의 펼침/접힘 상태 저장
    private val expandedStates = mutableMapOf<Long, Boolean>()

    /**
     * 데이터 업데이트
     * @param newList 새로운 처방전 리스트
     */
    fun submitList(newList: List<PrescriptionWithDrugs>) {
        prescriptions = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PrescriptionViewHolder {
        val binding = ItemPrescriptionCalendarBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PrescriptionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PrescriptionViewHolder, position: Int) {
        holder.bind(prescriptions[position])
    }

    override fun getItemCount(): Int = prescriptions.size

    /**
     * ViewHolder: 개별 처방전 카드를 담당
     */
    inner class PrescriptionViewHolder(
        private val binding: ItemPrescriptionCalendarBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(prescription: PrescriptionWithDrugs) {
            // 진단명 설정
            binding.tvDiagnosis.text = prescription.diagnosis

            // 처방 정보 설정 (예: "7일분 · 3개 약품")
            binding.tvPrescriptionInfo.text =
                "${prescription.totalDays}일분 · ${prescription.drugCount}개 약품"

            // 체크박스 상태 설정 (모든 약이 완료되었는지 확인)
            val allCompleted = isAllDrugsCompleted(prescription)
            binding.cbPrescription.isChecked = allCompleted

            // 펼침/접힘 상태 복원
            val isExpanded = expandedStates[prescription.prescriptionId] ?: true
            binding.layoutDrugs.visibility = if (isExpanded) View.VISIBLE else View.GONE

            // 화살표 아이콘 회전
            binding.ivExpand.rotation = if (isExpanded) 180f else 0f

            // 약 리스트 어댑터 설정
            val drugAdapter = DrugCalendarAdapter { drugId ->
                // 개별 약 체크박스 클릭 시 처방전 체크박스 상태도 업데이트
                updatePrescriptionCheckbox(prescription)
            }
            binding.rvDrugs.adapter = drugAdapter
            drugAdapter.submitList(prescription.drugsByTimeSlot)

            // 헤더 클릭 시 펼침/접힘
            binding.layoutHeader.setOnClickListener {
                val newState = !isExpanded
                expandedStates[prescription.prescriptionId] = newState
                notifyItemChanged(adapterPosition)
            }

            // 체크박스 클릭 시 해당 처방전의 모든 약 체크/해제
            binding.cbPrescription.setOnClickListener {
                val isChecked = binding.cbPrescription.isChecked
                onPrescriptionCheckChanged(prescription.prescriptionId, isChecked)
            }
        }

        /**
         * 처방전의 모든 약이 완료되었는지 확인
         */
        private fun isAllDrugsCompleted(prescription: PrescriptionWithDrugs): Boolean {
            for (drugList in prescription.drugsByTimeSlot.values) {
                for (drug in drugList) {
                    if (!drug.isCompleted) {
                        return false
                    }
                }
            }
            return true
        }

        /**
         * 처방전 체크박스 상태 업데이트
         * (개별 약 체크 시 호출)
         */
        private fun updatePrescriptionCheckbox(prescription: PrescriptionWithDrugs) {
            val allCompleted = isAllDrugsCompleted(prescription)
            binding.cbPrescription.isChecked = allCompleted
        }
    }
}