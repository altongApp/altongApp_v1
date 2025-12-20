package com.example.altong_v2.ui.medicine

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.altong_v2.R
import com.example.altong_v2.databinding.FragmentSearchResultBinding

/*
 * 약품 검색 결과 Fragment
 * 일반의약품 또는 전문의약품 검색 결과 표시
 */
class SearchResultFragment : Fragment() {

    private var _binding: FragmentSearchResultBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: MedicineViewModel
    private var searchQuery: String = ""
    private var medicineType: String = TYPE_GENERAL  // "general" or "prescription"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        searchQuery = arguments?.getString(ARG_QUERY) ?: ""
        medicineType = arguments?.getString(ARG_TYPE) ?: TYPE_GENERAL
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchResultBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity())[MedicineViewModel::class.java]

        setupToolbar()
        setupMedicineList()
        observeViewModel()

        // 검색 실행
        performSearch()
    }

    /**
     * 툴바 설정
     */
    private fun setupToolbar() {
        binding.toolbar.apply {
            title = "\"$searchQuery\" 검색 결과"
            setNavigationOnClickListener {
                requireActivity().supportFragmentManager.popBackStack()
            }
        }
    }

    /**
     * 약품 리스트 설정
     */
    private fun setupMedicineList() {
        val adapter = if (medicineType == TYPE_GENERAL) {
            // 일반의약품 어댑터
            MedicineAdapter(
                onItemClick = { medicine ->
                    // TODO: 상세 화면으로 이동
                    navigateToDetail(medicine.medicine_id, MedicineDetailFragment.TYPE_GENERAL)
                },
                onFavoriteClick = { medicine ->
                    viewModel.addFavorite(medicine)
                }
            )
        } else {
            // 전문의약품 어댑터
            PrescriptionMedicineAdapter(
                onItemClick = { medicine ->
                    // TODO: 상세 화면으로 이동
                    navigateToDetail(medicine.medicine_id, MedicineDetailFragment.TYPE_PRESCRIPTION)
                },
                onFavoriteClick = { medicine ->
                    viewModel.addPrescriptionFavorite(medicine)
                }
            )
        }

        binding.medicineRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            this.adapter = adapter

            // 페이지네이션 (검색 결과도 20개씩)
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)

                    if (dy <= 0) return

                    if (!recyclerView.canScrollVertically(1)) {
                        val isLoading = if (medicineType == TYPE_GENERAL) {
                            viewModel.isLoadingGeneral.value ?: false
                        } else {
                            viewModel.isLoadingPrescription.value ?: false
                        }

                        if (!isLoading) {
                            Log.d(TAG, "📜 검색 결과 추가 로드")
                            performSearch(loadMore = true)
                        }
                    }
                }
            })
        }
    }

    /**
     * 검색 실행
     */
    private fun performSearch(loadMore: Boolean = false) {
        if (medicineType == TYPE_GENERAL) {
            if (loadMore) {
                viewModel.searchMoreGeneralMedicines(searchQuery)
            } else {
                viewModel.searchGeneralMedicines(searchQuery)
            }
        } else {
            if (loadMore) {
                viewModel.searchMorePrescriptionMedicines(searchQuery)
            } else {
                viewModel.searchPrescriptionMedicines(searchQuery)
            }
        }
    }

    /**
     * ⭐ 상세 화면으로 이동
     */
    private fun navigateToDetail(medicineId: String, type: String) {
        val fragment = MedicineDetailFragment.newInstance(medicineId, type)

        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    /**
     * ViewModel 관찰
     */
    private fun observeViewModel() {
        if (medicineType == TYPE_GENERAL) {
            // 일반의약품 관찰
            viewModel.generalMedicines.observe(viewLifecycleOwner) { medicines ->
                val adapter = binding.medicineRecyclerView.adapter as? MedicineAdapter
                adapter?.submitList(medicines)

                // 결과 개수 표시
                binding.resultCount.text = "총 ${medicines.size}개"

                // 빈 화면 처리
                updateEmptyView(medicines.isEmpty())
            }

            viewModel.isLoadingGeneral.observe(viewLifecycleOwner) { isLoading ->
                binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            }

        } else {
            // 전문의약품 관찰
            viewModel.prescriptionMedicines.observe(viewLifecycleOwner) { medicines ->
                val adapter = binding.medicineRecyclerView.adapter as? PrescriptionMedicineAdapter
                adapter?.submitList(medicines)

                // 결과 개수 표시
                binding.resultCount.text = "총 ${medicines.size}개"

                // 빈 화면 처리
                updateEmptyView(medicines.isEmpty())
            }

            viewModel.isLoadingPrescription.observe(viewLifecycleOwner) { isLoading ->
                binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            }
        }
    }

    /**
     * 빈 화면 표시
     */
    private fun updateEmptyView(isEmpty: Boolean) {
        if (isEmpty) {
            binding.emptyView.visibility = View.VISIBLE
            binding.medicineRecyclerView.visibility = View.GONE
        } else {
            binding.emptyView.visibility = View.GONE
            binding.medicineRecyclerView.visibility = View.VISIBLE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val TAG = "SearchResultFragment"
        private const val ARG_QUERY = "query"
        private const val ARG_TYPE = "type"

        const val TYPE_GENERAL = "general"
        const val TYPE_PRESCRIPTION = "prescription"

        /**
         * Fragment 생성
         */
        fun newInstance(query: String, type: String): SearchResultFragment {
            return SearchResultFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_QUERY, query)
                    putString(ARG_TYPE, type)
                }
            }
        }
    }
}