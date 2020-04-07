package com.example.gizi

import android.Manifest
import android.content.pm.PackageManager
import android.media.*
import android.media.AudioFormat.CHANNEL_OUT_MONO
import android.media.AudioFormat.ENCODING_PCM_16BIT
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import org.jtransforms.fft.DoubleFFT_1D
import kotlin.math.max


class MainActivity : AppCompatActivity() {

    private val PERMISSION_CODE = 1234

    // サンプリングレート (Hz)
    // 全デバイスサポート保障は44100のみ
    private val samplingRate = 44100

    // フレームレート (fps)
    // 1秒間に何回音声データを処理したいか
    // 各自好きに決める
    private val frameRate = 10

    // 1フレームの音声データ(=Short値)の数
    private val oneFrameDataCount = samplingRate / frameRate

    // 1フレームの音声データのバイト数 (byte)
    // Byte = 8 bit, Short = 16 bit なので, Shortの倍になる
    private val oneFrameSizeInByte = oneFrameDataCount * 2

    // 音声データのバッファサイズ (byte)
    // 要件1:oneFrameSizeInByte より大きくする必要がある
    // 要件2:デバイスの要求する最小値より大きくする必要がある
    private val audioBufferSizeInByte =
            max(oneFrameSizeInByte, // 適当に10フレーム分のバッファを持たせた
                    android.media.AudioRecord.getMinBufferSize(samplingRate,
                            AudioFormat.CHANNEL_IN_MONO,
                            AudioFormat.ENCODING_PCM_16BIT))

    private var audioTrack : AudioTrack? = null
    private var audioRecord : AudioRecord? = null

    private var isPlaying: Boolean = false
    private var ncOn: Boolean = false

    fun start() {

        audioTrack = AudioTrack.Builder()
            .setAudioAttributes(AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build())
            .setAudioFormat(AudioFormat.Builder()
                .setEncoding(ENCODING_PCM_16BIT)
                .setSampleRate(samplingRate)
                .setChannelMask(CHANNEL_OUT_MONO)
                .build())
            .setBufferSizeInBytes(audioBufferSizeInByte)
            .build()

        var audioSource : Int? = null

        if (ncOn) {
            audioSource = MediaRecorder.AudioSource.DEFAULT
        } else {
            audioSource = MediaRecorder.AudioSource.UNPROCESSED
        }

        audioRecord = AudioRecord(
            audioSource, // 音声のソース
            samplingRate, // サンプリングレート
            AudioFormat.CHANNEL_IN_MONO, // チャネル設定. MONO and STEREO が全デバイスサポート保障
            AudioFormat.ENCODING_PCM_16BIT, // PCM16が全デバイスサポート保障
            audioBufferSizeInByte) // バッファ

        // 音声データを幾つずつ処理するか( = 1フレームのデータの数)
        audioRecord!!.positionNotificationPeriod = oneFrameDataCount

        // ここで指定した数になったタイミングで, 後続の onMarkerReached が呼び出される
        // 通常のストリーミング処理では必要なさそう？
        audioRecord!!.notificationMarkerPosition = 40000 // 使わないなら設定しない.

        // 音声データを格納する配列
        val audioDataArray = ShortArray(oneFrameDataCount)

        // コールバックを指定
        audioRecord!!.setRecordPositionUpdateListener(object : AudioRecord.OnRecordPositionUpdateListener {

            // フレームごとの処理
            override fun onPeriodicNotification(recorder: AudioRecord) {
                recorder.read(audioDataArray, 0, oneFrameDataCount) // 音声データ読込
                // 好きに処理する
                val fft = DoubleFFT_1D(audioDataArray.size.toLong())

                // フーリエ変換(FFT)の実行
                // フーリエ変換(FFT)の実行
                var data: DoubleArray = audioDataArray.map {it.toDouble()}.toDoubleArray()
                fft.realForward(data)
                // data[0]は実数成分、data[1]は虚数成分～data[n]は実数成分、data[n+1}は虚数成分
                // data[0]は実数成分、data[1]は虚数成分～data[n]は実数成分、data[n+1}は虚数成分
                val coefficinet = samplingRate/audioDataArray.size/2
                val low = Integer.parseInt(findViewById<EditText>(R.id.CUTOFF_LOW).text.toString())
                val high = Integer.parseInt(findViewById<EditText>(R.id.CUTOFF_HIGH).text.toString())
                for (it in low..high step coefficinet) {
                    if (it % coefficinet == 0) {
                        data[it/coefficinet] = 0.0
                    }
                }
                var peakFq = data.indices.maxBy { data[it] }
                peakFq = peakFq!!.toInt() * coefficinet
                Log.d("FFTPEAKFQ", peakFq.toString())

                // 逆フーリエ変換
                // 逆フーリエ変換
                fft.realInverse(data, true)
                audioTrack!!.write(data.map {it.toShort()}.toShortArray(), 0, audioDataArray.size)
            }

            // マーカータイミングの処理.
            // notificationMarkerPosition に到達した際に呼ばれる
            override fun onMarkerReached(recorder: AudioRecord) {
                recorder.read(audioDataArray, 0, oneFrameDataCount) // 音声データ読込
                Log.v("AudioRecord", "onMarkerReached size=${audioDataArray.size}")
                // 好きに処理する
            }
        })

        audioRecord!!.startRecording()
        audioTrack!!.play()
    }

    fun stop() {
        audioTrack!!.pause()
        audioRecord!!.stop()
        audioTrack!!.release()
        audioRecord!!.release()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.prototype_main)
//        if (ContextCompat.checkSelfPermission(this,
//                        Manifest.permission.RECORD_AUDIO)
//                != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(this,
//                    arrayOf(Manifest.permission.RECORD_AUDIO),
//                PERMISSION_CODE);
//        }
//        val checkBox : CheckBox = findViewById(R.id.checkbox);
//        checkBox.setOnClickListener {
//            val check: Boolean = checkBox.isChecked()
//            if (check) {
//                ncOn = true
//            } else {
//                ncOn = false
//            }
//        }
//        val playButton: Button = findViewById(R.id.button)
//        playButton.setOnClickListener {
//            if (!isPlaying) {
//                isPlaying = true
//                start()
//                playButton.setText("停止")
//            } else {
//                isPlaying = false
//                stop()
//                playButton.setText("実行")
//            }
//        }

    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSION_CODE -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return
            }

            // Add other 'when' lines to check for other
            // permissions this app might request.
            else -> {
                // Ignore all other requests.
            }
        }
    }


}
