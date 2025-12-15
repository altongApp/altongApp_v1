package com.example.altong_v2.data.repository

import com.example.altong_v2.data.local.dao.DrugDao
import com.example.altong_v2.data.local.dao.PrescriptionDao
import com.example.altong_v2.data.local.entity.DrugEntity
import com.example.altong_v2.data.local.entity.PrescriptionEntity
import kotlinx.coroutines.flow.Flow
import com.example.altong_v2.data.model.PrescriptionWithDrugs
import kotlinx.coroutines.flow.first

/*
 * 처방전 Repository
 * 처방전 및 약 데이터 접근을 추상화
*/

class PrescriptionRepository(
    private val prescriptionDao: PrescriptionDao,
    private val drugDao: DrugDao
) {
    // 모든 처방전 조회 (Flow가 실시간 업데이트되게 해줌)
    val allPrescriptions: Flow<List<PrescriptionEntity>> =
        prescriptionDao.getAllPrescriptions()
    // 처방전 추가
    suspend fun insertPrescription(prescription: PrescriptionEntity): Long {
        return prescriptionDao.insert(prescription)
    }
    // 처방전 수정
    suspend fun updatePrescription(prescription: PrescriptionEntity) {
        prescriptionDao.update(prescription)
    }
    // 처방전 삭제 (CASCADE로 관련 약도 자동 삭제됨)
    suspend fun deletePrescription(prescription: PrescriptionEntity) {
        prescriptionDao.delete(prescription)
    }

    // ID로 처방전 조회
    suspend fun getPrescriptionById(id: Long): PrescriptionEntity? {
        return prescriptionDao.getPrescriptionById(id)
    }

    // 진단명으로 검색
    suspend fun searchByDiagnosis(query: String): List<PrescriptionEntity> {
        return prescriptionDao.searchByDiagnosis(query)
    }

    // 처방전 개수
    suspend fun getPrescriptionCount(): Int {
        return prescriptionDao.getCount()
    }

    // ========== 약 관련 ==========
    // 특정 처방전의 약 조회
    fun getDrugsByPrescription(prescriptionId: Long): Flow<List<DrugEntity>> {
        return drugDao.getDrugsByPrescription(prescriptionId)
    }
    // 약 추가
    suspend fun insertDrug(drug: DrugEntity): Long {
        return drugDao.insert(drug)
    }
    // 약 수정
    suspend fun updateDrug(drug: DrugEntity) {
        drugDao.update(drug)
    }
    // 약 삭제
    suspend fun deleteDrug(drug: DrugEntity) {
        drugDao.delete(drug)
    }
    // ID로 약 조회
    suspend fun getDrugById(id: Long): DrugEntity? {
        return drugDao.getDrugById(id)
    }
    // 특정 처방전의 약 개수
    suspend fun getDrugCount(prescriptionId: Long): Int {
        return drugDao.getDrugCountByPrescription(prescriptionId)
    }
    // 모든 약 조회 (캘린더용)
    fun getAllDrugs(): Flow<List<DrugEntity>> {
        return drugDao.getAllDrugs()
    }

    // 모든 처방전과 약품 가져오기
    suspend fun getAllPrescriptionsWithDrugs(): List<PrescriptionWithDrugs> {
        val prescriptions = prescriptionDao.getAllPrescriptionsSync() // 모든 처방전
        // 각 처방전의 약품 가져오기
        return prescriptions.map { prescription ->
            val drugs = drugDao.getDrugsByPrescription(prescription.id).first()
            PrescriptionWithDrugs(prescription, drugs)
        }
    }
}