package com.example.gizi.database

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface SoundControlDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertGain(mGain:Gain)

    @Query("DELETE FROM gain_table")
    fun deleteAllGains()

    @Query("SELECT * from gain_table ORDER BY id ASC")
    fun getAllGains():LiveData<List<Gain>>

    @Query("SELECT * from gain_table LIMIT 1")
    fun getAnyGain():Array<Gain>

    @Delete
    fun deleteGain(gain:Gain)

    @Update
    fun updateGain(gain: Gain)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertSetting(mSetting:Setting)

    @Query("DELETE FROM setting_table")
    fun deleteAllSettings()

    @Query("SELECT * from setting_table ORDER BY id ASC")
    fun getAllSettings():LiveData<List<Setting>>

    @Query("SELECT * from setting_table")
    fun getSetting():LiveData<List<Setting>>

    @Query("SELECT * from setting_table LIMIT 1")
    fun getAnySetting():Array<Setting>

    @Delete
    fun deleteSetting(setting:Setting)

    @Update
    fun updateSetting(setting: Setting)
}