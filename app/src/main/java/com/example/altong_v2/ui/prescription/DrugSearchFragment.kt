package com.example.altong_v2.ui.prescription

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.altong_v2.R
import com.example.altong_v2.databinding.FragmentDrugSearchBinding


/* * 약 검색 화면*/
class DrugSearchFragment : Fragment() {
    private var _binding: FragmentDrugSearchBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PrescriptionViewModel by activityViewModels()

    // 검색 결과 어댑터
    private lateinit var searchAdapter: DrugSearchAdapter
    // 선택된 약
    private var selectedDrug: DrugSearchResult? = null
    // 샘플 약 데이터
    private val sampleDrugs = listOf(
        DrugSearchResult("타이레놀정 500mg", "해열진통제 | 아세트아미노펜"),
        DrugSearchResult("타이레놀ER서방정 650mg", "해열진통제 | 서방형"),
        DrugSearchResult("코푸시럽", "기침 감기약"),
        DrugSearchResult("무코펙트정", "가래 제거제"),
        DrugSearchResult("아스피린정 100mg", "해열진통제 | 아세틸살리실산"),
        DrugSearchResult("게보린정", "해열진통제 | 복합제")
    )

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

    // 약 검색
    private fun searchDrugs(query: String) {
        if (query.isEmpty()) {
            // 검색어가 없으면 초기 상태로
            binding.layoutEmptyState.visibility = View.VISIBLE
            binding.rvSearchResults.visibility = View.GONE
            selectedDrug = null
            binding.btnSelectDrug.isEnabled = false
            return
        }

        // 검색어로 필터링
        val results = sampleDrugs.filter {
            it.name.contains(query, ignoreCase = true)
        }
        if (results.isNotEmpty()) {
            binding.layoutEmptyState.visibility = View.GONE
            binding.rvSearchResults.visibility = View.VISIBLE
            searchAdapter.submitList(results)
        } else {
            binding.layoutEmptyState.visibility = View.VISIBLE
            binding.rvSearchResults.visibility = View.GONE
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
         val fragment = DrugDetailFragment.newInstance(drug.name, drug.description)
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
    val description: String
)