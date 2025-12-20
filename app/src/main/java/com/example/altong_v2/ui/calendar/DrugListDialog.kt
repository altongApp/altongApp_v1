package com.example.altong_v2.ui.calendar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.altong_v2.data.model.CalendarPrescription
import com.example.altong_v2.databinding.DialogDrugListBinding

// 약 리스트 다이얼로그 - 진단명 카드 클릭시 나오는 거
class DrugListDialog : DialogFragment() {

    private var _binding: DialogDrugListBinding? = null
    private val binding get() = _binding!!

    private lateinit var prescription: CalendarPrescription
    private lateinit var drugAdapter: DrugCalendarAdapter

    // 콜백
    private var onDrugCheckChanged: ((Long, String) -> Unit)? = null
    private var onCheckAllClicked: ((Long, Boolean) -> Unit)? = null  // (prescriptionId, newState)
    private var onDataRefreshNeeded: ((Long, (CalendarPrescription) -> Unit) -> Unit)? = null

    companion object {
        private const val ARG_PRESCRIPTION = "prescription"

        //Dialog 인스턴스 생성
        fun newInstance(
            prescription: CalendarPrescription,
            onDrugCheckChanged: (Long, String) -> Unit,
            onCheckAllClicked: (Long, Boolean) -> Unit,
            onDataRefreshNeeded: (Long, (CalendarPrescription) -> Unit) -> Unit
        ): DrugListDialog {
            return DrugListDialog().apply {
                this.onDrugCheckChanged = onDrugCheckChanged
                this.onCheckAllClicked = onCheckAllClicked
                this.onDataRefreshNeeded = onDataRefreshNeeded
                arguments = Bundle().apply {
                    tempPrescription = prescription
                }
            }
        }

        private var tempPrescription: CalendarPrescription? = null
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogDrugListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 임시 저장된 prescription 가져오기
        prescription = tempPrescription ?: run {
            dismiss()
            return
        }

        setupUI()
        setupRecyclerView()
        setupClickListeners()
    }

    override fun onStart() {
        super.onStart()
        // Dialog 크기 설정
        dialog?.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.9).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    // UI 설정
    private fun setupUI() {
        // 진단명
        binding.tvDiagnosis.text = prescription.diagnosis

        // 처방 정보
        binding.tvPrescriptionInfo.text =
            "${prescription.totalDays}일분 · ${prescription.drugCount}개 약품"

        // 처방일
        binding.tvPrescriptionDate.text =
            "처방일: ${prescription.prescriptionDate}"
    }


     // RecyclerView 설정
    private fun setupRecyclerView() {
        drugAdapter = DrugCalendarAdapter { drugId, timeSlot ->
            // 약 체크박스 클릭 (timeSlot 전달!)
            onDrugCheckChanged?.invoke(drugId, timeSlot)
        }

        binding.rvDrugs.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = drugAdapter
        }

        // 데이터 전달
        drugAdapter.submitList(prescription.drugsByTimeSlot)
    }


    // 클릭 리스너 설정
    private fun setupClickListeners() {
        // 모두 체크/해제 버튼
        updateCheckAllButton()  // 초기 버튼 텍스트 설정

        binding.btnCheckAll.setOnClickListener {
            // 현재 시점의 상태 확인
            val allCompleted = isAllDrugsCompleted()
            android.util.Log.d("DrugListDialog", "버튼 클릭: allCompleted=$allCompleted, 진단명=${prescription.diagnosis}")

            // 현재 상태의 반대로 토글 (allCompleted면 해제, 아니면 체크)
            val newState = !allCompleted
            onCheckAllClicked?.invoke(prescription.prescriptionId, newState)  // 명확한 상태 전달

            //500ms 후 최신 데이터 가져와서 Dialog 갱신
            binding.root.postDelayed({
                onDataRefreshNeeded?.invoke(prescription.prescriptionId) { updatedPrescription ->
                    prescription = updatedPrescription
                    setupRecyclerView()
                    updateCheckAllButton()  // 버튼 텍스트 업데이트

                    android.util.Log.d("DrugListDialog", "갱신 완료: 새로운 상태=${isAllDrugsCompleted()}")
                }
            }, 500)
        }

        // 닫기 버튼
        binding.btnClose.setOnClickListener {
            dismiss()
        }
    }

    // "모두 체크" / "모두 해제" 버튼 텍스트 업데이트
    private fun updateCheckAllButton() {
        val allCompleted = isAllDrugsCompleted()
        binding.btnCheckAll.text = if (allCompleted) "모두 해제" else "모두 체크"
    }


    // 모든 약이 완료되었는지 확인
    private fun isAllDrugsCompleted(): Boolean {
        for (drugList in prescription.drugsByTimeSlot.values) {
            for (drug in drugList) {
                if (!drug.isCompleted) {
                    return false
                }
            }
        }
        return true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}