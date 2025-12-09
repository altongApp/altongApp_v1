package com.example.altong_v2.ui.medicine

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.bumptech.glide.Glide
import com.example.altong_v2.R
import com.example.altong_v2.data.model.Medicine
import com.example.altong_v2.data.model.PrescriptionMedicine
import com.example.altong_v2.databinding.FragmentMedicineDetailBinding
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.launch

class MedicineDetailFragment : Fragment() {

    private var _binding: FragmentMedicineDetailBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: MedicineViewModel

    private var medicineId: String? = null
    private var medicineType: String = TYPE_GENERAL
    private var isFavorite: Boolean = false

    // 현재 약품 정보 저장
    private var currentMedicine: Medicine? = null
    private var currentPrescriptionMedicine: PrescriptionMedicine? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            medicineId = it.getString(ARG_MEDICINE_ID)
            medicineType = it.getString(ARG_MEDICINE_TYPE) ?: TYPE_GENERAL
        }
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
        loadMedicineData()
        setupFavoriteButton()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }
    }

    private fun loadMedicineData() {
        binding.progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                if (medicineType == TYPE_GENERAL) {
                    val medicine = viewModel.getMedicineById(medicineId ?: "")
                    if (medicine != null) {
                        currentMedicine = medicine
                        displayGeneralMedicine(medicine)
                        checkFavoriteStatus()
                    }
                } else {
                    val medicine = viewModel.getPrescriptionMedicineById(medicineId ?: "")
                    if (medicine != null) {
                        currentPrescriptionMedicine = medicine
                        displayPrescriptionMedicine(medicine)
                        checkFavoriteStatus()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "약품 로드 실패", e)
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun displayGeneralMedicine(medicine: Medicine) {
        if (!medicine.image_url.isNullOrBlank()) {
            Glide.with(this)
                .load(medicine.image_url)
                .placeholder(R.drawable.medicine_image_placeholder)
                .error(R.drawable.medicine_image_placeholder)
                .into(binding.medicineImage)
        } else {
            binding.medicineImage.setImageResource(R.drawable.medicine_image_placeholder)
        }

        binding.medicineName.text = medicine.medicine_name
        binding.medicineCompany.text = medicine.manufacturer

        binding.memoButton.setOnClickListener {
            Log.d(TAG, "메모 버튼 클릭")
            Toast.makeText(requireContext(), "메모 기능은 추후 구현 예정입니다", Toast.LENGTH_SHORT).show()
        }

        setupTabsForGeneral(medicine)
    }

    private fun displayPrescriptionMedicine(medicine: PrescriptionMedicine) {
        if (!medicine.image_url.isNullOrBlank()) {
            Glide.with(this)
                .load(medicine.image_url)
                .placeholder(R.drawable.medicine_image_placeholder)
                .error(R.drawable.medicine_image_placeholder)
                .into(binding.medicineImage)
        } else {
            binding.medicineImage.setImageResource(R.drawable.medicine_image_placeholder)
        }

        binding.medicineName.text = medicine.medicine_name
        binding.medicineCompany.text = medicine.manufacturer

        binding.memoButton.setOnClickListener {
            Log.d(TAG, "메모 버튼 클릭")
            Toast.makeText(requireContext(), "메모 기능은 추후 구현 예정입니다", Toast.LENGTH_SHORT).show()
        }

        setupTabsForPrescription(medicine)
    }

    private fun setupTabsForGeneral(medicine: Medicine) {
        val adapter = DetailPagerAdapter(
            fragment = this,
            basicInfo = DetailBasicInfoFragment.newInstance(
                medicineName = medicine.medicine_name,
                manufacturer = medicine.manufacturer,
                thirdInfo = medicine.categories.joinToString(", "),
                thirdLabel = "카테고리"
            ),
            efficacy = DetailEfficacyFragment.newInstance(medicine.efficacy),
            usage = DetailUsageFragment.newInstance(
                medicine.usage_method,
                medicine.storage_method
            ),
            precautions = DetailPrecautionsFragment.newInstance(
                medicine.warning,
                medicine.precautions,
                medicine.side_effects
            )
        )

        binding.viewPager.adapter = adapter
        binding.viewPager.isUserInputEnabled = false

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "기본정보"
                1 -> "효능/효과"
                2 -> "용법/용량"
                3 -> "주의사항"
                else -> ""
            }
        }.attach()
    }

    private fun setupTabsForPrescription(medicine: PrescriptionMedicine) {
        val adapter = DetailPagerAdapter(
            fragment = this,
            basicInfo = DetailBasicInfoFragment.newInstance(
                medicineName = medicine.medicine_name,
                manufacturer = medicine.manufacturer,
                thirdInfo = medicine.ingredients ?: "-",
                thirdLabel = "성분정보"
            ),
            efficacy = DetailEfficacyFragment.newInstance(medicine.efficacy),
            usage = DetailUsageFragment.newInstance(
                medicine.usage_method,
                medicine.storage_method
            ),
            precautions = DetailPrecautionsFragment.newInstance(
                null,
                medicine.precautions,
                null
            )
        )

        binding.viewPager.adapter = adapter
        binding.viewPager.isUserInputEnabled = false

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "기본정보"
                1 -> "효능/효과"
                2 -> "용법/용량"
                3 -> "주의사항"
                else -> ""
            }
        }.attach()
    }

    /**
     *  찜 상태 확인
     */
    private fun checkFavoriteStatus() {
        lifecycleScope.launch {
            isFavorite = viewModel.isFavorite(medicineId ?: "")
            updateFavoriteButton()
        }
    }

    /**
     *  찜 버튼 설정
     */
    private fun setupFavoriteButton() {
        binding.favoriteButton.setOnClickListener {
            toggleFavorite()
        }
    }

    /**
     *  찜 토글 (추가/취소)
     */
    private fun toggleFavorite() {
        lifecycleScope.launch {
            if (isFavorite) {
                // 찜 해제
                viewModel.removeFavorite(medicineId ?: "")
                isFavorite = false
                Toast.makeText(requireContext(), "찜이 해제되었습니다", Toast.LENGTH_SHORT).show()
            } else {
                // 찜 추가
                if (medicineType == TYPE_GENERAL) {
                    currentMedicine?.let {
                        viewModel.addFavorite(it)
                        Toast.makeText(requireContext(), "약국약 찜에 추가되었습니다", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    currentPrescriptionMedicine?.let {
                        viewModel.addPrescriptionFavorite(it)
                        Toast.makeText(requireContext(), "병원약 찜에 추가되었습니다", Toast.LENGTH_SHORT).show()
                    }
                }
                isFavorite = true
            }
            updateFavoriteButton()
        }
    }

    /**
     *  찜 버튼 UI 업데이트
     */
    private fun updateFavoriteButton() {
        binding.favoriteButton.text = if (isFavorite) {
            if (medicineType == TYPE_GENERAL) "💙 약국약 찜 취소" else "❤️ 병원약 찜 취소"
        } else {
            if (medicineType == TYPE_GENERAL) "💙 약국약 찜에 추가" else "❤️ 병원약 찜에 추가"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val TAG = "MedicineDetailFragment"
        private const val ARG_MEDICINE_ID = "medicine_id"
        private const val ARG_MEDICINE_TYPE = "medicine_type"

        const val TYPE_GENERAL = "general"
        const val TYPE_PRESCRIPTION = "prescription"

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

class DetailPagerAdapter(
    fragment: Fragment,
    private val basicInfo: Fragment,
    private val efficacy: Fragment,
    private val usage: Fragment,
    private val precautions: Fragment
) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = 4

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> basicInfo
            1 -> efficacy
            2 -> usage
            3 -> precautions
            else -> basicInfo
        }
    }
}