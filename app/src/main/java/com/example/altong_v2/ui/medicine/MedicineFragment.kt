package com.example.altong_v2.ui.medicine

import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.altong_v2.R
import com.example.altong_v2.databinding.FragmentMedicineBinding
import com.google.android.material.tabs.TabLayoutMediator

/**
 * 약 검색 Fragment (메인)
 * TabLayout + ViewPager2로 일반의약품/전문의약품 탭 구성
 */
class MedicineFragment : Fragment() {

    private var _binding: FragmentMedicineBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: MedicineViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(TAG, "🎨 onCreateView 호출")
        _binding = FragmentMedicineBinding.inflate(inflater, container, false)
        Log.d(TAG, "✅ Binding 생성 완료")
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d(TAG, "🎯 onViewCreated 호출")

        // ViewModel 초기화
        viewModel = ViewModelProvider(requireActivity())[MedicineViewModel::class.java]
        Log.d(TAG, "✅ ViewModel 생성 완료")

        setupViewPager()
        setupSearchBar()

        Log.d(TAG, "✅ 모든 설정 완료")
    }

    /**
     * ViewPager2 + TabLayout 설정
     */
    private fun setupViewPager() {
        Log.d(TAG, "📱 ViewPager 어댑터 설정 중...")

        val adapter = MedicineViewPagerAdapter(this)
        binding.viewPager.adapter = adapter

        Log.d(TAG, "📑 TabLayout 연결 중...")

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "💊 일반의약품"
                1 -> "🏥 전문의약품"
                else -> ""
            }
        }.attach()

        Log.d(TAG, "✅ ViewPager 설정 완료!")
    }

    /**
     * ⭐ 검색바 설정 (버튼 + 엔터 둘 다 지원)
     */
    private fun setupSearchBar() {
        Log.d(TAG, "🔧 setupSearchBar 호출됨")

        // ⭐ 검색 버튼 클릭
        binding.searchButton.setOnClickListener {
            val query = binding.searchEditText.text.toString().trim()
            Log.d(TAG, "🔍 검색 버튼 클릭: $query")

            if (query.isNotBlank()) {
                performSearch(query)
                hideKeyboard()
            } else {
                Log.w(TAG, "⚠️ 검색어가 비어있습니다")
            }
        }

        // 엔터 키 (보조 기능)
        binding.searchEditText.setOnEditorActionListener { textView, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                actionId == EditorInfo.IME_ACTION_DONE ||
                (event?.action == KeyEvent.ACTION_DOWN && event.keyCode == KeyEvent.KEYCODE_ENTER)
            ) {
                val query = textView.text.toString().trim()
                Log.d(TAG, "⌨️ 엔터 키 입력: $query")

                if (query.isNotBlank()) {
                    performSearch(query)
                    hideKeyboard()
                }
                return@setOnEditorActionListener true
            }
            false
        }
    }

    /**
     * ⭐ 검색 실행 (검색 결과 화면으로 이동)
     */
    private fun performSearch(query: String) {
        val currentTab = binding.viewPager.currentItem

        // 검색 타입 결정
        val searchType = when (currentTab) {
            0 -> SearchResultFragment.TYPE_GENERAL
            1 -> SearchResultFragment.TYPE_PRESCRIPTION
            else -> SearchResultFragment.TYPE_GENERAL
        }

        Log.d(TAG, "🔍 검색 실행: $query (타입: $searchType)")

        // 검색 결과 Fragment로 이동
        val fragment = SearchResultFragment.newInstance(
            query = query,
            type = searchType
        )

        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    /**
     * 키보드 숨기기
     */
    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE)
                as android.view.inputmethod.InputMethodManager
        imm.hideSoftInputFromWindow(binding.searchEditText.windowToken, 0)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val TAG = "MedicineFragment"
    }
}

/**
 * ViewPager2 Adapter
 */
class MedicineViewPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        Log.d("MedicineViewPagerAdapter", "🔨 Fragment 생성 중... position=$position")

        return when (position) {
            0 -> {
                Log.d("MedicineViewPagerAdapter", "✅ GeneralMedicineTabFragment 생성!")
                GeneralMedicineTabFragment()
            }
            1 -> {
                Log.d("MedicineViewPagerAdapter", "✅ PrescriptionMedicineTabFragment 생성!")
                PrescriptionMedicineTabFragment()
            }
            else -> GeneralMedicineTabFragment()
        }
    }
}