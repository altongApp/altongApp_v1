package com.example.altong_v2.ui.prescription

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.altong_v2.R
import com.example.altong_v2.databinding.FragmentDrugEditListBinding

/* * 약 수정 리스트 화면*/

class DrugEditListFragment : Fragment() {
    private var _binding: FragmentDrugEditListBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PrescriptionViewModel by activityViewModels()
    private lateinit var drugEditAdapter: DrugEditAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDrugEditListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupClickListeners()
        loadDrugs()
    }

    private fun setupRecyclerView() {
        drugEditAdapter = DrugEditAdapter(
            onEditClick = { position ->
                // TODO: 약품 수정
                showToast("약품 수정 기능은 다음 단계에서")
            },
            onDeleteClick = { position ->
                // TODO: 약품 삭제
                showToast("약품 삭제 기능은 다음 단계에서")
            }
        )

        binding.rvDrugs.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = drugEditAdapter
        }
    }

    private fun setupClickListeners() {
        // 뒤로가기
        binding.btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
        // 약품 추가
        binding.btnAddDrug.setOnClickListener {
            navigateToAddDrug()
        }
        // 수정 완료
        binding.btnComplete.setOnClickListener {
            savePrescription()
        }
    }
    // 약 목록 로드
    private fun loadDrugs() {
        val drugs = viewModel.tempDrugs

        if (drugs.isEmpty()) {
            binding.layoutEmpty.visibility = View.VISIBLE
            binding.rvDrugs.visibility = View.GONE
        } else {
            binding.layoutEmpty.visibility = View.GONE
            binding.rvDrugs.visibility = View.VISIBLE
            drugEditAdapter.submitList(drugs)
        }

        binding.tvDrugCount.text = "${drugs.size}개"
    }
    // 약 추가화면으로 이동
    private fun navigateToAddDrug() {
        val fragment = DrugSearchFragment()

        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }
    // 처방전 수정 저장
    private fun savePrescription() {
        viewModel.updatePrescriptionWithDrugs()
        showToast("처방전이 수정되었습니다")
        // 상세 화면으로 돌아가기
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