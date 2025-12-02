package com.example.altong_v2.ui.prescription

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.altong_v2.R
import com.example.altong_v2.databinding.FragmentPrescriptionCompleteBinding


/* * 처방전 등록 완료 화면 (Transition)*/

class PrescriptionCompleteFragment : Fragment() {
    private var _binding: FragmentPrescriptionCompleteBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PrescriptionViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPrescriptionCompleteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        // 지금 등록하기 버튼
        binding.btnRegisterNow.setOnClickListener {
            startDrugRegistration()
        }
        // 나중에 하기 버튼
        binding.btnSkip.setOnClickListener {
            skipDrugRegistration()
        }
    }

    // 약 등록하러 가기
    private fun startDrugRegistration() {
         val fragment = DrugSearchFragment()
         parentFragmentManager.beginTransaction()
             .replace(R.id.fragment_container, fragment)
             .addToBackStack(null)
             .commit()

        showToast("약 등록 기능은 Phase 5에서 구현됩니다")
    }

    // 약 등록 건뛰
    private fun skipDrugRegistration() {
    // 약 등록 건뛰하면, 처방전만 저장
        viewModel.savePrescriptionWithDrugs()

        showToast("처방전이 등록되었습니다")
        // 처방전 목록 화면으로 돌아가기
        // BackStack을 모두 제거하고 PrescriptionFragment로 이동
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
}