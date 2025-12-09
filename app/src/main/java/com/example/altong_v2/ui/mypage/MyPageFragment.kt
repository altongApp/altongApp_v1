package com.example.altong_v2.ui.mypage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.altong_v2.databinding.FragmentMypageBinding

/* * 마이페이지 Fragment
 * 설정, 알림 설정, 앱 정보 */

class MyPageFragment : Fragment() {
    private var _binding: FragmentMypageBinding? = null
    private val binding get() = _binding!!
    private lateinit var alarmSettings: AlarmSettings

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
        // AlarmSettings 초기화
        alarmSettings = AlarmSettings(requireContext())

        setupClickListeners()
    }

    private fun setupClickListeners() {
        // 알림 설정
        binding.menuAlarmSettings.setOnClickListener {
            navigateToAlarmSettings()
        }
        // 알림 기록
        binding.menuAlarmHistory.setOnClickListener {
            navigateToAlarmHistory()
        }

        // 이용약관
        binding.menuTerms.setOnClickListener {
            showToast("이용약관")
        }
        // 개인정보처리방침
        binding.menuPrivacy.setOnClickListener {
            showToast("개인정보처리방침")
        }
        // 로그아웃
        binding.menuLogout.setOnClickListener {
            showToast("로그아웃 되었습니다")
        }
    }

    private fun navigateToAlarmSettings() {
        showToast("알림 설정 화면으로 이동 ")
    }

    private fun navigateToAlarmHistory() {
        showToast("알림 기록 화면으로 이동")
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}