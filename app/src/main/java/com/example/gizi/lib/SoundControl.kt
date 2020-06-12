package com.example.gizi.lib

import android.media.*
import android.media.AudioFormat.CHANNEL_OUT_MONO
import android.media.AudioFormat.ENCODING_PCM_16BIT
import android.os.Build
import androidx.annotation.RequiresApi
import com.example.gizi.database.Gain
import org.jtransforms.fft.DoubleFFT_1D
import kotlin.math.max

class SoundControl {

    // サンプリングレート (Hz)
    // 全デバイスサポート保障は44100のみ
    private val samplingRate = 8000

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
                    AudioRecord.getMinBufferSize(samplingRate,
                            AudioFormat.CHANNEL_IN_MONO,
                            ENCODING_PCM_16BIT
                    ))

    private var audioTrack : AudioTrack? = null
    private var audioRecord : AudioRecord? = null

    private var nrOn: Boolean = false
    private var cutOffs: List<Map<String,Int>> = listOf()

    private var isPlaying: Boolean = false

    private var mAudioManager: AudioManager? = null
    private var bluetoothMic: Boolean? = null

    fun setAudioManager(audioManager: AudioManager) {
        mAudioManager = audioManager
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun setNr(new: Boolean) {
        nrOn = new
        if (isPlaying) {
            restart()
        }
    }

    fun setBluetoothMic(new: Boolean) {
        bluetoothMic = new
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun setCutOff(new: List<Gain>) {
        val newCutOffs = mutableListOf<Map<String,Int>>()
        for (n in new) {
            val splited = n.mFrequencies.split(",")
            val gain = n.mGain
            for (s in splited) {
                val low = s.split("-")[0].toInt()
                val high = s.split("-")[1].toInt()
                newCutOffs.add(mapOf("high" to high, "low" to low, "gain" to gain))
            }
        }
        cutOffs = newCutOffs
        if (isPlaying) {
            restart()
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun start() {
        if (bluetoothMic!!) {
            //https://developer.android.com/reference/android/media/AudioManager#startBluetoothSco()
            //https://developer.android.com/guide/topics/connectivity/bluetooth#Profiles

            mAudioManager!!.mode = AudioManager.MODE_IN_CALL
            mAudioManager!!.startBluetoothSco()
            mAudioManager!!.isBluetoothScoOn = true

        }

        audioTrack = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_UNKNOWN)
                .build())
            .setAudioFormat(AudioFormat.Builder()
                .setEncoding(ENCODING_PCM_16BIT)
                .setSampleRate(samplingRate)
                .setChannelMask(CHANNEL_OUT_MONO)
                .build())
            .setBufferSizeInBytes(audioBufferSizeInByte)
            .build()

        var audioSource = MediaRecorder.AudioSource.UNPROCESSED

        if (nrOn) {
            audioSource = MediaRecorder.AudioSource.DEFAULT
        }

        audioRecord = AudioRecord(
            audioSource, // 音声のソース
            samplingRate, // サンプリングレート
            AudioFormat.CHANNEL_IN_MONO, // チャネル設定. MONO and STEREO が全デバイスサポート保障
            ENCODING_PCM_16BIT, // PCM16が全デバイスサポート保障
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
                val data: DoubleArray = audioDataArray.map {it.toDouble()}.toDoubleArray()
                fft.realForward(data)
                // data[0]は実数成分、data[1]は虚数成分～data[n]は実数成分、data[n+1}は虚数成分
                // data[0]は実数成分、data[1]は虚数成分～data[n]は実数成分、data[n+1}は虚数成分
                val coefficinet = samplingRate/audioDataArray.size/2
                for (n in cutOffs) {
                    val low = n["low"] ?: error("")
                    val high = n["high"] ?: error("")
                    val gain = (n["gain"] ?: error("")) / 50
                    for (it in low..high step coefficinet) {
                        if (it % coefficinet == 0) {
                            data[it/coefficinet] = data[it/coefficinet] * gain
                        }
                    }
                }
//                var peakFq = data.indices.maxBy { data[it] }
//                var peakVl = data.max()
//                peakFq = peakFq!!.toInt() * coefficinet
//                Log.d("MAXVOL", peakVl.toString())

                // 逆フーリエ変換
                // 逆フーリエ変換
                fft.realInverse(data, true)
                audioTrack!!.write(data.map {it.toShort()}.toShortArray(), 0, audioDataArray.size)
            }

            // マーカータイミングの処理.
            // notificationMarkerPosition に到達した際に呼ばれる
            override fun onMarkerReached(recorder: AudioRecord) {
            }
        })

        audioRecord!!.startRecording()
        audioTrack!!.play()

        isPlaying = true
    }

    fun stop() {
        audioTrack!!.pause()
        audioRecord!!.stop()
        audioTrack!!.release()
        audioRecord!!.release()
        isPlaying = false
        if (bluetoothMic!!) {
            mAudioManager!!.stopBluetoothSco()
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun restart() {
        stop()
        start()
    }
}