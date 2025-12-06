package com.example.altong_v2.ui.medicine

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.altong_v2.databinding.FragmentFavoriteMedicineBinding
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.launch

/**
 * 찜 목록 Fragment
 * Tab 1: 약국약 찜 (일반의약품)
 * Tab 2: 병원약 찜 (전문의약품)
 */
class FavoriteMedicineFragment : Fragment() {

    private var _binding: FragmentFavoriteMedicineBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: MedicineViewModel

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

        viewModel = ViewModelProvider(requireActivity())[MedicineViewModel::class.java]

        setupToolbar()
        setupViewPager()
    }

    /**
     * 툴바 설정
     */
    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    /**
     * ViewPager + TabLayout 설정
     */
    private fun setupViewPager() {
        val adapter = FavoriteViewPagerAdapter(this)
        binding.viewPager.adapter = adapter

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "💙 약국약 찜"
                1 -> "❤️ 병원약 찜"
                else -> ""
            }
        }.attach()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(): FavoriteMedicineFragment {
            return FavoriteMedicineFragment()
        }
    }
}

/**
 * 찜 목록 ViewPager Adapter
 */
class FavoriteViewPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> FavoriteListTabFragment.newInstance("otc")  // 약국약
            1 -> FavoriteListTabFragment.newInstance("prescription")  // 병원약
            else -> FavoriteListTabFragment.newInstance("otc")
        }
    }
}