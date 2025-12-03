package com.example.altong_v2.ui.prescription

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.altong_v2.R
import com.example.altong_v2.databinding.FragmentPrescriptionDetailBinding
import kotlinx.coroutines.launch

/* * 처방전 상세 화면*/

class PrescriptionDetailFragment : Fragment() {
    private var _binding: FragmentPrescriptionDetailBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PrescriptionViewModel by activityViewModels()

    private var prescriptionId: Long = -1

    // 약품 리스트 어댑터
    private lateinit var drugAdapter: DrugDetailAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            prescriptionId = it.getLong(ARG_PRESCRIPTION_ID, -1)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPrescriptionDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (prescriptionId == -1L) {
            showToast("처방전 정보를 불러올 수 없습니다")
            parentFragmentManager.popBackStack()
            return
        }
        setupRecyclerView()
        setupClickListeners()
        loadPrescriptionData()
    }

// 리사이클러 뷰 설정
    private fun setupRecyclerView() {
        drugAdapter = DrugDetailAdapter()

        binding.rvDrugs.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = drugAdapter
        }
    }

    private fun setupClickListeners() {
        // 뒤로가기 버튼
        binding.btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
        // 메뉴 버튼 (수정/삭제)
        binding.btnMenu.setOnClickListener {
            showPopupMenu(it)
        }
        // 약품 추가 버튼
        binding.btnAddDrug.setOnClickListener {
            navigateToAddDrug()
        }
    }

    private fun showPopupMenu(view: View) {
        val popup = PopupMenu(requireContext(), view)
        popup.menuInflater.inflate(R.menu.menu_prescription_detail, popup.menu)

        popup.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_edit -> {
                    navigateToEdit()
                    true
                }
                R.id.action_delete -> {
                    showDeleteDialog()
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    private fun navigateToAddDrug() {
        lifecycleScope.launch {
            // ViewModel에 추가 모드 시작
            viewModel.startAddDrugMode(prescriptionId) 
            // 약품 검색 화면으로 이동
            val fragment = DrugSearchFragment()
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }
    }

// 삭제 확인 다이얼
    private fun showDeleteDialog() {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("처방전 삭제")
            .setMessage("이 처방전을 삭제하시겠습니까?\n등록된 약품 정보도 함께 삭제됩니다.")
            .setPositiveButton("삭제") { _, _ ->
                deletePrescription()
            }
            .setNegativeButton("취소", null)
            .show()
    }

// 처방전 삭제
    private fun deletePrescription() {
        lifecycleScope.launch {
            val prescription = viewModel.getPrescriptionById(prescriptionId)
            prescription?.let {
                viewModel.deletePrescription(it)
                showToast("처방전이 삭제되었습니다")
                parentFragmentManager.popBackStack()
            }
        }
    }
// 처방전 수정하러가기
    private fun navigateToEdit() {
        lifecycleScope.launch {
            // ViewModel에 수정 모드 시작
            viewModel.startEditMode(prescriptionId)

            // Step 1 화면으로 이동 (수정 모드)
            val fragment = AddPrescriptionStep1Fragment()

            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }
    }

// 처방전 데이터 로드하기
    private fun loadPrescriptionData() {
        lifecycleScope.launch {
            // 1. 처방전 정보 로드
            val prescription = viewModel.getPrescriptionById(prescriptionId)
            if (prescription == null) {
                showToast("처방전을 찾을 수 없습니다")
                parentFragmentManager.popBackStack()
                return@launch
            }
            // 2. UI 업데이트
            binding.tvDate.text = prescription.date
            binding.tvHospital.text = prescription.hospital
            binding.tvDepartment.text = prescription.department
            binding.tvDiagnosis.text = prescription.diagnosis
            // 약국명 (있을 경우만 표시)
            if (!prescription.pharmacy.isNullOrEmpty()) {
                binding.layoutPharmacy.visibility = View.VISIBLE
                binding.tvPharmacy.text = prescription.pharmacy
            } else {
                binding.layoutPharmacy.visibility = View.GONE
            }

            // ~~~~~~ 처방전 사진 처리 (추가해야함)
            // TODO: 나중에 실제 이미지 경로가 있을 때 처리
            // if (!prescription.prescriptionImagePath.isNullOrEmpty()) {
            //     binding.layoutNoImage.visibility = View.GONE
            //     binding.ivPrescriptionImage.visibility = View.VISIBLE
            //     // Glide 또는 Coil로 이미지 로드
            // } else {
            //     binding.layoutNoImage.visibility = View.VISIBLE
            //     binding.ivPrescriptionImage.visibility = View.GONE
            // }

            // 현재는 항상 "사진 없음" 표시
            binding.layoutNoImage.visibility = View.VISIBLE
            binding.ivPrescriptionImage.visibility = View.GONE
            // 3. 약품 정보 로드
            loadDrugs()
        }
    }

// 약품 정보 로드
    private fun loadDrugs() {
        viewModel.getDrugsByPrescription(prescriptionId).observe(viewLifecycleOwner) { drugs ->
            if (drugs.isEmpty()) {
                // 약품 없음
                binding.rvDrugs.visibility = View.GONE
                binding.layoutEmptyDrugs.visibility = View.VISIBLE
                binding.tvDrugCount.text = "0개"
            } else {
                // 약품 있음
                binding.rvDrugs.visibility = View.VISIBLE
                binding.layoutEmptyDrugs.visibility = View.GONE
                binding.tvDrugCount.text = "${drugs.size}개"

                drugAdapter.submitList(drugs)
            }
        }
    }

    private fun showToast(message: String) {
        android.widget.Toast.makeText(requireContext(), message, android.widget.Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_PRESCRIPTION_ID = "prescription_id"

        fun newInstance(prescriptionId: Long) = PrescriptionDetailFragment().apply {
            arguments = Bundle().apply {
                putLong(ARG_PRESCRIPTION_ID, prescriptionId)
            }
        }
    }
}