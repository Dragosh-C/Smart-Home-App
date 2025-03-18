package app.smarthomeapp

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.fragment.app.Fragment
import app.smarthomeapp.mainpage.HomeFragment
import app.smarthomeapp.routinespage.ScenariosFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_screen)

        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("FCM", "Fetching FCM registration token failed", task.exception)
                return@addOnCompleteListener

            }

            // Get the token
            val token = task.result
            Log.d("FCM", "FCM Token: $token")
            sendRegistrationToServer(token!!)

        }

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        enableEdgeToEdge()
        // Set background gradient
        val mainLayout = findViewById<android.widget.LinearLayout>(R.id.main_layout)
        val gradientDrawable = resources.getDrawable(R.drawable.background_gardient) as GradientDrawable
        gradientDrawable.setDither(true)
        mainLayout.background = gradientDrawable

        // BottomNavigationView
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.selectedItemId = R.id.nav_home
        loadFragment(HomeFragment())

        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    loadFragment(HomeFragment())
                    true
                }
                R.id.nav_scenarios -> {
                    loadFragment(ScenariosFragment())
                    true
                }
                R.id.nav_profile -> {
                    loadFragment(ProfileFragment())
                    true
                }

                R.id.nav_camera -> {
                    loadFragment(CameraFragment())
                    true
                }
                else -> false
            }
        }
    }

    // load fragments
    private fun loadFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container, fragment)
        transaction.commit()
    }

    private fun sendRegistrationToServer(token: String) {

        // Get the Firebase Realtime Database reference
        val database: DatabaseReference = FirebaseDatabase.getInstance(
            "https://smart-home-app-7c709-default-rtdb.europe-west1.firebasedatabase.app/"
        ).reference

        database.child("userToken").setValue(token)
            .addOnSuccessListener {
                println("Token successfully sent to the database.")
            }
            .addOnFailureListener { exception ->
                println("Failed to send token to the database: ${exception.message}")
            }
    }
}
