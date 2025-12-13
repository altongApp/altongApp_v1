package com.example.altong_v2.ui.prescription

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.altong_v2.R
import com.example.altong_v2.data.local.entity.PrescriptionEntity
import com.example.altong_v2.databinding.FragmentPrescriptionBinding
import kotlinx.coroutines.launch
import kotlin.collections.map
import com.example.altong_v2.data.local.entity.DrugEntity

import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/* * 나의 약통 Fragment
 * 처방전 리스트 표시 및 관리*/

class PrescriptionFragment : Fragment() {
    private var _binding: FragmentPrescriptionBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PrescriptionViewModel by activityViewModels()
    private lateinit var prescriptionAdapter: PrescriptionAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPrescriptionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupClickListeners()
        observeViewModel()
    }

    // 리사이클러뷰 설정
    private fun setupRecyclerView() {
        prescriptionAdapter = PrescriptionAdapter(
            onItemClick = { prescription ->
                // 처방전 상세 화면으로 이동
                navigateToPrescriptionDetail(prescription.id)
            },
            onAddDrugClick = { prescriptionId ->
                navigateToAddDrugFromList(prescriptionId)
            }
        )
        binding.recyclerViewPrescriptions.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = prescriptionAdapter
        }
    }

    private fun setupClickListeners() {
        // 처방전 추가 버튼
        binding.btnAddPrescription.setOnClickListener {
            navigateToAddPrescription()
        }
    }

    private fun observeViewModel() {
        // 처방전 리스트 관찰
        viewModel.allPrescriptions.observe(viewLifecycleOwner) { prescriptions ->
            updateUI(prescriptions)
        }
    }

    private fun updateUI(prescriptions: List<PrescriptionEntity>) {
        // 통계 업데이트
        val totalCount = prescriptions.size
        binding.tvStatTotal.text = "${totalCount} 건"
        binding.tvStatTaking.text = "${totalCount} 건"
        binding.tvListCount.text = "${totalCount}건"

        // 빈 상태 처리
        if (prescriptions.isEmpty()) {
            binding.emptyState.visibility = View.VISIBLE
            binding.recyclerViewPrescriptions.visibility = View.GONE
        } else {
            binding.emptyState.visibility = View.GONE
            binding.recyclerViewPrescriptions.visibility = View.VISIBLE

            // 위 블록 대신 이것만 추가 ↓
            lifecycleScope.launch {
                val prescriptionsWithDrugs = prescriptions.map { prescription ->
                    val drugs = viewModel.getDrugsList(prescription.id)
                    PrescriptionAdapter.PrescriptionWithDrugs(
                        prescription = prescription,
                        drugCount = drugs.size,
                        drugNames = drugs.take(3).map { it.name }
                    )
                }
                prescriptionAdapter.submitList(prescriptionsWithDrugs)
            }

/*            // 수정: 각 처방전의 약 정보를 LiveData로 관찰
            val prescriptionsWithDrugsMap = mutableMapOf<Long, PrescriptionAdapter.PrescriptionWithDrugs>()

            prescriptions.forEach { prescription ->
                // 초기값 설정
                prescriptionsWithDrugsMap[prescription.id] = PrescriptionAdapter.PrescriptionWithDrugs(
                    prescription = prescription,
                    drugCount = 0,
                    drugNames = emptyList()
                )

                // LiveData로 약 정보 관찰
                viewModel.getDrugsByPrescription(prescription.id)
                    .observe(viewLifecycleOwner) { drugs ->
                        // 약 정보 업데이트
                        prescriptionsWithDrugsMap[prescription.id] = PrescriptionAdapter.PrescriptionWithDrugs(
                            prescription = prescription,
                            drugCount = drugs.size,
                            drugNames = drugs.take(3).map { it.name }
                        )
                        // 전체 리스트 다시 제출
                        val updatedList = prescriptions.mapNotNull { p ->
                            prescriptionsWithDrugsMap[p.id]
                        }
                        prescriptionAdapter.submitList(updatedList)
                    }
            }
            //초기 리스트 표시 (약 정보 없는 상태)
            val initialList = prescriptions.map { prescription ->
                prescriptionsWithDrugsMap[prescription.id]!!
            }
            prescriptionAdapter.submitList(initialList)*/
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null  // 메모리 누수 방지
    }

    // 기존 처방전 보러가기(조회)
    private fun navigateToPrescriptionDetail(prescriptionId: Long) {
        val fragment = PrescriptionDetailFragment.newInstance(prescriptionId)

        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    // 새 처방전 등록하러하기(생성)
    private fun navigateToAddPrescription() {
        val fragment = AddPrescriptionStep1Fragment()
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)  // 뒤로가기 지원
            .commit()
    }

    // 처방전 목록에서 약추가
    private fun navigateToAddDrugFromList(prescriptionId: Long) {
        lifecycleScope.launch {
            viewModel.startAddDrugMode(prescriptionId)
            // 약 검색 화면으로 이동
            val fragment = DrugSearchFragment()

            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }
    }
}
