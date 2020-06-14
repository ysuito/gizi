package com.example.gizi.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import android.os.AsyncTask
import android.util.Log
import com.example.gizi.R
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

@Database(entities = [Gain::class,Setting::class,Station::class], version = 22, exportSchema = false)
abstract class SoundControlRoomDatabase:RoomDatabase() {

    abstract fun soundControlDao():SoundControlDao

    companion object {
        @Volatile
        private var instance: SoundControlRoomDatabase? = null
        private val LOCK = Any()

        operator fun invoke(context: Context)= instance ?: synchronized(LOCK){
            instance ?: buildDatabase(context).also { instance = it}
        }

        private fun buildDatabase(context: Context) = Room.databaseBuilder(context,
            SoundControlRoomDatabase::class.java, "sound_control_database")
            .fallbackToDestructiveMigration()
            .allowMainThreadQueries()
            .addCallback(object: RoomDatabase.Callback() {
                override fun onOpen(db: SupportSQLiteDatabase) {
                    super.onOpen(db)
                    instance?.let { PopulateDbAsync(it, context).execute() }
                }
            })
            .build()
    }

    /**
     * Populate the database in the background.
     */
    private class PopulateDbAsync internal constructor(db: SoundControlRoomDatabase, context: Context) : AsyncTask<Void, Void, Void>() {

        val ctx = context
        private val mDao: SoundControlDao = db.soundControlDao()
        internal var gains = arrayOf(arrayOf("耳栓","0-20000",0))
        val stations = readStationCsv(ctx.getString(R.string.station_csv_file))

        override fun doInBackground(vararg params: Void): Void? {
            // Start the app with a clean database every time.
            // Not needed if you only populate the database
            // when it is first created

            if (mDao.getAnyGain().isEmpty()){
                for (i in gains.indices) {
                    val gain = Gain(0, gains[i][0] as String, gains[i][1] as String, gains[i][2] as Int)
                    mDao.insertGain(gain)
                }
            }
            if (mDao.getAnySetting().isEmpty()){
                val setting = Setting(
                    id = 0,
                    mNr = false,
                    mNrTranportation = false,
                    mBluetoothMic = false
                )
                mDao.insertSetting(setting)
            }
            if (mDao.getAnyStation().isEmpty()) {
                for (station in stations) {
                    val newStation = Station(
                        id = 0,
                        station_cd = station.station_cd,
                        station_name = station.station_name,
                        lng = station.lng,
                        lat = station.lat
                    )
                    mDao.insertStation(newStation)
                }
            }


            return null
        }

        // 参考にしたCSV取得
        // https://qiita.com/daichi77/items/375b5c50aa0d52f18beb
        data class StationInfo (
            //名称,概略
            var station_cd: Int,
            var station_name: String,
            var lng: Double = 0.0,
            var lat: Double = 0.0
        )

        //stationinfoインスタンスを取得
        private fun getStationFromCSV(line: Array<String>): StationInfo {
            return StationInfo(
                station_cd = line[0].toInt(),
                station_name = line[2],
                lng = line[9].toDouble(),
                lat = line[10].toDouble()
            )
        }

        private fun readStationCsv(filename: String):List<StationInfo>{
            val stations: MutableList<StationInfo> = emptyList<StationInfo>().toMutableList()
            try {
                //assetsからCSV情報を持ってくる
                val file = ctx.resources.assets.open(filename)
                val fileReader = BufferedReader(InputStreamReader(file))

                var i = 0
                fileReader.forEachLine{
                    if (it.isNotBlank()) {
                        if (i == 0) {
                            //1行目はカラムなのでスキップ。
                        } else {
                            //2行目以降
                            val line = it.split(",").toTypedArray()
                            stations += getStationFromCSV(line)
                        }
                    }
                    i++
                }
            }catch (e: IOException) {
                //例外処理
                print(e)
            }
            return stations
        }
    }

}