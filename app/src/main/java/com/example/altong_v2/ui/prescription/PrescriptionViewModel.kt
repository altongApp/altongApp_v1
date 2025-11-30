package com.example.altong_v2.ui.prescription

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.altong_v2.data.local.AppDatabase
import com.example.altong_v2.data.local.entity.DrugEntity
import com.example.altong_v2.data.local.entity.PrescriptionEntity
import com.example.altong_v2.data.repository.PrescriptionRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/* * 나의 약통 ViewModel
 * 처방전 및 약품 데이터 관리*/

class PrescriptionViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: PrescriptionRepository

    // 모든 처방전 (LiveData로 실시간 업데이트)
    val allPrescriptions: LiveData<List<PrescriptionEntity>>

    init {
        val database = AppDatabase.getDatabase(application)
        val prescriptionDao = database.prescriptionDao()
        val drugDao = database.drugDao()

        repository = PrescriptionRepository(prescriptionDao, drugDao)
        allPrescriptions = repository.allPrescriptions.asLiveData()
    }
    // 처방전 추가
    fun insertPrescription(prescription: PrescriptionEntity, onSuccess: (Long) -> Unit) {
        viewModelScope.launch {
            val id = repository.insertPrescription(prescription)
            onSuccess(id)
        }
    }
    // 처방전 수정
    fun updatePrescription(prescription: PrescriptionEntity) {
        viewModelScope.launch {
            repository.updatePrescription(prescription)
        }
    }
    // 처방전 삭제
    fun deletePrescription(prescription: PrescriptionEntity) {
        viewModelScope.launch {
            repository.deletePrescription(prescription)
        }
    }
    // id로 처방전 조회
    suspend fun getPrescriptionById(id: Long): PrescriptionEntity? {
        return repository.getPrescriptionById(id)
    }
    // 처방전 개수
    suspend fun getPrescriptionCount(): Int {
        return repository.getPrescriptionCount()
    }

// 보고자하는 처방전의 약 조회
    fun getDrugsByPrescription(prescriptionId: Long): LiveData<List<DrugEntity>> {
        return repository.getDrugsByPrescription(prescriptionId).asLiveData()
    }
    //테스트하면서 추가
    suspend fun getDrugsList(prescriptionId: Long): List<DrugEntity> {
        return repository.getDrugsByPrescription(prescriptionId).first()
    }

    // 약 추가
    fun insertDrug(drug: DrugEntity, onSuccess: (Long) -> Unit = {}) {
        viewModelScope.launch {
            val id = repository.insertDrug(drug)
            onSuccess(id)
        }
    }
    // 약 수정
    fun updateDrug(drug: DrugEntity) {
        viewModelScope.launch {
            repository.updateDrug(drug)
        }
    }
    // 약 삭제
    fun deleteDrug(drug: DrugEntity) {
        viewModelScope.launch {
            repository.deleteDrug(drug)
        }
    }
    // id로 약 조회
    suspend fun getDrugCount(prescriptionId: Long): Int {
        return repository.getDrugCount(prescriptionId)
    }
}