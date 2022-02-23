package com.geekyouup.android.ustopwatch

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter
import androidx.fragment.app.FragmentTransaction
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.OnPageChangeListener

/**
 * Created with IntelliJ IDEA.
 * User: rhyndman
 * Date: 10/4/12
 * Time: 3:33 PM
 */
class TabsAdapter(private val mActivity: AppCompatActivity, pager: ViewPager?) :
    FragmentPagerAdapter(
        mActivity.supportFragmentManager
    ), ActionBar.TabListener, OnPageChangeListener {
    private val mActionBar: ActionBar?
    private val mViewPager: ViewPager?
    private val mTabs = ArrayList<TabInfo>()
    var currentTabNum = 0
        private set

    internal class TabInfo(val clss: Class<*>, val args: Bundle?)

    fun addTab(tab: ActionBar.Tab, clss: Class<*>, args: Bundle?) {
        val info = TabInfo(clss, args)
        tab.tag = info
        tab.setTabListener(this)
        mTabs.add(info)
        mActionBar!!.addTab(tab)
        notifyDataSetChanged()
    }

    override fun getCount(): Int {
        return mTabs.size
    }

    override fun getItem(position: Int): Fragment {
        val info = mTabs[position]
        return Fragment.instantiate(
            mActivity,
            info.clss.name, info.args
        )
    }

    override fun onPageScrolled(
        position: Int, positionOffset: Float,
        positionOffsetPixels: Int
    ) {
    }

    override fun onPageSelected(position: Int) {
        mActionBar!!.setSelectedNavigationItem(position)
    }

    override fun onPageScrollStateChanged(state: Int) {
        when (state) {
            ViewPager.SCROLL_STATE_IDLE -> {
                var i = 0
                while (i < 2) {
                    val child = mViewPager!!.getChildAt(i)
                    if (child.visibility != View.GONE) {
                        child.isDrawingCacheEnabled = false
                    }
                    ++i
                }
            }
            ViewPager.SCROLL_STATE_DRAGGING -> {
                var i = 0
                while (i < 2) {
                    val child = mViewPager!!.getChildAt(i)
                    if (child.visibility != View.GONE) {
                        child.isDrawingCacheEnabled = true
                    }
                    ++i
                }
            }
            ViewPager.SCROLL_STATE_SETTLING -> {}
        }
    }

    override fun onTabSelected(tab: ActionBar.Tab, ft: FragmentTransaction) {
        val tag = tab.tag
        for (i in mTabs.indices) {
            if (mTabs[i] == tag) {
                mViewPager!!.setCurrentItem(i, true)
                currentTabNum = i
            }
        }
        mActivity.invalidateOptionsMenu()
    }

    override fun onTabUnselected(tab: ActionBar.Tab, ft: FragmentTransaction) {}
    override fun onTabReselected(tab: ActionBar.Tab, ft: FragmentTransaction) {}

    init {
        mActionBar = mActivity.supportActionBar
        mViewPager = pager
        mViewPager!!.adapter = this
        mViewPager.setOnPageChangeListener(this)
    }
}