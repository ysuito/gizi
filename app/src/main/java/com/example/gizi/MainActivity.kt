package com.example.gizi

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.media.AudioManager
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageButton
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
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
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class MainActivity : AppCompatActivity() {

    private val permissionCode = 1234

    private var adapter:GainListAdapter?= null
    private var mSoundControlViewModel:SoundControlViewModel? = null
    private var isOn: Boolean = false
    private var isTrainOn: Boolean = false
    private var isAirplaneOn: Boolean = false

    private val sCtrl = SoundControl()

    private var _latitude = 0.0     //　緯度フィールド
    private var _longitude = 0.0    // 経度フィールド
    private val insideLatitudeRange = 0.01  // 範囲内とする緯度の値
    private val insideLongitudeRange = 0.01  // 範囲内とする経度の値

    private val _hnd_geo_lat = 35.549404         // 羽田空港の代表緯度
    private val _hnd_geo_long = 139.780118       // 羽田空港の代表経度
    private val _hnd_insideRange = 0.05          // 範囲内とする緯度経度の値（暫定）
    private val _minutes_required_for_arrival:Long = 10 // 到着に必要な時間(分)。到着予定時刻のこの時間の前からノイズキャンセラ実行

    private val stationLimit = 3 // 最寄り駅として選定する数

    private val handler = Handler()
    private var getTrainRunnable: Runnable? = null
    private var getAirplanRunnable: Runnable? = null

    @RequiresApi(Build.VERSION_CODES.N)
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

        mSoundControlViewModel!!.getAllGains().observe(this, Observer {
            // データベース初期化時にnullがobserveされるためチェック
            if (it != null) {
                sCtrl.setCutOff(it)
                adapter!!.setGains(it)
            }
        })

        mSoundControlViewModel!!.getSetting().observe(this, Observer {
            // データベース初期化時にnullがobserveされるためチェック
            if (it != null) {

                switchNr.isChecked = it.mNr
                sCtrl.setNr(it.mNr)

                switchNrTranportation.isChecked = it.mNrTranportation
                handleNrTransportation(it.mNrTranportation)

                switchBluetoothMic.isChecked = it.mBluetoothMic
                sCtrl.setBluetoothMic(it.mBluetoothMic)
            }
        })

        switchNr.setOnClickListener {
            mSoundControlViewModel!!.switchNr()
        }
        switchNrTranportation.setOnClickListener {
            mSoundControlViewModel!!.switchNrTransportation()
        }
        switchBluetoothMic.setOnClickListener {
            mSoundControlViewModel!!.switchBluetoothMic()
        }

        val onOffButton = findViewById<ImageButton>(R.id.onOffButton)
        onOffButton.setOnClickListener {
            if (!isOn) {
                sCtrl.startFiltering()
                onOffButton.setImageResource(R.drawable.ic_pause_circle_outline_200dp)
            } else {
                sCtrl.stopFiltering()
                onOffButton.setImageResource(R.drawable.ic_play_circle_outline_200dp)
            }
            isOn = !isOn
        }

        checkPermission()

        //LocationManagerオブジェクトを取得
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        // 位置情報が更新された際のリスナオブジェクトを生成
        val locationListener = GPSLocationListener()
        //位置情報の追跡を開始。
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0f, locationListener)

    }

    private fun handleNrTransportation(start: Boolean) {
        val name: String = getString(R.string.public_transport_gain_name)
        val frequencies: String = getString(R.string.public_transport_gain_freq)
        val gainInt = 0
        // delete train noise gain
        val dupGains = mSoundControlViewModel!!.queryGains(name)
        for (n in dupGains) {
            mSoundControlViewModel!!.deleteGain(n)
        }
        if (start){
            val gain = Gain(0, name, frequencies, gainInt)
            mSoundControlViewModel!!.insertGain(gain)
            // 列車判定の繰り返し実行
            getTrainRunnable = Runnable {
                val stationNameList = getStationNameListInCurrentVicinity()
                val stationText = findViewById<TextView>(R.id.near_station)
                if (stationNameList.isNotEmpty()){
                    val param = stationNameList.joinToString(separator = ",")
                    stationText.text = param
                    val receiver = OdptStationInfoReceiver()
                    receiver.execute("dc:title=$param")
                }else {
                    stationText.text = getString(R.string.msg_station_not_found_in_db)
                    stopTrainNoiseReduction(getString(R.string.msg_station_not_found_in_db))
                }
                handler.postDelayed(getTrainRunnable!!, 10000)
            }
            handler.post(getTrainRunnable!!)

            // 飛行機判定の繰り返し実行
            getAirplanRunnable= Runnable {
                if (isInsideHndNoise()){
                    val receiver = OdptAirportInfoReceiver()
                    receiver.execute("odpt:arrivalAirport=odpt.Airport:HND")
                }else {
                    stopAirplaneNoiseReduction(getString(R.string.msg_not_inside_hnd_airport))
                }
                handler.postDelayed(getAirplanRunnable!!, 10000)
            }
            handler.post(getAirplanRunnable!!)

        } else {
            if (getTrainRunnable !=null){
                handler.removeCallbacks(getTrainRunnable!!)
                getTrainRunnable =null
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val mAudioManager = this.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        sCtrl.setAudioManager(mAudioManager)
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
                permissionCode)
        }
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.MODIFY_AUDIO_SETTINGS)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(android.Manifest.permission.MODIFY_AUDIO_SETTINGS),
                permissionCode)
        }
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.BLUETOOTH)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(android.Manifest.permission.BLUETOOTH),
                permissionCode)
        }
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.BLUETOOTH_ADMIN)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(android.Manifest.permission.BLUETOOTH_ADMIN),
                permissionCode)
        }
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                permissionCode)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == 1111 && resultCode == Activity.RESULT_OK){
            val type:String = data!!.getStringExtra("type")!!
            val id:Int = data.getIntExtra("id", 0)
            val name: String = data.getStringExtra("name")!!
            val frequencies: String = data.getStringExtra("frequencies")!!
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
     * 羽田空港の雑音範囲内にいるか判定する
     */
    private fun isInsideHndNoise():Boolean{
        val minLongitude = _hnd_geo_long-_hnd_insideRange
        val maxLongitude= _hnd_geo_long+_hnd_insideRange
        val minLatitude = _hnd_geo_lat-_hnd_insideRange
        val maxLatitude= _hnd_geo_lat+_hnd_insideRange
        if(minLongitude < _longitude && _longitude < maxLongitude &&  minLatitude< _latitude && _latitude <maxLatitude){
            return true
        }
        return  false
    }
    /**
     * 周辺の駅名の一覧を取得するメソッド。
     */
    @SuppressLint("Recycle")
    private fun getStationNameListInCurrentVicinity():List<String> {

        // 特定の範囲に入ったら表示
        val minLongitude = _longitude-insideLongitudeRange
        val maxLongitude= _longitude+insideLongitudeRange
        val minLatitude = _latitude-insideLatitudeRange
        val maxLatitude= _latitude+insideLatitudeRange

        // 経度緯度による範囲内の駅(簡易的に平面距離で求めている)
        val nearStations = mSoundControlViewModel!!.getNearStations(_latitude,_longitude,
            minLongitude,maxLongitude,minLatitude,maxLatitude,stationLimit)

        val nameList: MutableList<String> = mutableListOf()
        for (station in nearStations) {
            nameList.add(station.station_name)
        }
        // 重複を削除して戻す
        return nameList.distinct()
    }

    /**
     * 表示されたパーミッションダイアログに対して、ユーザーが許可しないを選択した場合に呼び出されるメソッド。
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        //ACCESS_FINE_LOCATIONに対するパーミションダイアログでかつ許可を選択したなら…
        if(requestCode == permissionCode && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
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
     * URlからWEB情報(JSON)取得するメソッド
     *
     * @param urlStr 対象URL
     * @return 取得JSON内容
     */
    private fun getWebJsonData(urlStr: String):String{
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
        Log.i("getWebData", "Response Code is $resCode")

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

    /**
     * 駅情報オープンデータを非同期でAPIデータを取得するクラス。
     */
    @SuppressLint("StaticFieldLeak")
    private inner class OdptStationInfoReceiver : AsyncTask<String, String, String>() {

        /**
         * バックグラウンドで実行される処理（API実行）
         */
        override fun doInBackground(vararg params: String): String {
            val apiKey =  getString(R.string.api_key)
            var targetApi = "${getString(R.string.station_api_url)}?"
            targetApi += params.joinToString("&")
            //API keyを使って接続URL文字列を作成。
            val urlStr = targetApi + "&acl:consumerKey=${apiKey}"
            return getWebJsonData(urlStr)
        }

        /**
         * API処理結果を受けて実行する処理
         */
        override fun onPostExecute(result: String) {
            //JSON文字列からJSONObjectオブジェクトを生成。これをルートJSONオブジェクトとする。
            val arrayJSON = JSONArray(result)
            val stationNames: MutableList<String> = mutableListOf()
            for (i in 0 until arrayJSON.length()) {
                val currentJSON = arrayJSON.getJSONObject(i)
                //各種データを取得
                val stationName = currentJSON.getString("owl:sameAs")
                stationNames.add(stationName)
            }
            if(stationNames.size >0){
                val param = stationNames.joinToString(separator = ",")
                val receiver = OdptTrainInfoReceiver()
                receiver.execute("odpt:fromStation=$param")
            }else{
                stopTrainNoiseReduction("API駅情報なし")
            }
        }
    }

    /**
     * 列車オープンデータを非同期でAPIデータを取得するクラス。
     */
    @SuppressLint("StaticFieldLeak")
    private inner class OdptTrainInfoReceiver : AsyncTask<String, String, String>() {

        /**
         * バックグラウンドで実行される処理（API実行）
         */
        override fun doInBackground(vararg params: String): String {
            val apiKey =  getString(R.string.api_key)
            var targetApi = "${getString(R.string.train_api_url)}?"
            targetApi += params.joinToString("&")
            //API keyを使って接続URL文字列を作成。
            val urlStr = targetApi + "&acl:consumerKey=${apiKey}"
            return getWebJsonData(urlStr)
        }

        /**
         * API処理結果をUIに反映される処理
         */
        override fun onPostExecute(result: String) {
            //JSON文字列からJSONObjectオブジェクトを生成。これをルートJSONオブジェクトとする。
            val arrayJSON = JSONArray(result)

            val trainNumbers: MutableList<String> = mutableListOf()
            for (i in 0 until arrayJSON.length()) {
                val currentJSON = arrayJSON.getJSONObject(i)
                //各種データを取得
                val toStation = currentJSON.getString("odpt:toStation")     // 列車が向かっている駅を表すID
                if (toStation != "null"){
                    val trainNumber = currentJSON.getString("owl:sameAs") // 列車番号
                    trainNumbers.add(trainNumber)
                }
            }
            if(trainNumbers.count() >0){
                startTrainNoiseReduction(trainNumbers)
            }else{
                stopTrainNoiseReduction("列車情報なし")
            }
        }
    }

    /**
     * 列車ノイズ低減を開始する関数
     */
    private fun startTrainNoiseReduction(trainList: List<String>){
        isTrainOn = true
        val note = trainList.joinToString(separator = ",")
        val statonNameText = findViewById<TextView>(R.id.train_name)
        statonNameText.text =note
        if(!isOn){
            onOffButton.callOnClick()
        }
    }

    /**
     * 列車ノイズ低減を開停止する関数
     */
    private fun stopTrainNoiseReduction(reason: String){
        isTrainOn = false
        val statonNameText = findViewById<TextView>(R.id.train_name)
        statonNameText.text =reason
        if(isAirplaneOn==false && isOn){
            onOffButton.callOnClick()
        }
    }

    /**
     * 空港情報オープンデータを非同期でAPIデータを取得するクラス。
     */
    @SuppressLint("StaticFieldLeak")
    private inner class OdptAirportInfoReceiver : AsyncTask<String, String, String>() {

        /**
         * バックグラウンドで実行される処理（API実行）
         */
        override fun doInBackground(vararg params: String): String {
            val apiKey =  getString(R.string.api_key)
            var targetApi = "${getString(R.string.flight_info_arrival)}?"
            targetApi += params.joinToString("&")
            //API keyを使って接続URL文字列を作成。
            val urlStr = targetApi + "&acl:consumerKey=${apiKey}"
            return getWebJsonData(urlStr)
        }

        /**
         * API処理結果を受けて実行する処理
         */
        @RequiresApi(Build.VERSION_CODES.O)
        override fun onPostExecute(result: String) {
            //JSON文字列からJSONObjectオブジェクトを生成。これをルートJSONオブジェクトとする。
            val arrayJSON = JSONArray(result)
            val airplanesNames: MutableList<String> = mutableListOf()
            val now = LocalDateTime.now()
            val nowDate = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            val arrivalRequiredMinutes = now.plusMinutes(_minutes_required_for_arrival)
            for (i in 0 until arrayJSON.length()) {
                val currentJSON = arrayJSON.getJSONObject(i)
                // 到着データを持っていない　＝　まだ到着していない
                if(currentJSON.has("odpt:actualArrivalTime") == false){
                    val scheduledArrivalTime = currentJSON.getString("odpt:scheduledArrivalTime")
                    val scheduledArrivalDateString = "${nowDate}T${scheduledArrivalTime}:00"
                    val scheduledArrivalDate = LocalDateTime.parse(scheduledArrivalDateString)
                    if(now.isBefore(scheduledArrivalDate) && arrivalRequiredMinutes.isAfter(scheduledArrivalDate)){
                        val airplaneName = currentJSON.getString("odpt:flightNumber")
                        airplanesNames.add(airplaneName)
                    }
                }
            }

            if(airplanesNames.count() >0){
                startAirplaneNoiseReduction(airplanesNames)
            }else{
                stopAirplaneNoiseReduction("該当飛行情報なし")
            }
        }
    }

    /**
     * 飛行機ノイズ低減を開始する関数
     */
    private fun startAirplaneNoiseReduction(trainList: List<String>){
        isAirplaneOn = true
        val note = trainList.joinToString(separator = ",")
        val airplaneNameText = findViewById<TextView>(R.id.airplane_name)
        airplaneNameText.text =note
        if(!isOn){
            onOffButton.callOnClick()
        }
    }

    /**
     * 飛行機ノイズ低減を開停止する関数
     */
    private fun stopAirplaneNoiseReduction(reason: String){
        isAirplaneOn = false
        val airplaneNameText = findViewById<TextView>(R.id.airplane_name)
        airplaneNameText.text =reason
        if(isTrainOn==false && isOn){
            onOffButton.callOnClick()
        }
    }
}
