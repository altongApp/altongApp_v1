package com.example.altong_v2.ui.medicine

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.adapter.FragmentStateAdapter
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
        android.util.Log.d("MedicineFragment", "🎨 onCreateView 호출")
        _binding = FragmentMedicineBinding.inflate(inflater, container, false)
        android.util.Log.d("MedicineFragment", "✅ Binding 생성 완료")
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        android.util.Log.d("MedicineFragment", "🎯 onViewCreated 호출")

        // ViewModel 초기화
        viewModel = ViewModelProvider(this)[MedicineViewModel::class.java]
        android.util.Log.d("MedicineFragment", "✅ ViewModel 생성 완료")

        setupViewPager()
        setupSearchBar()

        android.util.Log.d("MedicineFragment", "✅ 모든 설정 완료")
    }

    /**
     * ViewPager2 + TabLayout 설정
     */
    private fun setupViewPager() {
        android.util.Log.d("MedicineFragment", "📱 ViewPager 어댑터 설정 중...")

        // ViewPager2 어댑터 설정
        val adapter = MedicineViewPagerAdapter(this)
        binding.viewPager.adapter = adapter

        android.util.Log.d("MedicineFragment", "📑 TabLayout 연결 중...")

        // TabLayout과 ViewPager2 연결
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "💊 일반의약품"
                1 -> "🏥 전문의약품"
                else -> ""
            }
        }.attach()

        android.util.Log.d("MedicineFragment", "✅ ViewPager 설정 완료!")
    }

    /**
     * 검색바 설정
     */
    private fun setupSearchBar() {
        binding.searchEditText.setOnEditorActionListener { textView, actionId, _ ->
            val query = textView.text.toString()
            if (query.isNotBlank()) {
                performSearch(query)
            }
            true
        }
    }

    /**
     * 검색 실행
     */
    private fun performSearch(query: String) {
        val currentTab = binding.viewPager.currentItem
        when (currentTab) {
            0 -> viewModel.searchGeneralMedicines(query)
            1 -> viewModel.searchPrescriptionMedicines(query)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

/**
 * ViewPager2 Adapter
 * 일반의약품 탭 / 전문의약품 탭
 */
class MedicineViewPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        android.util.Log.d("MedicineViewPagerAdapter", "🔨 Fragment 생성 중... position=$position")

        return when (position) {
            0 -> {
                android.util.Log.d("MedicineViewPagerAdapter", "✅ GeneralMedicineTabFragment 생성!")
                GeneralMedicineTabFragment()
            }
            1 -> {
                android.util.Log.d("MedicineViewPagerAdapter", "✅ PrescriptionMedicineTabFragment 생성!")
                PrescriptionMedicineTabFragment()
            }
            else -> GeneralMedicineTabFragment()
        }
    }
}