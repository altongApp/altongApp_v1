package com.example.altong_v2.ui.medicine

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.altong_v2.databinding.FragmentDetailPrecautionsBinding

/*
 * 약품 상세 - 주의사항 탭
 */
class DetailPrecautionsFragment : Fragment() {

    private var _binding: FragmentDetailPrecautionsBinding? = null
    private val binding get() = _binding!!

    private var warning: String? = null
    private var precautions: String? = null
    private var sideEffects: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            warning = it.getString(ARG_WARNING)
            precautions = it.getString(ARG_PRECAUTIONS)
            sideEffects = it.getString(ARG_SIDE_EFFECTS)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetailPrecautionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 경고 (있으면 표시)
        if (!warning.isNullOrBlank()) {
            binding.warningSection.visibility = View.VISIBLE
            binding.warningText.text = warning
        } else {
            binding.warningSection.visibility = View.GONE
        }

        // 사용상의 주의사항
        binding.precautionsText.text = precautions ?: "정보 없음"

        // 부작용 (있으면 표시)
        if (!sideEffects.isNullOrBlank()) {
            binding.sideEffectsSection.visibility = View.VISIBLE
            binding.sideEffectsText.text = sideEffects
        } else {
            binding.sideEffectsSection.visibility = View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_WARNING = "warning"
        private const val ARG_PRECAUTIONS = "precautions"
        private const val ARG_SIDE_EFFECTS = "side_effects"

        fun newInstance(
            warning: String?,
            precautions: String?,
            sideEffects: String?
        ): DetailPrecautionsFragment {
            return DetailPrecautionsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_WARNING, warning)
                    putString(ARG_PRECAUTIONS, precautions)
                    putString(ARG_SIDE_EFFECTS, sideEffects)
                }
            }
        }
    }
}