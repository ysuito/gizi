package com.example.gizi

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.SpinnerAdapter
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.new_gain_activity.*

class NewGainActivity:AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.new_gain_activity)

        val spinnerItems = arrayOf(
            "カスタム",
            "人の声",
            "ビープ音",
            "チャイム",
            "車の音"
        )
        val noises = arrayOf(
            mapOf(
                "name" to "カスタム",
                "frequencies" to ""
            ),
            mapOf(
                "name" to "人の声",
                "frequencies" to "100-1000"
            ),
            mapOf(
                "name" to "ビープ音",
                "frequencies" to "1995-2005"
            ),
            mapOf(
                "name" to "チャイム",
                "frequencies" to "440-550"
            ),
            mapOf(
                "name" to "車の音",
                "frequencies" to "0-100"
            )
        )

        val id = intent.getIntExtra("id", 0)
        val name = intent.getStringExtra("name")
        val frequencies = intent.getStringExtra("frequencies")
        val gain = intent.getIntExtra("gain", 50)

        if (name != null && frequencies != null) {
            edit_name.setText(name)
            edit_frequencies.setText(frequencies)
            spinnerItems[0] = name
            noises[0] = mapOf("name" to name, "frequencies" to frequencies)
        }

        val spinner: Spinner = findViewById(R.id.presetSpinner)
        val adapter = ArrayAdapter(
            this,
            R.layout.support_simple_spinner_dropdown_item,
            spinnerItems
        )
        adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item)
        spinner.adapter = adapter
        spinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                edit_name.setText(noises.get(position).get("name"))
                edit_frequencies.setText(noises.get(position).get("frequencies"))
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }


        button_save.setOnClickListener {
            val intent = Intent()

            val nameStr = edit_name.text.toString()
            val frequenciesStr = edit_frequencies.text.toString()
            if(TextUtils.isEmpty(nameStr) || TextUtils.isEmpty(frequenciesStr)){
                setResult(Activity.RESULT_CANCELED, intent)
            } else {
                intent.putExtra("id", id)
                intent.putExtra("name", nameStr)
                intent.putExtra("frequencies", frequenciesStr)
                intent.putExtra("gain", gain)
                intent.putExtra("type", "save")
                setResult(Activity.RESULT_OK, intent)
            }
            finish()
        }

        button_delete.setOnClickListener {
            val intent = Intent()

            val nameStr = edit_name.text.toString()
            val frequenciesStr = edit_frequencies.text.toString()
            if(TextUtils.isEmpty(nameStr) || TextUtils.isEmpty(frequenciesStr)){
                setResult(Activity.RESULT_CANCELED, intent)
            } else {
                intent.putExtra("id", id)
                intent.putExtra("name", nameStr)
                intent.putExtra("frequencies", frequenciesStr)
                intent.putExtra("gain", gain)
                intent.putExtra("type", "delete")
                setResult(Activity.RESULT_OK, intent)
            }
            finish()
        }
    }
}