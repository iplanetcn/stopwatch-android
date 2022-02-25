package com.cherry.android.stopwatch.fragments

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.LinearLayout
import android.widget.TextView
import com.cherry.android.stopwatch.R
import com.cherry.android.stopwatch.utils.TimeUtils
import com.cherry.android.stopwatch.databinding.LapTimeItemBinding
import com.cherry.android.stopwatch.databinding.LapTimesHolderListItemBinding

class LapTimesBaseAdapter(
    private val mContext: Context,
    private val mDataSet: ArrayList<LapTimeBlock>
) : BaseAdapter() {
    private lateinit var mLayoutInflater: LayoutInflater
    override fun getCount(): Int {
        return mDataSet.size
    }

    override fun getItem(position: Int): LapTimeBlock {
        return mDataSet[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(
        position: Int,
        convertView: View?,
        parent: ViewGroup
    ): View {
        mLayoutInflater = LayoutInflater.from(mContext)
        var v = convertView
        if (v == null) v = LapTimesHolderListItemBinding.inflate(mLayoutInflater).root
        val listItemHolder = v.findViewById<View>(R.id.lap_times_list_item_holder) as LinearLayout
        listItemHolder.removeAllViews()
        val ltb = mDataSet[position]
        val lapTimes = ltb.lapTimes
        for (i in lapTimes.indices) {
            val lapItemView = LapTimeItemBinding.inflate(mLayoutInflater).root
            if (i == 0) {
                val t = lapItemView.findViewById<View>(R.id.lap_time_moment_text) as TextView
                t.text = TimeUtils.createStyledSpannableString(
                    mContext,
                    lapTimes[i],
                    true
                )
            }
            val t2 = lapItemView.findViewById<View>(R.id.lap_time_duration_text) as TextView
            if (i < lapTimes.size - 1 && lapTimes.size > 1) {
                var lapTime = lapTimes[i] - lapTimes[i + 1]
                if (lapTime < 0) lapTime = lapTimes[i]
                t2.text = TimeUtils.createStyledSpannableString(
                    mContext,
                    lapTime,
                    true
                )
            } else {
                t2.text = TimeUtils.createStyledSpannableString(
                    mContext,
                    lapTimes[i],
                    true
                )
            }
            listItemHolder.addView(lapItemView)
        }
        return v
    }
}