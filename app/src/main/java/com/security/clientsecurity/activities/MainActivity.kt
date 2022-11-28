package com.security.clientsecurity.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import com.security.clientsecurity.databinding.ActivityMainBinding
import com.security.clientsecurity.providers.AuthProvider

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    val authProvider = AuthProvider()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

        binding.btnRegister.setOnClickListener { goToRegister() }
        binding.btnLogin.setOnClickListener { login() }
    }

    private fun login(){
        val email = binding.textFieldEmail.text.toString()
        val password = binding.textFieldPassword.text.toString()

        if (isValidForm(email, password)){
            authProvider.login(email, password).addOnCompleteListener {
                if (it.isSuccessful){
                    gotoMap()
                }else{
                    Toast.makeText(this@MainActivity, "Error iniciando sesion", Toast.LENGTH_SHORT).show()
                    Log.d("FIREBASE", "Error: ${it.exception.toString()}")
                }
            }
        }

    }

    private fun gotoMap(){
        val i = Intent(this, MapActivity::class.java)
        i.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(i)
    }

    private  fun isValidForm(email: String, password: String): Boolean{
        if (email.isEmpty()){
            Toast.makeText(this, "Ingresar correo electronico", Toast.LENGTH_SHORT).show()
            return false
        }

        if (password.isEmpty()){
            Toast.makeText(this, "Ingresar una contraseña", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun goToRegister(){
        val i = Intent(this, RegisterActivity::class.java)
        startActivity(i)
    }

    override fun onStart() {
        super.onStart()

        if (authProvider.existSession()){
            gotoMap()
        }
    }
}