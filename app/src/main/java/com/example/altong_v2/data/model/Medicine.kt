package com.example.altong_v2.data.model

// 일반의약품 (OTC - Over The Counter)
data class Medicine(
    val medicine_id: String = "",           // 품목기준코드
    val medicine_name: String = "",         // 약품명
    val manufacturer: String = "",          // 업체명 (제조사)
    val efficacy: String? = null,          // 효능효과
    val usage_method: String? = null,      // 용법용량
    val precautions: String? = null,       // 사용상의주의사항
    val storage_method: String? = null,    // 저장방법
    val image_url: String? = null,         // 이미지 URL
    val warning: String? = null,           // 경고
    val interactions: String? = null,      // 상호작용
    val side_effects: String? = null,      // 부작용
    val categories: List<String> = emptyList(), // 카테고리 (배열: 다중 카테고리 가능)
    val medicine_type: String = "otc"      // 약품 타입
) {

    // 특정 카테고리 포함 여부 확인
    fun containsCategory(category: String): Boolean {
        return categories.any { it.contains(category, ignoreCase = true) }
    }
    // 검색어 매칭 여부 확인
    fun matchesSearchQuery(query: String): Boolean {
        if (query.isBlank()) return true
        return medicine_name.contains(query, ignoreCase = true) ||
                manufacturer.contains(query, ignoreCase = true)
    }
}

// 전문의약품 (Prescription Medicine)
data class PrescriptionMedicine(
    val medicine_id: String = "",           // 문서ID
    val medicine_name: String = "",         // 약품명
    val manufacturer: String = "",          // 업체명 (제조사)
    val efficacy: String? = null,          // 효능효과
    val usage_method: String? = null,      // 용법용량
    val precautions: String? = null,       // 사용상의주의사항
    val storage_method: String? = null,    // 저장방법
    val image_url: String? = null,         // 이미지 URL
    val classification: String? = null,    // 분류
    val ingredients: String? = null,       // 성분정보
    val validity_period: String? = null,   // 사용기간
    val medicine_type: String = "prescription" // 약품 타입
) {

 // 검색어 매칭 여부 확인
    fun matchesSearchQuery(query: String): Boolean {
        if (query.isBlank()) return true
        return medicine_name.contains(query, ignoreCase = true) ||
                manufacturer.contains(query, ignoreCase = true)
    }
}

 // 약품 카테고리 정의
object MedicineCategory {
    const val COLD_RESPIRATORY = "감기/호흡기"
    const val DIGESTIVE = "소화기"
    const val PAIN_INFLAMMATION = "통증/염증"
    const val VITAMIN_SUPPLEMENT = "영양제/비타민"
    const val EYE_EAR_ORAL = "눈/귀/구강"
    const val ETC = "기타"

    // 전체 카테고리 리스트
    val ALL_CATEGORIES = listOf(
        COLD_RESPIRATORY,
        DIGESTIVE,
        PAIN_INFLAMMATION,
        VITAMIN_SUPPLEMENT,
        EYE_EAR_ORAL,
        ETC
    )

    // 카테고리 이모지 매핑
    fun getEmoji(category: String): String {
        return when (category) {
            COLD_RESPIRATORY -> "🤧"
            DIGESTIVE -> "🤮"
            PAIN_INFLAMMATION -> "💊"
            VITAMIN_SUPPLEMENT -> "💊"
            EYE_EAR_ORAL -> "👁️"
            ETC -> "📦"
            else -> "💊"
        }
    }
}