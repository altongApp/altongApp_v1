package com.example.altong_v2.ui.alarm

import android.R.attr.description
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.altong_v2.R
import com.example.altong_v2.MainActivity

/*
 * 알림 Helper 클래스
 * 복약 알림을 생성하고 표시
 */
class NotificationHelper(private val context: Context) {
    companion object {
        private const val TAG = "NotificationHelper"
        private const val CHANNEL_ID = "medication_alarm_channel"
        private const val CHANNEL_NAME = "복약 알림"
        private const val CHANNEL_DESCRIPTION = "약 복용 시간을 알려드립니다"

        const val EXTRA_PRESCRIPTION_ID = "prescription_id"
        const val EXTRA_DRUG_ID = "drug_id"
        const val EXTRA_DRUG_NAME = "drug_name"
        const val EXTRA_TIME_SLOT = "time_slot"
        const val EXTRA_SCHEDULED_DATE = "scheduled_date"
        const val EXTRA_SHOW_ALARM_CONFIRM = "show_alarm_confirm"
    }
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    init { createNotificationChannel() }

    // 알림 채널 생성 (Android 8.0+)
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH  // 소리 + 헤드업 알림
            ).apply {
                description = CHANNEL_DESCRIPTION
                enableVibration(true)  // 진동 활성화
                vibrationPattern = longArrayOf(0, 500, 250, 500)  // 진동 패턴
                setShowBadge(true)  // 뱃지 표시
            }
            notificationManager.createNotificationChannel(channel)
            Log.d(TAG, "알림 채널 생성: $CHANNEL_ID")
        }
    }

    fun showMedicationNotification(
        prescriptionId: Long,
        drugId: Long,
        drugName: String,
        timeSlot: String,
        diagnosis: String,
        scheduledDate: Long
    ) {
        Log.d(TAG, "알림 표시 시작: drug=$drugName, slot=$timeSlot")

        // 알림 ID (고유값)
        val notificationId = generateNotificationId(prescriptionId, timeSlot)
        // 시간대 아이콘 및 라벨
        val (icon, label) = getTimeSlotInfo(timeSlot)
        // 알림 제목
        val title = "$icon $label 약 드실 시간입니다!"
        // 알림 내용
        val content = buildString {
            append("📋 $diagnosis\n")
            append("💊 $drugName")
        }

        val intent = Intent(context, com.example.altong_v2.MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_PRESCRIPTION_ID, prescriptionId)
            putExtra(EXTRA_DRUG_ID, drugId)
            putExtra(EXTRA_DRUG_NAME, drugName)
            putExtra(EXTRA_TIME_SLOT, timeSlot)
            putExtra(EXTRA_SCHEDULED_DATE, scheduledDate)
            putExtra(EXTRA_SHOW_ALARM_CONFIRM, true)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 알림 빌드
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(content)
            .setStyle(NotificationCompat.BigTextStyle().bigText(content))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setVibrate(longArrayOf(0, 500, 250, 500))
            .setSound(android.provider.Settings.System.DEFAULT_NOTIFICATION_URI)
            .build()

        // 알림 표시
        notificationManager.notify(notificationId, notification)
        Log.d(TAG, "알림 표시 완료: notificationId=$notificationId")

        Log.d(TAG, "Intent 데이터:")
        Log.d(TAG, "  EXTRA_PRESCRIPTION_ID: $prescriptionId")
        Log.d(TAG, "  EXTRA_DRUG_ID: $drugId")
        Log.d(TAG, "  EXTRA_DRUG_NAME: $drugName")
        Log.d(TAG, "  EXTRA_TIME_SLOT: $timeSlot")
        Log.d(TAG, "  EXTRA_SCHEDULED_DATE: $scheduledDate")
        Log.d(TAG, "  EXTRA_SHOW_ALARM_CONFIRM: true")
        Log.d(TAG, "PendingIntent requestCode: $notificationId")
    }

    // 시간대 정보 가져오기
    private fun getTimeSlotInfo(timeSlot: String): Pair<String, String> {
        return when (timeSlot) {
            "morning" -> "🌅" to "아침"
            "lunch" -> "☀️" to "점심"
            "dinner" -> "🌙" to "저녁"
            "bedtime" -> "🛌" to "취침 전"
            else -> "⏰" to "알림"
        }
    }
    // 알림 ID 생성
    private fun generateNotificationId(prescriptionId: Long, timeSlot: String): Int {
        val timeSlotCode = when (timeSlot) {
            "morning" -> 1
            "lunch" -> 2
            "dinner" -> 3
            "bedtime" -> 4
            else -> 0
        }
        // prescriptionId + timeSlotCode
        return (prescriptionId * 10 + timeSlotCode).toInt()
    }
}