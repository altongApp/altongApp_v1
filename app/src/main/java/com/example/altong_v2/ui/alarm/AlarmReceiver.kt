package com.example.altong_v2.ui.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

/*
 * 알림 BroadcastReceiver
 * AlarmManager에서 설정한 시간에 호출됨
*/

class AlarmReceiver : BroadcastReceiver() {
    companion object {
        private const val TAG = "AlarmReceiver"
        const val ACTION_MEDICATION_ALARM = "com.example.altong_v2.MEDICATION_ALARM"

        // Intent Extra Keys
        const val EXTRA_PRESCRIPTION_ID = "prescription_id"
        const val EXTRA_DRUG_ID = "drug_id"  // ✅ drugId 추가
        const val EXTRA_DRUG_NAME = "drug_name"
        const val EXTRA_TIME_SLOT = "time_slot"
        const val EXTRA_DIAGNOSIS = "diagnosis"
        const val EXTRA_SCHEDULED_DATE = "scheduled_date"
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "onReceive: action=${intent.action}")

        when (intent.action) {
            ACTION_MEDICATION_ALARM -> {
                handleMedicationAlarm(context, intent)
            }
            Intent.ACTION_BOOT_COMPLETED -> {
                // 디바이스 재부팅 시 알림 재등록
                Log.d(TAG, "Boot completed - 알림 재등록 필요")
                // TODO: 모든 활성 처방전 알림 재등록
            }
        }
    }

    // 복약 알림 처리
    private fun handleMedicationAlarm(context: Context, intent: Intent) {
        val prescriptionId = intent.getLongExtra(EXTRA_PRESCRIPTION_ID, -1)
        val drugId = intent.getLongExtra(EXTRA_DRUG_ID, -1)  // ✅ drugId 추출
        val drugName = intent.getStringExtra(EXTRA_DRUG_NAME) ?: ""
        val timeSlot = intent.getStringExtra(EXTRA_TIME_SLOT) ?: ""
        val diagnosis = intent.getStringExtra(EXTRA_DIAGNOSIS) ?: ""
        val scheduledDate = intent.getLongExtra(EXTRA_SCHEDULED_DATE, System.currentTimeMillis())

        Log.d(TAG, "복약 알림 수신: prescription=$prescriptionId, drugId=$drugId, drug=$drugName, slot=$timeSlot")

        // NotificationHelper로 알림 표시
        try {
            val notificationHelper = NotificationHelper(context)
            notificationHelper.showMedicationNotification(
                prescriptionId = prescriptionId,
                drugId = drugId,  // ✅ drugId 전달
                drugName = drugName,
                timeSlot = timeSlot,
                diagnosis = diagnosis,
                scheduledDate = scheduledDate
            )
            Log.d(TAG, "알림 표시 성공")
        } catch (e: Exception) {
            Log.e(TAG, "알림 표시 실패: ${e.message}", e)
        }
        Log.d(TAG, "========================================")
    }
}