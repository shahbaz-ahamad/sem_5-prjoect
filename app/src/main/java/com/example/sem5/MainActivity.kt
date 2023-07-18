@file:Suppress("DEPRECATION")

package com.example.sem5

import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.sem5.databinding.ActivityMainBinding
import com.example.sem5.databinding.NavHeaderBinding
import com.example.sem5.datamodel.LocationDataClass
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity(), OnMapReadyCallback {


    private lateinit var binding: ActivityMainBinding
    private lateinit var auth:FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var toogle:ActionBarDrawerToggle
    private lateinit var navBinding:NavHeaderBinding
    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var currentLocation:Location
    private val LOCATION_PERMISSION_REQUEST_CODE = 123
    private lateinit var databaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityMainBinding.inflate(layoutInflater)
        //for the nvaigation header
        navBinding=NavHeaderBinding.bind(binding.navigationView.getHeaderView(0))
        setContentView(binding.root)

        toogle= ActionBarDrawerToggle(this,binding.drawerLayout,R.string.open,R.string.close)
        binding.drawerLayout.addDrawerListener(toogle)
        toogle.syncState()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)


        //for the map
        auth=Firebase.auth
        val currentUser=auth.currentUser



        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()

         googleSignInClient = GoogleSignIn.getClient(this, gso)


        //for setting the username and email to the navigationBar
        navBinding.userName.text=currentUser?.displayName
        navBinding.email.text=currentUser?.email

        val photoUrl=currentUser?.photoUrl
        if(photoUrl!=null){
            Glide.with(this@MainActivity)
                .load(photoUrl)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into((navBinding.profileImage))

        }

        binding.navigationView.setNavigationItemSelectedListener {
            when(it.itemId){
                R.id.logout -> logout()
            }
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            true
        }


        //FOR THE CURRENT COCATION
        fusedLocationProviderClient= LocationServices.getFusedLocationProviderClient(this)
        getCurrentLocationUser()

        databaseReference=FirebaseDatabase.getInstance().getReference("Users")
    }

    private fun getCurrentLocationUser() {

        // Check if the location permission is granted
        if (ContextCompat.checkSelfPermission(
                this,android.
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Request location permission
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )

            return
        }

        //if the location is already granted
        val getLocation =fusedLocationProviderClient.lastLocation.addOnSuccessListener {



            if(it!=null){
                currentLocation = it

                //for the current date and time
                val currentDateTime = getCurrentDateTime()
                val currentUser=auth.currentUser
                if(currentUser!=null){

                    databaseReference.child(currentUser.uid).child(currentDateTime).setValue(LocationDataClass(currentLocation.latitude.toString(),currentLocation.longitude.toString()))
                }


                // Obtain the SupportMapFragment and get notified when the map is ready to be used.
                val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
                mapFragment.getMapAsync(this)
            }


        }
    }

    private fun logout() {

        Toast.makeText(applicationContext,"login out",Toast.LENGTH_SHORT).show()
        Firebase.auth.signOut()
        auth?.signOut()
        googleSignInClient?.signOut()
        gotoSigninActivity()



    }

    private fun gotoSigninActivity() {
        val intent=Intent(this@MainActivity,Login::class.java)
        startActivity(intent)
        finish()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (toogle.onOptionsItemSelected(item)) {
            true
        } else super.onOptionsItemSelected(item)
    }


    //for the map

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        // Add a marker in Sydney and move the camera
        val location = LatLng(currentLocation.latitude,currentLocation.longitude)
        mMap.addMarker(
            MarkerOptions()
            .position(location)
            .title("Current Location"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(location))
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Location permission granted
                getCurrentLocationUser()
            } else {
                // Location permission denied
                // Handle the scenario where the user denied the permission
                Toast.makeText(this,"Permission Denied",Toast.LENGTH_SHORT).show()

            }
        }
    }

    private fun getCurrentDateTime(): String {
        val calendar = Calendar.getInstance()
        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return simpleDateFormat.format(calendar.time)
    }
}
