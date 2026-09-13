package com.example.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PrayerLogDao {
    @Query("SELECT * FROM prayer_logs WHERE date = :date")
    fun getLogsForDate(date: String): Flow<List<PrayerLogEntity>>

    @Query("SELECT * FROM prayer_logs ORDER BY timestamp DESC LIMIT 100")
    fun getRecentLogs(): Flow<List<PrayerLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateLog(log: PrayerLogEntity)

    @Query("SELECT COUNT(*) FROM prayer_logs WHERE status = 'PRAYED' OR status = 'PRAYED_LATE'")
    fun getTotalPrayedCount(): Flow<Int>

    @Query("DELETE FROM prayer_logs WHERE id = :id")
    suspend fun deleteLog(id: Long)
}

@Dao
interface BookmarkDao {
    @Query("SELECT * FROM bookmarks ORDER BY createdAt DESC")
    fun getAllBookmarks(): Flow<List<BookmarkEntity>>

    @Query("SELECT * FROM bookmarks WHERE surahNumber = :surah AND ayahNumber = :ayah LIMIT 1")
    suspend fun getBookmark(surah: Int, ayah: Int): BookmarkEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: BookmarkEntity): Long

    @Query("DELETE FROM bookmarks WHERE surahNumber = :surah AND ayahNumber = :ayah")
    suspend fun deleteBookmark(surah: Int, ayah: Int)

    @Query("DELETE FROM bookmarks WHERE id = :id")
    suspend fun deleteById(id: Long)
}

@Dao
interface KhatmahDao {
    @Query("SELECT * FROM khatmah_plans WHERE isActive = 1 LIMIT 1")
    fun getActivePlan(): Flow<KhatmahPlanEntity?>

    @Query("SELECT * FROM khatmah_plans ORDER BY startDateMillis DESC")
    fun getAllPlans(): Flow<List<KhatmahPlanEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlan(plan: KhatmahPlanEntity): Long

    @Update
    suspend fun updatePlan(plan: KhatmahPlanEntity)

    @Query("UPDATE khatmah_plans SET currentPage = :page, lastReadDate = :date WHERE id = :id")
    suspend fun updateProgress(id: Long, page: Int, date: String)

    @Query("DELETE FROM khatmah_plans WHERE id = :id")
    suspend fun deletePlan(id: Long)
}

@Dao
interface AdhanSettingsDao {
    @Query("SELECT * FROM adhan_settings")
    fun getAllSettings(): Flow<List<AdhanSettingsEntity>>

    @Query("SELECT * FROM adhan_settings WHERE prayerName = :prayerName LIMIT 1")
    suspend fun getSettingsForPrayer(prayerName: String): AdhanSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSettings(settings: AdhanSettingsEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(settings: List<AdhanSettingsEntity>)
}
