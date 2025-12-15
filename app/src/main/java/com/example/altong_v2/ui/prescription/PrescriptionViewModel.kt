package com.example.altong_v2.ui.prescription

import android.app.Application
import android.util.Log
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
import com.example.altong_v2.ui.alarm.AlarmScheduler

/* * 나의 약통 ViewModel
 * 처방전 및 약 데이터 관리*/

class PrescriptionViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: PrescriptionRepository
    private val alarmScheduler: AlarmScheduler

    // 모든 처방전 (LiveData로 실시간 업데이트)
    val allPrescriptions: LiveData<List<PrescriptionEntity>>

    init {
        val database = AppDatabase.getDatabase(application)
        val prescriptionDao = database.prescriptionDao()
        val drugDao = database.drugDao()

        repository = PrescriptionRepository(prescriptionDao, drugDao)
        allPrescriptions = repository.allPrescriptions.asLiveData()
        alarmScheduler = AlarmScheduler(application)
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

    suspend fun getDrugsList(prescriptionId: Long): List<DrugEntity> {
        return repository.getDrugsByPrescription(prescriptionId).first()
    }

    // 약 추가
    fun insertDrug(drug: DrugEntity, onSuccess: (Long) -> Unit = {}) {
        viewModelScope.launch {
            val id = repository.insertDrug(drug)

            // 알림 등록 추가
            try {
                val prescription = repository.getPrescriptionById(drug.prescriptionId)
                prescription?.let {
                    alarmScheduler.scheduleMedicationAlarms(
                        prescriptionId = drug.prescriptionId,
                        drug = drug,
                        prescriptionDate = it.date
                    )
                }
            } catch (e: Exception) {
                Log.e("PrescriptionVM", "알림 등록 실패: ${e.message}")
            }

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
            //알림 취소 추가
            try {
                val prescription = repository.getPrescriptionById(drug.prescriptionId)
                prescription?.let {
                    alarmScheduler.cancelMedicationAlarms(
                        prescriptionId = drug.prescriptionId,
                        drug = drug,
                        prescriptionDate = it.date
                    )
                }
            } catch (e: Exception) {
                Log.e("PrescriptionVM", "알림 취소 실패: ${e.message}")
            }

            repository.deleteDrug(drug)
        }
    }
    // id로 약 조회
    suspend fun getDrugCount(prescriptionId: Long): Int {
        return repository.getDrugCount(prescriptionId)
    }

    // ----------처방전 등록 임시데이터를 추가하겠수다~
    // Step 1 데이터
    var tempDate: String = ""
    var tempHospital: String = ""
    var tempDepartment: String = ""

    // Step 2 데이터
    var tempDiagnosis: String = ""

    // Step 3 데이터
    var tempPharmacy: String = ""
//    var tempPhotoPath: String? = null

    // 약 리스트
    var tempDrugs: MutableList<TempDrugData> = mutableListOf()

    // 처방전 데이터 초기화
    fun clearTempData() {
        android.util.Log.d("DrugAdd", "=== clearTempData 호출 ===")
        android.util.Log.d("DrugAdd", "호출 스택: ${Thread.currentThread().stackTrace[3]}")

        tempDate = ""
        tempHospital = ""
        tempDepartment = ""
        tempDiagnosis = ""
        tempPharmacy = ""
//        tempPhotoPath = null
        tempDrugs.clear()
    }

    // 처방전 + 약 저장
    fun savePrescriptionWithDrugs() {
        viewModelScope.launch {
            try {
                // 1. 처방전 저장
                val prescription = PrescriptionEntity(
                    date = tempDate,
                    hospital = tempHospital,
                    department = tempDepartment,
                    diagnosis = tempDiagnosis,
                    pharmacy = tempPharmacy,
//                    prescriptionImagePath = tempPhotoPath
                )

                val prescriptionId = repository.insertPrescription(prescription)
                // 2. 약 저장
                tempDrugs.forEach { drug ->
                    val drugEntity = DrugEntity(
                        prescriptionId = prescriptionId,
                        name = drug.name,
                        dosage = drug.dosage,
                        frequency = drug.frequency,
                        days = drug.days,
                        timing = drug.timing,
                        memo = drug.memo,
                        timeSlots = drug.timeSlots.joinToString(","),
                        imageUrl = drug.imageUrl
                    )
                    repository.insertDrug(drugEntity)

                    //알림 등록 추가
                    try {
                        alarmScheduler.scheduleMedicationAlarms(
                            prescriptionId = prescriptionId,
                            drug = drugEntity,
                            prescriptionDate = tempDate
                        )
                    } catch (e: Exception) {
                        Log.e("PrescriptionVM", "알림 등록 실패: ${e.message}")
                    }
                }

                // 3. 임시 데이터 초기화
                clearTempData()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

// ~~ 처방전 정보 수정 ~~
    // 수정 모드 플래그
    var isEditMode: Boolean = false
    var editingPrescriptionId: Long = -1L

    // 수정모드 on - 일단 기존 처방전 불러오기
    suspend fun startEditMode(prescriptionId: Long) {
        isEditMode = true
        editingPrescriptionId = prescriptionId

        // 기존 처방전 데이터 로드
        val prescription = getPrescriptionById(prescriptionId)
        prescription?.let {
            tempDate = it.date
            tempHospital = it.hospital.orEmpty()
            tempDepartment = it.department.orEmpty()
            tempDiagnosis = it.diagnosis
            tempPharmacy = it.pharmacy.orEmpty()
        }
    }

// 처방전 수정하고 저장하기
    fun updatePrescriptionInfoOnly() {
        viewModelScope.launch {
            try {
                // 1. 처방전 업데이트
                val prescription = PrescriptionEntity(
                    id = editingPrescriptionId,
                    date = tempDate,
                    hospital = tempHospital,
                    department = tempDepartment,
                    diagnosis = tempDiagnosis,
                    pharmacy = tempPharmacy
                )
                repository.updatePrescription(prescription)

                isEditMode = false
                editingPrescriptionId = -1L
                clearTempData()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // 약 수정하러하기
    suspend fun startEditModeForDrugsOnly(prescriptionId: Long) {
        isEditMode = true
        editingPrescriptionId = prescriptionId

        // 약 데이터만 로드
        val drugs = getDrugsList(prescriptionId)
        tempDrugs.clear()
        drugs.forEach { drug ->
            tempDrugs.add(
                TempDrugData(
                    name = drug.name,
                    dosage = drug.dosage,
                    frequency = drug.frequency,
                    days = drug.days,
                    timing = drug.timing.orEmpty(),
                    memo = drug.memo.orEmpty(),
                    timeSlots = drug.timeSlots.split(","),
                    imageUrl = drug.imageUrl
                )
            )
        }
    }
    // 약만 업뎃
    fun updateDrugsOnly() {
        viewModelScope.launch {
            try {
                // 처방일 미리 가져오기
                val prescription = repository.getPrescriptionById(editingPrescriptionId)
                val prescriptionDate = prescription?.date ?: ""

                // 1. 기존 약품 전체 삭제
                val oldDrugs = getDrugsList(editingPrescriptionId)
                oldDrugs.forEach { drug ->
                    //  알림 취소 추가
                    try {
                        alarmScheduler.cancelMedicationAlarms(
                            prescriptionId = editingPrescriptionId,
                            drug = drug,
                            prescriptionDate = prescriptionDate
                        )
                    } catch (e: Exception) {
                        Log.e("PrescriptionVM", "알림 취소 실패: ${e.message}")
                    }
                    repository.deleteDrug(drug)
                }
                // 2. 새로운 약품 전체 추가
                tempDrugs.forEach { drug ->
                    val drugEntity = DrugEntity(
                        prescriptionId = editingPrescriptionId,
                        name = drug.name,
                        dosage = drug.dosage,
                        frequency = drug.frequency,
                        days = drug.days,
                        timing = drug.timing,
                        memo = drug.memo,
                        timeSlots = drug.timeSlots.joinToString(","),
                        imageUrl = drug.imageUrl
                    )
                    repository.insertDrug(drugEntity)
                    // 알림 등록 추가
                    try {
                        alarmScheduler.scheduleMedicationAlarms(
                            prescriptionId = editingPrescriptionId,
                            drug = drugEntity,
                            prescriptionDate = prescriptionDate
                        )
                    } catch (e: Exception) {
                        Log.e("PrescriptionVM", "알림 등록 실패: ${e.message}")
                    }
                }
                isEditMode = false
                editingPrescriptionId = -1L
                clearTempData()

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // 약 추가 모드 플래그
    var isAddDrugMode: Boolean = false
    var addingToPrescriptionId: Long = -1L

    // 약추가모드
    suspend fun startAddDrugMode(prescriptionId: Long) {
        isAddDrugMode = true
        addingToPrescriptionId = prescriptionId
        // 임시 약 리스트 초기화
        tempDrugs.clear()
    }
    // 기존 처방전에 약만 추가하는
    fun addDrugToPrescription(drug: TempDrugData) {
        viewModelScope.launch {
            try {
                Log.d("PrescriptionVM", "==== addDrugToPrescription 시작 ====")
                Log.d("PrescriptionVM", "addingToPrescriptionId: $addingToPrescriptionId")
                Log.d("PrescriptionVM", "약품명: ${drug.name}")
                Log.d("PrescriptionVM", "timeSlots: ${drug.timeSlots}")

                val drugEntity = DrugEntity(
                    prescriptionId = addingToPrescriptionId,
                    name = drug.name,
                    dosage = drug.dosage,
                    frequency = drug.frequency,
                    days = drug.days,
                    timing = drug.timing,
                    memo = drug.memo,
                    timeSlots = drug.timeSlots.joinToString(","),
                    imageUrl = drug.imageUrl
                )
                Log.d("PrescriptionVM", "DrugEntity 생성 완료")
                Log.d("PrescriptionVM", "  - timeSlots 저장값: '${drugEntity.timeSlots}'")

                repository.insertDrug(drugEntity)
                Log.d("PrescriptionVM", "DB 저장 완료")

                //알림 등록 추가
                try {
                    Log.d("PrescriptionVM", "처방전 조회 시작...")
                    val prescription = repository.getPrescriptionById(addingToPrescriptionId)

                    if (prescription == null) {
                        Log.e("PrescriptionVM", "❌ 처방전을 찾을 수 없습니다! prescriptionId=$addingToPrescriptionId")
                    } else {
                        Log.d("PrescriptionVM", "✅ 처방전 조회 성공")
                        Log.d("PrescriptionVM", "  - 처방일: ${prescription.date}")
                        Log.d("PrescriptionVM", "  - 진단명: ${prescription.diagnosis}")

                        Log.d("PrescriptionVM", "AlarmScheduler 호출 시작...")
                        alarmScheduler.scheduleMedicationAlarms(
                            prescriptionId = addingToPrescriptionId,
                            drug = drugEntity,
                            prescriptionDate = prescription.date
                        )
                        Log.d("PrescriptionVM", "AlarmScheduler 호출 완료")
                    }
                } catch (e: Exception) {
                    Log.e("PrescriptionVM", "❌ 알림 등록 실패: ${e.message}", e)
                    e.printStackTrace()
                }

                // 추가 모드 종료
//                isAddDrugMode = false
  //              addingToPrescriptionId = -1L

                Log.d("PrescriptionVM", "==== addDrugToPrescription 완료 ====")
            } catch (e: Exception) {
                Log.e("PrescriptionVM", "❌ 전체 프로세스 실패: ${e.message}", e)
                e.printStackTrace()
            }
        }
    }

    // 약 수정 모드 플래그
    var isEditDrugMode: Boolean = false
    var editingDrugIndex: Int = -1  // tempDrugs 리스트의 인덱스

    fun startEditDrugMode(index: Int) {
        isEditDrugMode = true
        editingDrugIndex = index
    }
    // 약 수정 저장
    fun updateDrugInList(drug: TempDrugData) {
        if (editingDrugIndex >= 0 && editingDrugIndex < tempDrugs.size) {
            tempDrugs[editingDrugIndex] = drug
        }
        isEditDrugMode = false
        editingDrugIndex = -1
    }
}

// 임시 약 데이터 클래스
data class TempDrugData(
    val name: String,
    val dosage: String,
    val frequency: String,
    val days: Int,
    val timing: String,
    val memo: String,
    val timeSlots: List<String>,
    val imageUrl: String? = null
)
