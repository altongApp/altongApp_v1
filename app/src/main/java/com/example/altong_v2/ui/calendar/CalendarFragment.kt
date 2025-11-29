package com.example.altong_v2.ui.calendar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.altong_v2.databinding.FragmentCalendarBinding

/* * 캘린더 Fragment (팀원 담당)
 * 월간 캘린더 및 복약 체크리스트*/

class CalendarFragment : Fragment() {
    private var _binding: FragmentCalendarBinding? = null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCalendarBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // TODO (팀원): 월간 캘린더 GridLayout 설정
        // TODO (팀원): 날짜 선택 처리
        // TODO (팀원): 복약 체크리스트 RecyclerView 설정
        // TODO (팀원): ViewModel 연결
    }

    override fun onDestroyView() { super.onDestroyView()
        _binding = null
    }
}