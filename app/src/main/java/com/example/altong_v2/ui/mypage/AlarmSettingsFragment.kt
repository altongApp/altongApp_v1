package com.example.altong_v2.ui.mypage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.altong_v2.databinding.FragmentAlarmSettingsBinding

/*
~~ 알림 설정 Fragment ~~
- 복약 알림 ON/OFF
- 처방 종료 알림 ON/OFF
- 시간대별 시간 설정 (아침/점심/저녁/취침)
*/

class AlarmSettingsFragment : Fragment() {
    private var _binding: FragmentAlarmSettingsBinding? = null
    private val binding get() = _binding!!
    private lateinit var alarmSettings: AlarmSettings

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAlarmSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        alarmSettings = AlarmSettings(requireContext())

        loadSettings()
        setupClickListeners()
        setupSwitches()
    }
    //저장된 설정 불러오기
    private fun loadSettings() {
        // 스위치 상태
        binding.switchAlarmEnabled.isChecked = alarmSettings.isAlarmEnabled
        binding.switchEndAlarmEnabled.isChecked = alarmSettings.isEndAlarmEnabled

        // 시간 표시
        binding.tvMorningTime.text = alarmSettings.morningTime
        binding.tvLunchTime.text = alarmSettings.lunchTime
        binding.tvDinnerTime.text = alarmSettings.dinnerTime
        binding.tvBedtimeTime.text = alarmSettings.bedtimeTime
    }


    //클릭 리스너 설정
    private fun setupClickListeners() {
        // 뒤로가기
        binding.btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        // 시간 설정 메뉴
        binding.menuMorningTime.setOnClickListener {
            navigateToTimeSetting("morning", "아침")
        }
        binding.menuLunchTime.setOnClickListener {
            navigateToTimeSetting("lunch", "점심")
        }
        binding.menuDinnerTime.setOnClickListener {
            navigateToTimeSetting("dinner", "저녁")
        }
        binding.menuBedtimeTime.setOnClickListener {
            navigateToTimeSetting("bedtime", "취침 전")
        }
    }


    //스위치 리스너 설정
    private fun setupSwitches() {
        // 복약 알림
        binding.switchAlarmEnabled.setOnCheckedChangeListener { _, isChecked ->
            alarmSettings.isAlarmEnabled = isChecked
            // TODO: Step 20에서 알람 재등록/취소 로직 추가
        }
        // 처방 종료 알림
        binding.switchEndAlarmEnabled.setOnCheckedChangeListener { _, isChecked ->
            alarmSettings.isEndAlarmEnabled = isChecked
            // TODO: Step 20에서 알람 재등록/취소 로직 추가
        }
    }


    // 시간 설정 화면으로 이동
    private fun navigateToTimeSetting(timeSlot: String, timeLabel: String) {
        val fragment = TimeSettingFragment.newInstance(timeSlot, timeLabel)
        parentFragmentManager.beginTransaction()
            .replace(android.R.id.content, fragment)
            .addToBackStack(null)
            .commit()
    }


    // 화면 다시 보일 때 (시간 설정에서 돌아올 때)
    override fun onResume() {
        super.onResume()
        loadSettings() // 변경된 시간 다시 불러오기
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}