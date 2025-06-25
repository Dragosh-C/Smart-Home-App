package app.smarthomeapp.mainpage

import android.content.Context
import android.util.Log
import android.widget.GridLayout
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import app.smarthomeapp.FirebaseUtils
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
            val databaseReference = FirebaseUtils.databaseRef

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

            databaseReference.child("box_id").child(boxId).child("air_quality")
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val airQuality = snapshot.getValue(Int::class.java) ?: 0
                        lightText.text = "$airQuality AQI"
                        Log.d("RealtimeDatabase", "AQI updated for selected boxId: $airQuality")

                         if (airQuality in 50..100) {
                             lightText.setTextColor(lightText.context.getColor(android.R.color.holo_orange_light))
                        }

                       else if (airQuality in 101 .. 300) {
                            lightText.setTextColor(lightText.context.getColor(android.R.color.holo_orange_dark))
                        }

                       else if (airQuality > 300) {
                            lightText.setTextColor(lightText.context.getColor(android.R.color.holo_red_dark))
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
            databaseReference.child("box_id").child(boxId).child("power_usage")
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

    private val databaseReference =
        FirebaseDatabase.getInstance("https://smart-home-app-7c709-default-rtdb.europe-west1.firebasedatabase.app/").reference

    private val widgetListeners = mutableMapOf<String, ValueEventListener>()

    fun loadWidgetsFromFirebase(
        context: Context,
        db: FirebaseFirestore,
        auth: FirebaseAuth,
        gridLayout: GridLayout,
        isEditMode: Boolean,
        widgetViews: MutableMap<String, SwitchCompat>
    ) {
        widgetListeners.clear()

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

                                    widgetViews[widget.name]?.setOnCheckedChangeListener { _, isChecked ->

                                        if (widget.type == "Device") {
                                            databaseReference.child("devices").child(widget.port).child("actuator").setValue(isChecked)
                                        } else {
                                            databaseReference.child("devices").child("ports").child(widget.port).child("port").setValue(isChecked)
                                        }
                                    }

                                    // Listen to port state changes
//                                    val portRef = databaseReference.child("ports").child(widget.port)
//                                    val listener = object : ValueEventListener {
//                                        override fun onDataChange(snapshot: DataSnapshot) {
//                                            val isEnabled = snapshot.getValue(Boolean::class.java) ?: false
//                                            widgetViews[widget.name]?.isChecked = isEnabled
//                                        }
//
//                                        override fun onCancelled(error: DatabaseError) {
//                                            Log.e("WidgetLoader", "Error reading port data: ${error.message}")
//                                        }
//                                    }

                                    val portRef = if (widget.type == "Device") {
                                        databaseReference.child("devices").child(widget.port).child("actuator")
                                    } else {
                                        databaseReference.child("devices").child("ports").child(widget.port).child("port")
                                    }

                                    val listener = object : ValueEventListener {
                                        override fun onDataChange(snapshot: DataSnapshot) {
                                            val isEnabled = snapshot.getValue(Boolean::class.java) ?: false
                                            widgetViews[widget.name]?.isChecked = isEnabled
                                        }

                                        override fun onCancelled(error: DatabaseError) {
                                            Log.e("WidgetLoader", "Error reading port data: ${error.message}")
                                        }
                                    }

                                    portRef.addValueEventListener(listener)
                                    widgetListeners[widget.port] = listener
                                }
                            }
                    }
                }
            }
    }


}
