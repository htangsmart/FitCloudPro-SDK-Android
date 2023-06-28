package com.topstep.fitcloud.sample2.ui.device.game.push

import android.os.Bundle
import androidx.navigation.fragment.NavHostFragment
import com.topstep.fitcloud.sample2.R

class GamePushFragment : NavHostFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        navController.setGraph(R.navigation.game_push_nav_graph)
    }

}