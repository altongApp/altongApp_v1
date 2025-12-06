package com.example.altong_v2.ui.medicine

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.altong_v2.databinding.FragmentFavoriteListTabBinding
import com.example.altong_v2.data.local.entity.FavoriteMedicineEntity
import kotlinx.coroutines.launch

/**
 * 찜 목록 탭 Fragment
 * type에 따라 약국약 또는 병원약 찜 목록 표시
 */
class FavoriteListTabFragment : Fragment() {

    private var _binding: FragmentFavoriteListTabBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: MedicineViewModel
    private var medicineType: String = "otc"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        medicineType = arguments?.getString(ARG_TYPE) ?: "otc"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFavoriteListTabBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity())[MedicineViewModel::class.java]

        setupFavoriteList()
        loadFavorites()
    }

    /**
     * 찜 목록 리스트 설정
     */
    private fun setupFavoriteList() {
        val adapter = FavoriteMedicineAdapter(
            onItemClick = { favorite ->
                // 상세 화면으로 이동
                navigateToDetail(favorite.medicineId, favorite.medicineType)
            },
            onDeleteClick = { favorite ->
                // 찜 해제
                viewModel.removeFavorite(favorite.medicineId)
                loadFavorites()  // 목록 새로고침
            }
        )

        binding.favoriteRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            this.adapter = adapter
        }
    }

    /**
     * 찜 목록 로드
     */
    private fun loadFavorites() {
        lifecycleScope.launch {
            binding.progressBar.visibility = View.VISIBLE

            val favorites = viewModel.getFavoritesByType(medicineType)

            val adapter = binding.favoriteRecyclerView.adapter as? FavoriteMedicineAdapter
            adapter?.submitList(favorites)

            // 결과 개수 표시
            binding.resultCount.text = "총 ${favorites.size}개"

            // 빈 화면 처리
            if (favorites.isEmpty()) {
                binding.emptyView.visibility = View.VISIBLE
                binding.favoriteRecyclerView.visibility = View.GONE
            } else {
                binding.emptyView.visibility = View.GONE
                binding.favoriteRecyclerView.visibility = View.VISIBLE
            }

            binding.progressBar.visibility = View.GONE
        }
    }

    /**
     * 상세 화면으로 이동
     */
    private fun navigateToDetail(medicineId: String, medicineType: String) {
        val fragment = MedicineDetailFragment.newInstance(medicineId, medicineType)

        parentFragmentManager.beginTransaction()
            .replace(android.R.id.content, fragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onResume() {
        super.onResume()
        // 화면 돌아올 때마다 새로고침
        loadFavorites()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_TYPE = "type"

        fun newInstance(type: String): FavoriteListTabFragment {
            return FavoriteListTabFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_TYPE, type)
                }
            }
        }
    }
}