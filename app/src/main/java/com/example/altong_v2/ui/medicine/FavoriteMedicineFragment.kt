package com.example.altong_v2.ui.medicine

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.altong_v2.databinding.FragmentFavoriteMedicineBinding
import com.google.android.material.tabs.TabLayoutMediator

/**
 * 찜 목록 Fragment (메인)
 * 탭: 약국약 / 병원약
 */
class FavoriteMedicineFragment : Fragment() {

    private var _binding: FragmentFavoriteMedicineBinding? = null
    private val binding get() = _binding!!

    private var initialTab: Int = 0  // 초기 탭 (0=약국약, 1=병원약)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initialTab = arguments?.getInt(ARG_INITIAL_TAB) ?: 0
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFavoriteMedicineBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupViewPager()

        // ⭐ 초기 탭 설정
        binding.viewPager.setCurrentItem(initialTab, false)
    }

    /**
     * 툴바 설정
     */
    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }
    }

    /**
     * ViewPager 설정
     */
    private fun setupViewPager() {
        val adapter = FavoriteViewPagerAdapter(this)
        binding.viewPager.adapter = adapter

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "💊 약국약"
                1 -> "🏥 병원약"
                else -> ""
            }
        }.attach()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_INITIAL_TAB = "initial_tab"

        // 약국약 탭부터 보기 (기본)
        fun newInstance(): FavoriteMedicineFragment {
            return newInstance(0)
        }

        /*
         * 특정 탭부터 보기
         * @param tabIndex 0=약국약, 1=병원약
         */
        fun newInstance(tabIndex: Int): FavoriteMedicineFragment {
            return FavoriteMedicineFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_INITIAL_TAB, tabIndex)
                }
            }
        }
    }
}

/*
 * ViewPager2 Adapter
 */
class FavoriteViewPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> FavoriteListTabFragment.newInstance("general")  //  "otc" → "general"
            1 -> FavoriteListTabFragment.newInstance("prescription")
            else -> FavoriteListTabFragment.newInstance("general")
        }
    }
}