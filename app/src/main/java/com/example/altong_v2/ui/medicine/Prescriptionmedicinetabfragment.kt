package com.example.altong_v2.ui.medicine

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.altong_v2.databinding.FragmentPrescriptionMedicineTabBinding

/**
 * 전문의약품 탭 Fragment
 * 전체 전문의약품 리스트 (8,208개)
 */
class PrescriptionMedicineTabFragment : Fragment() {

    private var _binding: FragmentPrescriptionMedicineTabBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: MedicineViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPrescriptionMedicineTabBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 부모 Fragment의 ViewModel 공유
        viewModel = ViewModelProvider(requireParentFragment())[MedicineViewModel::class.java]

        setupMedicineList()
        setupFavoriteButton()
        observeViewModel()

        // 초기 데이터 로드
        viewModel.loadPrescriptionMedicines()
    }

    /**
     * 약품 리스트 설정
     */
    private fun setupMedicineList() {
        val adapter = PrescriptionMedicineAdapter(
            onItemClick = { medicine ->
                // 약품 클릭 시 상세 화면으로 이동
                // TODO: MedicineDetailFragment로 이동
            },
            onFavoriteClick = { medicine ->
                // 찜 버튼 클릭
                viewModel.addPrescriptionFavorite(medicine)
            }
        )

        binding.medicineRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            this.adapter = adapter
        }
    }

    /**
     * 병원약 찜 보기 버튼 설정
     */
    private fun setupFavoriteButton() {
        binding.favoriteButton.setOnClickListener {
            // 찜 목록 화면으로 이동
            // TODO: FavoriteMedicineFragment로 이동 (type = "prescription")
        }
    }

    /**
     * ViewModel 관찰
     */
    private fun observeViewModel() {
        // 전문의약품 리스트 관찰
        viewModel.prescriptionMedicines.observe(viewLifecycleOwner) { medicines ->
            val adapter = binding.medicineRecyclerView.adapter as? PrescriptionMedicineAdapter
            adapter?.submitList(medicines)
        }

        // 로딩 상태 관찰
        viewModel.isLoadingPrescription.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        // 에러 메시지 관찰
        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                // TODO: Snackbar 또는 Toast로 에러 표시
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}