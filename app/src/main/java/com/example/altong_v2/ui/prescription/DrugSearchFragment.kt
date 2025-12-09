package com.example.altong_v2.ui.prescription

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.altong_v2.R
import com.example.altong_v2.data.model.PrescriptionMedicine
import com.example.altong_v2.data.repository.MedicineRepository
import com.example.altong_v2.databinding.FragmentDrugSearchBinding
import kotlinx.coroutines.launch


/* * 약 검색 화면*/
class DrugSearchFragment : Fragment() {
    private var _binding: FragmentDrugSearchBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PrescriptionViewModel by activityViewModels()
    // 파이어베이스 레포랑 연결하기
    private val medicineRepository = MedicineRepository()
    // 검색 결과 어댑터
    private lateinit var searchAdapter: DrugSearchAdapter
    // 선택된 약
    private var selectedDrug: DrugSearchResult? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDrugSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupSearchInput()
        setupClickListeners()
    }

    private var selectedPosition: Int = -1
    private fun setupRecyclerView() {
        searchAdapter = DrugSearchAdapter(
        { drug , position ->
                onDrugSelected(drug,position)
            }
        )
        binding.rvSearchResults.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = searchAdapter
        }
    }

    private fun setupSearchInput() {
        binding.etDrugSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchDrugs(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    // 약 검색 - 파베연결
    private fun searchDrugs(query: String) {
        if (query.isEmpty()) {
            // 검색어가 없으면 초기 상태로
            binding.layoutEmptyState.visibility = View.VISIBLE
            binding.rvSearchResults.visibility = View.GONE
            selectedDrug = null
            binding.btnSelectDrug.isEnabled = false
            return
        }
        // 로딩 표시
        binding.progressBar.visibility = View.VISIBLE
        binding.layoutEmptyState.visibility = View.GONE
        binding.rvSearchResults.visibility = View.GONE

        // Firebase에서 전문의약품 검색
        lifecycleScope.launch {
            try {
                val (medicines, _) = medicineRepository.searchPrescriptionMedicines(query)
                // PrescriptionMedicine → DrugSearchResult 변환
                val results = medicines.map { medicine ->
                    DrugSearchResult(
                        name = medicine.medicine_name,
                        description = buildDescription(medicine),
                        imageUrl = medicine.image_url
                    )
                }

                // UI 업데이트
                binding.progressBar.visibility = View.GONE

                if (results.isNotEmpty()) {
                    binding.layoutEmptyState.visibility = View.GONE
                    binding.rvSearchResults.visibility = View.VISIBLE
                    searchAdapter.submitList(results)
                } else {
                    binding.layoutEmptyState.visibility = View.VISIBLE
                    binding.rvSearchResults.visibility = View.GONE
                 }
            } catch (e: Exception) {
                binding.progressBar.visibility = View.GONE
                binding.layoutEmptyState.visibility = View.VISIBLE
                binding.rvSearchResults.visibility = View.GONE
                showToast("검색 중 오류가 발생했습니다")
            }
        }
    }

    // 약품 설명 생성
    private fun buildDescription(medicine: PrescriptionMedicine): String {
        val parts = mutableListOf<String>()

        // 제조사
        if (medicine.manufacturer.isNotBlank()) {
            parts.add(medicine.manufacturer)
        }

        // 분류
        if (!medicine.classification.isNullOrBlank()) {
            parts.add(medicine.classification)
        }

        // 성분 (너무 길면 축약)
        if (!medicine.ingredients.isNullOrBlank()) {
            val ingredients = medicine.ingredients
            if (ingredients.length > 50) {
                parts.add(ingredients.substring(0, 50) + "...")
            } else {
                parts.add(ingredients)
            }
        }

        return if (parts.isNotEmpty()) {
            parts.joinToString(" | ")
        } else {
            "전문의약품"
        }
    }

    // 약 선택 처리
    private fun onDrugSelected(drug: DrugSearchResult, position: Int) {
        selectedDrug = drug
        selectedPosition = position
        binding.btnSelectDrug.isEnabled = true
        searchAdapter.setSelectedPosition(position)
    }
    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
        binding.btnSelectDrug.setOnClickListener {
            selectedDrug?.let { drug ->
                navigateToDrugDetail(drug)
            }
        }
        binding.btnSkip.setOnClickListener {
            skipDrugRegistration()
        }
    }

    // 약 상세화면으로
    private fun navigateToDrugDetail(drug: DrugSearchResult) {
         val fragment = DrugDetailFragment.newInstance(drug.name, drug.description, imageUrl = drug.imageUrl)
         parentFragmentManager.beginTransaction()
             .replace(R.id.fragment_container, fragment)
             .addToBackStack(null)
             .commit()
    }

    // 약 등록 건뛰
    private fun skipDrugRegistration() {
        android.util.Log.d("DrugAdd", "=== skipDrugRegistration 호출 ===")
        android.util.Log.d("DrugAdd", "isAddDrugMode: ${viewModel.isAddDrugMode}")

        parentFragmentManager.popBackStack(
            null,
            androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE
        )
        showToast("처방전이 등록되었습니다")
    }

    private fun showToast(message: String) {
        android.widget.Toast.makeText(requireContext(), message, android.widget.Toast.LENGTH_SHORT).show()
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

// 약 검색 결과 데이터 클래스
data class DrugSearchResult(
    val name: String,
    val description: String,
    val imageUrl: String? = null
)