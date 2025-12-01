package com.example.altong_v2.ui.prescription

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.altong_v2.R
import com.example.altong_v2.databinding.FragmentAddPrescriptionStep2Binding

/* * 처방전 추가 Step 2: 약품 정보 입력*/

class AddPrescriptionStep2Fragment : Fragment() {
    private var _binding: FragmentAddPrescriptionStep2Binding? = null
    private val binding get() = _binding!!
    private val viewModel: PrescriptionViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddPrescriptionStep2Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnPrevious.setOnClickListener {
            navigateToPreviousStep()
        }
        binding.btnNext.setOnClickListener {
            if (validateInputs()) {
                navigateToNextStep()
            }
        }
    }

    private fun validateInputs(): Boolean {
        var isValid = true

        // 진단명 검사 (필수)
        if (binding.etDiagnosis.text.isNullOrEmpty()) {
            binding.tilDiagnosis.error = "진단명을 입력해주세요"
            binding.etDiagnosis.requestFocus()
            isValid = false
        } else {
            binding.tilDiagnosis.error = null
        }

        return isValid
    }

    private fun navigateToPreviousStep() {
        parentFragmentManager.popBackStack()
    }
    private fun navigateToNextStep() {
        val fragment = AddPrescriptionStep3Fragment()
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