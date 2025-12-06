package com.example.altong_v2.ui.medicine

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.altong_v2.databinding.FragmentGeneralMedicineTabBinding
import com.example.altong_v2.data.model.MedicineCategory

/**
 * 일반의약품 탭 Fragment
 * 카테고리 그리드 + 약품 리스트
 */
class GeneralMedicineTabFragment : Fragment() {

    private var _binding: FragmentGeneralMedicineTabBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: MedicineViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGeneralMedicineTabBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 부모 Fragment의 ViewModel 공유
        viewModel = ViewModelProvider(requireParentFragment())[MedicineViewModel::class.java]

        setupCategoryGrid()
        setupMedicineList()
        observeViewModel()

        // 초기 데이터 로드
        viewModel.loadGeneralMedicines()
    }

    /**
     * 카테고리 그리드 설정
     */
    private fun setupCategoryGrid() {
        val categoryAdapter = CategoryAdapter { category: String ->
            // 카테고리 클릭 시 해당 카테고리 약품 목록 화면으로 이동
            // TODO: CategoryMedicineListFragment로 이동
        }

        binding.categoryRecyclerView.apply {
            layoutManager = GridLayoutManager(requireContext(), 3)
            adapter = categoryAdapter
        }

        // 카테고리 데이터 설정
        categoryAdapter.submitList(MedicineCategory.ALL_CATEGORIES)
    }

    /**
     * 약품 리스트 설정
     */
    private fun setupMedicineList() {
        val medicineAdapter = MedicineAdapter(
            onItemClick = { medicine ->
                // 약품 클릭 시 상세 화면으로 이동
                // TODO: MedicineDetailFragment로 이동
            },
            onFavoriteClick = { medicine ->
                // 찜 버튼 클릭
                viewModel.addFavorite(medicine)
            }
        )

        binding.medicineRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = medicineAdapter
        }
    }

    /**
     * ViewModel 관찰
     */
    private fun observeViewModel() {
        // 일반의약품 리스트 관찰
        viewModel.generalMedicines.observe(viewLifecycleOwner) { medicines ->
            val adapter = binding.medicineRecyclerView.adapter as? MedicineAdapter
            adapter?.submitList(medicines)
        }

        // 로딩 상태 관찰
        viewModel.isLoadingGeneral.observe(viewLifecycleOwner) { isLoading ->
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