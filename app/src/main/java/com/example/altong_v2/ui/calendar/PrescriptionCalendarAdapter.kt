package com.example.altong_v2.ui.calendar

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.altong_v2.data.model.CalendarPrescription
import com.example.altong_v2.databinding.ItemPrescriptionCalendarBinding


 // 처방전 리스트 어댑터 - 캘린더 화면에서 진단명별로 약을 그룹핑해서 표시

class PrescriptionCalendarAdapter(
    private val activity: FragmentActivity,
    private val onPrescriptionCheckChanged: (Long, Boolean) -> Unit,
    private val onDrugCheckChanged: (Long, String) -> Unit,
    private val onGetUpdatedPrescription: (Long, (CalendarPrescription?) -> Unit) -> Unit  // 최신 데이터 가져오기
) : RecyclerView.Adapter<PrescriptionCalendarAdapter.PrescriptionViewHolder>() {

    // 표시할 처방전 리스트
    private var prescriptions = listOf<CalendarPrescription>()

    /*
     - 데이터 업데이트
     - @param newList 새로운 처방전 리스트
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

    /*
     - ViewHolder: 개별 처방전 카드를 담당
     */
    inner class PrescriptionViewHolder(
        private val binding: ItemPrescriptionCalendarBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(prescription: CalendarPrescription) {
            // 진단명 설정
            binding.tvDiagnosis.text = prescription.diagnosis

            // 처방 정보 설정 (예: "7일분 · 3개 약품")
            binding.tvPrescriptionInfo.text =
                "${prescription.totalDays}일분 · ${prescription.drugCount}개 약품"

            //복용 완료 라벨 표시 (모든 약이 완료되었는지 확인)
            val allCompleted = isAllDrugsCompleted(prescription)
            binding.tvCompletedLabel.visibility = if (allCompleted) View.VISIBLE else View.GONE

            //  카드 전체 클릭 시 Dialog 띄우기
            binding.layoutHeader.setOnClickListener {
                showDrugListDialog(prescription)
            }
        }


        //  처방전의 모든 약이 완료되었는지 확인

        private fun isAllDrugsCompleted(prescription: CalendarPrescription): Boolean {
            // drugsByTimeSlot의 모든 약을 확인
            for (drugList in prescription.drugsByTimeSlot.values) {
                for (drug in drugList) {
                    if (!drug.isCompleted) {
                        return false
                    }
                }
            }
            return true
        }


        // 약 리스트 Dialog 표시
        private fun showDrugListDialog(prescription: CalendarPrescription) {
            val dialog = DrugListDialog.newInstance(
                prescription = prescription,
                onDrugCheckChanged = { drugId, timeSlot ->
                    onDrugCheckChanged(drugId, timeSlot)
                },
                onCheckAllClicked = { prescriptionId, newState ->
                    // Dialog에서 계산한 newState를 그대로 전달
                    onPrescriptionCheckChanged(prescriptionId, newState)
                },
                onDataRefreshNeeded = { prescriptionId, callback ->
                    // 최신 데이터 가져오기
                    onGetUpdatedPrescription(prescriptionId) { updatedPrescription ->
                        updatedPrescription?.let { callback(it) }
                    }
                }
            )

            dialog.show(activity.supportFragmentManager, "DrugListDialog")
        }
    }
}