package com.example.gizi.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "station_table")
data class Station(
    @PrimaryKey(autoGenerate = true)
    var id:Int,
    @ColumnInfo(name = "station_cd")
    val station_cd:Int,
    @ColumnInfo(name = "station_name")
    val station_name:String,
    @ColumnInfo(name = "lng")
    val lng:Double,
    @ColumnInfo(name = "lat")
    val lat:Double
)