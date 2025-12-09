package com.example.altong_v2.ui.mypage

import android.content.Context
import android.content.SharedPreferences

/* 알림 설정 관리 클래스
SharedPreferences를 사용해서 알람관련 설정 저장/불러오기 */

class AlarmSettings(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )
    companion object {
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
            "morning" -> morningTime
            "lunch" -> lunchTime
            "dinner" -> dinnerTime
            "bedtime" -> bedtimeTime
            else -> DEFAULT_MORNING
        }
    }

    // 시간대에 대핟ㅇ하는 시간 설정하기
    fun setTimeBySlot(timeSlot: String, time: String) {
        when (timeSlot) {
            "morning" -> morningTime = time
            "lunch" -> lunchTime = time
            "dinner" -> dinnerTime = time
            "bedtime" -> bedtimeTime = time
        }
    }
    // 모든 설정 초기화
    fun reset() {
        prefs.edit().clear().apply()
    }
}