package com.security.clientsecurity.providers

import android.net.Uri
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.security.clientsecurity.models.Client
import java.io.File

class ClientProvider {

    val db = Firebase.firestore.collection("Clients")
    var storage = FirebaseStorage.getInstance().getReference().child("profile")

    fun create(client: Client): Task<Void>{
        return db.document(client.id!!).set(client)
    }

    fun uploadImage(id: String, file: File): StorageTask<UploadTask.TaskSnapshot> {
        var fromFile = Uri.fromFile(file)
        val ref = storage.child("$id.jpg")
        storage = ref
        val uploadTask = ref.putFile(fromFile)

        return uploadTask.addOnFailureListener {
            Log.d("STORAGE", "ERROR: ${it.message}")
        }
    }

    fun getImageUrl(): Task<Uri> {
        return storage.downloadUrl
    }

    fun getClient(idUser: String): Task<DocumentSnapshot>{
        return db.document(idUser).get()
    }

    fun update(client: Client): Task<Void>{
        val map: MutableMap<String, Any> = HashMap()
        map["name"] = client?.name!!
        map["app"] = client?.app!!
        map["phone"] = client?.phone!!
        map["image"] = client?.image!!
        return db.document(client?.id!!).update(map)
    }
}