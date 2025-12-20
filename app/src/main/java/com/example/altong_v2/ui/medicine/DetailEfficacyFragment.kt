package com.example.altong_v2.ui.medicine

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.altong_v2.databinding.FragmentDetailEfficacyBinding

/*
 * 약품 상세 - 효능/효과 탭
 */
class DetailEfficacyFragment : Fragment() {

    private var _binding: FragmentDetailEfficacyBinding? = null
    private val binding get() = _binding!!

    private var efficacy: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            efficacy = it.getString(ARG_EFFICACY)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetailEfficacyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.efficacyText.text = efficacy ?: "정보 없음"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_EFFICACY = "efficacy"

        fun newInstance(efficacy: String?): DetailEfficacyFragment {
            return DetailEfficacyFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_EFFICACY, efficacy)
                }
            }
        }
    }
}