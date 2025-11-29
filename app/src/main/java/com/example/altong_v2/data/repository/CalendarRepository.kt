package com.example.altong_v2.data.repository

import com.example.altong_v2.data.local.dao.DrugCompletionDao
import com.example.altong_v2.data.local.dao.DrugDao
import com.example.altong_v2.data.local.dao.PrescriptionDao
import com.example.altong_v2.data.local.entity.DrugCompletionEntity
import com.example.altong_v2.data.local.entity.DrugEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.format.DateTimeFormatter


/* * 캘린더 Repository
 * 날짜별 복용 약품 및 완료 기록 관리
 * TODO (민주가): 캘린더 로직 구현*/

class CalendarRepository(
    private val drugDao: DrugDao,
    private val drugCompletionDao: DrugCompletionDao,
    private val prescriptionDao: PrescriptionDao
) {
    // 모든 약품 조회
    val allDrugs: Flow<List<DrugEntity>> = drugDao.getAllDrugs()

    // ========== 복용 완료 기록 ==========
    // 특정 날짜의 복용 기록 조회
    fun getCompletionsByDate(date: String): Flow<List<DrugCompletionEntity>> {
        return drugCompletionDao.getCompletionsByDate(date)
    }

    // 복용 완료 토글
    suspend fun toggleCompletion(drugId: Long, date: String) {
        val existing = drugCompletionDao.getCompletion(drugId, date)

        if (existing == null) {
            // 기록 없으면 새로 추가 (완료로)
            val completion = DrugCompletionEntity(
                drugId = drugId,
                date = date,
                isCompleted = true,
                completedAt = System.currentTimeMillis()
            )
            drugCompletionDao.insert(completion)
        } else {
            // 기록 있으면 토글
            val newStatus = !existing.isCompleted
            val completedAt = if (newStatus) System.currentTimeMillis() else null
            drugCompletionDao.updateCompletion(drugId, date, newStatus, completedAt)
        }
    }

    // 특정 날짜의 완료율 계산
    suspend fun getCompletionRate(date: String): Pair<Int, Int> {
        val completed = drugCompletionDao.getCompletedCount(date)
        val total = drugCompletionDao.getTotalCount(date)
        return Pair(completed, total)
    }

    // 날짜 범위의 복용 기록 조회
    fun getCompletionsByDateRange(startDate: String, endDate: String): Flow<List<DrugCompletionEntity>> {
        return drugCompletionDao.getCompletionsByDateRange(startDate, endDate)
    }

    // ========== 날짜별 복용할 약품 계산 (민주가 구현) ==========
    // TODO (팀원): 특정 날짜에 복용해야 할 약품 리스트 계산
    // suspend fun getDrugsForDate(date: String): List<DrugEntity> {
    //     // 1. 모든 처방전 가져오기
    //     // 2. 각 처방전의 약품 가져오기
    //     // 3. 날짜 범위 체크 (처방일 + 총 처방 일수)
    //     // 4. 해당 날짜에 복용해야 하는 약품만 필터링
    // }

    // TODO (팀원): 월간 캘린더 데이터 생성
    // suspend fun getMonthlyCalendarData(year: Int, month: Int): List<CalendarDay> {
    //     // 1. 해당 월의 모든 날짜 생성
    //     // 2. 각 날짜별로 복용할 약품 개수 계산
    //     // 3. 완료한 약품 개수 계산
    //     // 4. CalendarDay 객체 리스트 반환
    // }

    // ========== 유틸리티 메서드 ==========
    // 날짜가 처방 기간 내인지 체크
    private fun isDateInPrescriptionRange(
        targetDate: String,
        prescriptionDate: String,
        days: Int
    ): Boolean {
        // TODO (팀원): LocalDate를 사용한 날짜 계산
        return try {
            val target = LocalDate.parse(targetDate, DateTimeFormatter.ISO_DATE)
            val start = LocalDate.parse(prescriptionDate, DateTimeFormatter.ISO_DATE)
            val end = start.plusDays(days.toLong())

            !target.isBefore(start) && !target.isAfter(end)
        } catch (e: Exception) {
            false
        }
    }
}