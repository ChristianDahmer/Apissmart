package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entities.HivePhotoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HivePhotoDao {
    @Query("SELECT * FROM hive_photos WHERE hiveId = :hiveId ORDER BY dateTaken DESC")
    fun getPhotosByHive(hiveId: Long): Flow<List<HivePhotoEntity>>

    @Query("SELECT * FROM hive_photos ORDER BY dateTaken DESC")
    fun getAllPhotos(): Flow<List<HivePhotoEntity>>

    @Query("SELECT * FROM hive_photos ORDER BY id ASC")
    suspend fun getAllPhotosOnce(): List<HivePhotoEntity>

    @Query("SELECT * FROM hive_photos WHERE hiveId = :hiveId ORDER BY dateTaken DESC LIMIT 1")
    fun getLatestPhotoForHive(hiveId: Long): Flow<HivePhotoEntity?>

    @Query("SELECT COUNT(*) FROM hive_photos WHERE hiveId = :hiveId")
    fun getPhotosCountForHive(hiveId: Long): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhoto(photo: HivePhotoEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhotos(photos: List<HivePhotoEntity>)

    @Update
    suspend fun updatePhoto(photo: HivePhotoEntity)

    @Delete
    suspend fun deletePhoto(photo: HivePhotoEntity)

    @Query("DELETE FROM hive_photos")
    suspend fun deleteAllPhotos()
}
