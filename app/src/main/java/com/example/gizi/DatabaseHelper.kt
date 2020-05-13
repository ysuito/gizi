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

        // インサート用SQL文字列の用意
        val sqlInsert = "INSERT INTO stations (_id, station_cd, station_name, lon, lat) VALUES(?, ?, ?, ?, ?)"

        // SQK文字列を元にプリペアドスレートメント取得
        var stat = db.compileStatement(sqlInsert)

        // 変数のバイド
        stat.bindLong(1,1)
        stat.bindLong(2,1110101)
        stat.bindString(3,"函館")
        stat.bindDouble(4,140.726413)
        stat.bindDouble(5,41.773709)

        // インサートの実行
        stat.executeInsert()

        // SQK文字列を元にプリペアドスレートメント取得
        stat = db.compileStatement(sqlInsert)

        // 変数のバイド
        stat.bindLong(1,2)
        stat.bindLong(2,1130101)
        stat.bindString(3,"東京")
        stat.bindDouble(4,139.766103)
        stat.bindDouble(5,35.681391)

        // インサートの実行
        stat.executeInsert()

        // SQK文字列を元にプリペアドスレートメント取得
        stat = db.compileStatement(sqlInsert)

        // 変数のバイド
        stat.bindLong(1,3)
        stat.bindLong(2,1160213)
        stat.bindString(3,"新大阪")
        stat.bindDouble(4,135.501852)
        stat.bindDouble(5,34.734136)

        // インサートの実行
        stat.executeInsert()

        // SQK文字列を元にプリペアドスレートメント取得
        stat = db.compileStatement(sqlInsert)

        // 変数のバイド
        stat.bindLong(1,4)
        stat.bindLong(2,1190101)
        stat.bindString(3,"博多")
        stat.bindDouble(4,130.420622)
        stat.bindDouble(5,33.590002)

        // インサートの実行
        stat.executeInsert()
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
    }
}