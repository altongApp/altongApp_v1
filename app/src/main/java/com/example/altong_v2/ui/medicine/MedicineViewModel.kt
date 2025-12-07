package com.example.altong_v2.ui.medicine

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.altong_v2.data.local.AppDatabase
import com.example.altong_v2.data.local.entity.FavoriteMedicineEntity
import com.example.altong_v2.data.model.Medicine
import com.example.altong_v2.data.model.PrescriptionMedicine
import com.example.altong_v2.data.repository.MedicineRepository
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * 약품 검색 ViewModel
 * Firebase에서 약품 데이터를 가져오고 UI에 전달
 */
class MedicineViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: MedicineRepository
    private val TAG = "MedicineViewModel"

    init {
        val favoriteMedicineDao = AppDatabase.getDatabase(application).favoriteMedicineDao()
        repository = MedicineRepository(favoriteMedicineDao)
    }

    // ========== 일반의약품 관련 ==========

    // 일반의약품 리스트
    private val _generalMedicines = MutableLiveData<List<Medicine>>(emptyList())
    val generalMedicines: LiveData<List<Medicine>> = _generalMedicines

    // ⭐ 전체 데이터 백업 (필터링용)
    private var allGeneralMedicines: List<Medicine> = emptyList()

    // 마지막 문서 (페이지네이션용)
    private var lastGeneralDocument: DocumentSnapshot? = null

    // 로딩 상태
    private val _isLoadingGeneral = MutableLiveData<Boolean>(false)
    val isLoadingGeneral: LiveData<Boolean> = _isLoadingGeneral

    // 에러 메시지
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    /**
     * 일반의약품 목록 로드 (첫 페이지)
     */
    fun loadGeneralMedicines() {
        // 이미 로딩 중이면 무시
        if (_isLoadingGeneral.value == true) {
            Log.d(TAG, "⚠️ 이미 로딩 중 - 중복 요청 무시")
            return
        }

        viewModelScope.launch {
            try {
                _isLoadingGeneral.value = true
                _errorMessage.value = null

                val (medicines, lastDoc) = repository.getGeneralMedicines()

                // ⭐ 전체 데이터 백업
                allGeneralMedicines = medicines

                _generalMedicines.value = medicines
                lastGeneralDocument = lastDoc

                Log.d(TAG, "일반의약품 로드 완료: ${medicines.size}개")
            } catch (e: Exception) {
                Log.e(TAG, "일반의약품 로드 실패", e)
                _errorMessage.value = "약품을 불러오는데 실패했습니다."
            } finally {
                _isLoadingGeneral.value = false
            }
        }
    }

    /**
     * 일반의약품 다음 페이지 로드 (스크롤 시)
     */
    fun loadMoreGeneralMedicines() {
        if (_isLoadingGeneral.value == true) return

        _isLoadingGeneral.value = true

        viewModelScope.launch {
            try {
                val (medicines, lastDoc) = repository.getGeneralMedicines(lastGeneralDocument)

                if (medicines.isNotEmpty()) {
                    // ⭐ 백업에도 추가
                    allGeneralMedicines = allGeneralMedicines + medicines

                    val currentList = _generalMedicines.value ?: emptyList()
                    _generalMedicines.value = currentList + medicines
                    lastGeneralDocument = lastDoc
                    Log.d(TAG, "일반의약품 추가 로드: ${medicines.size}개")
                }
            } catch (e: Exception) {
                Log.e(TAG, "일반의약품 추가 로드 실패", e)
            } finally {
                _isLoadingGeneral.value = false
            }
        }
    }

    /**
     * ⭐ 카테고리별 필터링 (클라이언트 필터링)
     */
    fun loadMedicinesByCategory(category: String) {
        viewModelScope.launch {
            try {
                _isLoadingGeneral.value = true

                // ⭐ Firebase 쿼리 (전체 데이터)
                val (medicines, lastDoc) = repository.getMedicinesByCategory(category)

                _generalMedicines.value = medicines
                lastGeneralDocument = lastDoc

                Log.d(TAG, "✅ Firebase 로드: ${medicines.size}개")

            } catch (e: Exception) {
                Log.e(TAG, "Firebase 실패, 백업 사용", e)

                // 백업: 클라이언트 필터링
                if (allGeneralMedicines.isNotEmpty()) {
                    val filtered = allGeneralMedicines.filter { medicine ->
                        medicine.categories.any { cat ->
                            cat.trim().trim('\'').trim('"') == category
                        }
                    }
                    _generalMedicines.value = filtered
                }
            } finally {
                _isLoadingGeneral.value = false
            }
        }
    }

    /**
     * 카테고리별 약품 다음 페이지 로드
     */
    fun loadMoreMedicinesByCategory(category: String) {
        if (_isLoadingGeneral.value == true) return

        _isLoadingGeneral.value = true

        viewModelScope.launch {
            try {
                Log.d(TAG, "📄 카테고리 [$category] 추가 로드 시도...")

                val (medicines, lastDoc) = repository.getMedicinesByCategory(
                    category,
                    lastGeneralDocument
                )

                if (medicines.isNotEmpty()) {
                    val currentList = _generalMedicines.value ?: emptyList()
                    _generalMedicines.value = currentList + medicines
                    lastGeneralDocument = lastDoc

                    Log.d(TAG, "✅ 추가 로드 완료: ${medicines.size}개 (총 ${_generalMedicines.value?.size}개)")
                } else {
                    Log.d(TAG, "⚠️ 더 이상 데이터 없음")
                }

            } catch (e: Exception) {
                Log.e(TAG, "카테고리 추가 로드 실패", e)
            } finally {
                _isLoadingGeneral.value = false
            }
        }
    }

    /**
     * ⭐ 필터 초기화 (전체 보기)
     */
    fun clearCategoryFilter() {
        _generalMedicines.value = allGeneralMedicines
        Log.d(TAG, "✅ 필터 해제: ${allGeneralMedicines.size}개 표시")
    }

    /**
     * 일반의약품 검색
     */
    fun searchGeneralMedicines(query: String) {
        if (query.isBlank()) {
            loadGeneralMedicines()
            return
        }

        viewModelScope.launch {
            try {
                _isLoadingGeneral.value = true
                _errorMessage.value = null

                val (medicines, lastDoc) = repository.searchGeneralMedicines(query)
                _generalMedicines.value = medicines
                lastGeneralDocument = lastDoc

                Log.d(TAG, "검색 결과: ${medicines.size}개")
            } catch (e: Exception) {
                Log.e(TAG, "약품 검색 실패", e)
                _errorMessage.value = "검색에 실패했습니다."
            } finally {
                _isLoadingGeneral.value = false
            }
        }
    }

    // ========== 전문의약품 관련 ==========

    // 전문의약품 리스트
    private val _prescriptionMedicines = MutableLiveData<List<PrescriptionMedicine>>(emptyList())
    val prescriptionMedicines: LiveData<List<PrescriptionMedicine>> = _prescriptionMedicines

    // 마지막 문서 (페이지네이션용)
    private var lastPrescriptionDocument: DocumentSnapshot? = null

    // 로딩 상태
    private val _isLoadingPrescription = MutableLiveData<Boolean>(false)
    val isLoadingPrescription: LiveData<Boolean> = _isLoadingPrescription

    /**
     * 전문의약품 목록 로드 (첫 페이지)
     */
    fun loadPrescriptionMedicines() {
        viewModelScope.launch {
            try {
                _isLoadingPrescription.value = true
                _errorMessage.value = null

                val (medicines, lastDoc) = repository.getPrescriptionMedicines()
                _prescriptionMedicines.value = medicines
                lastPrescriptionDocument = lastDoc

                Log.d(TAG, "전문의약품 로드 완료: ${medicines.size}개")
            } catch (e: Exception) {
                Log.e(TAG, "전문의약품 로드 실패", e)
                _errorMessage.value = "약품을 불러오는데 실패했습니다."
            } finally {
                _isLoadingPrescription.value = false
            }
        }
    }

    /**
     * 전문의약품 다음 페이지 로드 (스크롤 시)
     */
    fun loadMorePrescriptionMedicines() {
        if (_isLoadingPrescription.value == true) return

        _isLoadingGeneral.value = true

        viewModelScope.launch {
            try {
                _isLoadingPrescription.value = true

                val (medicines, lastDoc) = repository.getPrescriptionMedicines(lastPrescriptionDocument)

                if (medicines.isNotEmpty()) {
                    val currentList = _prescriptionMedicines.value ?: emptyList()
                    _prescriptionMedicines.value = currentList + medicines
                    lastPrescriptionDocument = lastDoc
                    Log.d(TAG, "전문의약품 추가 로드: ${medicines.size}개")
                }
            } catch (e: Exception) {
                Log.e(TAG, "전문의약품 추가 로드 실패", e)
            } finally {
                _isLoadingPrescription.value = false
            }
        }
    }

    /**
     * 전문의약품 검색
     */
    fun searchPrescriptionMedicines(query: String) {
        if (query.isBlank()) {
            loadPrescriptionMedicines()
            return
        }

        viewModelScope.launch {
            try {
                _isLoadingPrescription.value = true
                _errorMessage.value = null

                val (medicines, lastDoc) = repository.searchPrescriptionMedicines(query)
                _prescriptionMedicines.value = medicines
                lastPrescriptionDocument = lastDoc

                Log.d(TAG, "전문의약품 검색 결과: ${medicines.size}개")
            } catch (e: Exception) {
                Log.e(TAG, "전문의약품 검색 실패", e)
                _errorMessage.value = "검색에 실패했습니다."
            } finally {
                _isLoadingPrescription.value = false
            }
        }
    }

    // ========== 찜 기능 관련 ==========

    /**
     * 약품 ID로 일반의약품 조회
     */
    suspend fun getMedicineById(medicineId: String): Medicine? {
        return repository.getMedicineById(medicineId)
    }

    /**
     * 약품 ID로 전문의약품 조회
     */
    suspend fun getPrescriptionMedicineById(medicineId: String): PrescriptionMedicine? {
        return repository.getPrescriptionMedicineById(medicineId)
    }

    /**
     * 찜 여부 확인
     */
    suspend fun isFavorite(medicineId: String): Boolean {
        return repository.isFavorite(medicineId)
    }

    /**
     * 찜 추가
     */
    fun addFavorite(medicine: Medicine) {
        viewModelScope.launch {
            try {
                val favorite = FavoriteMedicineEntity(
                    medicineId = medicine.medicine_id,
                    medicineName = medicine.medicine_name,
                    manufacturer = medicine.manufacturer,
                    medicineType = "otc",
                    imageUrl = medicine.image_url ?: ""
                )
                repository.addFavorite(favorite)
                Log.d(TAG, "찜 추가: ${medicine.medicine_name}")
            } catch (e: Exception) {
                Log.e(TAG, "찜 추가 실패", e)
                _errorMessage.value = "찜하기에 실패했습니다."
            }
        }
    }

    /**
     * 전문의약품 찜 추가
     */
    fun addPrescriptionFavorite(medicine: PrescriptionMedicine) {
        viewModelScope.launch {
            try {
                val favorite = FavoriteMedicineEntity(
                    medicineId = medicine.medicine_id,
                    medicineName = medicine.medicine_name,
                    manufacturer = medicine.manufacturer,
                    medicineType = "prescription",
                    imageUrl = medicine.image_url ?: ""
                )
                repository.addFavorite(favorite)
                Log.d(TAG, "찜 추가: ${medicine.medicine_name}")
            } catch (e: Exception) {
                Log.e(TAG, "찜 추가 실패", e)
                _errorMessage.value = "찜하기에 실패했습니다."
            }
        }
    }

    /**
     * 찜 해제
     */
    fun removeFavorite(medicineId: String) {
        viewModelScope.launch {
            try {
                repository.removeFavoriteById(medicineId)
                Log.d(TAG, "찜 해제: $medicineId")
            } catch (e: Exception) {
                Log.e(TAG, "찜 해제 실패", e)
                _errorMessage.value = "찜 해제에 실패했습니다."
            }
        }
    }

    /**
     * 타입별 찜 목록 조회 (일반의약품 or 전문의약품)
     */
    suspend fun getFavoritesByType(type: String): List<FavoriteMedicineEntity> {
        return repository.getFavoritesByType(type).first()
    }
}