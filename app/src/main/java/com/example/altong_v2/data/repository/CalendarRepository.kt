package com.example.altong_v2.data.repository

import com.example.altong_v2.data.local.dao.DrugCompletionDao
import com.example.altong_v2.data.local.dao.DrugDao
import com.example.altong_v2.data.local.dao.PrescriptionDao
import com.example.altong_v2.data.local.entity.DrugCompletionEntity
import com.example.altong_v2.data.local.entity.DrugEntity
import com.example.altong_v2.data.local.entity.PrescriptionEntity
import com.example.altong_v2.data.model.CalendarDayData
import com.example.altong_v2.data.model.DrugItem
import com.example.altong_v2.data.model.CalendarPrescription
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * 캘린더 Repository
 * 날짜별 복용 약 및 완료 기록 관리
 */
class CalendarRepository(
    private val drugDao: DrugDao,
    private val drugCompletionDao: DrugCompletionDao,
    private val prescriptionDao: PrescriptionDao
) {
    // 모든 약 조회
    val allDrugs: Flow<List<DrugEntity>> = drugDao.getAllDrugs()

    // ========== 복용 완료 기록 ==========

    /**
     * 특정 날짜의 복용 기록 조회
     */
    fun getCompletionsByDate(date: String): Flow<List<DrugCompletionEntity>> {
        return drugCompletionDao.getCompletionsByDate(date)
    }

    /**
     * 개별 약의 복용 완료 토글
     * 체크박스 클릭 시 호출
     */
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

    /**
     * 특정 날짜의 완료율 계산
     * @return Pair(완료한 약 개수, 전체 약 개수)
     */
    suspend fun getCompletionRate(date: String): Pair<Int, Int> {
        val completed = drugCompletionDao.getCompletedCount(date)
        val total = drugCompletionDao.getTotalCount(date)
        return Pair(completed, total)
    }

    /**
     * 날짜 범위의 복용 기록 조회
     */
    fun getCompletionsByDateRange(startDate: String, endDate: String): Flow<List<DrugCompletionEntity>> {
        return drugCompletionDao.getCompletionsByDateRange(startDate, endDate)
    }

    // ========== 날짜별 복용할 약 계산 ==========

    /**
     * 특정 날짜에 복용해야 할 약 리스트 계산
     * @param date 조회할 날짜 (YYYY-MM-DD)
     * @return 해당 날짜의 처방전별 약 리스트
     */
    suspend fun getDrugsForDate(date: String): CalendarDayData {
        // 1. 모든 처방전 가져오기
        val allPrescriptions = prescriptionDao.getAllPrescriptions().first()

        // 2. 날짜에 해당하는 처방전들을 처리
        val prescriptionsWithDrugs = mutableListOf<CalendarPrescription>()

        for (prescription in allPrescriptions) {
            // 3. 각 처방전의 약들 가져오기
            val drugs = drugDao.getDrugsByPrescription(prescription.id).first()

            // 4. 해당 날짜에 복용해야 하는 약만 필터링
            val validDrugs = drugs.filter { drug ->
                isDateInPrescriptionRange(
                    targetDate = date,
                    prescriptionDate = prescription.date,
                    days = drug.days
                )
            }

            // 약이 있는 처방전만 추가
            if (validDrugs.isNotEmpty()) {
                // 5. 시간대별로 약 그룹핑
                val drugsByTimeSlot = groupDrugsByTimeSlot(validDrugs, date)

                // 6. 최대 처방 일수 계산 (이 처방전의 약들 중 가장 긴 처방 일수)
                val maxDays = validDrugs.maxOf { it.days }

                prescriptionsWithDrugs.add(
                    CalendarPrescription(
                        prescriptionId = prescription.id,
                        diagnosis = prescription.diagnosis,
                        prescriptionDate = prescription.date,
                        totalDays = maxDays,
                        drugCount = validDrugs.size,
                        drugsByTimeSlot = drugsByTimeSlot
                    )
                )
            }
        }

        return CalendarDayData(
            date = date,
            prescriptions = prescriptionsWithDrugs
        )
    }

    /**
     * 월간 캘린더에서 약이 있는 날짜들 조회
     * @param year 연도
     * @param month 월 (1-12)
     * @return 약이 있는 날짜들의 Set (YYYY-MM-DD)
     */
    suspend fun getMonthlyDrugDates(year: Int, month: Int): Set<String> {
        val drugDates = mutableSetOf<String>()

        // 1. 모든 처방전 가져오기
        val allPrescriptions = prescriptionDao.getAllPrescriptions().first()

        // 2. 해당 월의 첫날과 마지막날 계산
        val firstDay = LocalDate.of(year, month, 1)
        val lastDay = firstDay.plusMonths(1).minusDays(1)

        // 3. 각 처방전의 약들을 확인
        for (prescription in allPrescriptions) {
            val drugs = drugDao.getDrugsByPrescription(prescription.id).first()

            for (drug in drugs) {
                // 약의 처방 시작일과 종료일 계산
                val startDate = LocalDate.parse(prescription.date, DateTimeFormatter.ISO_DATE)
                val endDate = startDate.plusDays(drug.days.toLong() - 1)

                // 해당 월과 겹치는 날짜들을 추가
                var currentDate = maxOf(startDate, firstDay)
                val finalDate = minOf(endDate, lastDay)

                while (!currentDate.isAfter(finalDate)) {
                    drugDates.add(currentDate.format(DateTimeFormatter.ISO_DATE))
                    currentDate = currentDate.plusDays(1)
                }
            }
        }

        return drugDates
    }

    /**
     * 특정 처방전의 모든 약 일괄 체크/해제
     * 진단명 체크박스 클릭 시 호출
     */
    suspend fun togglePrescriptionCompletion(
        prescriptionId: Long,
        date: String,
        isCompleted: Boolean
    ) {
        // 해당 처방전의 모든 약 가져오기
        val drugs = drugDao.getDrugsByPrescription(prescriptionId).first()

        // 각 약에 대해 복용 완료 상태 업데이트
        for (drug in drugs) {
            // 해당 날짜에 복용해야 하는 약인지 확인
            val prescription = prescriptionDao.getPrescriptionById(prescriptionId)
            prescription?.let {
                if (isDateInPrescriptionRange(date, it.date, drug.days)) {
                    // 기존 기록 확인
                    val existing = drugCompletionDao.getCompletion(drug.id, date)

                    if (existing == null) {
                        // 기록 없으면 새로 생성
                        if (isCompleted) {
                            drugCompletionDao.insert(
                                DrugCompletionEntity(
                                    drugId = drug.id,
                                    date = date,
                                    isCompleted = true,
                                    completedAt = System.currentTimeMillis()
                                )
                            )
                        }
                    } else {
                        // 기록 있으면 업데이트
                        val completedAt = if (isCompleted) System.currentTimeMillis() else null
                        drugCompletionDao.updateCompletion(drug.id, date, isCompleted, completedAt)
                    }
                }
            }
        }
    }

    /**
     * 특정 날짜의 모든 약 일괄 체크/해제
     * "모두 복용 완료" 버튼 클릭 시 호출
     */
    suspend fun toggleAllDrugsForDate(date: String, isCompleted: Boolean) {
        // 해당 날짜에 복용해야 할 모든 약 조회
        val dayData = getDrugsForDate(date)

        // 모든 처방전의 모든 약에 대해 처리
        for (prescription in dayData.prescriptions) {
            for (drugList in prescription.drugsByTimeSlot.values) {
                for (drugItem in drugList) {
                    // 기존 기록 확인
                    val existing = drugCompletionDao.getCompletion(drugItem.drugId, date)

                    if (existing == null) {
                        // 기록 없으면 새로 생성
                        if (isCompleted) {
                            drugCompletionDao.insert(
                                DrugCompletionEntity(
                                    drugId = drugItem.drugId,
                                    date = date,
                                    isCompleted = true,
                                    completedAt = System.currentTimeMillis()
                                )
                            )
                        }
                    } else {
                        // 기록 있으면 업데이트
                        val completedAt = if (isCompleted) System.currentTimeMillis() else null
                        drugCompletionDao.updateCompletion(drugItem.drugId, date, isCompleted, completedAt)
                    }
                }
            }
        }
    }

    // ========== 내부 유틸리티 메서드 ==========

    /**
     * 약들을 시간대별로 그룹핑
     * @param drugs 약 리스트
     * @param date 체크 상태를 조회할 날짜
     * @return Map<시간대, 약 리스트>
     */
    private suspend fun groupDrugsByTimeSlot(
        drugs: List<DrugEntity>,
        date: String
    ): Map<String, List<DrugItem>> {
        val grouped = mutableMapOf<String, MutableList<DrugItem>>()

        for (drug in drugs) {
            // timeSlots 파싱 ("아침,점심,저녁" → ["아침", "점심", "저녁"])
            val timeSlots = drug.timeSlots.split(",").map { it.trim() }

            // 남은 일수 계산
            val prescription = prescriptionDao.getPrescriptionById(drug.prescriptionId)
            val remainingDays = prescription?.let {
                calculateRemainingDays(it.date, drug.days, date)
            } ?: 0

            // 체크 상태 조회
            val completion = drugCompletionDao.getCompletion(drug.id, date)
            val isCompleted = completion?.isCompleted ?: false

            // DrugItem 생성
            val drugItem = DrugItem(
                drugId = drug.id,
                drugName = drug.name,
                dosage = drug.dosage,
                timing = drug.timing,
                remainingDays = remainingDays,
                isCompleted = isCompleted
            )

            // 각 시간대에 약 추가
            for (slot in timeSlots) {
                if (slot.isNotEmpty()) {
                    grouped.getOrPut(slot) { mutableListOf() }.add(drugItem)
                }
            }
        }

        return grouped
    }

    /**
     * 날짜가 처방 기간 내인지 체크
     * @param targetDate 확인할 날짜
     * @param prescriptionDate 처방일
     * @param days 처방 일수
     * @return 처방 기간 내이면 true
     */
    private fun isDateInPrescriptionRange(
        targetDate: String,
        prescriptionDate: String,
        days: Int
    ): Boolean {
        return try {
            val target = LocalDate.parse(targetDate, DateTimeFormatter.ISO_DATE)
            val start = LocalDate.parse(prescriptionDate, DateTimeFormatter.ISO_DATE)
            val end = start.plusDays(days.toLong() - 1)  // days - 1 (예: 7일분 = 0~6일)

            // 시작일 <= 대상 날짜 <= 종료일
            !target.isBefore(start) && !target.isAfter(end)
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 남은 복용 일수 계산
     * @param prescriptionDate 처방일
     * @param totalDays 총 처방 일수
     * @param currentDate 현재 날짜
     * @return 남은 일수
     */
    private fun calculateRemainingDays(
        prescriptionDate: String,
        totalDays: Int,
        currentDate: String
    ): Int {
        return try {
            val start = LocalDate.parse(prescriptionDate, DateTimeFormatter.ISO_DATE)
            val end = start.plusDays(totalDays.toLong() - 1)
            val current = LocalDate.parse(currentDate, DateTimeFormatter.ISO_DATE)

            // 종료일 - 현재 날짜 = 남은 일수
            val remaining = end.toEpochDay() - current.toEpochDay()
            maxOf(0, remaining.toInt())  // 음수 방지
        } catch (e: Exception) {
            0
        }
    }
}