package com.emtanveer.uberremake

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.emtanveer.uberremake.model.DriverInfoModel
import com.firebase.ui.auth.AuthMethodPickerLayout
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.coroutines.*
import java.util.*
import java.util.concurrent.TimeUnit

@SuppressLint("CustomSplashScreen")
class SplashScreenActivity : AppCompatActivity() {

    private lateinit var providers: List<AuthUI.IdpConfig>
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var listener: FirebaseAuth.AuthStateListener

    //for Firebase Registration Purpose
    private lateinit var database: FirebaseDatabase
    private lateinit var driverInfoRef: DatabaseReference

    override fun onStart() {
        super.onStart()

        delaySplashScreen()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        init()

    }

    private fun init() {

        database = FirebaseDatabase.getInstance()
        driverInfoRef = database.getReference(Common.DRIVER_INFO_REFERENCE)

        providers = mutableListOf(
            AuthUI.IdpConfig.PhoneBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build()
        )

        firebaseAuth = FirebaseAuth.getInstance()
        listener = FirebaseAuth.AuthStateListener { myFirebaseAuth ->
            val user = myFirebaseAuth.currentUser

            if (user != null) {
                checkUserFromFirebase()
            } else {
                showLoginLayout()
            }
        }

    }

    private fun checkUserFromFirebase() {
        driverInfoRef
            .child(FirebaseAuth.getInstance().currentUser!!.uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                      //  Toast.makeText(this@SplashScreenActivity, "User Already Registered!",).show()
                        val model = dataSnapshot.getValue(DriverInfoModel::class.java)
                        gotoHomeActivity(model)
                    } else {
                        showRegisterLayout()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(
                        this@SplashScreenActivity,
                        error.message,
                        Toast.LENGTH_SHORT
                    ).show()
                }

            })
    }

    private fun gotoHomeActivity(model: DriverInfoModel?) {
        Common.currentUser = model
        startActivity(Intent(this@SplashScreenActivity, DriverHomeActivity::class.java))
        finish()
    }

    private fun showRegisterLayout() {
        val builder = AlertDialog.Builder(this@SplashScreenActivity, R.style.DialogTheme)
        val itemView = LayoutInflater.from(this).inflate(R.layout.activity_layout_register, null)

        val edtFirstName = itemView.findViewById<View>(R.id.editFirstName) as TextInputEditText
        val edtLastName = itemView.findViewById<View>(R.id.editLastName) as TextInputEditText
        val edtPhoneNumber = itemView.findViewById<View>(R.id.editPhoneNumber) as TextInputEditText

        val btnContinueRegister = itemView.findViewById<View>(R.id.btnRegister)

        val progressBar = itemView.findViewById<View>(R.id.progress_bar)

        //Set Data
        val userPhoneNumberFromFirebaseDatabase: String? =
            FirebaseAuth.getInstance().currentUser!!.phoneNumber

        if (userPhoneNumberFromFirebaseDatabase != null && !TextUtils.isDigitsOnly(
                userPhoneNumberFromFirebaseDatabase
            )
        ) {
            edtPhoneNumber.setText(userPhoneNumberFromFirebaseDatabase)
        }

        //View
        builder.setView(itemView)
        val dialog = builder.create()

        //Event
        btnContinueRegister.setOnClickListener {
            when {
                TextUtils.isDigitsOnly(edtFirstName.text.toString()) -> {
                    Toast.makeText(
                        this@SplashScreenActivity,
                        "Please Enter First Name",
                        Toast.LENGTH_SHORT
                    ).show()

                    return@setOnClickListener
                }
                TextUtils.isDigitsOnly(edtLastName.text.toString()) -> {
                    Toast.makeText(
                        this@SplashScreenActivity,
                        "Please Enter Last Name",
                        Toast.LENGTH_SHORT
                    ).show()

                    return@setOnClickListener
                }
                TextUtils.isDigitsOnly(edtPhoneNumber.text.toString()) -> {
                    Toast.makeText(
                        this@SplashScreenActivity,
                        "Please Enter Phone Number",
                        Toast.LENGTH_SHORT
                    ).show()

                    return@setOnClickListener
                }
                else -> {
                    val model = DriverInfoModel()
                    model.firstName = edtFirstName.text.toString()
                    model.lastName = edtLastName.text.toString()
                    model.phoneNumber = edtPhoneNumber.text.toString()

                    model.rating = 0.0

                    driverInfoRef.child(FirebaseAuth.getInstance().currentUser!!.uid)
                        .setValue(model)
                        .addOnFailureListener { exception ->
                            Toast.makeText(
                                this@SplashScreenActivity,
                                exception.message.toString(),
                                Toast.LENGTH_SHORT
                            ).show()
                            dialog.dismiss()

                            progressBar.visibility = View.GONE
                        }
                        .addOnSuccessListener {
                            Toast.makeText(this@SplashScreenActivity, "Registered Successfully", Toast.LENGTH_SHORT).show()
                            dialog.dismiss()

                            gotoHomeActivity(model)

                            progressBar.visibility = View.GONE
                        }
                }
            }
        }

        dialog.show()
    }

    private fun showLoginLayout() {
        val authMethodPickerLayout =
            AuthMethodPickerLayout.Builder(R.layout.activity_layout_sign_in)
                .setPhoneButtonId(R.id.btnPhoneSignIn)
                .setGoogleButtonId(R.id.btnGoogleSignIn)
                .build()

        /*        startActivityForResult(
            AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAuthMethodPickerLayout(authMethodPickerLayout)
                .setTheme(R.style.LoginTheme)
                .setAvailableProviders(providers)
                .setIsSmartLockEnabled(false)
                .build()
            ,
            LOGIN_REQUEST_CODE)
            */

        //New way to startActivityForResult
        startYourActivityForResult(authMethodPickerLayout)
    }

    private fun startYourActivityForResult(authMethodPickerLayout: AuthMethodPickerLayout) {
        /* Instead of using:
         val intent =  Intent(this, SomeActivity::class.java)
         we will use the intent for firebase accordingly.
         */
        val intent = AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setAuthMethodPickerLayout(authMethodPickerLayout)
            .setTheme(R.style.LoginTheme)
            .setAvailableProviders(providers)
            .setIsSmartLockEnabled(false)
            .build()
        launchSomeActivity.launch(intent)
    }

    //Get callback here just like (ForActivityResult method)
    var launchSomeActivity =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val data: Intent? = result.data

            // your operation...
            if (data != null) {
                val response = IdpResponse.fromResultIntent(data)
                if (result.resultCode == Activity.RESULT_OK) {
                    val user = FirebaseAuth.getInstance().currentUser
                } else {
                    Toast.makeText(
                        this@SplashScreenActivity,
                        "${response?.error?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

    private fun delaySplashScreen() {
        CoroutineScope(Dispatchers.IO).launch {
            delay(TimeUnit.SECONDS.toMillis(3))

            //this will be called after 3 seconds on MainThread
            withContext(Dispatchers.Main) {
                firebaseAuth.addAuthStateListener(listener)
            }
        }
    }

    override fun onStop() {
        if (firebaseAuth != null && listener != null) firebaseAuth.removeAuthStateListener(listener)
        super.onStop()
    }
}