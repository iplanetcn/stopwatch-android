package com.geekyouup.android.ustopwatch.fragments

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.LinearLayout
import android.widget.TextView
import com.geekyouup.android.ustopwatch.R
import com.geekyouup.android.ustopwatch.TimeUtils

class LapTimesBaseAdapter(
    private val mContext: Context?,
    private val mDataSet: ArrayList<LapTimeBlock?>?
) : BaseAdapter() {
    private var mLayoutInflator: LayoutInflater? = null
    override fun getCount(): Int {
        return mDataSet?.size ?: 0
    }

    override fun getItem(position: Int): LapTimeBlock? {
        return mDataSet?.get(position)
    }
    override fun getItemId(position: Int): Long {
        return 0
    }

    override fun getView(position: Int, convertView: View, parent: ViewGroup): View {
        if (mLayoutInflator == null) mLayoutInflator =
            mContext!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        var v = convertView
        if (v == null) v = mLayoutInflator!!.inflate(R.layout.laptimes_holder_list_item, null)
        val listItemHolder = v.findViewById<View>(R.id.laptimes_list_item_holder) as LinearLayout
        listItemHolder.removeAllViews()
        val ltb = mDataSet!![position]
        val lapTimes = ltb!!.lapTimes
        for (i in lapTimes!!.indices) {
            val lapItemView = mLayoutInflator!!.inflate(R.layout.laptime_item, null)
            if (i == 0) {
                val t = lapItemView.findViewById<View>(R.id.laptime_text) as TextView
                t.text = TimeUtils.createStyledSpannableString(
                    mContext,
                    lapTimes!![i]!!,
                    true
                )
            }
            val t2 = lapItemView.findViewById<View>(R.id.laptime_text2) as TextView
            if (i < lapTimes!!.size - 1 && lapTimes!!.size > 1) {
                var laptime = lapTimes!![i]!! - lapTimes!![i + 1]!!
                if (laptime < 0) laptime = lapTimes!![i]!!
                t2.text = TimeUtils.createStyledSpannableString(
                    mContext,
                    laptime,
                    true
                )
            } else {
                t2.text = TimeUtils.createStyledSpannableString(
                    mContext,
                    lapTimes!![i]!!,
                    true
                )
            }
            listItemHolder.addView(lapItemView)
        }
        return v
    }
}