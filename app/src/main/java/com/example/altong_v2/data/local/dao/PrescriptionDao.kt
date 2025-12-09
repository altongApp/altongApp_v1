package com.example.altong_v2.data.local.dao


import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.altong_v2.data.local.entity.PrescriptionEntity
import kotlinx.coroutines.flow.Flow


/*
 * 처방전 DAO
 * 처방전 테이블에 대한 CRUD 작업 정의
*/

@Dao
interface PrescriptionDao {

    // 처방전 추가
    @Insert
    suspend fun insert(prescription: PrescriptionEntity): Long  // 생성된 ID 반환

    // 처방전 수정
    @Update
    suspend fun update(prescription: PrescriptionEntity)

    // 처방전 삭제
    @Delete
    suspend fun delete(prescription: PrescriptionEntity)

    // 모든 처방전 조회 (최신순으로 가져와라)
    @Query("SELECT * FROM prescriptions ORDER BY date DESC")
    fun getAllPrescriptions(): Flow<List<PrescriptionEntity>>

    // ID로 처방전 조회
    @Query("SELECT * FROM prescriptions WHERE id = :id")
    suspend fun getPrescriptionById(id: Long): PrescriptionEntity?

    // 진단명으로 검색
    @Query("SELECT * FROM prescriptions WHERE diagnosis LIKE '%' || :query || '%' ORDER BY date DESC")
    suspend fun searchByDiagnosis(query: String): List<PrescriptionEntity>

    // 날짜 범위로 조회
    @Query("SELECT * FROM prescriptions WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    suspend fun getPrescriptionsByDateRange(startDate: String, endDate: String): List<PrescriptionEntity>

    // 모든 처방전 삭제 (개발/테스트용)
    @Query("DELETE FROM prescriptions")
    suspend fun deleteAll()

    // 처방전 개수 조회
    @Query("SELECT COUNT(*) FROM prescriptions")
    suspend fun getCount(): Int
}