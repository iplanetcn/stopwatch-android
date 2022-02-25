package com.cherry.android.stopwatch.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.*
import androidx.appcompat.view.ActionMode
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.cherry.android.stopwatch.MainActivity
import com.cherry.android.stopwatch.R
import com.cherry.android.stopwatch.databinding.FragmentLapTimesBinding
import com.cherry.android.stopwatch.utils.LapTimeRecorder

class LapTimesFragment : Fragment(), LapTimeListener {
    private var lapTimes: ArrayList<LapTimeBlock> = ArrayList()
    private lateinit var adapter: LapTimesAdapter
    private lateinit var binding: FragmentLapTimesBinding
    private var actionMode: ActionMode? = null
    private val actionModeCallback = object : ActionMode.Callback {
        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            mode.menuInflater.inflate(R.menu.menu_laptimes_contextual, menu)
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
            menu.findItem(R.id.menu_context_select_all).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
            menu.findItem(R.id.menu_context_delete).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
            return true
        }

        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
            return when (item.itemId) {
                R.id.menu_context_select_all -> {
                    selectAll()
                    return true
                }
                R.id.menu_context_delete -> {
                    deleteRows()
                    true
                }
                else -> {
                    mode.finish()
                    false
                }
            }
        }

        override fun onDestroyActionMode(mode: ActionMode) {
            adapter.removeSelection()
            actionMode = null
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLapTimesBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        adapter = LapTimesAdapter(lapTimes, onRowClicked, onRowLongClicked)
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.recyclerView.setHasFixedSize(true)
        binding.recyclerView.addItemDecoration(
            DividerItemDecoration(
                requireContext(),
                LinearLayoutManager.VERTICAL
            )
        )
        binding.recyclerView.itemAnimator = DefaultItemAnimator()
        (activity as MainActivity?)!!.registerLapTimeFragment(this)
    }

    private val onRowClicked: (position: Int) -> Unit = { position ->
        Toast.makeText(requireContext(), "click: $position", Toast.LENGTH_SHORT).show()
        enableActionMode(position)
    }

    private val onRowLongClicked: (position: Int) -> Unit = { position ->
        Toast.makeText(requireContext(), "longClick: $position", Toast.LENGTH_SHORT).show()
        enableActionMode(position)
    }

    private fun enableActionMode(position: Int) {
        if (actionMode == null) {
            val requireActivity = requireActivity() as AppCompatActivity
            actionMode = requireActivity.startSupportActionMode(actionModeCallback)
        }
        toggleSelection(position)
    }

    private fun toggleSelection(position: Int) {
        adapter.toggleSelection(position)
        val count: Int = adapter.getSelectedItemCount()

        if (count == 0) {
            actionMode!!.finish()
            actionMode = null
        } else {
            actionMode!!.title = count.toString()
            actionMode!!.invalidate()
        }
    }

    private fun selectAll() {
        adapter.selectAll()
        val count: Int = adapter.getSelectedItemCount()

        if (count == 0) {
            actionMode!!.finish()
        } else {
            actionMode!!.title = count.toString()
            actionMode!!.invalidate()
        }

        actionMode = null
    }

    private fun deleteRows() {
        val selectedItemPositions: List<Int> = adapter.getSelectedItems()
        for (i in selectedItemPositions.indices.reversed()) {
            adapter.removeData(selectedItemPositions[i])
        }
        notifyDataSetChanged()
        actionMode = null
    }


    override fun onResume() {
        super.onResume()
        lapTimes.clear()
        lapTimes.addAll(LapTimeRecorder.times)
        notifyDataSetChanged()
    }

    fun reset() {
        lapTimes.clear()
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun notifyDataSetChanged() {
        adapter.notifyDataSetChanged()
    }

    override fun lapTimesUpdated() {
        lapTimes.clear()
        lapTimes.addAll(LapTimeRecorder.times)
        notifyDataSetChanged()
    }
}