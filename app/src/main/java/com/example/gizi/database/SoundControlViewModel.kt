package com.example.gizi.database

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData

class SoundControlViewModel(application: Application) : AndroidViewModel(application) {
    private var mRepository:SoundControlRepository = SoundControlRepository(application)
    private var mAllGains:LiveData<List<Gain>>
    private var mSetting:LiveData<List<Setting>>

    init {
        mAllGains = mRepository.getAllGains()
        mSetting = mRepository.getSetting()
    }

    fun getAllGains():LiveData<List<Gain>>{
        return mAllGains;
    }

    fun insertGain(gain: Gain){
        mRepository.insertGain(gain)
    }

    fun deleteAllGain(){
        mRepository.deleteAllGain()
    }

    fun deleteGain(gain: Gain){
        mRepository.deleteGain(gain)
    }

    fun updateGain(gain: Gain){
        mRepository.updateGain(gain)
    }

    fun getSetting():LiveData<List<Setting>>{
        return mSetting;
    }

    fun insertSetting(setting: Setting){
        mRepository.insertSetting(setting)
    }

    fun deleteAllSetting(){
        mRepository.deleteAllSetting()
    }

    fun deleteSetting(setting: Setting){
        mRepository.deleteSetting(setting)
    }

    fun updateSetting(setting: Setting){
        mRepository.updateSetting(setting)
    }

    fun switchNr(){
        val newSetting = Setting(
            mSetting.value!![0].id,
            !mSetting.value!![0].mNr,
            mSetting.value!![0].mNrTranportation
        )
        mRepository.updateSetting(newSetting)
    }

    fun switchNrTranportation(){
        val newSetting = Setting(
            mSetting.value!![0].id,
            mSetting.value!![0].mNr,
            !mSetting.value!![0].mNrTranportation
        )
        mRepository.updateSetting(newSetting)
    }

}