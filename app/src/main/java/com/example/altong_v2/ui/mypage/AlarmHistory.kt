package com.example.altong_v2.ui.mypage


 // 알림 기록 데이터 클래스
data class AlarmHistory(
    val id: Long = 0,
    val timeSlot: String,           // "morning", "lunch", "dinner", "bedtime"
    val time: String,                // "08:00"
    val medicines: List<String>,     // ["감기약", "허리약"]
    val isCompleted: Boolean,        // true: 복용 완료, false: 대기
    val timestamp: Long = System.currentTimeMillis()
) {

    fun getTimeSlotIcon(): String {
        return when (timeSlot) {
            "morning" -> "🌅"
            "lunch" -> "☀️"
            "dinner" -> "🌙"
            "bedtime" -> "🛌"
            else -> "⏰"
        }
    }
    fun getTimeSlotLabel(): String {
        return when (timeSlot) {
            "morning" -> "아침"
            "lunch" -> "점심"
            "dinner" -> "저녁"
            "bedtime" -> "취침 전"
            else -> "알림"
        }
    }
    // 타이틀 생성
    fun getTitle(): String {
        val icon = getTimeSlotIcon()
        val label = getTimeSlotLabel()
        val status = if (isCompleted) "복용 완료" else "복용 대기"
        return "$icon $label 약 $status"
    }
    // 상세정보 생성
    fun getDetail(): String {
        val medicinesText = medicines.joinToString(", ")
        return "$time • $medicinesText"
    }
}