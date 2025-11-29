package com.example.altong_v2.data.local.dao


import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.altong_v2.data.local.entity.DrugCompletionEntity
import kotlinx.coroutines.flow.Flow


/* * 복용 완료 기록 DAO
 * 캘린더 체크리스트용 데이터 접근*/

@Dao
interface DrugCompletionDao {

    // 복용 기록 추가/업데이트 (중복 시 교체)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(completion: DrugCompletionEntity): Long

    // 복용 기록 수정
    @Update
    suspend fun update(completion: DrugCompletionEntity)

    // 특정 날짜의 모든 복용 기록 조회
    @Query("SELECT * FROM drug_completions WHERE date = :date")
    fun getCompletionsByDate(date: String): Flow<List<DrugCompletionEntity>>

    // 특정 약품의 특정 날짜 복용 기록 조회
    @Query("SELECT * FROM drug_completions WHERE drug_id = :drugId AND date = :date")
    suspend fun getCompletion(drugId: Long, date: String): DrugCompletionEntity?

    // 특정 약품의 복용 완료 여부 확인
    @Query("SELECT is_completed FROM drug_completions WHERE drug_id = :drugId AND date = :date")
    suspend fun isCompleted(drugId: Long, date: String): Boolean?

    // 특정 날짜의 복용 완료 개수
    @Query("SELECT COUNT(*) FROM drug_completions WHERE date = :date AND is_completed = 1")
    suspend fun getCompletedCount(date: String): Int

    // 특정 날짜의 전체 복용 개수
    @Query("SELECT COUNT(*) FROM drug_completions WHERE date = :date")
    suspend fun getTotalCount(date: String): Int

    // 날짜 범위의 복용 기록 조회
    @Query("SELECT * FROM drug_completions WHERE date BETWEEN :startDate AND :endDate")
    fun getCompletionsByDateRange(startDate: String, endDate: String): Flow<List<DrugCompletionEntity>>

    // 복용 완료 토글
    @Query("UPDATE drug_completions SET is_completed = :isCompleted, completed_at = :completedAt WHERE drug_id = :drugId AND date = :date")
    suspend fun updateCompletion(drugId: Long, date: String, isCompleted: Boolean, completedAt: Long?)

    // 모든 기록 삭제 (테스트용)
    @Query("DELETE FROM drug_completions")
    suspend fun deleteAll()
}