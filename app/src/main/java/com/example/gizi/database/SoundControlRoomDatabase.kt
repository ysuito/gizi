package com.example.gizi.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import android.os.AsyncTask

@Database(entities = [Gain::class,Setting::class], version = 16, exportSchema = false)
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
            .addCallback(sRoomDatabaseCallback)
            .build()

        private val sRoomDatabaseCallback = object : RoomDatabase.Callback() {

            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
                instance?.let { PopulateDbAsync(it).execute() }
            }
        }
    }

    /**
     * Populate the database in the background.
     */
    private class PopulateDbAsync internal constructor(db: SoundControlRoomDatabase) : AsyncTask<Void, Void, Void>() {

        private val mDao: SoundControlDao = db.soundControlDao()
        internal var gains = arrayOf(arrayOf("人の声","100-1000",90))

        override fun doInBackground(vararg params: Void): Void? {
            // Start the app with a clean database every time.
            // Not needed if you only populate the database
            // when it is first created

            if (mDao.getAnyGain().isEmpty()){
                for (i in 0 until gains.size) {
                    val gain = Gain(0, gains[i][0] as String, gains[i][1] as String, gains[i][2] as Int)
                    mDao.insertGain(gain)
                }
            }
            if (mDao.getAnySetting().isEmpty()){
                val setting = Setting(0, false, false, false)
                mDao.insertSetting(setting)
            }


            return null
        }
    }

}