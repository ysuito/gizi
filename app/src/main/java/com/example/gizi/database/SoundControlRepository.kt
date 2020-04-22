package com.example.gizi.database

import android.app.Application
import androidx.lifecycle.LiveData
import android.os.AsyncTask

class SoundControlRepository(application: Application) {
    var soundControlDao: SoundControlDao
    var mAllGains:LiveData<List<Gain>>
    var mSetting:LiveData<List<Setting>>

    init {
        val db:SoundControlRoomDatabase = SoundControlRoomDatabase.invoke(application)
        soundControlDao = db.soundControlDao()
        mAllGains = soundControlDao.getAllGains()
        mSetting = soundControlDao.getSetting()
    }

    fun getAllGains():LiveData<List<Gain>>{
        return mAllGains
    }

    fun insertGain(gain:Gain){
        InsertGainAsyncTask(soundControlDao, AppConstants.DBOperations.INSERT).execute(gain)
    }

    fun deleteAllGain(){
        DeleteAllGainAsyncTask(soundControlDao).execute()
    }

    fun deleteGain(gain:Gain){
        InsertGainAsyncTask(soundControlDao, AppConstants.DBOperations.DELETE).execute(gain)
    }

    fun updateGain(gain: Gain) {
        InsertGainAsyncTask(soundControlDao, AppConstants.DBOperations.UPDATE).execute(gain)
    }

    //    ====================== insert/Update/Delete data async ===========
    private class InsertGainAsyncTask internal constructor(private val mAsyncTaskDao: SoundControlDao, private val isInsert:AppConstants.DBOperations)
        :AsyncTask<Gain, Void, Void>() {
        override fun doInBackground(vararg params: Gain): Void? {
            when(isInsert){
                AppConstants.DBOperations.INSERT -> mAsyncTaskDao.insertGain(params[0])
                AppConstants.DBOperations.UPDATE -> mAsyncTaskDao.updateGain(params[0])
                AppConstants.DBOperations.DELETE -> mAsyncTaskDao.deleteGain(params[0])
            }
            return null
        }
    }

    //    ==================== delete All Gains/Data async =================
    private class DeleteAllGainAsyncTask internal constructor(private val mAsyncTaskDao: SoundControlDao)
        :AsyncTask<Void, Void, Void>() {
        override fun doInBackground(vararg params: Void?): Void? {
            mAsyncTaskDao.deleteAllGains()
            return null
        }
    }

    fun getSetting():LiveData<List<Setting>> {
        return mSetting
    }

    fun insertSetting(setting:Setting){
        InsertSettingAsyncTask(soundControlDao, AppConstants.DBOperations.INSERT).execute(setting)
    }

    fun deleteAllSetting(){
        DeleteAllSettingAsyncTask(soundControlDao).execute()
    }

    fun deleteSetting(setting: Setting){
        InsertSettingAsyncTask(soundControlDao, AppConstants.DBOperations.DELETE).execute(setting)
    }

    fun updateSetting(setting: Setting) {
        InsertSettingAsyncTask(soundControlDao, AppConstants.DBOperations.UPDATE).execute(setting)
    }

    //    ====================== insert/Update/Delete data async ===========
    private class InsertSettingAsyncTask internal constructor(private val mAsyncTaskDao: SoundControlDao, private val isInsert:AppConstants.DBOperations)
        :AsyncTask<Setting, Void, Void>() {
        override fun doInBackground(vararg params: Setting): Void? {
            when(isInsert){
                AppConstants.DBOperations.INSERT -> mAsyncTaskDao.insertSetting(params[0])
                AppConstants.DBOperations.UPDATE -> mAsyncTaskDao.updateSetting(params[0])
                AppConstants.DBOperations.DELETE -> mAsyncTaskDao.deleteSetting(params[0])
            }
            return null
        }
    }

    //    ==================== delete All Gains/Data async =================
    private class DeleteAllSettingAsyncTask internal constructor(private val mAsyncTaskDao: SoundControlDao)
        :AsyncTask<Void, Void, Void>() {
        override fun doInBackground(vararg params: Void?): Void? {
            mAsyncTaskDao.deleteAllSettings()
            return null
        }
    }

}