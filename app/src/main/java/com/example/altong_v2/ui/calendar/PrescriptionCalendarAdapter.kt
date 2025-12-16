package com.example.altong_v2.ui.calendar

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.altong_v2.R
import com.example.altong_v2.data.model.CalendarPrescription
import com.example.altong_v2.databinding.ItemPrescriptionCalendarBinding

/**
 * 처방전 리스트 어댑터
 * 캘린더 화면에서 진단명별로 약을 그룹핑해서 표시
 */
class PrescriptionCalendarAdapter(
    private val onPrescriptionCheckChanged: (Long, Boolean) -> Unit,  // 진단명 체크박스 클릭 콜백
    private val onToggleStateChanged: () -> Unit  // ✅ 토글 상태 변경 콜백 추가
) : RecyclerView.Adapter<PrescriptionCalendarAdapter.PrescriptionViewHolder>() {

    // 표시할 처방전 리스트
    private var prescriptions = listOf<CalendarPrescription>()

    // 각 처방전의 펼침/접힘 상태 저장
    private val expandedStates = mutableMapOf<Long, Boolean>()

    /**
     * 데이터 업데이트
     * @param newList 새로운 처방전 리스트
     */
    fun submitList(newList: List<CalendarPrescription>) {
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

        // ✅ Adapter를 ViewHolder의 멤버 변수로 저장 (재사용)
        private val drugAdapter = DrugCalendarAdapter { drugId ->
            // 개별 약 체크박스 클릭 시 처방전 체크박스 상태도 업데이트
            val position = adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                updatePrescriptionCheckbox(prescriptions[position])
            }
        }

        init {
            // ✅ LayoutManager는 초기화 시 1번만 설정
            binding.rvDrugs.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = drugAdapter
                setHasFixedSize(false)
            }
        }

        fun bind(prescription: CalendarPrescription) {
            // 진단명 설정
            binding.tvDiagnosis.text = prescription.diagnosis

            // 처방 정보 설정 (예: "7일분 · 3개 약품")
            binding.tvPrescriptionInfo.text =
                "${prescription.totalDays}일분 · ${prescription.drugCount}개 약품"

            // 체크박스 상태 설정 (모든 약이 완료되었는지 확인)
            val allCompleted = isAllDrugsCompleted(prescription)
            binding.cbPrescription.isChecked = allCompleted

            // 🔍 디버깅 로그 추가
            android.util.Log.d("PrescriptionAdapter", """
                ========================================
                진단명: ${prescription.diagnosis}
                처방전 ID: ${prescription.prescriptionId}
                drugsByTimeSlot 크기: ${prescription.drugsByTimeSlot.size}
                시간대 목록: ${prescription.drugsByTimeSlot.keys.joinToString()}
                총 약 개수: ${prescription.drugsByTimeSlot.values.sumOf { it.size }}
                ========================================
            """.trimIndent())

            // 각 시간대별 상세 정보
            prescription.drugsByTimeSlot.forEach { (timeSlot, drugs) ->
                android.util.Log.d("PrescriptionAdapter", "  $timeSlot: ${drugs.size}개 - ${drugs.joinToString { it.drugName }}")
            }

            // ✅ 데이터만 업데이트 (Adapter는 재사용)
            drugAdapter.submitList(prescription.drugsByTimeSlot)

            // 펼침/접힘 상태를 함수로 분리
            fun updateExpandState() {
                val isExpanded = expandedStates[prescription.prescriptionId] ?: false
                binding.layoutDrugs.visibility = if (isExpanded) View.VISIBLE else View.GONE
                binding.ivExpand.rotation = if (isExpanded) 180f else 0f

                android.util.Log.d("PrescriptionAdapter", "상태 업데이트: ${prescription.diagnosis} -> isExpanded=$isExpanded")
            }

            // 초기 상태 설정
            updateExpandState()

            // 헤더 클릭 시 펼침/접힘
            binding.layoutHeader.setOnClickListener {
                // ✅ 현재 상태를 Map에서 직접 가져오기
                val currentState = expandedStates[prescription.prescriptionId] ?: false
                val newState = !currentState
                expandedStates[prescription.prescriptionId] = newState

                android.util.Log.d("PrescriptionAdapter", "토글 클릭: ${prescription.diagnosis} -> $currentState → $newState")

                // 즉시 UI 업데이트
                updateExpandState()

                // ✅ Fragment에 알림 (Fragment에서 갱신)
                onToggleStateChanged()
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
        private fun isAllDrugsCompleted(prescription: CalendarPrescription): Boolean {
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
        private fun updatePrescriptionCheckbox(prescription: CalendarPrescription) {
            val allCompleted = isAllDrugsCompleted(prescription)
            binding.cbPrescription.isChecked = allCompleted
        }
    }
}