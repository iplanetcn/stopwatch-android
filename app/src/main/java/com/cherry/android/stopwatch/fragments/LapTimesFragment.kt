package com.cherry.android.stopwatch.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.widget.AbsListView.MultiChoiceModeListener
import android.widget.AdapterView
import android.widget.AdapterView.OnItemLongClickListener
import android.widget.ListView
import androidx.fragment.app.ListFragment
import com.cherry.android.stopwatch.R
import com.cherry.android.stopwatch.MainActivity
import com.cherry.android.stopwatch.databinding.FragmentLapTimesBinding
import com.cherry.android.stopwatch.utils.LapTimeRecorder

class LapTimesFragment : ListFragment(), LapTimeListener {
    private var mLapTimes: ArrayList<LapTimeBlock> = ArrayList()
    private var mCheckedItems: ArrayList<Int> = ArrayList()
    private lateinit var mAdapter: LapTimesBaseAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return FragmentLapTimesBinding.inflate(layoutInflater, container, false).root
    }

    override fun onStart() {
        super.onStart()
        val ltf = this
        val listView = listView
        listView.cacheColorHint = Color.WHITE
        listView.choiceMode = ListView.CHOICE_MODE_MULTIPLE_MODAL

        listView.setMultiChoiceModeListener(object : MultiChoiceModeListener {
            override fun onItemCheckedStateChanged(
                actionMode: ActionMode,
                i: Int,
                l: Long,
                checked: Boolean
            ) {
                if (checked) {
                    mCheckedItems.add(i)
                } else {
                    mCheckedItems.remove(i)
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
                return when (menuItem.itemId) {
                    R.id.menu_context_delete -> {
                        LapTimeRecorder.deleteLapTimes(mCheckedItems, ltf)
                        actionMode.finish() // Action picked, so close the CAB
                        mCheckedItems.clear()
                        true
                    }
                    else -> false
                }
            }

            override fun onDestroyActionMode(actionMode: ActionMode) {}
        })
        mAdapter = LapTimesBaseAdapter(requireActivity(), mLapTimes)
        listAdapter = mAdapter
        (activity as MainActivity?)!!.registerLapTimeFragment(this)

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


    override fun onResume() {
        super.onResume()
        mLapTimes.clear()
        mLapTimes.addAll(LapTimeRecorder.times)
        mAdapter.notifyDataSetChanged()
    }

    fun reset() {
        mLapTimes.clear()
        mAdapter.notifyDataSetChanged()
    }

    private fun notifyDataSetChanged() {
        mAdapter.notifyDataSetChanged()
    }

    override fun lapTimesUpdated() {
        mLapTimes.clear()
        mLapTimes.addAll(LapTimeRecorder.times)
        notifyDataSetChanged()
    }
}