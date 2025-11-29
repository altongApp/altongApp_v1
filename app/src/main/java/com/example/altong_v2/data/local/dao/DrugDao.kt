package com.example.altong_v2.data.local.dao


import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.altong_v2.data.local.entity.DrugEntity
import kotlinx.coroutines.flow.Flow


/*
 * 약품 DAO
 * 약품 테이블에 대한 CRUD 작업 정의
*/

@Dao
interface DrugDao {

    // 약품 추가
    @Insert
    suspend fun insert(drug: DrugEntity): Long  // 생성된 ID 반환

    // 약품 수정
    @Update
    suspend fun update(drug: DrugEntity)

    // 약품 삭제
    @Delete
    suspend fun delete(drug: DrugEntity)

    // 특정 처방전의 모든 약품 조회
    @Query("SELECT * FROM drugs WHERE prescription_id = :prescriptionId")
    fun getDrugsByPrescription(prescriptionId: Long): Flow<List<DrugEntity>>

    // ID로 약품 조회
    @Query("SELECT * FROM drugs WHERE id = :id")
    suspend fun getDrugById(id: Long): DrugEntity?

    // 모든 약품 조회 (캘린더용)
    @Query("SELECT * FROM drugs")
    fun getAllDrugs(): Flow<List<DrugEntity>>

    // 약품명으로 검색
    @Query("SELECT * FROM drugs WHERE name LIKE '%' || :query || '%'")
    suspend fun searchByName(query: String): List<DrugEntity>

    // 특정 처방전의 약품 개수
    @Query("SELECT COUNT(*) FROM drugs WHERE prescription_id = :prescriptionId")
    suspend fun getDrugCountByPrescription(prescriptionId: Long): Int

    // 특정 처방전의 약품 삭제 (CASCADE로 자동 삭제되지만 명시적으로도 가능함.)
    @Query("DELETE FROM drugs WHERE prescription_id = :prescriptionId")
    suspend fun deleteByPrescription(prescriptionId: Long)

    // 모든 약품 삭제 (테스트용)
    @Query("DELETE FROM drugs")
    suspend fun deleteAll()
}




