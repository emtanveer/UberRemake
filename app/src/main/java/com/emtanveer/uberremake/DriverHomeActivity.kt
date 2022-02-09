package com.emtanveer.uberremake

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.view.Menu
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.bumptech.glide.Glide
import com.emtanveer.uberremake.databinding.ActivityDriverHomeBinding
import com.emtanveer.uberremake.utils.UserUtils
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class DriverHomeActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityDriverHomeBinding
    private var navView: NavigationView? = null
    private lateinit var navController: NavController
    private lateinit var imgAvatar: ImageView
    private lateinit var waitingDialog: AlertDialog
    private lateinit var imageUriAvatar: Uri
    private lateinit var storageReference: StorageReference


    companion object {
        val PICK_IMAGE_REQUEST = 7272
    }

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

        //
        storageReference = FirebaseStorage.getInstance().reference

        //
        waitingDialog = AlertDialog.Builder(this)
            .setMessage("Waiting...")
            .setCancelable(false).create()

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
        imgAvatar = headerView.findViewById<View>(R.id.img_avatar) as ImageView

        userName.text = Common.buildWelcomeMessage()
        userPhone.text = Common.currentUser?.phoneNumber
        userStar.text = StringBuilder().append(Common.currentUser?.rating)

        if (Common.currentUser != null && Common.currentUser?.avatar != null && TextUtils.isEmpty(Common.currentUser?.avatar)) {
            Glide.with(this)
                .load(Common.currentUser?.avatar)
                .into(imgAvatar)
        }

        imgAvatar.setOnClickListener {
            val intent = Intent().also {
                it.type = "image/*"
                it.action = Intent.ACTION_GET_CONTENT
            }
            startActivityForResult(Intent.createChooser(intent, "Select Picture"),PICK_IMAGE_REQUEST)
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK){
            if(data!=null && data.data !=null){
                imageUriAvatar = data.data!!
                imgAvatar.setImageURI(imageUriAvatar)

                showDialogForAvatarUpload()
            }
        }
    }

    private fun showDialogForAvatarUpload() {
        val builder = AlertDialog.Builder(this@DriverHomeActivity)

        builder.setTitle("Change Avatar")
            .setMessage("Do you really want to change Avatar?")
            .setNegativeButton("CANCEL") { dialogInterface, _ -> dialogInterface.dismiss() }
            .setPositiveButton("CHANGE") { dialogInterface, _ ->
              if(imageUriAvatar != null) {
                  waitingDialog.show()

                  val avatarFolder = storageReference.child("avatars/"+FirebaseAuth.getInstance().currentUser?.uid)
                  avatarFolder.putFile(imageUriAvatar)
                      .addOnFailureListener{ error ->
                          Snackbar.make(binding.drawerLayout, error.message.toString(), Snackbar.LENGTH_LONG).show()
                          waitingDialog.dismiss()
                      }.addOnCompleteListener{ task ->
                          if(task.isSuccessful){
                              avatarFolder.downloadUrl.addOnSuccessListener { uri ->
                                  val updateData = HashMap<String, Any>()
                                  updateData.put("avatar", uri.toString())
                                  UserUtils.updateUser(binding.drawerLayout, updateData)
                              }
                          }
                          waitingDialog.dismiss()

                      }.addOnProgressListener { taskSnapShot ->
                          val progress = (100.0*taskSnapShot.bytesTransferred/taskSnapShot.totalByteCount)
                          waitingDialog.setMessage(StringBuilder("Uploading: ").append(progress).append("%"))
                      }
              }
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