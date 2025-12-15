package com.example.altong_v2.ui.mypage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.altong_v2.databinding.FragmentAlarmHistoryBinding
import java.util.Calendar

/* 알림 기록 Fragment */
class AlarmHistoryFragment : Fragment() {
    private var _binding: FragmentAlarmHistoryBinding? = null
    private val binding get() = _binding!!
    private lateinit var todayAdapter: AlarmHistoryAdapter
    private lateinit var yesterdayAdapter: AlarmHistoryAdapter
    private lateinit var previousAdapter: AlarmHistoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAlarmHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupClickListeners()
        setupRecyclerViews()
        loadAlarmHistory()
    }
    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }
    private fun setupRecyclerViews() {
        // 오늘
        todayAdapter = AlarmHistoryAdapter()
        binding.rvToday.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = todayAdapter
        }
        // 어제
        yesterdayAdapter = AlarmHistoryAdapter()
        binding.rvYesterday.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = yesterdayAdapter
        }
        // 이전 기록
        previousAdapter = AlarmHistoryAdapter()
        binding.rvPrevious.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = previousAdapter
        }
    }

/*     * 알림 기록 불러오기 - 임시 */
    private fun loadAlarmHistory() {
        // 더미 데이터 생성 (테스트용)
        val todayList = getDummyTodayData()
        val yesterdayList = getDummyYesterdayData()

        if (todayList.isEmpty() && yesterdayList.isEmpty()) {
            // 빈 상태 표시
            showEmptyState()
        } else {
            // 데이터 표시
            showContent(todayList, yesterdayList)
        }
    }

    private fun showEmptyState() {
        binding.emptyState.visibility = View.VISIBLE
        binding.contentContainer.visibility = View.GONE
    }
    private fun showContent(
        todayList: List<AlarmHistory>,
        yesterdayList: List<AlarmHistory>
    ) {
        binding.emptyState.visibility = View.GONE
        binding.contentContainer.visibility = View.VISIBLE

        // 오늘 섹션
        if (todayList.isNotEmpty()) {
            binding.tvTodayLabel.visibility = View.VISIBLE
            binding.cardToday.visibility = View.VISIBLE
            todayAdapter.submitList(todayList)
        }
        // 어제 섹션
        if (yesterdayList.isNotEmpty()) {
            binding.tvYesterdayLabel.visibility = View.VISIBLE
            binding.cardYesterday.visibility = View.VISIBLE
            yesterdayAdapter.submitList(yesterdayList)
        }
    }

    /**
     * 더미 데이터 - 오늘
     */
    private fun getDummyTodayData(): List<AlarmHistory> {
        val calendar = Calendar.getInstance()
        val today = calendar.timeInMillis

        return listOf(
            AlarmHistory(
                id = 1,
                timeSlot = "morning",
                time = "08:00",
                medicines = listOf("타이레놀정", "코푸시럽"),
                isCompleted = true,
                timestamp = today
            ),
            AlarmHistory(
                id = 2,
                timeSlot = "lunch",
                time = "12:00",
                medicines = listOf("타이레놀정"),
                isCompleted = false,
                timestamp = today
            )
        )
    }

    /**
     * 더미 데이터 - 어제
     */
    private fun getDummyYesterdayData(): List<AlarmHistory> {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_MONTH, -1)
        val yesterday = calendar.timeInMillis

        return listOf(
            AlarmHistory(
                id = 3,
                timeSlot = "dinner",
                time = "18:00",
                medicines = listOf("타이레놀정", "코푸시럽", "무코펙트정"),
                isCompleted = true,
                timestamp = yesterday
            )
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}