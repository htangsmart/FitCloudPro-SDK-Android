package com.github.kilnn.wristband2.sample.dial.library

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.ViewPager.SimpleOnPageChangeListener
import com.github.kilnn.wristband2.sample.BaseActivity
import com.github.kilnn.wristband2.sample.R
import com.github.kilnn.wristband2.sample.databinding.ActivityDialLibraryBinding

open class DialLibraryActivity : BaseActivity() {

    companion object {
        private const val STATE_CHECK_ID = "check_id"
    }

    private val viewBind: ActivityDialLibraryBinding by lazy(LazyThreadSafetyMode.NONE) {
        ActivityDialLibraryBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBind.root)
        viewBind.viewPager.adapter = InnerTabAdapter(supportFragmentManager)
        viewBind.viewPager.addOnPageChangeListener(object : SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int) {
                if (position == 0) {
                    viewBind.rgAction.check(R.id.rb_remote_dial)
                } else {
                    viewBind.rgAction.check(R.id.rb_local_dial)
                }
            }
        })
        val checkId: Int = savedInstanceState?.getInt(STATE_CHECK_ID) ?: R.id.rb_remote_dial
        viewBind.rgAction.setOnCheckedChangeListener { _, checkedId ->
            val selectItem: Int = if (checkedId == R.id.rb_remote_dial) {
                0
            } else {
                1
            }
            if (viewBind.viewPager.currentItem != selectItem) {
                viewBind.viewPager.currentItem = selectItem
            }
        }
        viewBind.rgAction.check(checkId)
        viewBind.toolBar.title = ""
        setSupportActionBar(viewBind.toolBar)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        /*FIX BUG:因为RadioButton的文字在语言改变后，还会缓存之前语言的文字，所以在布局里面属性设置为android:saveEnabled="false",所以需要额外保存选中ID的状态*/
        outState.putInt(STATE_CHECK_ID, viewBind.rgAction.checkedRadioButtonId)
    }

    private class InnerTabAdapter(fm: FragmentManager) : FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
        override fun getItem(position: Int): Fragment {
            //位置0 展示服务器列表
            //位置1 展示本地列表
            return DialLibraryListFragment.newInstance(position != 0)
        }

        override fun getCount(): Int {
            return 2
        }
    }

}