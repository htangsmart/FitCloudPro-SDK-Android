package com.topstep.fitcloud.sample2.ui.auth

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.navigation.NavHostController
import androidx.navigation.ui.NavigationUI
import com.topstep.fitcloud.sample2.R
import com.topstep.fitcloud.sample2.ui.base.BaseActivity
import com.topstep.fitcloud.sample2.utils.findNavControllerInNavHost

class AuthActivity : BaseActivity() {

    private lateinit var navController: NavHostController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)
        navController = findNavControllerInNavHost(R.id.nav_host)
        NavigationUI.setupActionBarWithNavController(this, navController)
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp()
    }

    companion object {
        fun start(context: Context) {
            context.startActivity(Intent(context, AuthActivity::class.java))
        }
    }

}