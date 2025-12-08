package com.example.altong_v2.ui.medicine

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.altong_v2.databinding.FragmentDetailBasicInfoBinding

/**
 * 약품 상세 - 기본정보 탭
 */
class DetailBasicInfoFragment : Fragment() {

    private var _binding: FragmentDetailBasicInfoBinding? = null
    private val binding get() = _binding!!

    private var medicineName: String? = null
    private var manufacturer: String? = null
    private var thirdInfo: String? = null // 카테고리 or 성분정보
    private var thirdInfoLabel: String = "카테고리" // 라벨

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            medicineName = it.getString(ARG_MEDICINE_NAME)
            manufacturer = it.getString(ARG_MANUFACTURER)
            thirdInfo = it.getString(ARG_THIRD_INFO)
            thirdInfoLabel = it.getString(ARG_THIRD_LABEL) ?: "카테고리"
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetailBasicInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 데이터 설정
        binding.medicineNameValue.text = medicineName ?: "-"
        binding.manufacturerValue.text = manufacturer ?: "-"
        binding.thirdInfoLabel.text = thirdInfoLabel
        binding.thirdInfoValue.text = thirdInfo ?: "-"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_MEDICINE_NAME = "medicine_name"
        private const val ARG_MANUFACTURER = "manufacturer"
        private const val ARG_THIRD_INFO = "third_info"
        private const val ARG_THIRD_LABEL = "third_label"

        fun newInstance(
            medicineName: String,
            manufacturer: String,
            thirdInfo: String,
            thirdLabel: String = "카테고리"
        ): DetailBasicInfoFragment {
            return DetailBasicInfoFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_MEDICINE_NAME, medicineName)
                    putString(ARG_MANUFACTURER, manufacturer)
                    putString(ARG_THIRD_INFO, thirdInfo)
                    putString(ARG_THIRD_LABEL, thirdLabel)
                }
            }
        }
    }
}