package com.example.altong_v2.ui.mypage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.altong_v2.databinding.FragmentMypageBinding

/* * 마이페이지 Fragment
 * 설정, 알림 설정, 앱 정보*/

class MyPageFragment : Fragment() {
    private var _binding: FragmentMypageBinding? = null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMypageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // TODO: 메뉴 리스트 설정
        // TODO: 알림 설정 화면 이동
        // TODO: 시간 설정 화면 이동
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}