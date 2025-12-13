package com.example.altong_v2.data.local.dao

import androidx.room.*
import com.example.altong_v2.data.local.entity.MedicationLog
import java.util.Date

@Dao
interface MedicationLogDao {
    @Insert
    suspend fun insert(log: MedicationLog): Long
    @Update
    suspend fun update(log: MedicationLog)
    @Query("""
        SELECT * FROM medication_log 
        WHERE prescription_id = :prescriptionId 
        AND drug_name = :drugName 
        AND time_slot = :timeSlot 
        AND DATE(scheduled_date/1000, 'unixepoch') = DATE(:date/1000, 'unixepoch')
        LIMIT 1
    """)
    suspend fun getLog(
        prescriptionId: Long,
        drugName: String,
        timeSlot: String,
        date: Long
    ): MedicationLog?

    @Query("""
        SELECT * FROM medication_log 
        WHERE DATE(scheduled_date/1000, 'unixepoch') = DATE(:date/1000, 'unixepoch')
        AND taken = 1
    """)
    suspend fun getTakenLogsForDate(date: Long): List<MedicationLog>

    @Query("""
        SELECT * FROM medication_log 
        WHERE prescription_id = :prescriptionId
        ORDER BY scheduled_date DESC
    """)
    suspend fun getLogsForPrescription(prescriptionId: Long): List<MedicationLog>
}