package app.smarthomeapp.mainpage

import android.content.Context
import android.util.Log
import android.widget.GridLayout
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import app.smarthomeapp.notifications.Notifications
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore

class RealtimeDatabaseListener(context: Context) {

    fun listenToRealtimeUpdates(
        boxId: String,
        selectedBoxId: String,
        temperatureText: TextView,
        humidityText: TextView,
        lightText: TextView,
        powerText: TextView
    ) {
        if (selectedBoxId == boxId) {
            val databaseReference =
                FirebaseDatabase.getInstance("https://smart-home-app-7c709-default-rtdb.europe-west1.firebasedatabase.app/").reference

            databaseReference.child("box_id").child(boxId).child("temperature")
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val newTemperature = snapshot.getValue(Int::class.java) ?: 0
                        temperatureText.text = "$newTemperature °C"
                        Log.d(
                            "RealtimeDatabase",
                            "Temperature updated for selected boxId: $newTemperature"
                        )
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e(
                            "RealtimeDatabase",
                            "Error listening for temperature updates: ${error.message}"
                        )
                    }
                })

            // Humidity Listener
            databaseReference.child("box_id").child(boxId).child("humidity")
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val newHumidity = snapshot.getValue(Int::class.java) ?: 0
                        humidityText.text = "$newHumidity %"
                        Log.d(
                            "RealtimeDatabase",
                            "Humidity updated for selected boxId: $newHumidity"
                        )
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e(
                            "RealtimeDatabase",
                            "Error listening for humidity updates: ${error.message}"
                        )
                    }
                })

            // Light Listener
//            databaseReference.child("box_id").child(boxId).child("light")
//                .addValueEventListener(object : ValueEventListener {
//                    override fun onDataChange(snapshot: DataSnapshot) {
//                        val newLight = snapshot.getValue(Int::class.java) ?: 0
//                        lightText.text = "$newLight Lux"
//                        Log.d("RealtimeDatabase", "Light updated for selected boxId: $newLight")
//                    }
//
//                    override fun onCancelled(error: DatabaseError) {
//                        Log.e(
//                            "RealtimeDatabase",
//                            "Error listening for light updates: ${error.message}"
//                        )
//                    }
//                })

            databaseReference.child("box_id").child(boxId).child("air_quality")
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val airQuality = snapshot.getValue(Int::class.java) ?: 0
                        lightText.text = "$airQuality AQI"
                        Log.d("RealtimeDatabase", "AQI updated for selected boxId: $airQuality")

                         if (airQuality in 50..100) {
                             lightText.setTextColor(lightText.context.getColor(android.R.color.holo_orange_light))
                             val notifications = Notifications(lightText.context)
                             notifications.createNotificationChannel()
                             notifications.sendNotification("Air Quality Alert", "Air quality is moderate. Consider opening the windows.")
                        }

                       else if (airQuality in 101 .. 300) {
                            lightText.setTextColor(lightText.context.getColor(android.R.color.holo_orange_dark))
                            val notifications = Notifications(lightText.context)
                            notifications.createNotificationChannel()
                            notifications.sendNotification("Air Quality Alert", "The indoor air quality is unhealthy. Ensure proper ventilation or consider using an air purifier.")

                        }

                       else if (airQuality > 300) {
                            lightText.setTextColor(lightText.context.getColor(android.R.color.holo_red_dark))
                            val notifications = Notifications(lightText.context)
                            notifications.createNotificationChannel()
                            notifications.sendNotification("Air Quality Alert", "The air quality is critically poor. This may indicate a fire or gas leak!")
                        }
                        else {
                            lightText.setTextColor(lightText.context.getColor(android.R.color.holo_green_light))
                        }

                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e(
                            "RealtimeDatabase",
                            "Error listening for light updates: ${error.message}"
                        )
                    }
                })

            // Power Listener
            databaseReference.child("box_id").child(boxId).child("power")
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val newPower = snapshot.getValue(Int::class.java) ?: 0
                        powerText.text = "$newPower W"
                        Log.d("RealtimeDatabase", "Power updated for selected boxId: $newPower")
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e(
                            "RealtimeDatabase",
                            "Error listening for power updates: ${error.message}"
                        )
                    }
                })


        } else {
            Log.d("RealtimeDatabase", "Skipping update for non-selected room (boxId: $boxId).")
        }
    }

    fun loadWidgetsFromFirebase(
        context: Context,
        db: FirebaseFirestore,
        auth: FirebaseAuth,
        gridLayout: GridLayout,
        isEditMode: Boolean,
        widgetViews: MutableMap<String, SwitchCompat>
    ) {
        db.collection("users").document(auth.currentUser!!.uid).collection("rooms").get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val room = document.data
                    val roomName = room["name"] as String
                    val isSelected = room["isSelected"] as Boolean
                    if (isSelected) {
                        db.collection("users").document(auth.currentUser!!.uid).collection("rooms")
                            .document(roomName).collection("widgets").get()
                            .addOnSuccessListener { res ->
                                for (doc in res) {
                                    val widget = doc.toObject(Widget::class.java)
                                    addWidgetToGridLayout(context, widget, gridLayout, db, auth, isEditMode, widgetViews)

                                    val databaseReference =
                                        FirebaseDatabase.getInstance("https://smart-home-app-7c709-default-rtdb.europe-west1.firebasedatabase.app/").reference

                                    widgetViews[widget.name]?.setOnCheckedChangeListener { _, isChecked ->
                                        databaseReference.child("ports").child(widget.port)
                                            .setValue(isChecked)
                                    }

                                    val portRef =
                                        databaseReference.child("ports").child(widget.port)
                                    portRef.addValueEventListener(object : ValueEventListener {
                                        override fun onDataChange(snapshot: DataSnapshot) {
                                            val isEnabled =
                                                snapshot.getValue(Boolean::class.java) ?: false
                                            widgetViews[widget.name]?.isChecked = isEnabled
                                        }

                                        override fun onCancelled(error: DatabaseError) {
                                            Log.e("WidgetLoader", "Error reading port data: ${error.message}")
                                        }
                                    })
                                }
                            }
                    }
                }
            }
    }

}
