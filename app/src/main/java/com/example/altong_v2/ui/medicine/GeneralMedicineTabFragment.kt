package com.example.altong_v2.ui.medicine

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.altong_v2.data.model.MedicineCategory
import com.example.altong_v2.databinding.FragmentGeneralMedicineTabBinding

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

        // ViewModel 초기화 (Activity 레벨)
        viewModel = ViewModelProvider(requireActivity())[MedicineViewModel::class.java]

        android.util.Log.d("GeneralMedicineTab", "Fragment onViewCreated")

        setupCategoryGrid()
        setupMedicineList()
        observeViewModel()

        // 초기 데이터 로드
        android.util.Log.d("GeneralMedicineTab", "Loading general medicines...")
        viewModel.loadGeneralMedicines()
    }

    /**
     * 카테고리 그리드 설정
     */
    private fun setupCategoryGrid() {
        android.util.Log.d("GeneralMedicineTab", "🎨 setupCategoryGrid 호출됨!")
        android.util.Log.d("GeneralMedicineTab", "📦 카테고리 개수: ${MedicineCategory.ALL_CATEGORIES.size}")

        val categoryAdapter = CategoryAdapter { category: String ->
            // 카테고리 클릭 시 해당 카테고리 약품 목록 화면으로 이동
            navigateToCategoryList(category)
        }

        binding.categoryRecyclerView.apply {
            layoutManager = GridLayoutManager(requireContext(), 3)
            adapter = categoryAdapter
        }

        // 카테고리 데이터 설정
        categoryAdapter.submitList(MedicineCategory.ALL_CATEGORIES)
    }

    /**
     * 카테고리별 약품 리스트 화면으로 이동
     */
    private fun navigateToCategoryList(category: String) {
        val fragment = CategoryMedicineListFragment.newInstance(category)

        parentFragmentManager.beginTransaction()
            .replace(android.R.id.content, fragment)
            .addToBackStack(null)
            .commit()
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

            // 페이지네이션: 스크롤 리스너 추가
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)

                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val visibleItemCount = layoutManager.childCount
                    val totalItemCount = layoutManager.itemCount
                    val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                    // 스크롤이 끝에 가까워지면 다음 페이지 로드
                    if (!viewModel.isLoadingGeneral.value!! &&
                        (visibleItemCount + firstVisibleItemPosition) >= totalItemCount - 5 &&
                        firstVisibleItemPosition >= 0) {

                        // 현재 상태에 따라 적절한 로딩 함수 호출
                        // TODO: 카테고리 필터 상태 추적 필요
                        viewModel.loadMoreGeneralMedicines()
                    }
                }
            })
        }
    }

    /**
     * ViewModel 관찰
     */
    private fun observeViewModel() {
        // 일반의약품 리스트 관찰
        viewModel.generalMedicines.observe(viewLifecycleOwner) { medicines ->
            android.util.Log.d("GeneralMedicineTab", "Medicines received: ${medicines.size}")
            val adapter = binding.medicineRecyclerView.adapter as? MedicineAdapter
            adapter?.submitList(medicines)
        }

        // 로딩 상태 관찰
        viewModel.isLoadingGeneral.observe(viewLifecycleOwner) { isLoading ->
            android.util.Log.d("GeneralMedicineTab", "Loading: $isLoading")
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        // 에러 메시지 관찰
        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                android.util.Log.e("GeneralMedicineTab", "Error: $it")
                // TODO: Snackbar 또는 Toast로 에러 표시
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}