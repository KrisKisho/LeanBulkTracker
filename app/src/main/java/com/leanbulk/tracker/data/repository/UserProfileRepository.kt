package com.leanbulk.tracker.data.repository

import com.leanbulk.tracker.data.local.dao.UserProfileDao
import com.leanbulk.tracker.data.local.entity.UserProfileEntity
import com.leanbulk.tracker.data.model.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserProfileRepository(
    private val userProfileDao: UserProfileDao
) {
    fun getUserProfile(): Flow<UserProfile> {
        return userProfileDao.getUserProfileFlow().map { entity ->
            entity?.toModel() ?: UserProfile()
        }
    }

    suspend fun getUserProfileSync(): UserProfile {
        return userProfileDao.getUserProfileSync()?.toModel() ?: UserProfile()
    }

    suspend fun ensureDefaultProfileSeeded() {
        val existing = userProfileDao.getUserProfileSync()
        if (existing == null) {
            userProfileDao.upsertUserProfile(UserProfileEntity.fromModel(UserProfile()))
        }
    }

    suspend fun updateProfile(profile: UserProfile) {
        userProfileDao.upsertUserProfile(UserProfileEntity.fromModel(profile))
    }

    suspend fun updateCurrentWeight(weightKg: Float) {
        userProfileDao.updateCurrentWeight(weightKg)
    }

    suspend fun updateCurrentWaist(waistInches: Float) {
        userProfileDao.updateCurrentWaist(waistInches)
    }
}
