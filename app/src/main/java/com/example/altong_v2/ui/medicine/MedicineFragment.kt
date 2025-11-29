package com.example.altong_v2.ui.medicine

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.altong_v2.databinding.FragmentMedicineBinding


/* * 약 검색 Fragment (팀원 담당)
 * 일반의약품/전문의약품 검색 및 찜 기능*/

class MedicineFragment : Fragment() {
    private var _binding: FragmentMedicineBinding? = null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMedicineBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // TODO (팀원): TabLayout 설정 (일반의약품 / 전문의약품)
        // TODO (팀원): 카테고리 그리드 설정
        // TODO (팀원): 약품 리스트 RecyclerView 설정
        // TODO (팀원): ViewModel 연결
    }

    override fun onDestroyView() { super.onDestroyView()
        _binding = null
    }
}