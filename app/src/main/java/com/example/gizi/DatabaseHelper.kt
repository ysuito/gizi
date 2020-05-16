package com.example.gizi

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.io.*


class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION ){
    val _context = context

    // クラス内のprivate定数を宣言するためにcompanion objectをブロックとする
    companion object{
        // データベースファイル名の定数フィールド
        private  const val DATABASE_NAME = "gizi.db"

        // バージョン情報の定数フィールド
        private  const val DATABASE_VERSION = 1
    }

    override fun onCreate(db: SQLiteDatabase) {
        //テーブル作成用SQL文字列の作成。
        var sb = StringBuilder()
        sb.append("CREATE TABLE stations (")
        sb.append("_id INTEGER PRIMARY KEY,")
        sb.append("station_cd INTEGER,")
        sb.append("station_name TEXT,")
        sb.append("lon REAL,")
        sb.append("lat REAL")
        sb.append(");")
        var sql = sb.toString()

        //SQLの実行。
        db.execSQL(sql)

        // 初期値をセット
        InitStationTable(db)

        sb = StringBuilder()
        sb.append("CREATE TABLE joines (")
        sb.append("_id INTEGER PRIMARY KEY,")
        sb.append("line_cd INTEGER,")
        sb.append("station_cd1 INTEGER,")
        sb.append("station_cd2 INTEGER")
        sb.append(");")
        sql = sb.toString()

        //SQLの実行。
        db.execSQL(sql)

        // 初期値をセット
        InitJoinTable(db)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
    }


    /**
     * 駅テーブルの初期化
     */
    fun InitStationTable(db: SQLiteDatabase){
        val sqlDelete = "DELETE FROM stations"
        var stmt =db.compileStatement(sqlDelete)
        // 削除の実行
        stmt.executeUpdateDelete()

        var stations = readStationCsv(_context.getString(R.string.station_csv_file))
        for ((index, station) in stations.withIndex()) {
            // インサート用SQL文字列の用意
            val sqlInsert = "INSERT INTO stations (_id, station_cd, station_name, lon, lat) VALUES(?, ?, ?, ?, ?)"

            // SQK文字列を元にプリペアドスレートメント取得
            var stat = db.compileStatement(sqlInsert)

            // 変数のバイド
            stat.bindLong(1,index.toLong())
            stat.bindLong(2,station.station_cd.toLong())
            stat.bindString(3,station.station_name)
            stat.bindDouble(4,station.lon)
            stat.bindDouble(5,station.lat)

            // インサートの実行
            stat.executeInsert()
        }
    }

    /**
     * 接続テーブルの初期化
     */
    fun InitJoinTable(db: SQLiteDatabase){
        val sqlDelete = "DELETE FROM joines"
        var stmt =db.compileStatement(sqlDelete)
        // 削除の実行
        stmt.executeUpdateDelete()

        var joinList = readJoinCsv(_context.getString(R.string.join_csv_file))
        for ((index, joinInfo) in joinList.withIndex()) {
            // インサート用SQL文字列の用意
            val sqlInsert = "INSERT INTO joines (_id, line_cd, station_cd1, station_cd2) VALUES(?, ?, ?, ?)"

            // SQK文字列を元にプリペアドスレートメント取得
            var stat = db.compileStatement(sqlInsert)

            // 変数のバイド
            stat.bindLong(1,index.toLong())
            stat.bindLong(2,joinInfo.line_cd.toLong())
            stat.bindLong(3,joinInfo.station_cd1.toLong())
            stat.bindLong(4,joinInfo.station_cd2.toLong() )

            // インサートの実行
            stat.executeInsert()
        }
    }

    //1行目が格納される。
    var column: Array<String> = emptyArray<String>()


    fun readStationCsv(filename: String):Array<StationInfo>{
        var stations: Array<StationInfo> = emptyArray()
        try {
            //assetsからCSV情報を持ってくる
            val file = _context.resources.assets.open(filename)
            val fileReader = BufferedReader(InputStreamReader(file))

            var i: Int = 0
            fileReader.forEachLine{
                if (it.isNotBlank()) {
                    if (i == 0) {
                        //1行目だけ別の配列に読み取る。
                        column = it.split(",").toTypedArray()
                    } else {
                        //2行目以降
                        val line = it.split(",").toTypedArray()
                        stations += getStationFromCSV(line)
                    }
                }
                i++;
            }
        }catch (e: IOException) {
            //例外処理
            print(e)
        }
        return stations
    }

    fun readJoinCsv(filename: String):Array<JoinInfo>{
        var joinList: Array<JoinInfo> = emptyArray()
        try {
            //assetsからCSV情報を持ってくる
            val file = _context.resources.assets.open(filename)
            val fileReader = BufferedReader(InputStreamReader(file))

            var i: Int = 0
            fileReader.forEachLine{
                if (it.isNotBlank()) {
                    if (i == 0) {
                        //1行目だけ別の配列に読み取る。
                        column = it.split(",").toTypedArray()
                    } else {
                        //2行目以降
                        val line = it.split(",").toTypedArray()
                        joinList += getJoinFromCSV(line)
                    }
                }
                i++;
            }
        }catch (e: IOException) {
            //例外処理
            print(e)
        }
        return joinList
    }

    //stationinfoインスタンスを取得
    fun getStationFromCSV(line: Array<String>): StationInfo {
        val info = StationInfo(
            station_cd = line[0].toInt(),
            station_name = line[2],
            lon = line[9].toDouble(),
            lat = line[10].toDouble()
        )
        return  info
    }

    //JoinInfoインスタンスを取得
    fun getJoinFromCSV(line: Array<String>): JoinInfo {
        val info = JoinInfo(
            line_cd = line[0].toInt(),
            station_cd1 =  line[1].toInt(),
            station_cd2 =  line[2].toInt()
        )
        return  info
    }

    // 参考にしたCSV取得
    // https://qiita.com/daichi77/items/375b5c50aa0d52f18beb
    data class StationInfo (
        //名称,概略
        var station_cd: Int,
        var station_name: String? = null,
        var lon: Double = 0.0,
        var lat: Double = 0.0
    ){}

    data class JoinInfo (
        //名称,概略
        var line_cd: Int,
        var station_cd1: Int,
        var station_cd2: Int
    ){}
}