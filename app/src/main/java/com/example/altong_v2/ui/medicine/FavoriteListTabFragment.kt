package com.example.altong_v2.ui.medicine

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.altong_v2.R
import com.example.altong_v2.databinding.FragmentFavoriteListTabBinding
import kotlinx.coroutines.launch


/**
 * 찜 목록 탭 Fragment
 * 약국약 또는 병원약 찜 목록 표시
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

        setupRecyclerView()
        loadFavorites()
    }

    /**
     * RecyclerView 설정
     */
    private fun setupRecyclerView() {
        val adapter = FavoriteMedicineAdapter(
            onItemClick = { favorite ->
                // 상세 화면으로 이동
                navigateToDetail(favorite.medicineId, favorite.medicineType)
            },
            onDeleteClick = { favorite ->
                // 찜 해제
                viewModel.removeFavorite(favorite.medicineId)
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
        binding.progressBar.visibility = View.VISIBLE

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.getFavoritesByType(medicineType).collect { favorites ->
                    val adapter = binding.favoriteRecyclerView.adapter as? FavoriteMedicineAdapter
                    adapter?.submitList(favorites)

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
        }
    }

    /**
     * 상세 화면으로 이동
     */
    private fun navigateToDetail(medicineId: String, type: String) {
        // ⭐ 이제 타입이 통일되어 변환 불필요!
        val fragment = MedicineDetailFragment.newInstance(medicineId, type)

        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val TAG = "FavoriteListTabFragment"
        private const val ARG_TYPE = "medicine_type"

        /**
         * ⭐ type: "general" (약국약) or "prescription" (병원약)
         */
        fun newInstance(type: String): FavoriteListTabFragment {
            return FavoriteListTabFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_TYPE, type)
                }
            }
        }
    }
}