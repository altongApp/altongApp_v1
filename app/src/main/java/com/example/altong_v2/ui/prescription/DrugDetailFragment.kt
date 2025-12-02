package com.example.altong_v2.ui.prescription

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.altong_v2.R
import com.example.altong_v2.databinding.FragmentDrugDetailBinding


 /*약 상세 정보 입력 화면
*/
class DrugDetailFragment : Fragment() {
    private var _binding: FragmentDrugDetailBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PrescriptionViewModel by activityViewModels()

    private var drugName: String = ""
    private var drugDescription: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            drugName = it.getString(ARG_DRUG_NAME, "")
            drugDescription = it.getString(ARG_DRUG_DESCRIPTION, "")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDrugDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupDrugInfo()
        setupTimeSlots()
        setupClickListeners()
    }

    private fun setupDrugInfo() {
        binding.tvDrugName.text = drugName
    }

    private fun setupTimeSlots() {
        // 아침
        binding.cardMorning.setOnClickListener {
            binding.cbMorning.isChecked = !binding.cbMorning.isChecked
            updateCardStyle(binding.cardMorning, binding.cbMorning.isChecked)
        }
        binding.cbMorning.setOnCheckedChangeListener { _, isChecked ->
            updateCardStyle(binding.cardMorning, isChecked)
        }

        // 점심
        binding.cardLunch.setOnClickListener {
            binding.cbLunch.isChecked = !binding.cbLunch.isChecked
            updateCardStyle(binding.cardLunch, binding.cbLunch.isChecked)
        }
        binding.cbLunch.setOnCheckedChangeListener { _, isChecked ->
            updateCardStyle(binding.cardLunch, isChecked)
        }

        // 저녁
        binding.cardDinner.setOnClickListener {
            binding.cbDinner.isChecked = !binding.cbDinner.isChecked
            updateCardStyle(binding.cardDinner, binding.cbDinner.isChecked)
        }
        binding.cbDinner.setOnCheckedChangeListener { _, isChecked ->
            updateCardStyle(binding.cardDinner, isChecked)
        }

        // 취침 전
        binding.cardBedtime.setOnClickListener {
            binding.cbBedtime.isChecked = !binding.cbBedtime.isChecked
            updateCardStyle(binding.cardBedtime, binding.cbBedtime.isChecked)
        }
        binding.cbBedtime.setOnCheckedChangeListener { _, isChecked ->
            updateCardStyle(binding.cardBedtime, isChecked)
        }
    }

    private fun updateCardStyle(card: com.google.android.material.card.MaterialCardView, isChecked: Boolean) {
        if (isChecked) {
            card.strokeColor = ContextCompat.getColor(requireContext(), R.color.primary_green_dark)
            card.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.primary_green_light))
        } else {
            card.strokeColor = ContextCompat.getColor(requireContext(), R.color.border_default)
            card.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
        }
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
        binding.btnAddAnother.setOnClickListener {
            if (validateInputs()) {
                saveDrugAndAddAnother()
            }
        }
        binding.btnComplete.setOnClickListener {
            if (validateInputs()) {
                saveDrugAndFinish()
            }
        }
    }

// 입력 검사
    private fun validateInputs(): Boolean {
        var isValid = true

        // 1회 복용량 검사
        if (binding.etDosage.text.isNullOrEmpty()) {
            binding.tilDosage.error = "1회 복용량을 입력해주세요"
            if (isValid) binding.etDosage.requestFocus()
            isValid = false
        } else {
            binding.tilDosage.error = null
        }

        // 1일 복용 횟수 검사
        if (binding.etFrequency.text.isNullOrEmpty()) {
            binding.tilFrequency.error = "1일 복용 횟수를 입력해주세요"
            if (isValid) binding.etFrequency.requestFocus()
            isValid = false
        } else {
            binding.tilFrequency.error = null
        }

        // 총 처방 일수 검사
        if (binding.etDays.text.isNullOrEmpty()) {
            binding.tilDays.error = "총 처방 일수를 입력해주세요"
            if (isValid) binding.etDays.requestFocus()
            isValid = false
        } else {
            binding.tilDays.error = null
        }

        // 알림 시간대 검사
        if (!binding.cbMorning.isChecked &&
            !binding.cbLunch.isChecked &&
            !binding.cbDinner.isChecked &&
            !binding.cbBedtime.isChecked) {
            showToast("알림 시간대를 최소 1개 이상 선택해주세요")
            isValid = false
        }
        return isValid
    }


     //약 저장하고 다른 약 추가
    private fun saveDrugAndAddAnother() {
         // 1. 입력값 수집
         val dosage = binding.etDosage.text.toString()
         val frequency = binding.etFrequency.text.toString()
         val days = binding.etDays.text.toString().toIntOrNull() ?: 0
         val timing = binding.etTiming.text.toString()
         val memo = binding.etMemo.text.toString()

         // 2. 선택된 시간대 수집
         val timeSlots = mutableListOf<String>()
         if (binding.cbMorning.isChecked) timeSlots.add("아침")
         if (binding.cbLunch.isChecked) timeSlots.add("점심")
         if (binding.cbDinner.isChecked) timeSlots.add("저녁")
         if (binding.cbBedtime.isChecked) timeSlots.add("취침 전")

         // 3. ViewModel에 약 추가
         viewModel.tempDrugs.add(
             TempDrugData(
                 name = drugName,
                 dosage = dosage,
                 frequency = frequency,
                 days = days,
                 timing = timing,
                 memo = memo,
                 timeSlots = timeSlots
             )
         )

        showToast("약이 추가되었습니다")
        // 입력 필드 초기화하고 검색 화면으로 돌아가기
        parentFragmentManager.popBackStack()
    }


     // 약 저장하고 등록 완료
    private fun saveDrugAndFinish() {
         // 1. 입력값 수집
         val dosage = binding.etDosage.text.toString()
         val frequency = binding.etFrequency.text.toString()
         val days = binding.etDays.text.toString().toIntOrNull() ?: 0
         val timing = binding.etTiming.text.toString()
         val memo = binding.etMemo.text.toString()

         // 2. 선택된 시간대 수집
         val timeSlots = mutableListOf<String>()
         if (binding.cbMorning.isChecked) timeSlots.add("아침")
         if (binding.cbLunch.isChecked) timeSlots.add("점심")
         if (binding.cbDinner.isChecked) timeSlots.add("저녁")
         if (binding.cbBedtime.isChecked) timeSlots.add("취침 전")

         // 3. ViewModel에 약 추가
         viewModel.tempDrugs.add(
             TempDrugData(
                 name = drugName,
                 dosage = dosage,
                 frequency = frequency,
                 days = days,
                 timing = timing,
                 memo = memo,
                 timeSlots = timeSlots
             )
         )
         // db에 처방전 + 약 전체 저장
         viewModel.savePrescriptionWithDrugs()

        showToast("약 등록이 완료되었습니다")
        // 처방전 목록 화면으로 돌아가기
        parentFragmentManager.popBackStack(
            null,
            androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE
        )
    }


    private fun showToast(message: String) {
        android.widget.Toast.makeText(requireContext(), message, android.widget.Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    companion object {
        private const val ARG_DRUG_NAME = "drug_name"
        private const val ARG_DRUG_DESCRIPTION = "drug_description"

        fun newInstance(drugName: String, drugDescription: String) = DrugDetailFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_DRUG_NAME, drugName)
                putString(ARG_DRUG_DESCRIPTION, drugDescription)
            }
        }
    }
}