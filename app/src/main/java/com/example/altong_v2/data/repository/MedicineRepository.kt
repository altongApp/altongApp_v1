package com.example.altong_v2.data.repository

import android.util.Log
import com.example.altong_v2.data.model.Medicine
import com.example.altong_v2.data.model.PrescriptionMedicine
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.coroutineScope


/* * 약품 검색 Repository
 * Firebase Firestore에서 약품 정보 조회
 * (찜 기능은 FavoriteMedicineRepository로 분리)*/

class MedicineRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val TAG = "MedicineRepository"

    companion object {
        private const val COLLECTION_MEDICINES = "medicines"
        private const val COLLECTION_PRESCRIPTION = "prescription_medicines"
        private const val PAGE_SIZE = 20
    }

    // ========== Firebase 일반의약품 조회 ==========

/*   * 일반의약품 목록 조회 (페이지네이션)
     * @param lastDocument 마지막 문서 (다음 페이지 로드용)
     * @return Pair<약품 리스트, 마지막 문서> */
    suspend fun getGeneralMedicines(
        lastDocument: DocumentSnapshot? = null
    ): Pair<List<Medicine>, DocumentSnapshot?> {
        return try {
            Log.d(TAG, "🔍 일반의약품 Firebase 쿼리 시작...")
            var query: Query = firestore.collection(COLLECTION_MEDICINES)
                .orderBy("medicine_name")
                .limit(PAGE_SIZE.toLong())

            // 페이지네이션: 마지막 문서 이후부터 조회
            if (lastDocument != null) {
                query = query.startAfter(lastDocument)
            }
            Log.d(TAG, "📡 Firebase 데이터 요청 보냄... (응답 대기 중)")
            val snapshot = query.get().await()

            Log.d(TAG, "📦 [디버그] 응답 도착! 문서 개수: ${snapshot.documents.size}")
            val medicines = snapshot.documents.mapNotNull { doc ->
                try {
                    Log.d(TAG, "👉 [디버그] 파싱 시도 ID: ${doc.id}")

                    val rawCats = doc.get("categories")
                    Log.d(TAG, "🧐 [디버그] categories 값: $rawCats / 타입: ${rawCats?.javaClass?.simpleName}")

                    val parsed = doc.toObject(Medicine::class.java)
                    Log.d(TAG, "✅ [디버그] 파싱 성공: ${parsed?.medicine_name}")
                    parsed
                } catch (e: Exception) {
                    Log.e(TAG, "❌ [디버그] 파싱 대실패!!! ID: ${doc.id} / 에러: ${e.message}", e)
                    null
                }
            }

            Log.d(TAG, "🎉 최종 리스트에 담긴 약품 개수: ${medicines.size}")
            val last = snapshot.documents.lastOrNull()
            Pair(medicines, last)
        } catch (e: Exception) {
            Log.e(TAG, "💥 [디버그] Firebase 통신 자체 에러!!!", e)
            Pair(emptyList(), null)
        }
    }

/*   * 카테고리별 일반의약품 조회
     * @param category 카테고리명 (예: "감기/호흡기")*/
    suspend fun getMedicinesByCategory(
        category: String,
        lastDocument: DocumentSnapshot? = null
    ): Pair<List<Medicine>, DocumentSnapshot?> = coroutineScope {
        try {
            Log.d(TAG, "🔍 카테고리 검색: $category")

            // ⭐ 작은따옴표 포함해서 쿼리
            val categoryWithQuotes = "'$category'"

            var query: Query = firestore.collection(COLLECTION_MEDICINES)
                .whereArrayContains("categories", categoryWithQuotes)  // ← 작은따옴표 포함!
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

                    // 파싱 시 작은따옴표 제거
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
            Log.e(TAG, "에러", e)
            Pair(emptyList(), null)
        }
    }

    // 일반의약품 검색 (약품명, 제조사)
    suspend fun searchGeneralMedicines(
        query: String,
        lastDocument: DocumentSnapshot? = null
    ): Pair<List<Medicine>, DocumentSnapshot?> {
        return try {
            Log.d(TAG, "🔍 검색 쿼리: $query")

            // Firestore는 부분 문자열 검색 불가능
            // 검색어 시작 문자로 범위 검색
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


     // 전문의약품 목록 조회 (페이지네이션)
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
            Log.e(TAG, "Error getting prescription medicines", e)
            Pair(emptyList(), null)
        }
    }


     //전문의약품 검색
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

     // 약품 ID로 일반의약품 상세 조회
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

     // 약품 ID로 전문의약품 상세 조회
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
}