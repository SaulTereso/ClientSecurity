package com.security.clientsecurity.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.security.clientsecurity.R
import com.security.clientsecurity.activities.*
import com.security.clientsecurity.models.Client
import com.security.clientsecurity.providers.*

class ModalBottomSheetMenu: BottomSheetDialogFragment() {

    val clientProvider = ClientProvider()
    val authProvider = AuthProvider()

    var textViewUsername: TextView? = null
    var linearLayoutLogout: LinearLayout? = null
    var linearLayoutProfile: LinearLayout? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.modal_bottom_sheet_menu, container, false)
        textViewUsername = view.findViewById(R.id.textViewUsername)
        linearLayoutLogout = view.findViewById(R.id.linearLayoutLogout)
        linearLayoutProfile = view.findViewById(R.id.linearLayoutProfile)

        getUser()

        linearLayoutLogout?.setOnClickListener { goTomain() }
        linearLayoutProfile?.setOnClickListener { goToProfile() }
        return view
    }
    private fun goToProfile(){
        val i = Intent(activity, ProfileActivity::class.java)
        startActivity(i)
    }

    private fun goTomain(){
        authProvider.logout()
        val i = Intent(activity, MainActivity::class.java)
        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(i)
    }

     private fun getUser(){
         clientProvider.getClient(authProvider.getId()).addOnSuccessListener { document ->
             if (document.exists()){
                 val client = document.toObject(Client::class.java)
                 textViewUsername?.text = "${client?.name} ${client?.app}"
             }
         }
     }

    companion object {
        const val TAG = "ModalBottomSheetMenu"
    }


}