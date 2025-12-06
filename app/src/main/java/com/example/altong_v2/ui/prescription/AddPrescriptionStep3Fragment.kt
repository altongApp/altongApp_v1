package com.example.altong_v2.ui.prescription

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.altong_v2.R
import com.example.altong_v2.databinding.FragmentAddPrescriptionStep3Binding
/*

 * 처방전 추가 Step 3: 추가 정보 (약국, 사진)
*/

class AddPrescriptionStep3Fragment : Fragment() {
    private var _binding: FragmentAddPrescriptionStep3Binding? = null
    private val binding get() = _binding!!
    private val viewModel: PrescriptionViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddPrescriptionStep3Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners()
        if (viewModel.isEditMode) {
            loadExistingData()
        }
    }
    private fun loadExistingData() {
        binding.etPharmacy.setText(viewModel.tempPharmacy)
    }

    private fun setupClickListeners() {
        // 사진 첨부 영역 클릭
        binding.cardImagePlaceholder.setOnClickListener {
            showToast("사진 첨부 기능은 추후 구현 예정입니다")
        }
        // 이전 버튼
        binding.btnPrevious.setOnClickListener {
            navigateToPreviousStep()
        }
        // 처방전 등록 완료 버튼
        binding.btnComplete.setOnClickListener {
            completePrescription()
        }
    }
    private fun navigateToPreviousStep() {
        parentFragmentManager.popBackStack()
    }

    // 처방전 등록 완료되면
    private fun completePrescription() {
        // 뷰모델에 step 3 데이터 저장
        viewModel.tempPharmacy = binding.etPharmacy.text.toString()

        if (viewModel.isEditMode) {
            viewModel.updatePrescriptionInfoOnly()
            showToast("처방전 정보가 수정되었습니다")
            // 상세 화면으로 복귀
            parentFragmentManager.popBackStack(
                null,
                androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE
            )
        } else {
            // Transition 화면으로 이동
            val fragment = PrescriptionCompleteFragment()
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }
    }

    private fun navigateToEditDrugs() {
        val fragment = DrugEditListFragment()

        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun showToast(message: String) {
        android.widget.Toast.makeText(requireContext(), message, android.widget.Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}