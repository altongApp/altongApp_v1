package com.example.altong_v2.ui.mypage

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.altong_v2.data.repository.PrescriptionRepository
import com.example.altong_v2.ui.alarm.AlarmScheduler

/* 알림 설정 관리 클래스
SharedPreferences를 사용해서 알람관련 설정 저장/불러오기 */

class AlarmSettings(
    private val context: Context,
    private val repository: PrescriptionRepository? = null,
    private val alarmScheduler: AlarmScheduler? = null
) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )
    companion object {
        private const val TAG = "AlarmSettings"
        private const val PREFS_NAME = "alarm_settings"

        // 알림 ON/OFF
        private const val KEY_ALARM_ENABLED = "alarm_enabled"
        private const val KEY_END_ALARM_ENABLED = "end_alarm_enabled"

        // 시간 설정
        private const val KEY_MORNING_TIME = "morning_time"
        private const val KEY_LUNCH_TIME = "lunch_time"
        private const val KEY_DINNER_TIME = "dinner_time"
        private const val KEY_BEDTIME_TIME = "bedtime_time"

        // 기본값
        const val DEFAULT_MORNING = "08:00"
        const val DEFAULT_LUNCH = "12:00"
        const val DEFAULT_DINNER = "18:00"
        const val DEFAULT_BEDTIME = "22:00"
    }


// ~~~ 알림 on / off ~~
    // 복약 알림 활성화 여부
    var isAlarmEnabled: Boolean
        get() = prefs.getBoolean(KEY_ALARM_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_ALARM_ENABLED, value).apply()

    // 처방 종료 알림 활성화 여부
    var isEndAlarmEnabled: Boolean
        get() = prefs.getBoolean(KEY_END_ALARM_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_END_ALARM_ENABLED, value).apply()

// ~~ 시간 ~~
    // 아침 시간
    var morningTime: String
    get() = prefs.getString(KEY_MORNING_TIME, DEFAULT_MORNING) ?: DEFAULT_MORNING
    set(value) = prefs.edit().putString(KEY_MORNING_TIME, value).apply()

    // 점심 시간
    var lunchTime: String
        get() = prefs.getString(KEY_LUNCH_TIME, DEFAULT_LUNCH) ?: DEFAULT_LUNCH
        set(value) = prefs.edit().putString(KEY_LUNCH_TIME, value).apply()

    // 저녁 시간
    var dinnerTime: String
        get() = prefs.getString(KEY_DINNER_TIME, DEFAULT_DINNER) ?: DEFAULT_DINNER
        set(value) = prefs.edit().putString(KEY_DINNER_TIME, value).apply()

    // 취침 전 시간
    var bedtimeTime: String
        get() = prefs.getString(KEY_BEDTIME_TIME, DEFAULT_BEDTIME) ?: DEFAULT_BEDTIME
        set(value) = prefs.edit().putString(KEY_BEDTIME_TIME, value).apply()

    /* 유틸리티 메서드 */
    // 시간대에 해당하는 시간 가져오기
    fun getTimeBySlot(timeSlot: String): String {
        return when (timeSlot) {
            "morning", "아침" -> morningTime
            "lunch", "점심" -> lunchTime
            "dinner", "저녁" -> dinnerTime
            "bedtime", "취침 전", "취침전" -> bedtimeTime
            else -> DEFAULT_MORNING
        }
    }

    // 시간대에 해당하는 시간 설정하기
    fun setTimeBySlot(timeSlot: String, time: String) {
        when (timeSlot) {
            "morning", "아침" -> morningTime = time
            "lunch", "점심" -> lunchTime = time
            "dinner", "저녁" -> dinnerTime = time
            "bedtime", "취침 전", "취침전" -> bedtimeTime = time
        }
    }
    // 모든 설정 초기화
    fun reset() {
        prefs.edit().clear().apply()
    }

    /**
     * 시간 설정 + 알람 자동 재등록
     * @param timeSlot "morning", "lunch", "dinner", "bedtime"
     * @param time "HH:mm" 형식 (예: "09:00")
     */
    suspend fun setTimeAndReschedule(timeSlot: String, time: String) {
        Log.d(TAG, "==================================================")
        Log.d(TAG, "⏰ 시간 변경 시작: $timeSlot = $time")

        // 1. 시간 저장 (기존 로직)
        setTimeBySlot(timeSlot, time)
        Log.d(TAG, "✅ SharedPreferences 저장 완료")

        // 2. Repository나 AlarmScheduler가 없으면 여기서 종료
        if (repository == null || alarmScheduler == null) {
            Log.w(TAG, "⚠️ Repository 또는 AlarmScheduler가 없어서 알람 재등록 건너뜀")
            Log.d(TAG, "==================================================")
            return
        }

        // 3. 영어 시간대를 한글로 변환
        val koreanTimeSlot = convertTimeSlotToKorean(timeSlot)
        Log.d(TAG, "🔍 시간대 변환: '$timeSlot' → '$koreanTimeSlot'")

        // 4. 모든 처방전과 약품 가져오기
        try {
            val allPrescriptions = repository.getAllPrescriptionsWithDrugs()
            Log.d(TAG, "📦 조회된 처방전: ${allPrescriptions.size}개")

            var rescheduledCount = 0

            // 5. 각 처방전의 약품 확인
            allPrescriptions.forEach { prescriptionWithDrugs ->
                val prescription = prescriptionWithDrugs.prescription
                val drugs = prescriptionWithDrugs.drugs

                drugs.forEach { drug ->
                    // 이 약품이 변경된 시간대를 사용하는지 확인
                    if (drug.timeSlots.contains(koreanTimeSlot)) {
                        Log.d(TAG, "🔄 알람 재등록 대상: ${drug.name} (처방전 ${prescription.id})")

                        try {
                            // 기존 알람 취소
                            alarmScheduler.cancelMedicationAlarms(prescription.id, drug, prescription.date)
                            Log.d(TAG, "   ❌ 기존 알람 취소 완료")

                            // ✅ 새 시간으로 알람 재등록 (3개 파라미터만!)
                            alarmScheduler.scheduleMedicationAlarms(
                                prescriptionId = prescription.id,
                                drug = drug,
                                prescriptionDate = prescription.date
                            )
                            Log.d(TAG, "   ✅ 새 알람 등록 완료")

                            rescheduledCount++
                        } catch (e: Exception) {
                            Log.e(TAG, "   ❌ 알람 재등록 실패: ${drug.name}", e)
                        }
                    }
                }
            }

            Log.d(TAG, "✅ 총 ${rescheduledCount}개 약품 알람 재등록 완료")

        } catch (e: Exception) {
            Log.e(TAG, "❌ 처방전 조회 실패", e)
        }

        Log.d(TAG, "==================================================")
    }

    /**
     * 시간대 변환: 영어 → 한글
     */
    private fun convertTimeSlotToKorean(timeSlot: String): String {
        return when (timeSlot) {
            "morning" -> "아침"
            "lunch" -> "점심"
            "dinner" -> "저녁"
            "bedtime" -> "취침 전"
            else -> timeSlot
        }
    }
}