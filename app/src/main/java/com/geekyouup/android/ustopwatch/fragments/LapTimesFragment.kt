package com.geekyouup.android.ustopwatch.fragments

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.*
import android.widget.AbsListView.MultiChoiceModeListener
import android.widget.AdapterView
import android.widget.AdapterView.OnItemLongClickListener
import android.widget.ListView
import androidx.fragment.app.ListFragment
import com.geekyouup.android.ustopwatch.R
import com.geekyouup.android.ustopwatch.UltimateStopwatchActivity

class LapTimesFragment : ListFragment(), LapTimeListener {
    private var mAdapter: LapTimesBaseAdapter? = null
    private var mLapTimes: ArrayList<LapTimeBlock?>? = ArrayList()
    private lateinit var mLapTimeRecorder: LapTimeRecorder
    private var mCheckedItems: ArrayList<Int?>? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mLapTimeRecorder = LapTimeRecorder.instance!!
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.laptimes_fragment, container, false)
    }

    override fun onStart() {
        super.onStart()
        val ltf = this
        val listView = listView
        listView.cacheColorHint = Color.WHITE
        listView.choiceMode = ListView.CHOICE_MODE_MULTIPLE_MODAL

        //MultiMode Choice is only available in Honeycomb+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            listView.setMultiChoiceModeListener(object : MultiChoiceModeListener {
                override fun onItemCheckedStateChanged(
                    actionMode: ActionMode,
                    i: Int,
                    l: Long,
                    checked: Boolean
                ) {
                    if (mCheckedItems == null) mCheckedItems = ArrayList()
                    if (checked) {
                        mCheckedItems!!.add(i)
                    } else {
                        mCheckedItems!!.remove(i)
                    }
                }

                override fun onCreateActionMode(actionMode: ActionMode, menu: Menu): Boolean {
                    val inflater = actionMode.menuInflater
                    inflater.inflate(R.menu.menu_laptimes_contextual, menu)
                    return true
                }

                override fun onPrepareActionMode(actionMode: ActionMode, menu: Menu): Boolean {
                    return false
                }

                override fun onActionItemClicked(
                    actionMode: ActionMode,
                    menuItem: MenuItem
                ): Boolean {
                    // Respond to clicks on the actions in the CAB
                    return when (menuItem.itemId) {
                        R.id.menu_context_delete -> {
                            mLapTimeRecorder!!.deleteLapTimes(mCheckedItems, ltf)
                            actionMode.finish() // Action picked, so close the CAB
                            mCheckedItems!!.clear()
                            mCheckedItems = null
                            true
                        }
                        else -> false
                    }
                }

                override fun onDestroyActionMode(actionMode: ActionMode) {}
            })
        }
        mAdapter = LapTimesBaseAdapter(activity, mLapTimes)
        listAdapter = mAdapter
        (activity as UltimateStopwatchActivity?)!!.registerLapTimeFragment(this)

        //on long touch start the contextual actionbar
        getListView().onItemLongClickListener =
            OnItemLongClickListener { _: AdapterView<*>?, _: View?, _: Int, _: Long ->
                requireActivity().startActionMode(object : ActionMode.Callback {
                    override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
                        return false
                    }

                    override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
                        return false
                    }

                    override fun onDestroyActionMode(mode: ActionMode) {}
                    override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
                        return false
                    }
                })
                true
            }
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onResume() {
        super.onResume()

        // if vars stored then use them
        mLapTimes!!.clear()
        mLapTimes!!.addAll(mLapTimeRecorder.times)
        mAdapter!!.notifyDataSetChanged()
    }

    fun reset() {
        mLapTimes!!.clear()
        mAdapter!!.notifyDataSetChanged()
    }

    fun notifyDataSetChanged() {
        mAdapter!!.notifyDataSetChanged()
    }

    override fun lapTimesUpdated() {
        if (mLapTimeRecorder == null) mLapTimeRecorder = LapTimeRecorder.instance!!
        if (mLapTimes == null) mLapTimes = ArrayList()
        mLapTimes!!.clear()
        mLapTimes!!.addAll(mLapTimeRecorder.times)
        notifyDataSetChanged()
    }
}