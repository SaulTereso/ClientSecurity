package com.security.clientsecurity.activities

import android.Manifest
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.location.Geocoder
import android.location.Location
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.easywaylocation.EasyWayLocation
import com.example.easywaylocation.Listener
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.firebase.firestore.GeoPoint
import com.google.maps.android.SphericalUtil
import com.security.clientsecurity.R
import com.security.clientsecurity.databinding.ActivityMapBinding
import com.security.clientsecurity.fragments.ModalBottomSheetMenu
import com.security.clientsecurity.models.UserLocation
import com.security.clientsecurity.providers.AuthProvider
import com.security.clientsecurity.providers.GeoProvider
import com.security.clientsecurity.utils.CarMoveAnim
import org.imperiumlabs.geofirestore.callbacks.GeoQueryEventListener

class MapActivity : AppCompatActivity(), OnMapReadyCallback, Listener {
    private lateinit var binding: ActivityMapBinding
    private var googleMap: GoogleMap? = null
    private var easyWayLocation: EasyWayLocation? = null
    private var myLocationLating: LatLng? = null
    private var geoProvider = GeoProvider()
    //private var authProvider = AuthProvider()

    //Variable Google Places
    /*private var places: PlacesClient? = null
    private var autocompleteOrigin: AutocompleteSupportFragment? = null
    private var autocompleteDestination: AutocompleteSupportFragment? = null*/

    //Variables que almacena el nombre de la direccion
    /*private var originName: String? = ""
    private var originLating: LatLng? = null*/

    //private var destinationName: String? = ""
    //private var destinationLating: LatLng? = null

    private var isLocationEnabled = false
    private val userMarkers = ArrayList<Marker>()
    private val userLocation = ArrayList<UserLocation>()

    private var modalMeu = ModalBottomSheetMenu()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val locationRequest = LocationRequest.create().apply{
            interval = 0
            fastestInterval = 0
            priority = Priority.PRIORITY_HIGH_ACCURACY
            smallestDisplacement = 1f
        }

        easyWayLocation = EasyWayLocation(this, locationRequest, false, false, this)
        LocationPermissions.launch(arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        ))
        //startGooglePlaces()
        binding.imageViewMenu.setOnClickListener{showModalMenu()}
    }


    var LocationPermissions = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){ permission ->
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            when{
                permission.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) ->{
                    Log.d("Localizacion", "Permiso concedido")
                    easyWayLocation?.startLocation();
                }
                permission.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) ->{
                    Log.d("Localizacion", "Permiso concedido con limitacion")
                    easyWayLocation?.startLocation();
                }
                else -> {
                    Log.d("Localizacion", "Permiso denegado")
                }
            }
        }
    }

    private fun getNearbyUser(){
        if (myLocationLating == null) return
        geoProvider.getNeartyUser(myLocationLating!!, 1950.0).addGeoQueryEventListener(object: GeoQueryEventListener {
            override fun onKeyEntered(documentID: String, location: GeoPoint) {

                Log.d("FIRESTORE", "Document id: $documentID")
                Log.d("FIRESTORE", "Location: $location")

                for (marker in userMarkers){
                    if (marker.tag != null){
                        if (marker.tag == documentID){
                            return
                        }
                    }
                }
                //Nuevo marcador para el usuario conectado
                val userLatLng = LatLng(location.latitude, location.longitude)
                val marker = googleMap?.addMarker(
                    MarkerOptions().position(userLatLng).title( "Persona encontrada").icon(
                        BitmapDescriptorFactory.fromResource(R.drawable.new_ubicacion_usuario)
                    )
                )
                marker?.tag = documentID
                userMarkers.add(marker!!)

                var Location = UserLocation()
                Location.id = documentID
                userLocation.add(Location)
            }

            override fun onKeyExited(documentID: String) {
                for (marker in userMarkers){
                    if (marker != null){
                        if (marker.tag == documentID){
                            marker.remove()
                            userMarkers.remove(marker)
                            userLocation.removeAt(getPositionUser(documentID))
                            return
                        }
                    }
                }
            }

            override fun onKeyMoved(documentID: String, location: GeoPoint) {
                for (marker in userMarkers){

                    val start = LatLng(location.latitude, location.longitude)
                    var end: LatLng? = null
                    val position = getPositionUser(marker.tag.toString())

                    if (marker.tag != null){
                        if (marker.tag == documentID){
                            //marker.position = LatLng(location.latitude,location.longitude)

                            if (userLocation[position].latLng != null){
                                end = userLocation[position].latLng
                            }
                            userLocation[position].latLng = LatLng(location.latitude, location.longitude)
                            if (end != null){
                                CarMoveAnim.carAnim(marker, end, start)
                            }

                        }
                    }
                }
            }

            override fun onGeoQueryError(exception: Exception) {

            }

            override fun onGeoQueryReady() {

            }

        })
    }

    private fun showModalMenu(){
        modalMeu.show(supportFragmentManager, ModalBottomSheetMenu.TAG)
    }

    private fun getPositionUser(id: String): Int{
        var position = 0
        for (i in userLocation.indices){
            if (id == userLocation[i].id){
                position = i
                break
            }
        }
        return position
    }

    /*private fun onCameraMove(){
        googleMap?.setOnCameraIdleListener {
            try{
                val geocoder = Geocoder( this)
                originLating = googleMap?.cameraPosition?.target

                if (originLating != null){
                    val addressList = geocoder.getFromLocation(originLating?.latitude!!, originLating?.longitude!!, 1)

                    if(addressList.size > 0){
                        val city = addressList[0].locality
                        val country = addressList[0].countryName
                        val address = addressList[0].getAddressLine(0)
                        originName = "$address $city"
                        autocompleteOrigin?.setText("$address $city")
                    }
                }

            } catch (e: Exception){
                Log.d("ERROR", "Mensaje error: ${e.message}")
            }
        }
    }

    private fun startGooglePlaces(){
        if (!Places.isInitialized()){
            Places.initialize(applicationContext, resources.getString(R.string.google_maps_key))
        }
        places = Places.createClient(this)
        //instanceAutoCompleteOrigin()
        //instanceAutoCompleteDestination()
    }*/

    /*private fun limitSearch(){
        val northSide = SphericalUtil.computeOffset(myLocationLating, 5000.0, 0.0)
        val southSide = SphericalUtil.computeOffset(myLocationLating, 5000.0, 180.0)

        autocompleteOrigin?.setLocationBias(RectangularBounds.newInstance(southSide,northSide))
        autocompleteDestination?.setLocationBias(RectangularBounds.newInstance(southSide,northSide))
    }

    private fun instanceAutoCompleteDestination(){
        autocompleteDestination = supportFragmentManager.findFragmentById(R.id.placesAutocompleteDestination) as AutocompleteSupportFragment
        autocompleteDestination?.setPlaceFields(
            listOf(
                Place.Field.ID,
                Place.Field.NAME,
                Place.Field.LAT_LNG,
                Place.Field.ADDRESS,
            )
        )
        autocompleteDestination?.setCountry("MX")
        autocompleteDestination?.setHint("Punto de reunion contacto")
        autocompleteDestination?.setOnPlaceSelectedListener(object: PlaceSelectionListener{
            override fun onPlaceSelected(place: Place) {
                destinationName = place.name!!
                destinationLating = place.latLng
                Log.d("PLACES", "Address: $destinationName")
                Log.d("PLACES", "Lat: ${destinationLating?.latitude}")
                Log.d("PLACES", "Lin: ${destinationLating?.longitude}")
            }

            override fun onError(p0: Status) {
                TODO("Not yet implemented")
            }
        })
    }

    private fun instanceAutoCompleteOrigin(){
        autocompleteOrigin = supportFragmentManager.findFragmentById(R.id.placesAutocompleteOrigin) as AutocompleteSupportFragment
        autocompleteOrigin?.setPlaceFields(
            listOf(
                Place.Field.ID,
                Place.Field.NAME,
                Place.Field.LAT_LNG,
                Place.Field.ADDRESS,
            )
        )
        autocompleteOrigin?.setHint("Ubicación actual")
        autocompleteOrigin?.setCountry("MX")
        autocompleteOrigin?.setOnPlaceSelectedListener(object: PlaceSelectionListener{
            override fun onPlaceSelected(place: Place) {
                originName = place.name!!
                originLating = place.latLng
                Log.d("PLACES", "Address: $originName")
                Log.d("PLACES", "Lat: ${originLating?.latitude}")
                Log.d("PLACES", "Lin: ${originLating?.longitude}")
            }

            override fun onError(p0: Status) {
                TODO("Not yet implemented")
            }
        })
    }*/

    //Se ejecuta cada que se abre la pantalla actual
    override fun onResume() {
        super.onResume()
        //easyWayLocation?.startLocation()
    }

    //Se ejecuta cuando se cierra la app o pasa a otra activity
    override fun onDestroy() {
        super.onDestroy()
        easyWayLocation?.endUpdates()
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap?.uiSettings?.isZoomControlsEnabled = true
        //onCameraMove()
        //easyWayLocation?.startLocation();

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            return
        }
        googleMap?.isMyLocationEnabled = true

        try {
            val sucess = googleMap?.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(this, R.raw.style)
            )
            if (!sucess!!){
                Log.d("Mapas", "No se encontro el estilo")
            }
        }catch (e: Resources.NotFoundException){
            Log.d("Mapas", "Error: ${e.toString()}")
        }
    }

    override fun locationOn() {

    }

    //Actuluzacion de la posicion en tiempo real
    override fun currentLocation(location: Location) {
        //Latitud y Longitud de la posicion actual
        myLocationLating = LatLng(location.latitude, location.longitude)

        if (!isLocationEnabled){
            isLocationEnabled = true
            googleMap?.moveCamera(CameraUpdateFactory.newCameraPosition(
                CameraPosition.builder().target(myLocationLating!!).zoom(15f).build()
            ))
            getNearbyUser()
            //limitSearch()
        }
    }

    override fun locationCancelled() {

    }


}