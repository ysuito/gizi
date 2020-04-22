package com.example.gizi

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.new_gain_activity.*

class NewGainActivity:AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.new_gain_activity)

        val id = intent.getIntExtra("id", 0)
        val name = intent.getStringExtra("name")
        val frequencies = intent.getStringExtra("frequencies")
        val gain = intent.getIntExtra("gain", 0)

        if (name != null) {
            edit_name.setText(name)
        }
        if (frequencies != null) {
            edit_frequencies.setText(frequencies)
        }
        if(gain != 0) {
            edit_gain.setText(gain.toString())
        }

        button_save.setOnClickListener {
            val intent = Intent()

            val nameStr = edit_name.text.toString()
            val frequenciesStr = edit_frequencies.text.toString()
            val gainStr = edit_gain.text.toString()
            if(TextUtils.isEmpty(gainStr)){
                setResult(Activity.RESULT_CANCELED, intent)
            } else {
                val gainInt = gainStr.toInt()
                intent.putExtra("id", id)
                intent.putExtra("name", nameStr)
                intent.putExtra("frequencies", frequenciesStr)
                intent.putExtra("gain", gainInt)
                intent.putExtra("type", "save")
                setResult(Activity.RESULT_OK, intent)
            }
            finish()
        }

        button_delete.setOnClickListener {
            val intent = Intent()

            val nameStr = edit_name.text.toString()
            val frequenciesStr = edit_frequencies.text.toString()
            val gainStr = edit_gain.text.toString()
            if(TextUtils.isEmpty(gainStr)){
                setResult(Activity.RESULT_CANCELED, intent)
            } else {
                val gainInt = gainStr.toInt()
                intent.putExtra("id", id)
                intent.putExtra("name", nameStr)
                intent.putExtra("frequencies", frequenciesStr)
                intent.putExtra("gain", gainInt)
                intent.putExtra("type", "delete")
                setResult(Activity.RESULT_OK, intent)
            }
            finish()
        }
    }
}