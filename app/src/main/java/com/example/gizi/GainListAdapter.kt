package com.example.gizi

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.gizi.database.Gain
import com.example.gizi.database.SoundControlViewModel
import com.example.gizi.databinding.RecyclerviewItemBinding


class GainListAdapter(val onClickListener: (Gain) -> Unit,
                      val mSoundControlViewModel: SoundControlViewModel
) : RecyclerView.Adapter<GainListAdapter.GainViewHolder>() {
    private var mGains:List<Gain>? = null

    fun setGains(mGains:List<Gain>){
        this.mGains = mGains
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GainViewHolder {
        val binding:RecyclerviewItemBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.recyclerview_item, parent, false)
        return GainViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return if (mGains != null){
            mGains!!.size
        } else{
            0
        }
    }

    override fun onBindViewHolder(holder: GainViewHolder, position: Int) {
        val gain:Gain = mGains!![position]
        holder.viewBinding.setVariable(BR.gain, gain)
        holder.viewBinding.executePendingBindings()

        holder.itemView.setOnClickListener{
            onClickListener(this.mGains!![position])
        }
        val seekBar = holder.itemView.findViewById<SeekBar>(R.id.seekBar)
        seekBar.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                }

                override fun onStartTrackingTouch(seekBar: SeekBar) {
                }

                override fun onStopTrackingTouch(seekBar: SeekBar) {
                    val newGain = Gain(gain.id,gain.mName,gain.mFrequencies,seekBar.progress)
                    mSoundControlViewModel.updateGain(newGain)
                }

            })
    }

    class GainViewHolder(binding: RecyclerviewItemBinding):RecyclerView.ViewHolder(binding.root){
        val viewBinding : RecyclerviewItemBinding = binding

    }
}