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
import com.example.altong_v2.databinding.FragmentPrescriptionMedicineTabBinding

/**
 * 전문의약품 탭 Fragment
 * 전체 전문의약품 리스트 (8,208개)
 */
class PrescriptionMedicineTabFragment : Fragment() {

    private var _binding: FragmentPrescriptionMedicineTabBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: MedicineViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPrescriptionMedicineTabBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ViewModel 초기화 (Acti₩vity 레벨)
        viewModel = ViewModelProvider(requireActivity())[MedicineViewModel::class.java]

        android.util.Log.d("PrescriptionMedicineTab", "Fragment onViewCreated")

        setupMedicineList()
        setupFavoriteButton()
        observeViewModel()

        // 초기 데이터 로드 (데이터가 없을 때만)
        Log.d("PrescriptionMedicineTab", "현재 약품 개수: ${viewModel.prescriptionMedicines.value?.size ?: 0}")

        if (viewModel.prescriptionMedicines.value.isNullOrEmpty()) {
            Log.d("PrescriptionMedicineTab", "데이터 없음 - Firebase 로드 시작")
            viewModel.loadPrescriptionMedicines()
        } else {
            Log.d("PrescriptionMedicineTab", "데이터 이미 있음 - 로드 스킵")
        }
    }

    /**
     * 약품 리스트 설정
     */
    private fun setupMedicineList() {
        val adapter = PrescriptionMedicineAdapter(
            onItemClick = { medicine ->
                // 약품 클릭 시 상세 화면으로 이동
                // TODO: MedicineDetailFragment로 이동
            },
            onFavoriteClick = { medicine ->
                // 찜 버튼 클릭
                viewModel.addPrescriptionFavorite(medicine)
            }
        )

        binding.medicineRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            this.adapter = adapter

            // 페이지네이션: 스크롤 리스너 추가
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)

                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val visibleItemCount = layoutManager.childCount
                    val totalItemCount = layoutManager.itemCount
                    val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                    // 스크롤이 끝에 가까워지면 다음 페이지 로드
                    // 마지막 5개 아이템이 보이면 다음 페이지를 미리 로드
                    if (!viewModel.isLoadingPrescription.value!! &&
                        (visibleItemCount + firstVisibleItemPosition) >= totalItemCount - 5 &&
                        firstVisibleItemPosition >= 0) {

                        viewModel.loadMorePrescriptionMedicines()
                    }
                }
            })
        }
    }

    /**
     * 병원약 찜 보기 버튼 설정
     */
    private fun setupFavoriteButton() {
        binding.favoriteButton.setOnClickListener {
            // 찜 목록 화면으로 이동 (병원약 탭으로)
            navigateToFavoriteList()
        }
    }

    /**
     * 찜 목록 화면으로 이동
     */
    private fun navigateToFavoriteList() {
        val fragment = FavoriteMedicineFragment.newInstance()

        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    /**
     * ViewModel 관찰
     */
    private fun observeViewModel() {
        // 전문의약품 리스트 관찰
        viewModel.prescriptionMedicines.observe(viewLifecycleOwner) { medicines ->
            android.util.Log.d("PrescriptionMedicineTab", "Medicines received: ${medicines.size}")
            val adapter = binding.medicineRecyclerView.adapter as? PrescriptionMedicineAdapter
            adapter?.submitList(medicines)
        }

        // 로딩 상태 관찰
        viewModel.isLoadingPrescription.observe(viewLifecycleOwner) { isLoading ->
            android.util.Log.d("PrescriptionMedicineTab", "Loading: $isLoading")
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        // 에러 메시지 관찰
        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                android.util.Log.e("PrescriptionMedicineTab", "Error: $it")
                // TODO: Snackbar 또는 Toast로 에러 표시
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}