package com.example.altong_v2.data.model

import com.example.altong_v2.data.local.entity.PrescriptionEntity
import com.example.altong_v2.data.local.entity.DrugEntity

/*
 * 처방전과 약품을 함께 담는 데이터 클래스
 * 알람 재등록 시 사용
 */
data class PrescriptionWithDrugs(
    val prescription: PrescriptionEntity,   // 처방전 1개
    val drugs: List<DrugEntity> // 그 처방전의 약품들
)