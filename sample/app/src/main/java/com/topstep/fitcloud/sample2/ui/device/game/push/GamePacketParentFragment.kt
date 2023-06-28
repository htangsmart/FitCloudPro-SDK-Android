package com.topstep.fitcloud.sample2.ui.device.game.push

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.topstep.fitcloud.sample2.R
import com.topstep.fitcloud.sample2.databinding.FragmentGamePacketParentBinding
import com.topstep.fitcloud.sample2.ui.base.BaseFragment
import com.topstep.fitcloud.sample2.utils.viewbinding.viewBinding

class GamePacketParentFragment : BaseFragment(R.layout.fragment_game_packet_parent) {

    private val viewBind: FragmentGamePacketParentBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (requireActivity() as AppCompatActivity?)?.supportActionBar?.setTitle(R.string.ds_game_push)

        viewBind.viewPager.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount(): Int {
                return 2
            }

            override fun createFragment(position: Int): Fragment {
                return GamePacketListFragment.newInstance(position != 0)
            }
        }
        TabLayoutMediator(
            viewBind.tabLayout, viewBind.viewPager, false
        ) { tab, position ->
            if (position == 0) {
                tab.text = getString(R.string.ds_game_center)
            } else {
                tab.text = getString(R.string.ds_game_mine)
            }
        }.attach()
    }
}