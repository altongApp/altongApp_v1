package com.example.altong_v2.data.repository

import android.util.Log
import com.example.altong_v2.data.model.Medicine
import com.example.altong_v2.data.model.PrescriptionMedicine
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.coroutineScope

/**
 * 약품 검색 Repository
 * Firebase Firestore에서 약품 정보 조회
 * (찜 기능은 FavoriteMedicineRepository로 분리)
 */
class MedicineRepository {
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
     */
    suspend fun getGeneralMedicines(
        lastDocument: DocumentSnapshot? = null
    ): Pair<List<Medicine>, DocumentSnapshot?> {
        return try {
            Log.d(TAG, "🔍 일반의약품 Firebase 쿼리 시작...")

            var query: Query = firestore.collection(COLLECTION_MEDICINES)
                .orderBy("medicine_name")
                .limit(PAGE_SIZE.toLong())

            if (lastDocument != null) {
                query = query.startAfter(lastDocument)
            }

            Log.d(TAG, "📡 Firebase 데이터 요청 보냄...")
            val snapshot = query.get().await()

            Log.d(TAG, "📦 응답 도착! 문서 개수: ${snapshot.documents.size}")

            val medicines = snapshot.documents.mapNotNull { doc ->
                try {
                    val rawCats = doc.get("categories")
                    Log.d(TAG, "🧐 categories 값: $rawCats / 타입: ${rawCats?.javaClass?.simpleName}")

                    val parsed = doc.toObject(Medicine::class.java)
                    Log.d(TAG, "✅ 파싱 성공: ${parsed?.medicine_name}")
                    parsed
                } catch (e: Exception) {
                    Log.e(TAG, "❌ 파싱 실패! ID: ${doc.id}", e)
                    null
                }
            }

            Log.d(TAG, "🎉 최종 약품 개수: ${medicines.size}")
            val last = snapshot.documents.lastOrNull()
            Pair(medicines, last)
        } catch (e: Exception) {
            Log.e(TAG, "💥 Firebase 통신 에러", e)
            Pair(emptyList(), null)
        }
    }

    /**
     * 카테고리별 일반의약품 조회
     */
    suspend fun getMedicinesByCategory(
        category: String,
        lastDocument: DocumentSnapshot? = null
    ): Pair<List<Medicine>, DocumentSnapshot?> = coroutineScope {
        try {
            Log.d(TAG, "🔍 카테고리 검색: $category")

            val categoryWithQuotes = "'$category'"

            var query: Query = firestore.collection(COLLECTION_MEDICINES)
                .whereArrayContains("categories", categoryWithQuotes)
                .orderBy("medicine_name")
                .limit(PAGE_SIZE.toLong())

            if (lastDocument != null) {
                query = query.startAfter(lastDocument)
            }

            val snapshot = query.get().await()
            Log.d(TAG, "📦 받은 문서 개수: ${snapshot.documents.size}")

            val medicines = snapshot.documents.mapNotNull { doc ->
                try {
                    val medicine = doc.toObject(Medicine::class.java)
                    medicine?.copy(
                        categories = medicine.categories.map { cat ->
                            cat.trim().trim('\'').trim('"')
                        }
                    )
                } catch (e: Exception) {
                    null
                }
            }

            Pair(medicines, snapshot.documents.lastOrNull())

        } catch (e: Exception) {
            Log.e(TAG, "카테고리 조회 에러", e)
            Pair(emptyList(), null)
        }
    }

    /**
     * 일반의약품 검색
     */
    suspend fun searchGeneralMedicines(
        query: String,
        lastDocument: DocumentSnapshot? = null
    ): Pair<List<Medicine>, DocumentSnapshot?> {
        return try {
            Log.d(TAG, "🔍 검색 쿼리: $query")

            var firestoreQuery: Query = firestore.collection(COLLECTION_MEDICINES)
                .orderBy("medicine_name")
                .startAt(query)
                .endAt(query + "\uf8ff")
                .limit(PAGE_SIZE.toLong())

            if (lastDocument != null) {
                firestoreQuery = firestoreQuery.startAfter(lastDocument)
            }

            Log.d(TAG, "📡 검색 데이터 요청 중...")
            val snapshot = firestoreQuery.get().await()
            Log.d(TAG, "📦 검색 결과: ${snapshot.documents.size}개")

            val medicines = snapshot.documents.mapNotNull { doc ->
                try {
                    doc.toObject(Medicine::class.java)
                } catch (e: Exception) {
                    Log.e(TAG, "검색 파싱 실패: ${doc.id}", e)
                    null
                }
            }

            val last = snapshot.documents.lastOrNull()
            Pair(medicines, last)
        } catch (e: Exception) {
            Log.e(TAG, "일반의약품 검색 에러: $query", e)
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
            Log.e(TAG, "약품 상세 조회 에러: $medicineId", e)
            null
        }
    }

    // ========== Firebase 전문의약품 조회 ==========

    /**
     * 전문의약품 목록 조회
     */
    suspend fun getPrescriptionMedicines(
        lastDocument: DocumentSnapshot? = null
    ): Pair<List<PrescriptionMedicine>, DocumentSnapshot?> {
        return try {
            Log.d(TAG, "🔍 전문의약품 Firebase 쿼리 시작...")

            var query: Query = firestore.collection(COLLECTION_PRESCRIPTION)
                .orderBy("medicine_name")
                .limit(PAGE_SIZE.toLong())

            if (lastDocument != null) {
                query = query.startAfter(lastDocument)
            }

            Log.d(TAG, "📡 전문의약품 데이터 요청 중...")
            val snapshot = query.get().await()
            Log.d(TAG, "📦 받은 문서 개수: ${snapshot.documents.size}")

            val medicines = snapshot.documents.mapNotNull { doc ->
                try {
                    Log.d(TAG, "✅ 문서 파싱: ${doc.id}")
                    doc.toObject(PrescriptionMedicine::class.java)
                } catch (e: Exception) {
                    Log.e(TAG, "❌ 파싱 실패: ${doc.id}", e)
                    null
                }
            }

            Log.d(TAG, "🎉 최종 약품 개수: ${medicines.size}")

            val last = snapshot.documents.lastOrNull()
            Pair(medicines, last)
        } catch (e: Exception) {
            Log.e(TAG, "전문의약품 조회 에러", e)
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
            Log.d(TAG, "🔍 전문의약품 검색 쿼리: $query")

            var firestoreQuery: Query = firestore.collection(COLLECTION_PRESCRIPTION)
                .orderBy("medicine_name")
                .startAt(query)
                .endAt(query + "\uf8ff")
                .limit(PAGE_SIZE.toLong())

            if (lastDocument != null) {
                firestoreQuery = firestoreQuery.startAfter(lastDocument)
            }

            Log.d(TAG, "📡 전문의약품 검색 데이터 요청 중...")
            val snapshot = firestoreQuery.get().await()
            Log.d(TAG, "📦 검색 결과: ${snapshot.documents.size}개")

            val medicines = snapshot.documents.mapNotNull { doc ->
                try {
                    doc.toObject(PrescriptionMedicine::class.java)
                } catch (e: Exception) {
                    Log.e(TAG, "검색 파싱 실패: ${doc.id}", e)
                    null
                }
            }

            val last = snapshot.documents.lastOrNull()
            Pair(medicines, last)
        } catch (e: Exception) {
            Log.e(TAG, "전문의약품 검색 에러: $query", e)
            Pair(emptyList(), null)
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
            Log.e(TAG, "전문의약품 상세 조회 에러: $medicineId", e)
            null
        }
    }
}