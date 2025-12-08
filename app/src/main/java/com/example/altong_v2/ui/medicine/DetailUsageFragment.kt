package com.example.altong_v2.ui.medicine

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.altong_v2.databinding.FragmentDetailUsageBinding

/**
 * 약품 상세 - 용법/용량 탭
 */
class DetailUsageFragment : Fragment() {

    private var _binding: FragmentDetailUsageBinding? = null
    private val binding get() = _binding!!

    private var usageMethod: String? = null
    private var storageMethod: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            usageMethod = it.getString(ARG_USAGE)
            storageMethod = it.getString(ARG_STORAGE)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetailUsageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.usageText.text = usageMethod ?: "정보 없음"
        binding.storageText.text = storageMethod ?: "정보 없음"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_USAGE = "usage_method"
        private const val ARG_STORAGE = "storage_method"

        fun newInstance(usageMethod: String?, storageMethod: String?): DetailUsageFragment {
            return DetailUsageFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_USAGE, usageMethod)
                    putString(ARG_STORAGE, storageMethod)
                }
            }
        }
    }
}