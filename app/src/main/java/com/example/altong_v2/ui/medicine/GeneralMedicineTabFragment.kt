package com.example.altong_v2.ui.medicine

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.altong_v2.R
import com.example.altong_v2.data.model.MedicineCategory
import com.example.altong_v2.databinding.FragmentGeneralMedicineTabBinding

/**
 * 일반의약품 탭 Fragment
 * 카테고리 그리드 + 약품 리스트
 * 구조: NestedScrollView 안에 RecyclerView가 있는 형태
 */
class GeneralMedicineTabFragment : Fragment() {

    private var _binding: FragmentGeneralMedicineTabBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: MedicineViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("GeneralMedicineTab", "🔴 onCreate 호출!")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d("GeneralMedicineTab", "🟡 onCreateView 호출!")
        _binding = FragmentGeneralMedicineTabBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d("GeneralMedicineTab", "🟢 onViewCreated 호출!")

        // ViewModel 초기화 (Activity 레벨)
        viewModel = ViewModelProvider(requireActivity())[MedicineViewModel::class.java]

        setupCategoryGrid()
        setupMedicineList()
        observeViewModel()

        // 초기 데이터 로드 (데이터가 없을 때만)
        Log.d("GeneralMedicineTab", "현재 약품 개수: ${viewModel.generalMedicines.value?.size ?: 0}")

        if (viewModel.generalMedicines.value.isNullOrEmpty()) {
            Log.d("GeneralMedicineTab", "데이터 없음 - Firebase 로드 시작")
            viewModel.loadGeneralMedicines()
        } else {
            Log.d("GeneralMedicineTab", "데이터 이미 있음 - 로드 스킵")
        }
    }

    /**
     * 카테고리 그리드 설정
     */
    private fun setupCategoryGrid() {
        Log.d("GeneralMedicineTab", "🎨 setupCategoryGrid 호출됨!")
        Log.d("GeneralMedicineTab", "📦 카테고리 개수: ${MedicineCategory.ALL_CATEGORIES.size}")

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

        Log.d("GeneralMedicineTab", "✅ 카테고리 데이터 submitList 완료!")
    }

    /**
     * 카테고리별 약품 리스트 화면으로 이동
     */
    private fun navigateToCategoryList(category: String) {
        val fragment = CategoryMedicineListFragment.newInstance(category)

        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    /**
     * 약품 리스트 설정
     * ★ 수정됨: 바닥에 닿기 전에 미리 로딩하도록 감도 조절
     */
    private fun setupMedicineList() {
        val medicineAdapter = MedicineAdapter(
            onItemClick = { medicine ->
                // 약품 클릭 시 상세 화면으로 이동
                // TODO: MedicineDetailFragment로 이동
                navigateToDetail(medicine.medicine_id, MedicineDetailFragment.TYPE_GENERAL)
            },
            onFavoriteClick = { medicine ->
                // 찜 버튼 클릭
                viewModel.addFavorite(medicine)
            }
        )

        // 1. 리사이클러뷰 연결
        binding.medicineRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = medicineAdapter
            // nestedScrollingEnabled="false" 이므로 리사이클러뷰 자체 스크롤 리스너는 사용 안 함
        }

        // 2. NestedScrollView 스크롤 감지 (미리 로딩 적용)
        binding.mainScrollView.setOnScrollChangeListener { _, _, scrollY, _, _ ->

            val scrollView = binding.mainScrollView

            // 전체 내용 높이
            val totalHeight = scrollView.getChildAt(0).measuredHeight
            // 현재 화면 높이
            val viewHeight = scrollView.measuredHeight

            // ★ 핵심 수정: 바닥에서 2000픽셀 정도 남았을 때 미리 로딩 (약 5~6개 아이템 높이)
            // 숫자가 클수록 더 빨리(위에서) 로딩됩니다.
            val threshold = 2000

            if (scrollY >= (totalHeight - viewHeight - threshold)) {

                val isLoading = viewModel.isLoadingGeneral.value ?: false

                // 로딩 중이 아닐 때만 요청
                if (!isLoading) {
                    Log.d("GeneralMedicineTab", "📜 (스크롤뷰) 추가 데이터 미리 로딩 요청 (Threshold: $threshold)")
                    viewModel.loadMoreGeneralMedicines()
                }
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
        // 일반의약품 리스트 관찰
        viewModel.generalMedicines.observe(viewLifecycleOwner) { medicines ->
            Log.d("GeneralMedicineTab", "📦 Medicines received: ${medicines.size}")
            val adapter = binding.medicineRecyclerView.adapter as? MedicineAdapter
            adapter?.submitList(medicines)
        }

        // 로딩 상태 관찰
        viewModel.isLoadingGeneral.observe(viewLifecycleOwner) { isLoading ->
            Log.d("GeneralMedicineTab", "⏳ Loading: $isLoading")
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        // 에러 메시지 관찰
        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                Log.e("GeneralMedicineTab", "❌ Error: $it")
                // TODO: Snackbar 또는 Toast로 에러 표시
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d("GeneralMedicineTab", "💀 onDestroyView 호출!")
        _binding = null
    }
}
//package com.example.altong_v2.ui.medicine
//
//import android.os.Bundle
//import android.util.Log
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import androidx.fragment.app.Fragment
//import androidx.lifecycle.ViewModelProvider
//import androidx.recyclerview.widget.GridLayoutManager
//import androidx.recyclerview.widget.LinearLayoutManager
//import androidx.recyclerview.widget.RecyclerView
//import com.example.altong_v2.data.model.MedicineCategory
//import com.example.altong_v2.databinding.FragmentGeneralMedicineTabBinding
//
///**
// * 일반의약품 탭 Fragment
// * 카테고리 그리드 + 약품 리스트
// */
//class GeneralMedicineTabFragment : Fragment() {
//
//    private var _binding: FragmentGeneralMedicineTabBinding? = null
//    private val binding get() = _binding!!
//
//    private lateinit var viewModel: MedicineViewModel
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        Log.d("GeneralMedicineTab", "🔴 onCreate 호출!")
//    }
//
//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View {
//        Log.d("GeneralMedicineTab", "🟡 onCreateView 호출!")
//        _binding = FragmentGeneralMedicineTabBinding.inflate(inflater, container, false)
//        return binding.root
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
//        Log.d("GeneralMedicineTab", "🟢 onViewCreated 호출!")
//
//        // ViewModel 초기화 (Activity 레벨)
//        viewModel = ViewModelProvider(requireActivity())[MedicineViewModel::class.java]
//
//        setupCategoryGrid()
//        setupMedicineList()
//        observeViewModel()
//
//        // 초기 데이터 로드 (데이터가 없을 때만)
//        Log.d("GeneralMedicineTab", "현재 약품 개수: ${viewModel.generalMedicines.value?.size ?: 0}")
//
//        if (viewModel.generalMedicines.value.isNullOrEmpty()) {
//            Log.d("GeneralMedicineTab", "데이터 없음 - Firebase 로드 시작")
//            viewModel.loadGeneralMedicines()
//        } else {
//            Log.d("GeneralMedicineTab", "데이터 이미 있음 - 로드 스킵")
//        }
//    }
//
//    /**
//     * 카테고리 그리드 설정
//     */
//    private fun setupCategoryGrid() {
//        Log.d("GeneralMedicineTab", "🎨 setupCategoryGrid 호출됨!")
//        Log.d("GeneralMedicineTab", "📦 카테고리 개수: ${MedicineCategory.ALL_CATEGORIES.size}")
//
//        val categoryAdapter = CategoryAdapter { category: String ->
//            // 카테고리 클릭 시 해당 카테고리 약품 목록 화면으로 이동
//            navigateToCategoryList(category)
//        }
//
//        binding.categoryRecyclerView.apply {
//            layoutManager = GridLayoutManager(requireContext(), 3)
//            adapter = categoryAdapter
//        }
//
//        // 카테고리 데이터 설정
//        categoryAdapter.submitList(MedicineCategory.ALL_CATEGORIES)
//
//        Log.d("GeneralMedicineTab", "✅ 카테고리 데이터 submitList 완료!")
//    }
//
//    /**
//     * 카테고리별 약품 리스트 화면으로 이동
//     */
//    private fun navigateToCategoryList(category: String) {
//        val fragment = CategoryMedicineListFragment.newInstance(category)
//
//        parentFragmentManager.beginTransaction()
//            .replace(android.R.id.content, fragment)
//            .addToBackStack(null)
//            .commit()
//    }
//
//    /**
//     * 약품 리스트 설정
//     */
//    /**
//     * 약품 리스트 설정
//     */
//    private fun setupMedicineList() {
//        val medicineAdapter = MedicineAdapter(
//            onItemClick = { medicine ->
//                // 약품 클릭 시 상세 화면으로 이동
//                // TODO: MedicineDetailFragment로 이동
//            },
//            onFavoriteClick = { medicine ->
//                // 찜 버튼 클릭
//                viewModel.addFavorite(medicine)
//            }
//        )
//
//        binding.medicineRecyclerView.apply {
//            layoutManager = LinearLayoutManager(requireContext())
//            adapter = medicineAdapter
//            // 페이지네이션: 스크롤 리스너 추가
//            addOnScrollListener(object : RecyclerView.OnScrollListener() {
//                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
//                    super.onScrolled(recyclerView, dx, dy)
//
//
//                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
//                    val visibleItemCount = layoutManager.childCount
//                    val totalItemCount = layoutManager.itemCount
//                    val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
//
//                    if (!recyclerView.canScrollVertically(1)) {
//
//                        // 3. 현재 로딩 중이 아닐 때만 요청 (null 처리 포함)
//                        val isLoading = viewModel.isLoadingGeneral.value ?: false
//                        if (!isLoading) {
//                            Log.d("GeneralMedicineTab", "📜 리스트 바닥 도착! 추가 데이터 요청")
//                            viewModel.loadMoreGeneralMedicines()
//                        }
//                    }
//                }
//            })
//            // 페이지네이션: 전문의약품 코드 기반 + 무한로딩 방지(dy > 0)
////            addOnScrollListener(object : RecyclerView.OnScrollListener() {
////                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
////                    super.onScrolled(recyclerView, dx, dy)
////
////                    // 1. dy <= 0 : 위로 올리거나 멈춰있을 때는 무시 (무한로딩 방지)
////                    if (dy <= 0) return
////
////                    // 2. !canScrollVertically(1) : 더 이상 아래로(1) 스크롤 할 수 없는가? (바닥에 닿음)
////                    // 이 방식이 아이템 개수 계산보다 훨씬 정확합니다.
////                    if (!recyclerView.canScrollVertically(1)) {
////
////                        val isLoading = viewModel.isLoadingGeneral.value ?: false
////
////                        if (!isLoading) {
////                            Log.d("GeneralMedicineTab", "✅ 리스트 바닥 감지! 추가 데이터 로드 요청")
////                            viewModel.loadMoreGeneralMedicines()
////                        }
////                    }
////                }
////            })
//        }
//    }
//
//            //////
//
////        }
////    }
//
//    /**
//     * ViewModel 관찰
//     */
//    private fun observeViewModel() {
//        // 일반의약품 리스트 관찰
//        viewModel.generalMedicines.observe(viewLifecycleOwner) { medicines ->
//            Log.d("GeneralMedicineTab", "📦 Medicines received: ${medicines.size}")
//            val adapter = binding.medicineRecyclerView.adapter as? MedicineAdapter
//            adapter?.submitList(medicines)
//        }
//
//        // 로딩 상태 관찰
//        viewModel.isLoadingGeneral.observe(viewLifecycleOwner) { isLoading ->
//            Log.d("GeneralMedicineTab", "⏳ Loading: $isLoading")
//            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
//        }
//
//        // 에러 메시지 관찰
//        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
//            message?.let {
//                Log.e("GeneralMedicineTab", "❌ Error: $it")
//                // TODO: Snackbar 또는 Toast로 에러 표시
//            }
//        }
//    }
//
//    override fun onDestroyView() {
//        super.onDestroyView()
//        Log.d("GeneralMedicineTab", "💀 onDestroyView 호출!")
//        _binding = null
//    }
//}