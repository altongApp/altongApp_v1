package com.example.altong_v2.ui.medicine

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.altong_v2.R
import com.example.altong_v2.databinding.FragmentMedicineDetailBinding
import com.example.altong_v2.data.local.entity.FavoriteMedicineEntity
import com.example.altong_v2.data.model.Medicine
import com.example.altong_v2.data.model.PrescriptionMedicine
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.launch

/**
 * 약품 상세 Fragment
 * Tab 1: 기본 정보 (효능, 용법, 주의사항)
 * Tab 2: 내 메모 (개인 메모)
 */
class MedicineDetailFragment : Fragment() {

    private var _binding: FragmentMedicineDetailBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: MedicineViewModel

    private var medicineId: String = ""
    private var medicineType: String = "otc"  // "otc" or "prescription"
    private var isFavorite: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        medicineId = arguments?.getString(ARG_MEDICINE_ID) ?: ""
        medicineType = arguments?.getString(ARG_MEDICINE_TYPE) ?: "otc"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMedicineDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity())[MedicineViewModel::class.java]

        setupToolbar()
        loadMedicineDetail()
        checkFavoriteStatus()
        setupFavoriteButton()
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
     * 약품 상세 정보 로드
     */
    private fun loadMedicineDetail() {
        lifecycleScope.launch {
            binding.progressBar.visibility = View.VISIBLE

            if (medicineType == "otc") {
                // 일반의약품
                val medicine = viewModel.getMedicineById(medicineId)
                medicine?.let { displayGeneralMedicine(it) }
            } else {
                // 전문의약품
                val medicine = viewModel.getPrescriptionMedicineById(medicineId)
                medicine?.let { displayPrescriptionMedicine(it) }
            }

            binding.progressBar.visibility = View.GONE
        }
    }

    /**
     * 일반의약품 정보 표시
     */
    private fun displayGeneralMedicine(medicine: Medicine) {
        binding.apply {
            // 약품명
            medicineName.text = medicine.medicine_name
            medicineCompany.text = medicine.manufacturer

            // 이미지
            if (!medicine.image_url.isNullOrBlank()) {
                Glide.with(requireContext())
                    .load(medicine.image_url)
                    .placeholder(R.drawable.medicine_image_placeholder)
                    .error(R.drawable.medicine_image_placeholder)
                    .into(medicineImage)
            }

            // 기본 정보
            efficacyText.text = medicine.efficacy ?: "정보 없음"
            usageText.text = medicine.usage_method ?: "정보 없음"
            precautionsText.text = medicine.precautions ?: "정보 없음"

            // 추가 정보
            if (!medicine.warning.isNullOrBlank()) {
                warningLabel.visibility = View.VISIBLE
                warningText.visibility = View.VISIBLE
                warningText.text = medicine.warning
            }

            if (!medicine.side_effects.isNullOrBlank()) {
                sideEffectsLabel.visibility = View.VISIBLE
                sideEffectsText.visibility = View.VISIBLE
                sideEffectsText.text = medicine.side_effects
            }

            if (!medicine.storage_method.isNullOrBlank()) {
                storageLabel.visibility = View.VISIBLE
                storageText.visibility = View.VISIBLE
                storageText.text = medicine.storage_method
            }
        }
    }

    /**
     * 전문의약품 정보 표시
     */
    private fun displayPrescriptionMedicine(medicine: PrescriptionMedicine) {
        binding.apply {
            // 약품명
            medicineName.text = medicine.medicine_name
            medicineCompany.text = medicine.manufacturer

            // 이미지
            if (!medicine.image_url.isNullOrBlank()) {
                Glide.with(requireContext())
                    .load(medicine.image_url)
                    .placeholder(R.drawable.medicine_image_placeholder)
                    .error(R.drawable.medicine_image_placeholder)
                    .into(medicineImage)
            }

            // 기본 정보
            efficacyText.text = medicine.efficacy ?: "정보 없음"
            usageText.text = medicine.usage_method ?: "정보 없음"
            precautionsText.text = medicine.precautions ?: "정보 없음"

            // 추가 정보
            if (!medicine.ingredients.isNullOrBlank()) {
                warningLabel.visibility = View.VISIBLE
                warningLabel.text = "성분 정보"
                warningText.visibility = View.VISIBLE
                warningText.text = medicine.ingredients
            }

            if (!medicine.storage_method.isNullOrBlank()) {
                storageLabel.visibility = View.VISIBLE
                storageText.visibility = View.VISIBLE
                storageText.text = medicine.storage_method
            }
        }
    }

    /**
     * 찜 상태 확인
     */
    private fun checkFavoriteStatus() {
        lifecycleScope.launch {
            isFavorite = viewModel.isFavorite(medicineId)
            updateFavoriteButton()
        }
    }

    /**
     * 찜 버튼 설정
     */
    private fun setupFavoriteButton() {
        binding.favoriteButton.setOnClickListener {
            toggleFavorite()
        }
    }

    /**
     * 찜 토글
     */
    private fun toggleFavorite() {
        lifecycleScope.launch {
            if (isFavorite) {
                // 찜 해제
                viewModel.removeFavorite(medicineId)
                isFavorite = false
                Toast.makeText(requireContext(), "찜 해제되었습니다", Toast.LENGTH_SHORT).show()
            } else {
                // 찜 추가
                if (medicineType == "otc") {
                    val medicine = viewModel.getMedicineById(medicineId)
                    medicine?.let { viewModel.addFavorite(it) }
                } else {
                    val medicine = viewModel.getPrescriptionMedicineById(medicineId)
                    medicine?.let { viewModel.addPrescriptionFavorite(it) }
                }
                isFavorite = true
                Toast.makeText(requireContext(), "찜 목록에 추가되었습니다", Toast.LENGTH_SHORT).show()
            }
            updateFavoriteButton()
        }
    }

    /**
     * 찜 버튼 UI 업데이트
     */
    private fun updateFavoriteButton() {
        if (isFavorite) {
            binding.favoriteButton.text = if (medicineType == "otc") "💙 약국약 찜 해제" else "❤️ 병원약 찜 해제"
        } else {
            binding.favoriteButton.text = if (medicineType == "otc") "💙 약국약 찜하기" else "❤️ 병원약 찜하기"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_MEDICINE_ID = "medicine_id"
        private const val ARG_MEDICINE_TYPE = "medicine_type"

        fun newInstance(medicineId: String, medicineType: String): MedicineDetailFragment {
            return MedicineDetailFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_MEDICINE_ID, medicineId)
                    putString(ARG_MEDICINE_TYPE, medicineType)
                }
            }
        }
    }
}