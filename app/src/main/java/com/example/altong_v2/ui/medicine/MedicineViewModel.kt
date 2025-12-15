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
import com.example.altong_v2.data.repository.FavoriteMedicineRepository
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.Flow

/**
 * 약품 검색 ViewModel
 * Firebase에서 약품 데이터를 가져오고 UI에 전달
 */
class MedicineViewModel(application: Application) : AndroidViewModel(application) {

    private val medicineRepository: MedicineRepository
    private val favoriteRepository: FavoriteMedicineRepository
    private val TAG = "MedicineViewModel"

    init {
        val favoriteMedicineDao = AppDatabase.getDatabase(application).favoriteMedicineDao()
        medicineRepository = MedicineRepository()
        favoriteRepository = FavoriteMedicineRepository(favoriteMedicineDao)
    }

    // ========== 일반의약품 관련 ==========

    private val _generalMedicines = MutableLiveData<List<Medicine>>(emptyList())
    val generalMedicines: LiveData<List<Medicine>> = _generalMedicines

    private var allGeneralMedicines: List<Medicine> = emptyList()
    private var lastGeneralDocument: DocumentSnapshot? = null

    private val _isLoadingGeneral = MutableLiveData<Boolean>(false)
    val isLoadingGeneral: LiveData<Boolean> = _isLoadingGeneral

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    /**
     * 일반의약품 목록 로드 (첫 페이지)
     */
    fun loadGeneralMedicines() {
        if (_isLoadingGeneral.value == true) {
            Log.d(TAG, "⚠️ 이미 로딩 중 - 중복 요청 무시")
            return
        }

        viewModelScope.launch {
            try {
                _isLoadingGeneral.value = true
                _errorMessage.value = null

                val (medicines, lastDoc) = medicineRepository.getGeneralMedicines()
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
     * 일반의약품 다음 페이지 로드
     */
    fun loadMoreGeneralMedicines() {
        if (_isLoadingGeneral.value == true) return

        _isLoadingGeneral.value = true

        viewModelScope.launch {
            try {
                val (medicines, lastDoc) = medicineRepository.getGeneralMedicines(lastGeneralDocument)

                if (medicines.isNotEmpty()) {
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
     * 카테고리별 약품 로드
     */
    fun loadMedicinesByCategory(category: String) {
        viewModelScope.launch {
            try {
                _isLoadingGeneral.value = true

                val (medicines, lastDoc) = medicineRepository.getMedicinesByCategory(category)
                _generalMedicines.value = medicines
                lastGeneralDocument = lastDoc

                Log.d(TAG, "✅ Firebase 로드: ${medicines.size}개")

            } catch (e: Exception) {
                Log.e(TAG, "Firebase 실패", e)
                _errorMessage.value = "약품을 불러오는데 실패했습니다."
            } finally {
                _isLoadingGeneral.value = false
            }
        }
    }

    /**
     * 카테고리별 약품 추가 로드
     */
    fun loadMoreMedicinesByCategory(category: String) {
        if (_isLoadingGeneral.value == true) return

        _isLoadingGeneral.value = true

        viewModelScope.launch {
            try {
                val (medicines, lastDoc) = medicineRepository.getMedicinesByCategory(
                    category,
                    lastGeneralDocument
                )

                if (medicines.isNotEmpty()) {
                    val currentList = _generalMedicines.value ?: emptyList()
                    _generalMedicines.value = currentList + medicines
                    lastGeneralDocument = lastDoc

                    Log.d(TAG, "✅ 추가 로드: ${medicines.size}개")
                }
            } catch (e: Exception) {
                Log.e(TAG, "카테고리 추가 로드 실패", e)
            } finally {
                _isLoadingGeneral.value = false
            }
        }
    }

    /**
     * 일반의약품 검색 (첫 페이지)
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

                val (medicines, lastDoc) = medicineRepository.searchGeneralMedicines(query)
                _generalMedicines.value = medicines
                lastGeneralDocument = lastDoc

                Log.d(TAG, "🔍 검색 결과: ${medicines.size}개 (검색어: $query)")
            } catch (e: Exception) {
                Log.e(TAG, "검색 실패", e)
                _errorMessage.value = "검색에 실패했습니다."
            } finally {
                _isLoadingGeneral.value = false
            }
        }
    }

    /**
     * 일반의약품 검색 (추가 페이지)
     */
    fun searchMoreGeneralMedicines(query: String) {
        if (_isLoadingGeneral.value == true) return

        _isLoadingGeneral.value = true

        viewModelScope.launch {
            try {
                val (medicines, lastDoc) = medicineRepository.searchGeneralMedicines(
                    query,
                    lastGeneralDocument
                )

                if (medicines.isNotEmpty()) {
                    val currentList = _generalMedicines.value ?: emptyList()
                    _generalMedicines.value = currentList + medicines
                    lastGeneralDocument = lastDoc

                    Log.d(TAG, "🔍 검색 추가 로드: ${medicines.size}개")
                }
            } catch (e: Exception) {
                Log.e(TAG, "검색 추가 로드 실패", e)
            } finally {
                _isLoadingGeneral.value = false
            }
        }
    }

    // ========== 전문의약품 관련 ==========

    private val _prescriptionMedicines = MutableLiveData<List<PrescriptionMedicine>>(emptyList())
    val prescriptionMedicines: LiveData<List<PrescriptionMedicine>> = _prescriptionMedicines

    private var lastPrescriptionDocument: DocumentSnapshot? = null

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

                val (medicines, lastDoc) = medicineRepository.getPrescriptionMedicines()
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
     * 전문의약품 다음 페이지 로드
     */
    fun loadMorePrescriptionMedicines() {
        if (_isLoadingPrescription.value == true) return

        _isLoadingPrescription.value = true

        viewModelScope.launch {
            try {
                val (medicines, lastDoc) = medicineRepository.getPrescriptionMedicines(lastPrescriptionDocument)

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
     * 전문의약품 검색 (첫 페이지)
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

                val (medicines, lastDoc) = medicineRepository.searchPrescriptionMedicines(query)
                _prescriptionMedicines.value = medicines
                lastPrescriptionDocument = lastDoc

                Log.d(TAG, "🔍 전문의약품 검색 결과: ${medicines.size}개")
            } catch (e: Exception) {
                Log.e(TAG, "전문의약품 검색 실패", e)
                _errorMessage.value = "검색에 실패했습니다."
            } finally {
                _isLoadingPrescription.value = false
            }
        }
    }

    /**
     * 전문의약품 검색 (추가 페이지)
     */
    fun searchMorePrescriptionMedicines(query: String) {
        if (_isLoadingPrescription.value == true) return

        _isLoadingPrescription.value = true

        viewModelScope.launch {
            try {
                val (medicines, lastDoc) = medicineRepository.searchPrescriptionMedicines(
                    query,
                    lastPrescriptionDocument
                )

                if (medicines.isNotEmpty()) {
                    val currentList = _prescriptionMedicines.value ?: emptyList()
                    _prescriptionMedicines.value = currentList + medicines
                    lastPrescriptionDocument = lastDoc

                    Log.d(TAG, "🔍 전문의약품 검색 추가: ${medicines.size}개")
                }
            } finally {
                _isLoadingPrescription.value = false
            }
        }
    }

    // ========== 약품 상세 조회 ==========

    /**
     * 약품 ID로 일반의약품 조회
     */
    suspend fun getMedicineById(medicineId: String): Medicine? {
        return medicineRepository.getMedicineById(medicineId)
    }

    /**
     * 약품 ID로 전문의약품 조회
     */
    suspend fun getPrescriptionMedicineById(medicineId: String): PrescriptionMedicine? {
        return medicineRepository.getPrescriptionMedicineById(medicineId)
    }

    // ========== 찜 기능 ==========

    /**
     * 찜 여부 확인
     */
    suspend fun isFavorite(medicineId: String): Boolean {
        return favoriteRepository.isFavorite(medicineId)
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
                    medicineType = "general",
                    imageUrl = medicine.image_url ?: ""
                )
                favoriteRepository.addFavorite(favorite)
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
                favoriteRepository.addFavorite(favorite)
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
                favoriteRepository.removeFavoriteById(medicineId)
                Log.d(TAG, "찜 해제: $medicineId")
            } catch (e: Exception) {
                Log.e(TAG, "찜 해제 실패", e)
                _errorMessage.value = "찜 해제에 실패했습니다."
            }
        }
    }

    /**
     * 타입별 찜 목록 조회
     */
    fun getFavoritesByType(type: String): Flow<List<FavoriteMedicineEntity>> {
        return favoriteRepository.getFavoritesByType(type)
    }

    // ========== 메모 기능 ==========

    /**
     * 메모 저장/수정 (일반의약품만)
     */
    fun saveMemo(medicine: Medicine, memo: String) {
        viewModelScope.launch {
            try {
                favoriteRepository.saveMemo(medicine, memo)

                if (memo.isBlank()) {
                    Log.d(TAG, "메모 삭제: ${medicine.medicine_name}")
                } else {
                    Log.d(TAG, "메모 저장: ${medicine.medicine_name}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "메모 저장 실패", e)
                _errorMessage.value = "메모 저장에 실패했습니다."
            }
        }
    }

    /**
     * 메모 조회
     */
    suspend fun getMemo(medicineId: String): String? {
        return favoriteRepository.getMemo(medicineId)
    }

    /**
     * 메모 있는지 확인
     */
    suspend fun hasMemo(medicineId: String): Boolean {
        val memo = favoriteRepository.getMemo(medicineId)
        return !memo.isNullOrBlank()
    }
}