package com.example.altong_v2.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.util.Date

@Entity(
    tableName = "medication_log",
    foreignKeys = [
        ForeignKey(
            entity = PrescriptionEntity::class,
            parentColumns = ["id"],
            childColumns = ["prescription_id"],
            onDelete = ForeignKey.Companion.CASCADE
        )
    ]
)
data class MedicationLog(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "log_id")
    val logId: Long = 0,

    @ColumnInfo(name = "prescription_id", index = true)
    val prescriptionId: Long,

    @ColumnInfo(name = "drug_name")
    val drugName: String,

    @ColumnInfo(name = "time_slot")
    val timeSlot: String,  // morning, lunch, dinner, bedtime

    @ColumnInfo(name = "scheduled_date")
    val scheduledDate: Date,  // 복용 예정 날짜

    @ColumnInfo(name = "taken")
    val taken: Boolean,  // 복용 완료 여부

    @ColumnInfo(name = "taken_at")
    val takenAt: Date? = null,  // 실제 복용 시간

    @ColumnInfo(name = "created_at")
    val createdAt: Date = Date()
)