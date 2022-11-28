package com.security.clientsecurity.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import com.security.clientsecurity.databinding.ActivityRegisterBinding
import com.security.clientsecurity.models.Client
import com.security.clientsecurity.providers.AuthProvider
import com.security.clientsecurity.providers.ClientProvider

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private val authProvider = AuthProvider()
    private val clientProvider = ClientProvider()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRegisterBinding.inflate(layoutInflater)

        setContentView(binding.root)
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

        binding.btnGoToLogin.setOnClickListener { goToLogin() }
        binding.btnRegister.setOnClickListener { register() }
    }

    private fun register(){
        val name = binding.textFieldName.text.toString()
        val app = binding.textFieldApp.text.toString()
        val phone = binding.textFieldPhone.text.toString()
        val email = binding.textFieldEmail.text.toString()
        val password = binding.textFieldPassword.text.toString()
        val confirmpassword = binding.textFieldConfirmPassword.text.toString()

        if (isValidateForm(name, app, phone, email, password, confirmpassword)){
            authProvider.register(email, password).addOnCompleteListener {
                if (it.isSuccessful){
                    val client = Client(
                        id = authProvider.getId(),
                        name = name,
                        app = app,
                        phone = phone,
                        email = email
                    )
                    clientProvider.create(client).addOnCompleteListener {
                        if (it.isSuccessful){
                            Toast.makeText(this@RegisterActivity, "Registro exitoso", Toast.LENGTH_SHORT).show()
                            gotoMap()
                        }else{
                            Toast.makeText(this@RegisterActivity, "Opps... habido un error en guardar datos ${it.exception.toString()}", Toast.LENGTH_SHORT).show()
                            Log.d("FIREBASE", "Error: ${it.exception.toString()}")
                        }
                    }

                }else{
                    Toast.makeText(this@RegisterActivity, "Registro fallido ${it.exception.toString()}", Toast.LENGTH_LONG).show()
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

    private fun isValidateForm(name: String, app: String, phone: String, email: String, password: String, confirmpassword: String): Boolean{
        if (name.isEmpty()){
            Toast.makeText(this, "Ingresar nombre", Toast.LENGTH_SHORT).show()
            return false
        }

        if (app.isEmpty()){
            Toast.makeText(this, "Ingresar apellidos", Toast.LENGTH_SHORT).show()
            return false
        }

        if (phone.isEmpty()){
            Toast.makeText(this, "Ingresar numero de telefono", Toast.LENGTH_SHORT).show()
            return false
        }

        if (email.isEmpty()){
            Toast.makeText(this, "Ingresar correo electornico", Toast.LENGTH_SHORT).show()
            return false
        }

        if (password.isEmpty()){
            Toast.makeText(this, "Ingresar una contraseña", Toast.LENGTH_SHORT).show()
            return false
        }

        if (confirmpassword.isEmpty()){
            Toast.makeText(this, "Por favor confirmar contraseña", Toast.LENGTH_SHORT).show()
            return false
        }

        if (password != confirmpassword){
            Toast.makeText(this, "Las contraseñas deben coincidir", Toast.LENGTH_SHORT).show()
            return false
        }

        if (password.length < 6 ){
            Toast.makeText(this, "La contraseña deben tener al menos 6 caracteres", Toast.LENGTH_LONG).show()
            return false
        }
        return true
    }

    private fun goToLogin(){
        val i = Intent( this, MainActivity::class.java)
        startActivity(i)
    }
}