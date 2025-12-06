package com.example.altong_v2.ui.prescription

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
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
                // 약수정
                editDrug(position)
            },
            onDeleteClick = { position ->
                // 약삭제
                deleteDrug(position)
            }
        )

        binding.rvDrugs.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = drugEditAdapter
        }
    }

    //약수정 펑션
    private fun editDrug(position: Int) {
        // ViewModel에 수정 모드 시작
        viewModel.startEditDrugMode(position)
        // 선택한 약 데이터 가져오기
        val drug = viewModel.tempDrugs[position]
        // DrugDetailFragment로 이동 (수정 모드)
        val fragment = DrugDetailFragment.newInstance(
            drugName = drug.name,
            drugDescription = ""  // 설명은 필요 없음
        )

        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }
    // 약삭제 펑션
    private fun deleteDrug(position: Int) {
        val drugName = viewModel.tempDrugs[position].name

        AlertDialog.Builder(requireContext())
            .setTitle("약품 삭제")
            .setMessage("'${drugName}'을(를) 삭제하시겠습니까?")
            .setPositiveButton("삭제") { _, _ ->
                // 리스트에서 삭제
                viewModel.tempDrugs.removeAt(position)

                // UI 업데이트
                loadDrugs()

                showToast("약품이 삭제되었습니다")
            }
            .setNegativeButton("취소", null)
            .show()
    }


    private fun setupClickListeners() {
        // 뒤로가기
        binding.btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
        // 약 추가
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
    // 처방전 수정 저장이였던.. 약 수정 저장
    private fun savePrescription() {
        // 약이 0개일때
        if (viewModel.tempDrugs.isEmpty()) {
            AlertDialog.Builder(requireContext())
                .setTitle("약품 없음")
                .setMessage("등록된 약품이 없습니다.\n약품을 추가하시겠습니까?")
                .setPositiveButton("추가") { _, _ ->
                    navigateToAddDrug()
                }
                .setNegativeButton("취소", null)
                .show()
            return
        }

        viewModel.updateDrugsOnly()
        showToast("약품이 수정되었습니다")
        parentFragmentManager.popBackStack()
    }

    private fun showToast(message: String) {
        android.widget.Toast.makeText(requireContext(), message, android.widget.Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}