package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    @Query("SELECT * FROM app_configurations")
    fun getAllConfigs(): Flow<List<AppConfigEntity>>

    @Query("SELECT * FROM app_configurations WHERE packageName = :packageName LIMIT 1")
    suspend fun getConfigForPackage(packageName: String): AppConfigEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateConfig(config: AppConfigEntity)

    @Query("DELETE FROM app_configurations WHERE packageName = :packageName")
    suspend fun deleteConfigForPackage(packageName: String)
}
