package com.example.altong_v2.ui.mypage

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.altong_v2.databinding.FragmentTimeSettingBinding
import java.util.Locale

/*
 * 시간 설정 Fragment
 * TimePicker를 사용하여 시간 선택
 */
class TimeSettingFragment : Fragment() {
    private var _binding: FragmentTimeSettingBinding? = null
    private val binding get() = _binding!!

    private lateinit var alarmSettings: AlarmSettings
    private lateinit var timeSlot: String
    private lateinit var timeLabel: String

    companion object {
        private const val ARG_TIME_SLOT = "time_slot"
        private const val ARG_TIME_LABEL = "time_label"

        /*
         - Fragment 생성 팩토리 메서드
         - @param timeSlot "morning", "lunch", "dinner", "bedtime"
         - @param timeLabel "아침", "점심", "저녁", "취침 전"
         */
        fun newInstance(timeSlot: String, timeLabel: String): TimeSettingFragment {
            val fragment = TimeSettingFragment()
            val args = Bundle()
            args.putString(ARG_TIME_SLOT, timeSlot)
            args.putString(ARG_TIME_LABEL, timeLabel)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        timeSlot = arguments?.getString(ARG_TIME_SLOT) ?: "morning"
        timeLabel = arguments?.getString(ARG_TIME_LABEL) ?: "아침"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTimeSettingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        alarmSettings = AlarmSettings(requireContext())

        setupUI()
        setupTimePicker()
        setupClickListeners()
    }

    private fun setupUI() {
        // 타이틀 설정
        binding.tvTitle.text = "$timeLabel 시간 설정"
        binding.tvTimeLabel.text = "$timeLabel 시간"
        val icon = when (timeSlot) {
            "morning" -> "🌅"
            "lunch" -> "☀️"
            "dinner" -> "🌙"
            "bedtime" -> "🛌"
            else -> "⏰"
        }
        binding.tvTimeIcon.text = icon
    }

     // TimePicker 설정
    private fun setupTimePicker() {
        // 24시간 형식 설정
        binding.timePicker.setIs24HourView(true)

        // 현재 저장된 시간 가져오기
        val currentTime = alarmSettings.getTimeBySlot(timeSlot)
        val (hour, minute) = currentTime.split(":").map { it.toInt() }

        // TimePicker에 현재 시간 설정
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            binding.timePicker.hour = hour
            binding.timePicker.minute = minute
        } else {
            @Suppress("DEPRECATION")
            binding.timePicker.currentHour = hour
            @Suppress("DEPRECATION")
            binding.timePicker.currentMinute = minute
        }
    }

    private fun setupClickListeners() {
        // 뒤로가기
        binding.btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
        // 저장 버튼
        binding.btnSave.setOnClickListener {
            saveTime()
        }
    }


     // 사용자가 선택한 시간 저장
    private fun saveTime() {
        // TimePicker에서 시간 가져오기
        val hour = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            binding.timePicker.hour
        } else {
            @Suppress("DEPRECATION")
            binding.timePicker.currentHour
        }

        val minute = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            binding.timePicker.minute
        } else {
            @Suppress("DEPRECATION")
            binding.timePicker.currentMinute
        }

        // "HH:mm" 형식으로 변환
        val timeString = String.format(Locale.getDefault(), "%02d:%02d", hour, minute)
        // SharedPreferences에 저장
        alarmSettings.setTimeBySlot(timeSlot, timeString)

        Toast.makeText(
            requireContext(),
            "저장되었습니다",
            Toast.LENGTH_SHORT
        ).show()

        // TODO: Step 20에서 알람 재등록 로직 추가
        // 뒤로가기
        parentFragmentManager.popBackStack()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}