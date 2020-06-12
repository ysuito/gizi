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

    @Query("SELECT * from gain_table WHERE name = :name ORDER BY id ASC")
    fun queryGains(name:String):List<Gain>

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

    @Query("SELECT * from setting_table ORDER BY id ASC LIMIT 1")
    fun getSetting():LiveData<Setting>

    @Query("SELECT * from setting_table LIMIT 1")
    fun getAnySetting():Array<Setting>

    @Delete
    fun deleteSetting(setting:Setting)

    @Update
    fun updateSetting(setting: Setting)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertStation(mStation:Station)

    @Query("SELECT * from station_table LIMIT 1")
    fun getAnyStation():List<Station>

    @Query("SELECT * from station_table WHERE :minLng < lng AND lng < :maxLng AND :minLat < lat AND lat < :maxLat ORDER BY ((:lat-lat)*(:lat-lat)+(:lng-lng)*(:lng-lng)) LIMIT :limit")
    fun getNearStations(lng:Double,lat:Double,minLng:Double,
                        maxLng:Double,minLat:Double,maxLat:Double,limit:Int):List<Station>
}