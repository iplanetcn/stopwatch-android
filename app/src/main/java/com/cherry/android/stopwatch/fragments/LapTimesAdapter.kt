package com.cherry.android.stopwatch.fragments

import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.cherry.android.stopwatch.databinding.ItemLapTimeBinding
import com.cherry.android.stopwatch.databinding.ItemLapTimesBinding
import com.cherry.android.stopwatch.utils.TimeUtils

class LapTimesAdapter(
    private val data: ArrayList<LapTimeBlock>,
    private val onRowClicked: ((position: Int) -> Unit)? = null,
    private val onRowLongClicked: ((position: Int) -> Unit)? = null
) : RecyclerView.Adapter<LapTimesAdapter.LapTimesViewHolder>() {
    private val selectedItems = mutableListOf<Int>()

    //region Implementations
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LapTimesViewHolder {
        return LapTimesViewHolder(
            ItemLapTimesBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: LapTimesViewHolder, position: Int) {
        holder.bindData(data[position])
        holder.binding.root.setOnClickListener {
            onRowClicked?.apply {
                invoke(position)
            }
        }

        holder.binding.root.setOnLongClickListener {
            onRowLongClicked?.apply {
                invoke(position)
                it.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
            }
            true
        }
    }
    //endregion

    //region Custom
    override fun getItemCount(): Int {
        return data.size
    }

    fun selectAll() {

    }

    fun getSelectedItemCount(): Int {
        return 0
    }

    fun toggleSelection(position: Int) {

    }

    fun getSelectedItems(): List<Int> {
        return selectedItems
    }

    fun removeData(position: Int) {

    }

    fun removeSelection() {

    }
    //endregion

    //region LapTimesViewHolder
    class LapTimesViewHolder(val binding: ItemLapTimesBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bindData(item: LapTimeBlock) {
            binding.lapTimesContainer.removeAllViews()
            val lapTimes = item.lapTimes
            for (i in lapTimes.indices) {
                val subBinding = ItemLapTimeBinding.inflate(LayoutInflater.from(itemView.context))
                if (i == 0) {
                    subBinding.lapTimeMomentText.text = TimeUtils.createStyledSpannableString(
                        itemView.context,
                        lapTimes[i],
                        true
                    )
                }
                if (i < lapTimes.size - 1 && lapTimes.size > 1) {
                    var lapTime = lapTimes[i] - lapTimes[i + 1]
                    if (lapTime < 0) lapTime = lapTimes[i]
                    subBinding.lapTimeDurationText.text = TimeUtils.createStyledSpannableString(
                        itemView.context,
                        lapTime,
                        true
                    )
                } else {
                    subBinding.lapTimeDurationText.text = TimeUtils.createStyledSpannableString(
                        itemView.context,
                        lapTimes[i],
                        true
                    )
                }
                binding.lapTimesContainer.addView(subBinding.root)
            }
        }
    }
    //endregion
}