package com.example.myapplication

//import com.google.android.gms.location.LocationListener
//import android.location.LocationListener
import android.Manifest
import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.myapplication.databinding.ActivityMapsBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import java.text.SimpleDateFormat
import java.util.*


class User {
    var firstName: String = ""
    var lastName: String = ""
    var birthDate: String = ""
}

class App : AppCompatActivity(), OnMapReadyCallback {

    private val userInfo: User = User()

    private lateinit var map: GoogleMap
    private lateinit var binding: ActivityMapsBinding

    private lateinit var locationManager: LocationManager
    private lateinit var locationListener: LocationListener
    private val locationPermissionCode = 1234
    private var userLocation: LatLng? = null


    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }


    override fun onPause() {
        super.onPause()

        stopLocationUpdates()
    }


    override fun onResume() {
        super.onResume()

        checkLocationPermission()
    }


    @SuppressLint("MissingPermission")
    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {

            startLocationUpdates()

        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                locationPermissionCode
            )
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == locationPermissionCode) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates()
            }
        }
    }


    @SuppressLint("MissingPermission")
    private fun initializeLocationVars() {
        locationManager = this.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        locationListener = LocationListener { location ->
            val tmpLocation = LatLng(location.latitude, location.longitude)
            if (tmpLocation == userLocation) {
            } else {
                userLocation = tmpLocation
                map.isMyLocationEnabled = true
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(userLocation!!, 15f))
            }
        }
    }


    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        locationManager.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            0,
            0f,
            locationListener
        )
    }


    @SuppressLint("MissingPermission")
    private fun stopLocationUpdates() {
        locationManager.removeUpdates(locationListener)
    }


    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap


        map.setOnMyLocationClickListener {
            showUserForm()
        }

        initializeLocationVars()
        checkLocationPermission()
    }


    private fun showUserForm() {
        val userFormDialog: AlertDialog = AlertDialog.Builder(this)
            .setView(R.layout.marker_form)
            .create()

        userFormDialog.show()

        val firstNameField = userFormDialog.findViewById<EditText>(R.id.firstName)
        val lastNameField = userFormDialog.findViewById<EditText>(R.id.lastName)
        val birthDateField = userFormDialog.findViewById<EditText>(R.id.birthDate)


        if (userInfo.firstName.isNotEmpty()) firstNameField!!.setText(userInfo.firstName)
        if (userInfo.lastName.isNotEmpty()) lastNameField!!.setText(userInfo.lastName)
        if (userInfo.birthDate.isNotEmpty()) birthDateField!!.setText(userInfo.birthDate)


        val calendar = Calendar.getInstance()

        val datePicker = DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->

            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

            updateBirthDate(calendar, birthDateField)
        }


        userFormDialog.setOnCancelListener {

            if (firstNameField!!.text.isNotEmpty()) userInfo.firstName =
                firstNameField.text.toString()

            if (lastNameField!!.text.isNotEmpty()) userInfo.lastName =
                lastNameField.text.toString()

            if (birthDateField!!.text.isNotEmpty()) userInfo.birthDate =
                birthDateField.text.toString()

        }


        birthDateField!!.setOnClickListener {
            DatePickerDialog(
                this, datePicker, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }


    private fun updateBirthDate(calendar: Calendar, birthDateField: EditText?) {
        val dateFormat = "dd. MMM yyyy"
        val sdf = SimpleDateFormat(dateFormat, Locale.getDefault())

        birthDateField!!.setText(sdf.format(calendar.time))
    }

}