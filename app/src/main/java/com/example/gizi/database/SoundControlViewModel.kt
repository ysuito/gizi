package com.example.gizi.database

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData

class SoundControlViewModel(application: Application) : AndroidViewModel(application) {
    private var mRepository:SoundControlRepository = SoundControlRepository(application)
    private var mAllGains:LiveData<List<Gain>>
    private var mSetting:LiveData<Setting>

    init {
        mAllGains = mRepository.getAllGains()
        mSetting = mRepository.getSetting()
    }

    fun getAllGains():LiveData<List<Gain>>{
        return mAllGains
    }

    fun queryGains(name: String):List<Gain> {
        return mRepository.queryGains(name)
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

    fun getSetting():LiveData<Setting>{
        return mSetting
    }

    fun switchNr(){
        val newSetting = Setting(
            mSetting.value!!.id,
            !mSetting.value!!.mNr,
            mSetting.value!!.mNrTranportation,
            mSetting.value!!.mBluetoothMic
        )
        mRepository.updateSetting(newSetting)
    }

    fun switchNrTransportation(){
        val newSetting = Setting(
            mSetting.value!!.id,
            mSetting.value!!.mNr,
            !mSetting.value!!.mNrTranportation,
            mSetting.value!!.mBluetoothMic
        )
        mRepository.updateSetting(newSetting)
    }

    fun switchBluetoothMic() {
        val newSetting = Setting(
            mSetting.value!!.id,
            mSetting.value!!.mNr,
            mSetting.value!!.mNrTranportation,
            !mSetting.value!!.mBluetoothMic
        )
        mRepository.updateSetting(newSetting)
    }

    fun getNearStations(lng:Double,lat:Double,minLng:Double,
                        maxLng:Double,minLat:Double,maxLat:Double,limit:Int):List<Station> {
        return mRepository.getNearStations(lng,lat,minLng,maxLng,minLat,maxLat,limit)
    }

}