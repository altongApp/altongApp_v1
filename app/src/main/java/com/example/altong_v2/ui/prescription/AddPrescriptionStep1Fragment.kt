package com.example.altong_v2.ui.prescription

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.altong_v2.R
import com.example.altong_v2.databinding.FragmentAddPrescriptionStep1Binding
import java.text.SimpleDateFormat
import java.util.*


/*
 * 처방전 추가 - Step 1: 기본 정보 입력*/

class AddPrescriptionStep1Fragment : Fragment() {
    private var _binding: FragmentAddPrescriptionStep1Binding? = null
    private val binding get() = _binding!!
    private val viewModel: PrescriptionViewModel by activityViewModels()
/*
    // 선택된 날짜
    private var selectedDate: String = ""*/
    // 날짜 포맷
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddPrescriptionStep1Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupDatePicker()
        setupClickListeners()
        setDefaultDate()
    }

    private fun setupDatePicker() {
        binding.etPrescriptionDate.setOnClickListener {
            showDatePicker()
        }
        binding.tilPrescriptionDate.setEndIconOnClickListener {
            showDatePicker()
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        // 현재 날짜 가져오기
        val currentDate = binding.etPrescriptionDate.text.toString()
        if (currentDate.isNotEmpty()) {
            try {
                calendar.time = dateFormat.parse(currentDate) ?: Date()
            } catch (e: Exception) {
                // 파싱 실패 시 현재 날짜 사용
            }
        }

        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                // 선택된 날짜 저장
                calendar.set(selectedYear, selectedMonth, selectedDay)
                val formattedDate = dateFormat.format(calendar.time)
                binding.etPrescriptionDate.setText(formattedDate)
            },
            year,
            month,
            day
        ).show()
    }

    // 오늘날짜를 기본값으로 설정하기
    private fun setDefaultDate() {
        val today = dateFormat.format(Date())
        binding.etPrescriptionDate.setText(today)
    }

    private fun setupClickListeners() {
        binding.btnNext.setOnClickListener {
            if (validateInputs()) {
                navigateToNextStep()
            }
        }
    }

    // 입력값 유효성 검사
    private fun validateInputs(): Boolean {
        var isValid = true
        // 날짜 검사
        if (binding.etPrescriptionDate.text.isNullOrEmpty()) {
            binding.tilPrescriptionDate.error = "처방 날짜를 입력해주세요"
            isValid = false
        } else {
            binding.tilPrescriptionDate.error = null
        }

        // 병원명 검사
        if (binding.etHospitalName.text.isNullOrEmpty()) {
            binding.tilHospitalName.error = "병원명을 입력해주세요"
            binding.etHospitalName.requestFocus()
            isValid = false
        } else {
            binding.tilHospitalName.error = null
        }

        // 진료과 검사
        if (binding.etDepartment.text.isNullOrEmpty()) {
            binding.tilDepartment.error = "진료과를 입력해주세요"
            if (isValid) binding.etDepartment.requestFocus()
            isValid = false
        } else {
            binding.tilDepartment.error = null
        }

        return isValid
    }
    private fun navigateToNextStep() {
        // 뷰모델에 step 1 데이터 저장
        viewModel.tempDate = binding.etPrescriptionDate.text.toString()
        viewModel.tempHospital = binding.etHospitalName.text.toString()
        viewModel.tempDepartment = binding.etDepartment.text.toString()

        val fragment = AddPrescriptionStep2Fragment()
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}