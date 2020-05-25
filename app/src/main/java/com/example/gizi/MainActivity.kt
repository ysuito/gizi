package com.example.gizi

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.media.AudioManager
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageButton
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gizi.database.Gain
import com.example.gizi.database.SoundControlViewModel
import com.example.gizi.lib.SoundControl
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : AppCompatActivity() {

    private val PERMISSION_CODE = 1234

    private var adapter:GainListAdapter?= null
    private var mSoundControlViewModel:SoundControlViewModel? = null
    private var onOffFlag: Boolean = true

    private val sCtrl = SoundControl()

    private var _latitude = 0.0     //　緯度フィールド
    private var _longitude = 0.0    // 経度フィールド
    private val _inside_latitude_range = 0.01  // 範囲内とする緯度の値
    private val _inside_longitude_range = 0.01  // 範囲内とする経度の値

    // データベースヘルパーオブジェクト
    private val _helper = DatabaseHelper(this@MainActivity)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(toolbar)

        mSoundControlViewModel = ViewModelProviders.of(this).get(SoundControlViewModel::class.java)

        adapter = GainListAdapter({ partItem : Gain -> partItemClicked(partItem)}, mSoundControlViewModel!!)

        recyclerview.layoutManager = LinearLayoutManager(this)
        recyclerview.setHasFixedSize(true)
        recyclerview.adapter = adapter


        fab.setOnClickListener {
            startActivityForResult(Intent(this, NewGainActivity::class.java), 1111)
        }

        val switchNr = findViewById<Switch>(R.id.switchNr)
        val switchNrTranportation = findViewById<Switch>(R.id.switchNrTransportation)
        val switchBluetoothMic = findViewById<Switch>(R.id.switchBluetoothMic)
        var isSwitchInitiated = false

        mSoundControlViewModel!!.getAllGains().observe(this, Observer {
            // データベース初期化時にnullがobserveされるためチェック
            if (it != null) {
                sCtrl.setCutOff(it)
                adapter!!.setGains(it)
            }
        })

        mSoundControlViewModel!!.getSetting().observe(this, Observer {
            // データベース初期化時にnullがobserveされるためチェック
            if (!isSwitchInitiated && it != null) {
                switchNr.isChecked = it.mNr
                sCtrl.setNr(it.mNr)
                switchBluetoothMic.isChecked = it.mBluetoothMic
                sCtrl.setBluetoothMic(it.mBluetoothMic)
                switchNrTranportation.isChecked = it.mNrTranportation
            }
            isSwitchInitiated = true
        })

        switchNr.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isSwitchInitiated){
                mSoundControlViewModel!!.switchNr(isChecked)
                sCtrl.setNr(isChecked)
            }
        }
        switchNrTranportation.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isSwitchInitiated) {
                mSoundControlViewModel!!.switchNrTransportation(isChecked)
            }
        }
        switchBluetoothMic.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isSwitchInitiated) {
                mSoundControlViewModel!!.switchBluetoothMic(isChecked)
                sCtrl.setBluetoothMic(isChecked)
            }
        }

        val onOffButton = findViewById<ImageButton>(R.id.onOffButton)
        onOffButton.setOnClickListener {
            if (onOffFlag) {
                checkPermission()
                sCtrl.start()
                onOffButton.setImageResource(R.drawable.ic_pause_circle_outline_200dp)
            } else {
                sCtrl.stop()
                onOffButton.setImageResource(R.drawable.ic_play_circle_outline_200dp)
            }
            onOffFlag = !onOffFlag
        }

        //LocationManagerオブジェクトを取得
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        // 位置情報が更新された際のリスナオブジェクトを生成
        val locationListener = GPSLocationListener()

        //位置情報の追跡を開始。
        if (ActivityCompat.checkSelfPermission(
                applicationContext,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED)  {
            val permission = arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION)
            ActivityCompat.requestPermissions(this@MainActivity, permission,1000)
            return
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0f, locationListener)
    }

    override fun onStart() {
        super.onStart()
        val mAudioManager = this.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        sCtrl.setAudioManager(mAudioManager!!)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_delete -> {
                mSoundControlViewModel!!.deleteAllGain()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun partItemClicked(gain : Gain) {
        Toast.makeText(this, "Clicked: ${gain.mGain}", Toast.LENGTH_LONG).show()
        val intent = Intent(this, NewGainActivity::class.java)
        intent.putExtra("id", gain.id)
        intent.putExtra("name", gain.mName)
        intent.putExtra("frequencies", gain.mFrequencies)
        intent.putExtra("gain", gain.mGain)

        startActivityForResult(intent, 1111)
    }

    private fun checkPermission() {
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(android.Manifest.permission.RECORD_AUDIO),
                PERMISSION_CODE);
        }
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.MODIFY_AUDIO_SETTINGS)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(android.Manifest.permission.MODIFY_AUDIO_SETTINGS),
                PERMISSION_CODE);
        }
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.BLUETOOTH)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(android.Manifest.permission.BLUETOOTH),
                PERMISSION_CODE);
        }
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.BLUETOOTH_ADMIN)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(android.Manifest.permission.BLUETOOTH_ADMIN),
                PERMISSION_CODE);
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == 1111 && resultCode == Activity.RESULT_OK){
            val type:String = data!!.getStringExtra("type")
            val id:Int = data!!.getIntExtra("id", 0)
            val name: String = data.getStringExtra("name")
            val frequencies: String = data.getStringExtra("frequencies")
            val gainInt: Int = data.getIntExtra("gain", 50)
            val gain = Gain(id, name, frequencies, gainInt)

            if (type=="save"){
                if(id == 0){
                    mSoundControlViewModel!!.insertGain(gain)
                } else{
                    mSoundControlViewModel!!.updateGain(gain)
                }
            } else if (type=="delete"){
                mSoundControlViewModel!!.deleteGain(gain)
            }
        }
    }

    override fun onDestroy() {
        // ヘルパーオブジェクトの開放
        _helper.close()
        super.onDestroy()
    }


    /**
     * ロケーションリスナクラス。
     */
    private inner class GPSLocationListener: LocationListener {
        override fun onLocationChanged(location: Location) {
            //引数のLocationオブジェクトから緯度を取得
            _latitude = location.latitude
            _longitude = location.longitude

            // 取得した緯度をテキストに表示
            val latitudeText = findViewById<TextView>(R.id.latitudeValue)
            latitudeText.text = _latitude.toString()
            val longitudeText = findViewById<TextView>(R.id.longitudeValue)
            longitudeText.text = _longitude.toString()
        }

        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
        }

        override fun onProviderEnabled(provider: String?) {
        }

        override fun onProviderDisabled(provider: String?) {
        }
    }


    /**
     * 駅名を取得するボタンのメソッド。
     */
    fun getStationonNameButtonClick(view: View) {

        //データベースヘルパーオブジェクトからデータベース接続オブジェクトを取得。
        val db = _helper.writableDatabase

        // 特定の範囲に入ったら表示
        val minLongitude = _longitude-_inside_longitude_range
        val maxLongitude= _longitude+_inside_longitude_range
        val minLatitude = _latitude-_inside_latitude_range
        val maxLatitude= _latitude+_inside_latitude_range

        // 経度緯度による範囲内の駅
        val sql ="SELECT * FROM stations" +
                " WHERE ${minLongitude.toString()} < lon AND lon < ${maxLongitude.toString()}" +
                " AND ${minLatitude.toString()} < lat AND lat < ${maxLatitude.toString()}"

        // SQLの実行
        val cursor = db.rawQuery(sql, null)
        var note =""
        //SQL実行の戻り値であるカーソルオブジェクトをループさせてデータベース内のデータを取得
        while (cursor.moveToNext()){
            // カラムのインデックス値を酒盗
            val idxName = cursor.getColumnIndex("station_name")
            //　カラムのインデックス値を元に実際のデータを取得
            if(note != ""){ note +=", "}
            note += cursor.getString(idxName)
        }
        val statonNameText = findViewById<TextView>(R.id.station_name)
        statonNameText.text =note

        val receiver = OdptInfoReceiver()
        receiver.execute("https://api.odpt.org/api/v4/odpt:Train?acl")
    }

    /**
     * 表示されたパーミッションダイアログに対して、ユーザーが許可しないを選択した場合に呼び出されるメソッド。
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        //ACCESS_FINE_LOCATIONに対するパーミションダイアログでかつ許可を選択したなら…
        if(requestCode == 1000 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            //LocationManagerオブジェクトを取得。
            val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
            //位置情報が更新された際のリスナオブジェクトを生成。
            val locationListener = GPSLocationListener()
            //再度ACCESS_FINE_LOCATIONの許可が下りていないかどうかのチェックをし、降りていないなら処理を中止。
            if(ActivityCompat.checkSelfPermission(applicationContext, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return
            }
            //位置情報の追跡を開始。
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0f, locationListener)
        }
    }


    /**
     * 非同期でAPIデータを取得するクラス。
     */
    private inner class OdptInfoReceiver(vararg params: String): AsyncTask<String, String, String>() {

        /**
         * バックグラウンドで実行される処理（API実行）
         */
        override fun doInBackground(vararg params: String): String {
            //可変長引数の1個目(インデックス0)を取得。これが都市ID
            val apikey =  getString(R.string.api_key)

            val target_api = params[0]

            //API keyを使って接続URL文字列を作成。
            val urlStr = target_api + ":consumerKey=${apikey}"

            //URLオブジェクトを生成。
            val url = URL(urlStr)
            //URLオブジェクトからHttpURLConnectionオブジェクトを取得。
            val con = url.openConnection() as HttpURLConnection
            //http接続メソッドを設定。
            con.requestMethod = "GET"

            //以下タイムアウトを設定する場合のコード。
			con.connectTimeout = 1000
			con.readTimeout = 1000

            //接続。
            con.connect()

            //以下HTTPステータスコードを取得する場合のコード。
			val resCode = con.responseCode
			Log.i("OdptInfoReceiver", "Response Code is ${resCode}")

            //HttpURLConnectionオブジェクトからレスポンスデータを取得。天気情報が格納されている。
            val stream = con.inputStream
            //レスポンスデータであるInputStreamオブジェクトを文字列(JSON文字列)に変換。
            val result = is2String(stream)
            //HttpURLConnectionオブジェクトを解放。
            con.disconnect()
            //InputStreamオブジェクトを解放。
            stream.close()

            //JSON文字列を返す。
            return result
        }

        /**
         * API処理結果をUIに反映される処理
         */
        override fun onPostExecute(result: String) {
            //JSON文字列からJSONObjectオブジェクトを生成。これをルートJSONオブジェクトとする。
            val arrayJSON = JSONArray(result)
            val rootJSON  = arrayJSON.getJSONObject(0)

            //ルートJSON直下の「description」JSONオブジェクトを取得。
            val id = rootJSON.getString("@id")
            val typevalue = rootJSON.getString("@type")
            val dcdate = rootJSON.getString("dc:date")
            val context = rootJSON.getString("@context")
            val dctvalid = rootJSON.getString("dct:valid")
            val odptdelay = rootJSON.getString("odpt:delay")
            val owlsameAs = rootJSON.getString("owl:sameAs")
            val odptrailway = rootJSON.getString("odpt:railway")
            val odptoperator = rootJSON.getString("odpt:operator")
            val odpttoStation = rootJSON.getString("odpt:toStation")
            val odpttrainType = rootJSON.getString("odpt:trainType")
            val odpttrainOwner = rootJSON.getString("odpt:trainOwner")
            // 消えた？
          //  val odptviaRailway = rootJSON.getJSONArray("odpt:viaRailway")
            val odptfromStation = rootJSON.getString("odpt:fromStation")
            val odpttrainNumber = rootJSON.getString("odpt:trainNumber")
            val odptoriginStation = rootJSON.getJSONArray("odpt:originStation")
            val odptrailDirection = rootJSON.getString("odpt:railDirection")
            val odptdestinationStation = rootJSON.getString("odpt:destinationStation")
        }

        /**
         * InputStreamオブジェクトを文字列に変換するメソッド。変換文字コードはUTF-8。
         *
         * @param stream 変換対象のInputStreamオブジェクト。
         * @return 変換された文字列。
         */
        private fun is2String(stream: InputStream): String {
            val sb = StringBuilder()
            val reader = BufferedReader(InputStreamReader(stream, "UTF-8"))
            var line = reader.readLine()
            while(line != null) {
                sb.append(line)
                line = reader.readLine()
            }
            reader.close()
            return sb.toString()
        }
    }
}
