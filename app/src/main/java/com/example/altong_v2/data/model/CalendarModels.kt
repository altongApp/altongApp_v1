package com.example.altong_v2.data.model

/**
 * 캘린더에 표시할 날짜별 약 데이터
 * 선택한 날짜에 복용해야 할 모든 처방전과 약 정보를 담음
 */
data class CalendarDayData(
    val date: String,                               // 날짜 (YYYY-MM-DD)
    val prescriptions: List<CalendarPrescription>   // 해당 날짜의 처방전 리스트 (이름 변경!)
)

/**
 * 캘린더용 처방전과 약 정보
 * 진단명별로 그룹핑하기 위한 구조
 * (약통용 PrescriptionWithDrugs와 구별하기 위해 이름 변경)
 */
data class CalendarPrescription(
    val prescriptionId: Long,                       // 처방전 ID
    val diagnosis: String,                          // 진단명 (예: "감기")
    val prescriptionDate: String,                   // 처방일
    val totalDays: Int,                             // 이 처방전의 최대 처방 일수
    val drugCount: Int,                             // 총 약 개수
    val drugsByTimeSlot: Map<String, List<DrugItem>>  // 시간대별 약 리스트
    // Key: "아침", "점심", "저녁", "취침 전"
    // Value: 해당 시간대에 먹을 약 리스트
)

/**
 * 캘린더에 표시할 개별 약 정보
 * 체크박스와 함께 표시될 약의 세부 정보
 */
data class DrugItem(
    val drugId: Long,                               // 약 ID
    val drugName: String,                           // 약 이름 (예: "아모스정 375mg")
    val dosage: String,                             // 1회 복용량 (예: "1정")
    val timing: String?,                            // 복용 시점 (예: "식후 30분")
    val remainingDays: Int,                         // 남은 복용 일수
    val isCompleted: Boolean,                       // 복용 완료 여부 (체크박스 상태)
    val timeSlot: String                            // ✅ 시간대 (예: "아침", "점심")
)