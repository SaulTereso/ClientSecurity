package com.security.clientsecurity.activities

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.bumptech.glide.Glide
import com.github.dhaval2404.imagepicker.ImagePicker
import com.security.clientsecurity.databinding.ActivityProfileBinding
import com.security.clientsecurity.models.Client
import com.security.clientsecurity.providers.AuthProvider
import com.security.clientsecurity.providers.ClientProvider
import java.io.File

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding:ActivityProfileBinding
    val clientProvider = ClientProvider()
    val authProvider = AuthProvider()

    private var imageFile: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        getClient()
        binding.imageViewBack.setOnClickListener{ finish() }
        binding.btnUpdate.setOnClickListener{ updateInfo() }
        binding.circleImageProfile.setOnClickListener { selectImage() }
    }

    private fun updateInfo() {

        val name = binding.textFieldName.text.toString()
        val lastname = binding.textFieldLastname.text.toString()
        val phone = binding.textFieldPhone.text.toString()

        val client = Client(
            id = authProvider.getId(),
            name = name,
            app = lastname,
            phone = phone,
        )

        if (imageFile != null) {
            clientProvider.uploadImage(authProvider.getId(), imageFile!!).addOnSuccessListener { taskSnapshot ->
                clientProvider.getImageUrl().addOnSuccessListener { url ->
                    val imageUrl = url.toString()
                    client.image = imageUrl
                    clientProvider.update(client).addOnCompleteListener {
                        if (it.isSuccessful) {
                            Toast.makeText(this@ProfileActivity, "Datos actualizados correctamente", Toast.LENGTH_LONG).show()
                        }
                        else {
                            Toast.makeText(this@ProfileActivity, "No se pudo actualizar la informacion", Toast.LENGTH_LONG).show()
                        }
                    }
                    Log.d("STORAGE", "$imageUrl")
                }
            }
        }
        else{
            clientProvider.update(client).addOnCompleteListener {
                if (it.isSuccessful){
                    Toast.makeText(this@ProfileActivity, "Datos actulizados correctamente", Toast.LENGTH_SHORT).show()
                }
                else{
                    Toast.makeText(this@ProfileActivity, "No se pudo actulizar la informacion", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun getClient(){
        clientProvider.getClient(authProvider.getId()).addOnSuccessListener { document ->
            if (document.exists()){
                val client = document.toObject(Client::class.java)
                binding.textViewEmail.text = client?.email
                binding.textFieldName.setText(client?.name)
                binding.textFieldLastname.setText(client?.app)
                binding.textFieldPhone.setText(client?.phone)

                if (client?.image != null) {
                    if (client.image != "") {
                        Glide.with(this).load(client.image).into(binding.circleImageProfile)
                    }
                }

            }
        }
    }

    private val startImageForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->

        val resultCode = result.resultCode
        val data = result.data

        if (resultCode == Activity.RESULT_OK) {
            val fileUri = data?.data
            imageFile = File(fileUri?.path)
            binding.circleImageProfile.setImageURI(fileUri)
        }
        else if (resultCode == ImagePicker.RESULT_ERROR) {
            Toast.makeText(this, ImagePicker.getError(data), Toast.LENGTH_LONG).show()
        }
        else {
            Toast.makeText(this, "Tarea cancelada", Toast.LENGTH_LONG).show()
        }

    }

    private fun selectImage() {
        ImagePicker.with(this)
            .crop()
            .compress(1024)
            .maxResultSize(1080,1080)
            .createIntent { intent ->
                startImageForResult.launch(intent)
            }
    }


}