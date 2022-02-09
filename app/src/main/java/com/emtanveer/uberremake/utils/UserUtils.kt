package com.emtanveer.uberremake.utils

import android.content.Context
import android.view.View
import android.widget.Toast
import com.emtanveer.uberremake.Common
import com.emtanveer.uberremake.model.TokenModel
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

object UserUtils {
    fun updateUser(
        view: View?,
        updateData: Map<String, Any>
    ){
        FirebaseDatabase.getInstance()
            .getReference(Common.DRIVER_INFO_REFERENCE)
            .child(FirebaseAuth.getInstance().currentUser!!.uid)
            .updateChildren(updateData)
            .addOnFailureListener { error ->
                Snackbar.make(view!!, error.message.toString(), Snackbar.LENGTH_LONG).show()
            }.addOnSuccessListener {
                Snackbar.make(view!!, "Update information success", Snackbar.LENGTH_LONG).show()
            }
    }

    fun updateToken(context: Context, token: String) {
        val tokenModel = TokenModel().apply {
            this.token = token
        }
        FirebaseDatabase.getInstance()
            .getReference(Common.TOKEN_REFERENCE)
            .child(FirebaseAuth.getInstance().currentUser!!.uid)
            .setValue(token)
            .addOnFailureListener{ error ->
                Toast.makeText(context, error.message.toString(), Toast.LENGTH_SHORT).show()
            }.addOnSuccessListener {   }
    }
}