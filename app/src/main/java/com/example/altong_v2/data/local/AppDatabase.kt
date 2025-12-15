package com.example.altong_v2.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.altong_v2.data.local.dao.DrugCompletionDao
import com.example.altong_v2.data.local.dao.DrugDao
import com.example.altong_v2.data.local.dao.FavoriteMedicineDao
import com.example.altong_v2.data.local.dao.MedicationLogDao
import com.example.altong_v2.data.local.dao.PrescriptionDao
import com.example.altong_v2.data.local.entity.DrugCompletionEntity
import com.example.altong_v2.data.local.entity.DrugEntity
import com.example.altong_v2.data.local.entity.FavoriteMedicineEntity
import com.example.altong_v2.data.local.entity.MedicationLog
import com.example.altong_v2.data.local.entity.PrescriptionEntity
import androidx.room.TypeConverters
import com.example.altong_v2.data.local.Converters

/**
 * Room Database 메인 클래스
 * 싱글톤 패턴으로 앱 전체에서 하나의 인스턴스만 사용
 *
 * 📝 Version History:
 * - v1: 초기 버전 (처방전, 처방약, 찜, 복용완료)
 * - v2: FavoriteMedicineEntity에 isFavorite 필드 추가 (찜/메모 분리)
 */

@TypeConverters(Converters::class)
@Database(
    entities = [
        PrescriptionEntity::class,
        DrugEntity::class,
        FavoriteMedicineEntity::class,
        DrugCompletionEntity::class,
        MedicationLog::class
    ],
    version = 3,    // 캘린더 작업 버전 3 변경
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    // DAO 인스턴스를 제공하는 추상 메서드
    abstract fun prescriptionDao(): PrescriptionDao
    abstract fun drugDao(): DrugDao
    abstract fun favoriteMedicineDao(): FavoriteMedicineDao
    abstract fun drugCompletionDao(): DrugCompletionDao
    abstract fun medicationLogDao(): MedicationLogDao

    companion object {
        // 싱글톤 패턴
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // medication_log 테이블 생성
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `medication_log` (
                        `log_id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `prescription_id` INTEGER NOT NULL,
                        `drug_name` TEXT NOT NULL,
                        `time_slot` TEXT NOT NULL,
                        `scheduled_date` INTEGER NOT NULL,
                        `taken` INTEGER NOT NULL,
                        `taken_at` INTEGER,
                        `created_at` INTEGER NOT NULL,
                        FOREIGN KEY(`prescription_id`) REFERENCES `prescriptions`(`id`) ON DELETE CASCADE
                    )
                """)

                // 인덱스 생성
                database.execSQL("""
                    CREATE INDEX IF NOT EXISTS `index_medication_log_prescription_id` 
                    ON `medication_log` (`prescription_id`)
                """)
            }
        }

         //Database 인스턴스 가져오기
         //없으면 생성, 있으면 기존 인스턴스 반환
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "altong_database"  // 우리 DB파일명
                )
                    //.addMigrations(MIGRATION_1_2, MIGRATION_2_3)   // 마이그레이션 추가
                    .fallbackToDestructiveMigration()  // 개발 중에만 사용 (데이터 삭제됨)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}