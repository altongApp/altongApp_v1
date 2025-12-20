package com.example.altong_v2.ui.medicine

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.altong_v2.R
import com.example.altong_v2.databinding.FragmentCategoryMedicineListBinding

/*
 * 카테고리별 약품 리스트 Fragment
 * 선택한 카테고리에 속하는 일반의약품만 표시
 */
class CategoryMedicineListFragment : Fragment() {

    private var _binding: FragmentCategoryMedicineListBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: MedicineViewModel
    private var categoryName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Arguments로 전달받은 카테고리명
        categoryName = arguments?.getString(ARG_CATEGORY) ?: ""
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCategoryMedicineListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 부모 Activity의 ViewModel 공유
        viewModel = ViewModelProvider(requireActivity())[MedicineViewModel::class.java]

        setupToolbar()
        setupMedicineList()
        observeViewModel()

        // 카테고리별 약품 로드
        viewModel.loadMedicinesByCategory(categoryName)
    }


    // 툴바 설정 (뒤로가기 + 카테고리명)
    private fun setupToolbar() {
        binding.toolbar.apply {
            title = categoryName
            setNavigationOnClickListener {
                parentFragmentManager.popBackStack()
            }
        }
    }

     // 약품 리스트 설정
    private fun setupMedicineList() {
        val adapter = MedicineAdapter(
            onItemClick = { medicine ->
                // 약품 상세 화면으로 이동
                // TODO: MedicineDetailFragment로 이동
                navigateToDetail(medicine.medicine_id, MedicineDetailFragment.TYPE_GENERAL)

            },
            onFavoriteClick = { medicine ->
                viewModel.addFavorite(medicine)
            }
        )

        binding.medicineRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            this.adapter = adapter

            // 페이지네이션 스크롤 리스너
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)

                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val visibleItemCount = layoutManager.childCount
                    val totalItemCount = layoutManager.itemCount
                    val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                    // 끝에 가까워지면 다음 페이지 로드
                    if (!viewModel.isLoadingGeneral.value!! &&
                        (visibleItemCount + firstVisibleItemPosition) >= totalItemCount - 5 &&
                        firstVisibleItemPosition >= 0) {

                        viewModel.loadMoreMedicinesByCategory(categoryName)
                    }
                }
            })
        }
    }

    //상세 화면으로 이동
    private fun navigateToDetail(medicineId: String, type: String) {
        val fragment = MedicineDetailFragment.newInstance(medicineId, type)

        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }


    private fun observeViewModel() {
        // 약품 리스트 관찰
        viewModel.generalMedicines.observe(viewLifecycleOwner) { medicines ->
            val adapter = binding.medicineRecyclerView.adapter as? MedicineAdapter
            adapter?.submitList(medicines)

            // 결과 개수 표시
            binding.resultCount.text = "총 ${medicines.size}개"

            // 빈 화면 처리
            if (medicines.isEmpty()) {
                binding.emptyView.visibility = View.VISIBLE
                binding.medicineRecyclerView.visibility = View.GONE
            } else {
                binding.emptyView.visibility = View.GONE
                binding.medicineRecyclerView.visibility = View.VISIBLE
            }
        }

        // 로딩 상태 관찰
        viewModel.isLoadingGeneral.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_CATEGORY = "category"

        // Fragment 생성 (Bundle로 카테고리 전달)
        fun newInstance(category: String): CategoryMedicineListFragment {
            return CategoryMedicineListFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_CATEGORY, category)
                }
            }
        }
    }
}