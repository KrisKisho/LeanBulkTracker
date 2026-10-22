package com.leanbulk.tracker.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.leanbulk.tracker.data.local.entity.UserProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {

    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    fun getUserProfileFlow(): Flow<UserProfileEntity?>

    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    suspend fun getUserProfileSync(): UserProfileEntity?

    @Upsert
    suspend fun upsertUserProfile(entity: UserProfileEntity)

    @Query("UPDATE user_profile SET currentWeightKg = :weightKg WHERE id = 1")
    suspend fun updateCurrentWeight(weightKg: Float)

    @Query("UPDATE user_profile SET currentWaistInches = :waistInches WHERE id = 1")
    suspend fun updateCurrentWaist(waistInches: Float)
}
