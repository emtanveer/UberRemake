package com.emtanveer.uberremake

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.emtanveer.uberremake.databinding.ActivityDriverHomeBinding
import com.google.firebase.auth.FirebaseAuth
import java.lang.StringBuilder

class DriverHomeActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityDriverHomeBinding
    private var navView: NavigationView? = null
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
       binding = ActivityDriverHomeBinding.inflate(layoutInflater)
       setContentView(binding.root)



        setSupportActionBar(binding.appBarDriverHome.toolbar)


        val drawerLayout: DrawerLayout = binding.drawerLayout
        navView = binding.navView
        navController = findNavController(R.id.nav_host_fragment_content_driver_home)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(R.id.nav_home, R.id.nav_sign_out), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView?.setupWithNavController(navController)

        initViews()

    }

    private fun initViews(){

        //Navigation Drawer Item Click Listener
        navView?.setNavigationItemSelectedListener { menuItem ->
            when(menuItem.itemId){
                R.id.nav_sign_out -> {
                    val builder = AlertDialog.Builder(this@DriverHomeActivity)
                    builder.setTitle("Sign out")
                        .setMessage("Do you really want to Sign out?")
                        .setNegativeButton("CANCEL") { dialogInterface, _ -> dialogInterface.dismiss() }
                        .setPositiveButton("SIGN OUT") { dialogInterface, _ ->
                            FirebaseAuth.getInstance().signOut()
                            val intent = Intent(this@DriverHomeActivity, SplashScreenActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                            finish()
                        }.setCancelable(false)

                    val dialog = builder.create()

                    dialog.setOnShowListener {
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                            .setTextColor(ContextCompat.getColor(this@DriverHomeActivity, R.color.holo_red_dark))
                        dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                            .setTextColor(ContextCompat.getColor(this@DriverHomeActivity, R.color.colorAccent))
                    }
                    dialog.show()
                }

            }
            true
        }

        val headerView = navView?.getHeaderView(0)
        val userName = headerView?.findViewById<View>(R.id.txt_name) as TextView
        val userPhone = headerView.findViewById<View>(R.id.txt_phone) as TextView
        val userStar = headerView.findViewById<View>(R.id.txt_star) as TextView

        userName.text = Common.buildWelcomeMessage()
        userPhone.text = Common.currentUser?.phoneNumber
        userStar.text = StringBuilder().append(Common.currentUser?.rating)



    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.driver_home, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_driver_home)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}