package com.cherry.android.stopwatch.fragments

import android.annotation.SuppressLint
import android.util.SparseBooleanArray
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.util.remove
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.NO_POSITION
import com.cherry.android.stopwatch.databinding.ItemLapTimeBinding
import com.cherry.android.stopwatch.databinding.ItemLapTimesBinding
import com.cherry.android.stopwatch.utils.TimeUtils

class LapTimesAdapter(
    private val data: ArrayList<LapTimeBlock>,
    private val onRowClicked: ((position: Int) -> Unit)? = null,
    private val onRowLongClicked: ((position: Int) -> Unit)? = null
) : RecyclerView.Adapter<LapTimesAdapter.LapTimesViewHolder>() {
    private val selectedItems = SparseBooleanArray()
    private var currentSelectedIndex = NO_POSITION
    private var isActionMode = false

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
        holder.binding.root.isActivated = selectedItems.get(position, false)
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

    @SuppressLint("NotifyDataSetChanged")
    fun selectAll() {
        for (i in 0 until itemCount) selectedItems.put(i, true)
        notifyDataSetChanged()
    }

    fun getSelectedItemCount(): Int {
        return selectedItems.size()
    }

    fun toggleSelection(position: Int) {
        currentSelectedIndex = position
        if (selectedItems.get(position, false)) {
            selectedItems.delete(position)
        } else {
            selectedItems.put(position, true)
        }

        isActionMode = true
        notifyItemChanged(position)
    }

    fun getSelectedItems(): ArrayList<Int> {
        val items = ArrayList<Int>()
        for (i in 0 until selectedItems.size()) {
            items.add(selectedItems.keyAt(i))
        }
        return items
    }

    fun removeData(position: Int) {
        data.removeAt(position)
        selectedItems.remove(position, false)
        resetCurrentIndex()
    }

    private fun resetCurrentIndex() {
        currentSelectedIndex = NO_POSITION
        isActionMode = false
    }


    @SuppressLint("NotifyDataSetChanged")
    fun removeSelection() {
        selectedItems.clear()
        resetCurrentIndex()
        notifyDataSetChanged()
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