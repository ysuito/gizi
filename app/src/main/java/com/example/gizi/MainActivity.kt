package com.example.gizi

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity;
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageButton
import android.widget.Switch
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import com.example.gizi.database.SoundControlViewModel
import androidx.lifecycle.ViewModelProviders
import com.example.gizi.database.Gain
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.ItemTouchHelper
import com.example.gizi.database.Setting

class MainActivity : AppCompatActivity() {

    private var adapter:GainListAdapter?= null
    private var mSoundControlViewModel:SoundControlViewModel? = null
    private var onOffFlag: Boolean = true

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

        mSoundControlViewModel!!.getAllGains().observe(this, Observer {
            adapter!!.setGains(it)
        })

        val switchNr = findViewById<Switch>(R.id.switchNr)
        val switchNrTranportation = findViewById<Switch>(R.id.switchNrTransportation)
        var isSwitchInitiated = false
        mSoundControlViewModel!!.getSetting().observe(this, Observer {
            if (!isSwitchInitiated) {
                switchNr.isChecked = it[0].mNr
                switchNrTranportation.isChecked = it[0].mNrTranportation   
            }
            isSwitchInitiated = true
        })
        switchNr.setOnCheckedChangeListener { buttonView, isChecked ->
            mSoundControlViewModel!!.switchNr()
        }
        switchNrTranportation.setOnCheckedChangeListener { buttonView, isChecked ->
            mSoundControlViewModel!!.switchNrTranportation()
        }

        val onOffButton = findViewById<ImageButton>(R.id.onOffButton)
        onOffButton.setOnClickListener {
            if (onOffFlag) {
                onOffButton.setImageResource(R.drawable.ic_pause_circle_outline_200dp)
            } else {
                onOffButton.setImageResource(R.drawable.ic_play_circle_outline_200dp)
            }
            onOffFlag = !onOffFlag
        }

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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == 1111 && resultCode == Activity.RESULT_OK){
            val type:String = data!!.getStringExtra("type")
            val id:Int = data!!.getIntExtra("id", 0)
            val name: String = data.getStringExtra("name")
            val frequencies: String = data.getStringExtra("frequencies")
            val gainInt: Int = data.getIntExtra("gain", 0)
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
}
