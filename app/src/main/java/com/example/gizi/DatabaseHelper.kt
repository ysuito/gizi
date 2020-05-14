package com.example.gizi

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.lang.StringBuilder

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION ){
    // クラス内のprivate定数を宣言するためにcompanion objectをブロックとする
    companion object{
        // データベースファイル名の定数フィールド
        private  const val DATABASE_NAME = "station.db"

        // バージョン情報の定数フィールド
        private  const val DATABASE_VERSION = 1
    }

    override fun onCreate(db: SQLiteDatabase) {
        //テーブル作成用SQL文字列の作成。
        val sb = StringBuilder()
        sb.append("CREATE TABLE stations (")
        sb.append("_id INTEGER PRIMARY KEY,")
        sb.append("station_cd INTEGER,")
        sb.append("station_name TEXT,")
        sb.append("lon REAL,")
        sb.append("lat REAL")
        sb.append(");")
        val sql = sb.toString()

        //SQLの実行。
        db.execSQL(sql)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
    }
}