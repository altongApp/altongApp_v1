package com.example.altong_v2.ui.calendar

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.altong_v2.data.local.AppDatabase
import com.example.altong_v2.data.model.CalendarDayData
import com.example.altong_v2.data.repository.CalendarRepository
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * 캘린더 ViewModel
 * 캘린더 화면의 데이터와 비즈니스 로직 관리
 */
class CalendarViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: CalendarRepository

    init {
        // Database 및 Repository 초기화
        val database = AppDatabase.getDatabase(application)
        repository = CalendarRepository(
            drugDao = database.drugDao(),
            drugCompletionDao = database.drugCompletionDao(),
            prescriptionDao = database.prescriptionDao()
        )
    }

    // ========== LiveData: UI에서 관찰할 데이터 ==========

    /**
     * 선택된 날짜의 약 데이터
     */
    private val _selectedDayData = MutableLiveData<CalendarDayData>()
    val selectedDayData: LiveData<CalendarDayData> = _selectedDayData

    /**
     * 약이 있는 날짜들 (캘린더에 점 표시용)
     */
    private val _drugDates = MutableLiveData<Set<String>>()
    val drugDates: LiveData<Set<String>> = _drugDates

    /**
     * 현재 선택된 날짜
     */
    private val _selectedDate = MutableLiveData<String>()
    val selectedDate: LiveData<String> = _selectedDate

    /**
     * 로딩 상태
     */
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // ========== 초기 설정 ==========

    init {
        // 오늘 날짜로 초기화
        val today = LocalDate.now().format(DateTimeFormatter.ISO_DATE)
        _selectedDate.value = today

        // 오늘 날짜의 약 정보 로드
        loadDrugsForDate(today)

        // 현재 월의 약 있는 날짜들 로드
        val now = LocalDate.now()
        loadMonthlyDrugDates(now.year, now.monthValue)
    }

    // ========== 날짜 선택 및 데이터 로드 ==========

    /**
     * 날짜 선택 시 호출
     * @param date 선택된 날짜 (YYYY-MM-DD)
     */
    fun selectDate(date: String) {
        _selectedDate.value = date
        loadDrugsForDate(date)
    }

    /**
     * 특정 날짜의 약 정보 로드
     */
    private fun loadDrugsForDate(date: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val data = repository.getDrugsForDate(date)
                _selectedDayData.value = data
            } catch (e: Exception) {
                e.printStackTrace()
                // 에러 발생 시 빈 데이터
                _selectedDayData.value = CalendarDayData(date, emptyList())
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * 월간 캘린더의 약 있는 날짜들 로드
     * @param year 연도
     * @param month 월 (1-12)
     */
    fun loadMonthlyDrugDates(year: Int, month: Int) {
        viewModelScope.launch {
            try {
                val dates = repository.getMonthlyDrugDates(year, month)
                _drugDates.value = dates
            } catch (e: Exception) {
                e.printStackTrace()
                _drugDates.value = emptySet()
            }
        }
    }

    // ========== 복용 완료 체크 기능 ==========

    /**
     * 개별 약의 체크박스 토글
     * @param drugId 약 ID
     * @param timeSlot 시간대 (예: "아침", "점심")
     */
    fun toggleDrugCompletion(drugId: Long, timeSlot: String) {
        val date = _selectedDate.value ?: return

        viewModelScope.launch {
            try {
                // ✅ date에 시간대 포함해서 전달
                val dateWithSlot = "$date-$timeSlot"
                repository.toggleCompletion(drugId, dateWithSlot)
                // 데이터 새로고침
                loadDrugsForDate(date)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * 특정 처방전의 모든 약 일괄 체크/해제
     * 진단명 체크박스 클릭 시 호출
     * @param prescriptionId 처방전 ID
     * @param isCompleted 체크 상태
     */
    fun togglePrescriptionCompletion(prescriptionId: Long, isCompleted: Boolean) {
        val date = _selectedDate.value ?: return

        viewModelScope.launch {
            try {
                repository.togglePrescriptionCompletion(prescriptionId, date, isCompleted)
                // 데이터 새로고침
                loadDrugsForDate(date)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * 선택된 날짜의 모든 약 일괄 체크/해제
     * "모두 복용 완료" 버튼 클릭 시 호출
     * @param isCompleted 체크 상태
     */
    fun toggleAllDrugs(isCompleted: Boolean) {
        val date = _selectedDate.value ?: return

        viewModelScope.launch {
            try {
                repository.toggleAllDrugsForDate(date, isCompleted)
                // 데이터 새로고침
                loadDrugsForDate(date)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // ========== 유틸리티 메서드 ==========

    /**
     * 현재 선택된 날짜의 모든 약이 완료되었는지 확인
     * "모두 복용 완료" vs "체크 해제" 버튼 텍스트 결정용
     * @return true면 모두 완료, false면 미완료 있음
     */
    fun areAllDrugsCompleted(): Boolean {
        val data = _selectedDayData.value ?: return false

        // 모든 처방전의 모든 약을 확인
        for (prescription in data.prescriptions) {
            for (drugList in prescription.drugsByTimeSlot.values) {
                for (drug in drugList) {
                    if (!drug.isCompleted) {
                        return false  // 하나라도 미완료면 false
                    }
                }
            }
        }

        return true  // 모두 완료
    }

    /**
     * 특정 처방전의 모든 약이 완료되었는지 확인
     * 진단명 체크박스 상태 결정용
     * @param prescriptionId 처방전 ID
     * @return true면 모두 완료, false면 미완료 있음
     */
    fun arePrescriptionDrugsCompleted(prescriptionId: Long): Boolean {
        val data = _selectedDayData.value ?: return false

        // 해당 처방전 찾기
        val prescription = data.prescriptions.find { it.prescriptionId == prescriptionId }
            ?: return false

        // 해당 처방전의 모든 약 확인
        for (drugList in prescription.drugsByTimeSlot.values) {
            for (drug in drugList) {
                if (!drug.isCompleted) {
                    return false  // 하나라도 미완료면 false
                }
            }
        }

        return true  // 모두 완료
    }

    /**
     * 현재 선택된 날짜가 오늘인지 확인
     */
    fun isToday(): Boolean {
        val today = LocalDate.now().format(DateTimeFormatter.ISO_DATE)
        return _selectedDate.value == today
    }

    /**
     * 날짜를 읽기 좋은 형식으로 변환
     * @param date "2024-12-10"
     * @return "12월 10일 (화)"
     */
    fun formatDateForDisplay(date: String): String {
        return try {
            val localDate = LocalDate.parse(date, DateTimeFormatter.ISO_DATE)
            val dayOfWeek = when (localDate.dayOfWeek.value) {
                1 -> "월"
                2 -> "화"
                3 -> "수"
                4 -> "목"
                5 -> "금"
                6 -> "토"
                7 -> "일"
                else -> ""
            }
            "${localDate.monthValue}월 ${localDate.dayOfMonth}일 ($dayOfWeek)"
        } catch (e: Exception) {
            date
        }
    }
}