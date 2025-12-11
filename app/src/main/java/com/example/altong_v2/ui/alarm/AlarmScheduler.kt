package com.example.altong_v2.ui.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.altong_v2.data.local.entity.DrugEntity
import com.example.altong_v2.ui.mypage.AlarmSettings
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/*
 * 알림 스케줄러
 * 처방전 약품 등록 시 AlarmManager에 알림을 예약
 */
class AlarmScheduler(private val context: Context) {
    companion object {
        private const val TAG = "AlarmScheduler"
        private const val REQUEST_CODE_BASE = 10000
    }
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val alarmSettings = AlarmSettings(context)


    // 처방전 약품 알림 등록
    fun scheduleMedicationAlarms(
        prescriptionId: Long,
        drug: DrugEntity,
        prescriptionDate: String
    ) {
        Log.d(TAG, "==================================================")
        Log.d(TAG, "알림 등록 시작: prescription=$prescriptionId, drug=${drug.name}")
        Log.d(TAG, "약품 timeSlots 원본: '${drug.timeSlots}'")

        // 알림이 비활성화되어 있으면 등록하지 않음
        if (!alarmSettings.isAlarmEnabled) {
            Log.d(TAG, "알림이 비활성화되어 있습니다")
            return
        }
        // 처방일 파싱
        val startDate = parseDate(prescriptionDate)
        if (startDate == null) {
            Log.e(TAG, "처방일 파싱 실패: $prescriptionDate")
            return
        }
        val totalDays = drug.days
        if (totalDays <= 0) {
            Log.e(TAG, "총 처방 일수가 올바르지 않습니다: ${drug.days}")
            return
        }

        // 시간대 파싱
        val timeSlots = parseTimeSlots(drug.timeSlots)
        Log.d(TAG, "파싱된 timeSlots: $timeSlots (개수: ${timeSlots.size})")
        if (timeSlots.isEmpty()) {
            Log.e(TAG, "알림 시간대가 설정되지 않았습니다")
            return
        }

        // 각 날짜마다 알림 등록
        for (dayOffset in 0 until totalDays) {
            val calendar = Calendar.getInstance().apply {
                time = startDate
                add(Calendar.DAY_OF_MONTH, dayOffset)
            }

            Log.d(TAG, "--- Day $dayOffset: ${calendar.time} ---")

            // 각 시간대마다 알림 등록
            timeSlots.forEach { timeSlot ->
                scheduleAlarmForTimeSlot(
                    prescriptionId = prescriptionId,
                    drug = drug,
                    calendar = calendar,
                    timeSlot = timeSlot,
                    dayOffset = dayOffset
                )
            }
        }
        Log.d(TAG, "알림 등록 완료: 총 ${totalDays * timeSlots.size}개")
        Log.d(TAG, "==================================================")
    }


    // 특정 시간대에 알림 등록
    private fun scheduleAlarmForTimeSlot(
        prescriptionId: Long,
        drug: DrugEntity,
        calendar: Calendar,
        timeSlot: String,
        dayOffset: Int
    ) {
        Log.d(TAG, "  → 시간대: '$timeSlot'")

        // AlarmSettings에서 설정된 시간 가져오기
        val time = alarmSettings.getTimeBySlot(timeSlot)
        Log.d(TAG, "    설정된 시간: $time")

        val timeParts = time.split(":")
        if (timeParts.size != 2) {
            Log.e(TAG, "    시간 형식 오류: $time")
            return
        }

        val hour = timeParts[0].toIntOrNull() ?: return
        val minute = timeParts[1].toIntOrNull() ?: return

        // 알림 시간 설정
        val alarmCalendar = calendar.clone() as Calendar
        alarmCalendar.set(Calendar.HOUR_OF_DAY, hour)
        alarmCalendar.set(Calendar.MINUTE, minute)
        alarmCalendar.set(Calendar.SECOND, 0)
        alarmCalendar.set(Calendar.MILLISECOND, 0)

        // 과거 시간이면 등록하지 않음
        if (alarmCalendar.timeInMillis <= System.currentTimeMillis()) {
            Log.d(TAG, "    과거 시간이므로 알림 등록 생략: ${alarmCalendar.time}")
            return
        }

        // Request Code 생성 (고유값)
        val requestCode = generateRequestCode(prescriptionId, dayOffset, timeSlot)
        Log.d(TAG, "    Request Code: $requestCode")

        // PendingIntent 생성
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_MEDICATION_ALARM
            putExtra(AlarmReceiver.EXTRA_PRESCRIPTION_ID, prescriptionId)
            putExtra(AlarmReceiver.EXTRA_DRUG_NAME, drug.name)
            putExtra(AlarmReceiver.EXTRA_TIME_SLOT, timeSlot)
            putExtra(AlarmReceiver.EXTRA_DIAGNOSIS, prescriptionId.toString())
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // AlarmManager에 등록
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    alarmCalendar.timeInMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    alarmCalendar.timeInMillis,
                    pendingIntent
                )
            }
            Log.d(TAG, "    ✅ 알림 등록 성공: ${alarmCalendar.time}")
        } catch (e: SecurityException) {
            Log.e(TAG, "    ❌ 알림 등록 실패 (권한 없음): ${e.message}")
        }
    }

    // 처방전 알림 전체 취소
    fun cancelMedicationAlarms(
        prescriptionId: Long,
        drug: DrugEntity,
        prescriptionDate: String
    ) {
        Log.d(TAG, "알림 취소 시작: prescription=$prescriptionId, drug=${drug.name}")

        val startDate = parseDate(prescriptionDate)
        if (startDate == null) {
            Log.e(TAG, "처방일 파싱 실패: $prescriptionDate")
            return
        }

        val totalDays = drug.days
        val timeSlots = parseTimeSlots(drug.timeSlots)

        // 각 날짜, 각 시간대의 알림 취소
        for (dayOffset in 0 until totalDays) {
            timeSlots.forEach { timeSlot ->
                val requestCode = generateRequestCode(prescriptionId, dayOffset, timeSlot)

                val intent = Intent(context, AlarmReceiver::class.java).apply {
                    action = AlarmReceiver.ACTION_MEDICATION_ALARM
                }
                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    requestCode,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                alarmManager.cancel(pendingIntent)
                Log.d(TAG, "알림 취소: requestCode=$requestCode")
            }
        }
        Log.d(TAG, "알림 취소 완료")
    }

    // 한글 -> 영어로 변환.
    // UI에서 한글을 사용하고 있어서, 영어로 변환..
    private fun normalizeTimeSlot(timeSlot: String): String {
        return when (timeSlot) {
            "아침" -> "morning"
            "점심" -> "lunch"
            "저녁" -> "dinner"
            "취침 전", "취침전" -> "bedtime"
            else -> timeSlot  // 이미 영어면 그대로
        }
    }

    // Request Code 생성 (고유값)
    private fun generateRequestCode(prescriptionId: Long, dayOffset: Int, timeSlot: String): Int {
        val timeSlotCode = when (timeSlot) {
            "morning" -> 1
            "lunch" -> 2
            "dinner" -> 3
            "bedtime" -> 4
            else -> 0
        }
        return (REQUEST_CODE_BASE + (prescriptionId % 100000) * 100 + dayOffset * 10 + timeSlotCode).toInt()
    }

    // 날짜 파싱 (yyyy-MM-dd)
    private fun parseDate(dateString: String): java.util.Date? {
        return try {
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dateString)
        } catch (e: Exception) {
            Log.e(TAG, "날짜 파싱 오류: $dateString", e)
            null
        }
    }
     // timeSlots 문자열 파싱
     //// "morning,dinner" -> ["morning", "dinner"]
    private fun parseTimeSlots(timeSlotsString: String): List<String> {
        return try {
            timeSlotsString
                .split(",")
                .map { it.trim() }
                .map { normalizeTimeSlot(it) }
                .filter { it.isNotEmpty() }
        } catch (e: Exception) {
            Log.e(TAG, "timeSlots 파싱 오류: $timeSlotsString", e)
            emptyList()
        }
    }
}