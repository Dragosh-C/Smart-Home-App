package app.smarthomeapp

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import app.smarthomeapp.R
import com.google.firebase.database.*

class SettingsGraphicsActivity : AppCompatActivity() {
    private lateinit var temperatureField: EditText
    private lateinit var humidityField: EditText
    private lateinit var powerField: EditText
    private lateinit var airQualityField: EditText
    private lateinit var lightField: EditText
    private lateinit var batteryField: EditText
    private lateinit var saveButton: Button

    private val database: DatabaseReference = FirebaseDatabase.getInstance("https://smart-home-app-7c709-default-rtdb.europe-west1.firebasedatabase.app")
        .getReference("/sensor_ids")

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings_graphics)

        temperatureField = findViewById(R.id.temperatureIdField)
        humidityField = findViewById(R.id.humidityIdField)
        powerField = findViewById(R.id.powerIdField)
        airQualityField = findViewById(R.id.airQualityIdField)
        lightField = findViewById(R.id.lightIdField)
        batteryField = findViewById(R.id.batteryIdField)
        saveButton = findViewById(R.id.saveButton)

        loadExistingIds()

        saveButton.setOnClickListener {
            saveIdsToFirebase()
        }
    }

    private fun loadExistingIds() {
        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                temperatureField.setText(snapshot.child("temperature").value?.toString() ?: "")
                humidityField.setText(snapshot.child("humidity").value?.toString() ?: "")
                powerField.setText(snapshot.child("power").value?.toString() ?: "")
                airQualityField.setText(snapshot.child("air_quality").value?.toString() ?: "")
                lightField.setText(snapshot.child("light").value?.toString() ?: "")
                batteryField.setText(snapshot.child("battery").value?.toString() ?: "")
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Error loading sensor IDs: ${error.message}")
            }
        })
    }

    private fun saveIdsToFirebase() {
        val sensorIds = mapOf(
            "temperature" to temperatureField.text.toString(),
            "humidity" to humidityField.text.toString(),
            "power" to powerField.text.toString(),
            "air_quality" to airQualityField.text.toString(),
            "light" to lightField.text.toString(),
            "battery" to batteryField.text.toString()
        )

        database.setValue(sensorIds).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d("Firebase", "Sensor IDs saved successfully")
            } else {
                Log.e("Firebase", "Failed to save sensor IDs", task.exception)
            }
        }
    }
}
