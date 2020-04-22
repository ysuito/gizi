package com.example.gizi.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "setting_table")
data class Setting(
    @PrimaryKey(autoGenerate = true)
    var id:Int,
    @ColumnInfo(name = "nr")
    val mNr:Boolean,
    @ColumnInfo(name = "nr_transportion")
    val mNrTranportation:Boolean
)