package com.example.altong_v2.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.altong_v2.data.local.dao.DrugCompletionDao
import com.example.altong_v2.data.local.dao.DrugDao
import com.example.altong_v2.data.local.dao.FavoriteMedicineDao
import com.example.altong_v2.data.local.dao.PrescriptionDao
import com.example.altong_v2.data.local.entity.DrugCompletionEntity
import com.example.altong_v2.data.local.entity.DrugEntity
import com.example.altong_v2.data.local.entity.FavoriteMedicineEntity
import com.example.altong_v2.data.local.entity.PrescriptionEntity

/**
 * Room Database 메인 클래스
 * 싱글톤 패턴으로 앱 전체에서 하나의 인스턴스만 사용
 *
 * 📝 Version History:
 * - v1: 초기 버전 (처방전, 처방약, 찜, 복용완료)
 * - v2: FavoriteMedicineEntity에 isFavorite 필드 추가 (찜/메모 분리)
 */
@Database(
    entities = [
        PrescriptionEntity::class,      // 처방전 (친구)
        DrugEntity::class,              // 처방약 (친구)
        FavoriteMedicineEntity::class,  // 찜 (너 - 수정됨!)
        DrugCompletionEntity::class     // 복용 완료 (친구)
    ],
    version = 2,  // ⭐ 1 → 2로 증가!
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    // DAO 인스턴스를 제공하는 추상 메서드
    abstract fun prescriptionDao(): PrescriptionDao
    abstract fun drugDao(): DrugDao
    abstract fun favoriteMedicineDao(): FavoriteMedicineDao
    abstract fun drugCompletionDao(): DrugCompletionDao

    companion object {
        // 싱글톤 패턴
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Database 인스턴스 가져오기
         * 없으면 생성, 있으면 기존 인스턴스 반환
         */
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "altong_database"  // 우리 DB파일명
                )
                    .fallbackToDestructiveMigration()  // ⭐ 개발 중에만 사용 (데이터 삭제됨)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}