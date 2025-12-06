package com.example.altong_v2.data.repository

import android.util.Log
import com.example.altong_v2.data.local.dao.FavoriteMedicineDao
import com.example.altong_v2.data.local.entity.FavoriteMedicineEntity
import com.example.altong_v2.data.model.Medicine
import com.example.altong_v2.data.model.PrescriptionMedicine
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await

/**
 * 약품 검색 Repository
 * Firebase Firestore에서 약품 정보 조회 + 찜 기능 관리
 */
class MedicineRepository(
    private val favoriteMedicineDao: FavoriteMedicineDao
) {
    private val firestore = FirebaseFirestore.getInstance()
    private val TAG = "MedicineRepository"

    companion object {
        private const val COLLECTION_MEDICINES = "medicines"
        private const val COLLECTION_PRESCRIPTION = "prescription_medicines"
        private const val PAGE_SIZE = 20
    }

    // ========== Firebase 일반의약품 조회 ==========

    /**
     * 일반의약품 목록 조회 (페이지네이션)
     * @param lastDocument 마지막 문서 (다음 페이지 로드용)
     * @return Pair<약품 리스트, 마지막 문서>
     */
    suspend fun getGeneralMedicines(
        lastDocument: DocumentSnapshot? = null
    ): Pair<List<Medicine>, DocumentSnapshot?> {
        return try {
            var query: Query = firestore.collection(COLLECTION_MEDICINES)
                .whereEqualTo("medicine_type", "otc")
                .orderBy("medicine_name")
                .limit(PAGE_SIZE.toLong())

            // 페이지네이션: 마지막 문서 이후부터 조회
            if (lastDocument != null) {
                query = query.startAfter(lastDocument)
            }

            val snapshot = query.get().await()
            val medicines = snapshot.documents.mapNotNull { doc ->
                try {
                    doc.toObject(Medicine::class.java)
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing medicine: ${doc.id}", e)
                    null
                }
            }

            val last = snapshot.documents.lastOrNull()
            Pair(medicines, last)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting general medicines", e)
            Pair(emptyList(), null)
        }
    }

    /**
     * 카테고리별 일반의약품 조회
     * @param category 카테고리명 (예: "감기/호흡기")
     */
    suspend fun getMedicinesByCategory(
        category: String,
        lastDocument: DocumentSnapshot? = null
    ): Pair<List<Medicine>, DocumentSnapshot?> {
        return try {
            var query: Query = firestore.collection(COLLECTION_MEDICINES)
                .whereEqualTo("medicine_type", "otc")
                .whereArrayContains("categories", category)
                .orderBy("medicine_name")
                .limit(PAGE_SIZE.toLong())

            if (lastDocument != null) {
                query = query.startAfter(lastDocument)
            }

            val snapshot = query.get().await()
            val medicines = snapshot.documents.mapNotNull { doc ->
                try {
                    doc.toObject(Medicine::class.java)
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing medicine by category: ${doc.id}", e)
                    null
                }
            }

            val last = snapshot.documents.lastOrNull()
            Pair(medicines, last)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting medicines by category: $category", e)
            Pair(emptyList(), null)
        }
    }

    /**
     * 일반의약품 검색 (약품명, 제조사)
     */
    suspend fun searchGeneralMedicines(
        query: String,
        lastDocument: DocumentSnapshot? = null
    ): Pair<List<Medicine>, DocumentSnapshot?> {
        return try {
            // Firestore는 부분 문자열 검색 불가능
            // 전체 데이터를 가져와서 클라이언트에서 필터링하는 방식 사용
            // 또는 검색어 시작 문자로 범위 검색
            var firestoreQuery: Query = firestore.collection(COLLECTION_MEDICINES)
                .whereEqualTo("medicine_type", "otc")
                .orderBy("medicine_name")
                .startAt(query)
                .endAt(query + "\uf8ff")
                .limit(PAGE_SIZE.toLong())

            if (lastDocument != null) {
                firestoreQuery = firestoreQuery.startAfter(lastDocument)
            }

            val snapshot = firestoreQuery.get().await()
            val medicines = snapshot.documents.mapNotNull { doc ->
                try {
                    doc.toObject(Medicine::class.java)
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing medicine in search: ${doc.id}", e)
                    null
                }
            }

            val last = snapshot.documents.lastOrNull()
            Pair(medicines, last)
        } catch (e: Exception) {
            Log.e(TAG, "Error searching general medicines: $query", e)
            Pair(emptyList(), null)
        }
    }

    // ========== Firebase 전문의약품 조회 ==========

    /**
     * 전문의약품 목록 조회 (페이지네이션)
     */
    suspend fun getPrescriptionMedicines(
        lastDocument: DocumentSnapshot? = null
    ): Pair<List<PrescriptionMedicine>, DocumentSnapshot?> {
        return try {
            var query: Query = firestore.collection(COLLECTION_PRESCRIPTION)
                .whereEqualTo("medicine_type", "prescription")
                .orderBy("medicine_name")
                .limit(PAGE_SIZE.toLong())

            if (lastDocument != null) {
                query = query.startAfter(lastDocument)
            }

            val snapshot = query.get().await()
            val medicines = snapshot.documents.mapNotNull { doc ->
                try {
                    doc.toObject(PrescriptionMedicine::class.java)
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing prescription medicine: ${doc.id}", e)
                    null
                }
            }

            val last = snapshot.documents.lastOrNull()
            Pair(medicines, last)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting prescription medicines", e)
            Pair(emptyList(), null)
        }
    }

    /**
     * 전문의약품 검색
     */
    suspend fun searchPrescriptionMedicines(
        query: String,
        lastDocument: DocumentSnapshot? = null
    ): Pair<List<PrescriptionMedicine>, DocumentSnapshot?> {
        return try {
            var firestoreQuery: Query = firestore.collection(COLLECTION_PRESCRIPTION)
                .whereEqualTo("medicine_type", "prescription")
                .orderBy("medicine_name")
                .startAt(query)
                .endAt(query + "\uf8ff")
                .limit(PAGE_SIZE.toLong())

            if (lastDocument != null) {
                firestoreQuery = firestoreQuery.startAfter(lastDocument)
            }

            val snapshot = firestoreQuery.get().await()
            val medicines = snapshot.documents.mapNotNull { doc ->
                try {
                    doc.toObject(PrescriptionMedicine::class.java)
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing prescription medicine in search: ${doc.id}", e)
                    null
                }
            }

            val last = snapshot.documents.lastOrNull()
            Pair(medicines, last)
        } catch (e: Exception) {
            Log.e(TAG, "Error searching prescription medicines: $query", e)
            Pair(emptyList(), null)
        }
    }

    /**
     * 약품 ID로 일반의약품 상세 조회
     */
    suspend fun getMedicineById(medicineId: String): Medicine? {
        return try {
            val doc = firestore.collection(COLLECTION_MEDICINES)
                .document(medicineId)
                .get()
                .await()
            doc.toObject(Medicine::class.java)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting medicine by ID: $medicineId", e)
            null
        }
    }

    /**
     * 약품 ID로 전문의약품 상세 조회
     */
    suspend fun getPrescriptionMedicineById(medicineId: String): PrescriptionMedicine? {
        return try {
            val doc = firestore.collection(COLLECTION_PRESCRIPTION)
                .document(medicineId)
                .get()
                .await()
            doc.toObject(PrescriptionMedicine::class.java)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting prescription medicine by ID: $medicineId", e)
            null
        }
    }

    // ========== 찜 기능 (Room DB 사용) ==========

    /**
     * 모든 찜한 약품 조회
     */
    val allFavorites: Flow<List<FavoriteMedicineEntity>> =
        favoriteMedicineDao.getAllFavorites()

    /**
     * 타입별 찜 목록 조회 (일반의약품 or 전문의약품)
     */
    fun getFavoritesByType(type: String): Flow<List<FavoriteMedicineEntity>> {
        return favoriteMedicineDao.getFavoritesByType(type)
    }

    /**
     * 찜 추가
     */
    suspend fun addFavorite(favorite: FavoriteMedicineEntity): Long {
        return favoriteMedicineDao.insert(favorite)
    }

    /**
     * 찜 삭제
     */
    suspend fun removeFavorite(favorite: FavoriteMedicineEntity) {
        favoriteMedicineDao.delete(favorite)
    }

    /**
     * 약품 ID로 찜 삭제
     */
    suspend fun removeFavoriteById(medicineId: String) {
        favoriteMedicineDao.deleteByMedicineId(medicineId)
    }

    /**
     * 찜 여부 확인
     */
    suspend fun isFavorite(medicineId: String): Boolean {
        return favoriteMedicineDao.isFavorite(medicineId)
    }

    /**
     * 찜 메모 수정
     */
    suspend fun updateFavorite(favorite: FavoriteMedicineEntity) {
        favoriteMedicineDao.update(favorite)
    }

    /**
     * 찜 개수
     */
    suspend fun getFavoriteCount(): Int {
        return favoriteMedicineDao.getCount()
    }
}