package com.example.gizi.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "gain_table")
data class Gain(
    @PrimaryKey(autoGenerate = true)
    var id:Int,
    @ColumnInfo(name = "name")
    val mName:String,
    @ColumnInfo(name = "frequencies")
    val mFrequencies:String,
    @ColumnInfo(name = "gain")
    val mGain:Int
)