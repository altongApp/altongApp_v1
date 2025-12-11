package com.example.altong_v2.ui.calendar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.altong_v2.R
import com.example.altong_v2.databinding.FragmentCalendarBinding
import com.example.altong_v2.ui.prescription.AddPrescriptionStep1Fragment
import com.prolificinteractive.materialcalendarview.CalendarDay
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * 캘린더 Fragment
 * 월간 캘린더와 선택된 날짜의 약 리스트 표시
 */
class CalendarFragment : Fragment() {

    private var _binding: FragmentCalendarBinding? = null
    private val binding get() = _binding!!

    // ViewModel 초기화
    private val viewModel: CalendarViewModel by viewModels()

    // 처방전 리스트 어댑터
    private lateinit var prescriptionAdapter: PrescriptionCalendarAdapter

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

        setupRecyclerView()
        setupCalendarView()
        setupClickListeners()
        observeViewModel()
    }

    /**
     * RecyclerView 설정
     */
    private fun setupRecyclerView() {
        prescriptionAdapter = PrescriptionCalendarAdapter { prescriptionId, isCompleted ->
            // 진단명 체크박스 클릭 시 해당 처방전의 모든 약 체크/해제
            viewModel.togglePrescriptionCompletion(prescriptionId, isCompleted)
        }

        binding.rvPrescriptions.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = prescriptionAdapter
        }
    }

    /**
     * Material CalendarView 설정
     */
    private fun setupCalendarView() {
        // 캘린더 초기 설정
        binding.calendarView.apply {
            // 선택 모드 설정
            selectionMode = com.prolificinteractive.materialcalendarview.MaterialCalendarView.SELECTION_MODE_SINGLE

            // 오늘 날짜로 초기화
            selectedDate = CalendarDay.today()

            // 날짜 선택 리스너
            setOnDateChangedListener { _, date, _ ->
                onDateSelected(date)
            }

            // 월 변경 리스너
            setOnMonthChangedListener { _, date ->
                // 월이 바뀌면 해당 월의 약 있는 날짜들 다시 로드
                viewModel.loadMonthlyDrugDates(date.year, date.month)
            }
        }
    }

    /**
     * 클릭 리스너 설정
     */
    private fun setupClickListeners() {
        // 처방전 등록 버튼
        binding.btnAddPrescription.setOnClickListener {
            showAddPrescriptionDialog()
        }

        // 모두 복용 완료 / 체크 해제 버튼
        binding.btnToggleAll.setOnClickListener {
            onToggleAllClicked()
        }
    }

    /**
     * ViewModel 데이터 관찰
     */
    private fun observeViewModel() {
        // 선택된 날짜의 약 데이터 관찰
        viewModel.selectedDayData.observe(viewLifecycleOwner) { dayData ->
            if (dayData.prescriptions.isEmpty()) {
                // 약이 없을 때
                binding.rvPrescriptions.visibility = View.GONE
                binding.btnToggleAll.visibility = View.GONE
                binding.layoutEmpty.visibility = View.VISIBLE
            } else {
                // 약이 있을 때
                binding.rvPrescriptions.visibility = View.VISIBLE
                binding.btnToggleAll.visibility = View.VISIBLE
                binding.layoutEmpty.visibility = View.GONE

                // 어댑터에 데이터 전달
                prescriptionAdapter.submitList(dayData.prescriptions)

                // "모두 복용 완료" 버튼 텍스트 업데이트
                updateToggleAllButtonText()
            }
        }

        // 약이 있는 날짜들 관찰 (캘린더에 점 표시)
        viewModel.drugDates.observe(viewLifecycleOwner) { drugDates ->
            updateCalendarDecorators(drugDates)
        }

        // 선택된 날짜 관찰
        viewModel.selectedDate.observe(viewLifecycleOwner) { date ->
            // 날짜 텍스트 업데이트
            binding.tvSelectedDate.text = viewModel.formatDateForDisplay(date)

            // 캘린더에 선택 표시
            val localDate = LocalDate.parse(date, DateTimeFormatter.ISO_DATE)
            val calendarDay = CalendarDay.from(
                localDate.year,
                localDate.monthValue,
                localDate.dayOfMonth
            )
            binding.calendarView.selectedDate = calendarDay
        }

        // 로딩 상태 관찰
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }

    /**
     * 캘린더 데코레이터 업데이트 (날짜 꾸미기)
     * @param drugDates 약이 있는 날짜들
     */
    private fun updateCalendarDecorators(drugDates: Set<String>) {
        // String을 CalendarDay로 변환
        val calendarDays = drugDates.mapNotNull { dateStr ->
            try {
                val localDate = LocalDate.parse(dateStr, DateTimeFormatter.ISO_DATE)
                CalendarDay.from(
                    localDate.year,
                    localDate.monthValue,
                    localDate.dayOfMonth
                )
            } catch (e: Exception) {
                null
            }
        }.toSet()

        // 기존 데코레이터 제거
        binding.calendarView.removeDecorators()

        // 약이 있는 날짜에 연두색 점 표시
        val drugDotColor = requireContext().getColor(R.color.calendar_drug_green)
        binding.calendarView.addDecorator(
            DrugDatesDecorator(calendarDays, drugDotColor)
        )

        // 선택된 날짜 데코레이터
        val selectedDate = binding.calendarView.selectedDate
        val pinkColor = requireContext().getColor(R.color.calendar_selected_pink)
        binding.calendarView.addDecorator(
            SelectedDateDecorator(selectedDate, pinkColor, android.graphics.Color.WHITE)
        )

        // 오늘 날짜 데코레이터 (선택되지 않았을 때 테두리 표시)
        val today = CalendarDay.today()
        if (selectedDate != today) {
            val todayBorderColor = requireContext().getColor(R.color.primary_green_dark)
            binding.calendarView.addDecorator(
                TodayDecorator(today, todayBorderColor)
            )
        }
    }

    /**
     * 날짜 선택 시 호출
     * @param date 선택된 날짜
     */
    private fun onDateSelected(date: CalendarDay) {
        val dateStr = String.format(
            "%04d-%02d-%02d",
            date.year,
            date.month,
            date.day
        )
        viewModel.selectDate(dateStr)
    }

    /**
     * "모두 복용 완료" 버튼 텍스트 업데이트
     * 모두 완료 상태면 "체크 해제", 아니면 "모두 복용 완료"
     */
    private fun updateToggleAllButtonText() {
        val allCompleted = viewModel.areAllDrugsCompleted()
        binding.btnToggleAll.text = if (allCompleted) {
            "✓ 체크 해제"
        } else {
            "✓ 모두 복용 완료"
        }
    }

    /**
     * "모두 복용 완료" / "체크 해제" 버튼 클릭
     */
    private fun onToggleAllClicked() {
        val allCompleted = viewModel.areAllDrugsCompleted()
        // 현재 상태의 반대로 토글
        viewModel.toggleAllDrugs(!allCompleted)
    }

    /**
     * 처방전 등록 다이얼로그 표시
     */
    private fun showAddPrescriptionDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("처방전 등록")
            .setMessage("처방전 등록 화면으로 이동하시겠습니까?")
            .setPositiveButton("예") { _, _ ->
                navigateToAddPrescription()
            }
            .setNegativeButton("아니오", null)
            .show()
    }

    /**
     * 처방전 등록 화면으로 이동
     */
    private fun navigateToAddPrescription() {
        val fragment = AddPrescriptionStep1Fragment()
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}