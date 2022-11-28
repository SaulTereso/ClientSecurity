package com.security.clientsecurity.providers

import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import org.imperiumlabs.geofirestore.GeoFirestore
import org.imperiumlabs.geofirestore.GeoQuery

class GeoProvider {
    val collection = FirebaseFirestore.getInstance().collection("Locations")
    val geoFirestore = GeoFirestore(collection)

    fun saveLocation(idUser: String, position: LatLng){
        geoFirestore.setLocation(idUser, GeoPoint(position.latitude, position.longitude))
    }

    fun getNeartyUser(position: LatLng, radius: Double): GeoQuery{
        val query = geoFirestore.queryAtLocation(GeoPoint(position.latitude, position.longitude), radius)
        query.removeAllListeners()
        return query
    }

    fun removeLocation(idUser: String){
        collection.document(idUser).delete()
    }

    fun getLocation(idUser: String): Task<DocumentSnapshot> {
        return collection.document(idUser).get().addOnFailureListener{ exception ->
            Log.d("FIREBASE", "Error: ${exception.toString()}")
        }
    }

}