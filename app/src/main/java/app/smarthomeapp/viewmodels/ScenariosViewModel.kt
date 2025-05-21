package app.smarthomeapp.viewmodels


import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.AlarmClock
import android.util.Log
import android.widget.Button
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import java.util.concurrent.TimeUnit

class ScenariosViewModel : ViewModel() {

    private val _alarmSetMessage = MutableLiveData<String>()
    val alarmSetMessage: LiveData<String> get() = _alarmSetMessage

    private val firestore = FirebaseFirestore.getInstance()
    // real time database
    private val rtb = FirebaseDatabase.getInstance()
    private val auth = FirebaseAuth.getInstance()

    fun startLocationTracking(context: Context) {

        val workRequest = PeriodicWorkRequestBuilder<PrintMessageWorker>(15, TimeUnit.MINUTES)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "UniqueWorker",
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )

    }

    fun stopLocationTracking(context: Context) {
        WorkManager.getInstance(context).cancelAllWork()
    }


    fun getUserLocation(context: Context, onLocationRetrieved: (latitude: Double?, longitude: Double?) -> Unit) {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val latitude = location.latitude
                    val longitude = location.longitude
                    onLocationRetrieved(latitude, longitude)
                } else {
                    onLocationRetrieved(null, null)
                }
            }.addOnFailureListener {
                onLocationRetrieved(null, null)
            }
        } else {
            onLocationRetrieved(null, null)
        }
    }


    fun checkUserLocation(context: Context) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {

            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val userLatitude = location.latitude
                    val userLongitude = location.longitude
                    Log.d("ScenariosViewModel", "Location retrieved: Latitude = $userLatitude, Longitude = $userLongitude")

                    var homeLatitude = 1.1
                    var homeLongitude = 1.1

                    val userId = auth.currentUser?.uid
                    if (userId == null) {
                        _alarmSetMessage.value = "User not authenticated."
                    }

                    val userDocRef = userId?.let { firestore.collection("users").document(it) }

                    userDocRef?.get()?.addOnSuccessListener { document ->
                        if (document.exists()) {
                            homeLatitude = document.getDouble("homeLatitude")!!
                            homeLongitude = document.getDouble("homeLongitude")!!

                            val isAtHome = isWithinRadius(userLatitude, userLongitude, homeLatitude, homeLongitude, 50.0)

                            if (isAtHome) {
                                Log.d("ScenariosViewModel", "User is at home. Routine triggered.")


                            } else {
                                Log.d("ScenariosViewModel", "User is not at home.")
                            }

                            val databaseReference = rtb.reference
                            val user = auth.currentUser?.uid


                            val userDoc = user?.let { databaseReference.child("users").child(it) }
                            userDoc?.child("is_home")?.setValue(isAtHome)

                            Log.d("ScenariosViewModel", "Adaptive light task set for user: $user")



                        } else {
                            _alarmSetMessage.value = "User document does not exist."
                            Log.e("ScenariosViewModel", "User document does not exist.")
                        }
                    }?.addOnFailureListener { e ->
                        _alarmSetMessage.value = "Failed to retrieve home location: ${e.localizedMessage}"
                        Log.e("ScenariosViewModel", "Error retrieving home location", e)
                    }
                } else {
                    Log.e("ScenariosViewModel", "Location retrieval failed")
                }
            }
        } else {
            Log.e("ScenariosViewModel", "Location permission not granted")
        }
    }

    fun setAlarm(context: Context, hour: Int, minute: Int, adaptiveLight: Boolean, repeat: Boolean) {

        val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
            putExtra(AlarmClock.EXTRA_HOUR, hour)
            putExtra(AlarmClock.EXTRA_MINUTES, minute)
            putExtra(AlarmClock.EXTRA_MESSAGE, "Smart-Home Alarm")
            if (repeat) {
                putExtra(AlarmClock.EXTRA_DAYS, intArrayOf(1, 2, 3, 4, 5, 6, 7))

            }
        }

        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
            _alarmSetMessage.value = "Alarm set for $hour:$minute"

        } else {
            _alarmSetMessage.value = "No compatible alarm app found!"
        }

        if (adaptiveLight) {
            // send to realtime database to trigger adaptive light in user/tasks
            val databaseReference = rtb.reference
            val userId = auth.currentUser?.uid
            if (userId == null) {
                _alarmSetMessage.value = "User not authenticated."
                return
            }

            val userDocRef = databaseReference.child("users").child(userId)
            userDocRef.child("tasks").child("adaptiveLight").setValue(true)

            if (repeat) {
                userDocRef.child("tasks").child("adaptiveLight").child("repeat").setValue(true)
            } else {
                userDocRef.child("tasks").child("adaptiveLight").child("repeat").setValue(false)
            }


            Log.d("ScenariosViewModel", "Adaptive light task set for user: $userId")


        }

    }


    fun updateHomeLocation(context: Context, latitude: Double, longitude: Double) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            _alarmSetMessage.value = "User not authenticated."
            return
        }

        // Log the location
        Log.d("ScenariosViewModel", "Updating home location: Latitude = $latitude, Longitude = $longitude")

        val userDocRef = firestore.collection("users").document(userId)
        Log.d("ScenariosViewModel", "Updating user document with ID: $userId")

        userDocRef.set(
            mapOf(
                "homeLatitude" to latitude,
                "homeLongitude" to longitude
            ), SetOptions.merge()
        ).addOnSuccessListener {
            _alarmSetMessage.value = "Home location updated!"
            Log.d("ScenariosViewModel", "Home location successfully updated in Firestore")
        }.addOnFailureListener { e ->
            Log.e("ScenariosViewModel", "Error updating home location", e)
            _alarmSetMessage.value = "Failed to update home location: ${e.localizedMessage}"
        }
    }

    fun setBuzzer(testButton: Button) {
        // Reference to the database
        val databaseReference = rtb.reference
        val boxId = "1212"

        // Fetch and toggle the alarm value atomically
        databaseReference.child("box_id").child(boxId).child("alarm").get().addOnSuccessListener { snapshot ->
            val alarmState = snapshot.value?.toString() == "true" // Current alarm state

            // Toggle the alarm state and update the database
            val newState = if (alarmState) "false" else "true"
            databaseReference.child("box_id").child(boxId).child("alarm").setValue(newState)
                .addOnSuccessListener {
                    // Update the button text after successful write
                    testButton.text = if (newState == "true") "Stop" else "Test"
                }
                .addOnFailureListener {
                    // Handle the error, if the write fails
                    testButton.text = "Error"
                }
        }.addOnFailureListener {
            // Handle the error, if the read fails
            testButton.text = "Error"
        }
    }



    private fun isWithinRadius(
        userLat: Double, userLon: Double,
        homeLat: Double, homeLon: Double,
        radiusInMeters: Double
    ): Boolean {
        val results = FloatArray(1)
        android.location.Location.distanceBetween(userLat, userLon, homeLat, homeLon, results)
        Log.d("ScenariosViewModel", "Distance between user and home: ${results[0]} meters")
        return results[0] < radiusInMeters
    }
}


