package app.smarthomeapp

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.lifecycle.ViewModelProvider
import app.smarthomeapp.viewmodels.ScenariosViewModel

class SettingsActivity : AppCompatActivity() {
    private lateinit var viewModel: ScenariosViewModel
    // init firebase database

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.setting_page_layout)

        viewModel = ViewModelProvider(this)[ScenariosViewModel::class.java]

        val switchButton = findViewById<SwitchCompat>(R.id.switch_enable_location)
        val testButton = findViewById<Button>(R.id.btn_test_alarm)

        val lockDoorButtonSwitch = findViewById<SwitchCompat>(R.id.lock_door)
        val enablePasswordAccessSwitch = findViewById<SwitchCompat>(R.id.enable_access_with_password)
        val enableRFIDAccessSwitch = findViewById<SwitchCompat>(R.id.enable_access_with_rfid)
        val changePasswordButton = findViewById<Button>(R.id.btn_change_password)
        val passwordEditText = findViewById<EditText>(R.id.tv_change_password)

        val databaseReference = FirebaseUtils.databaseRef


        // Restore the saved state of the switch
        val sharedPref = getSharedPreferences("app.smarthomeapp", MODE_PRIVATE)
        val switchState = sharedPref.getBoolean("switchButtonState", false)
        switchButton.isChecked = switchState

        switchButton.setOnCheckedChangeListener { _, isChecked ->
            with(sharedPref.edit()) {
                putBoolean("switchButtonState", isChecked)
                apply()
            }
            if (isChecked) {
                viewModel.startLocationTracking(this)
            } else {
                viewModel.stopLocationTracking(this)
            }
        }



        findViewById<ImageView>(R.id.backButton_settings).setOnClickListener {
            finish()
        }

        findViewById<Button>(R.id.btn_change_home_location).setOnClickListener {
            updateHomeLocationDialog()
        }

        testButton.setOnClickListener {
            viewModel.setBuzzer(testButton)
        }

        // send to firebase box_id/4123/door_lock
        lockDoorButtonSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                databaseReference.child("box_id").child("4123").child("door_lock").setValue(true)
                Toast.makeText(this, "Door locked", Toast.LENGTH_SHORT).show()
            } else {
                databaseReference.child("box_id").child("4123").child("door_lock").setValue(false)
                Toast.makeText(this, "Door unlocked", Toast.LENGTH_SHORT).show()
            }
        }

        enablePasswordAccessSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                databaseReference.child("box_id").child("4123").child("enable_pin").setValue(true)
                Toast.makeText(this, "Password access enabled", Toast.LENGTH_SHORT).show()
            } else {
                databaseReference.child("box_id").child("4123").child("enable_pin").setValue(false)
                Toast.makeText(this, "Password access disabled", Toast.LENGTH_SHORT).show()
            }
        }

        enableRFIDAccessSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                databaseReference.child("box_id").child("4123").child("enable_rfid").setValue(true)
                Toast.makeText(this, "RFID access enabled", Toast.LENGTH_SHORT).show()
            } else {
                databaseReference.child("box_id").child("4123").child("enable_rfid").setValue(false)
                Toast.makeText(this, "RFID access disabled", Toast.LENGTH_SHORT).show()
            }
        }

        changePasswordButton.setOnClickListener {
            val newPassword = passwordEditText.text.toString()
            if (newPassword.isNotBlank()) {
                // Save the new password to Firebase
                databaseReference.child("box_id").child("4123").child("password").setValue(newPassword)
                Toast.makeText(this, "Password changed successfully", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Please enter a new password", Toast.LENGTH_SHORT).show()
            }
        }



    }

    private fun updateHomeLocationDialog() {
        val builder = Dialog(this)
        builder.setContentView(R.layout.dialog_change_location)

        val latitudeInputField =
            builder.findViewById<EditText>(R.id.latitude_input)
        val longitudeInputField = builder.findViewById<EditText>(R.id.longitude_input)
        val saveButton = builder.findViewById<Button>(R.id.change_location_button)
        val autodetectLocationButton = builder.findViewById<Button>(R.id.autodetect_location_button)


        val gradientDrawable = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 30f
            setColor(Color.parseColor("#1c1d23"))
        }
        builder.window?.setBackgroundDrawable(gradientDrawable)

        autodetectLocationButton.setOnClickListener {
            // autodetect location
            viewModel.getUserLocation(this) { latitude, longitude ->
                if (latitude != null && longitude != null) {
                    Toast.makeText(this, "Location updated", Toast.LENGTH_SHORT).show()
                    latitudeInputField.setText(latitude.toString())
                    longitudeInputField.setText(longitude.toString())
                } else {
                    Toast.makeText(this, "Failed to autodetect location", Toast.LENGTH_SHORT).show()
                }
            }
        }

        saveButton.setOnClickListener {
            val latitudeName = latitudeInputField.text.toString()
            val longitudeName = longitudeInputField.text.toString()

            Toast.makeText(this, "Location saved", Toast.LENGTH_SHORT).show()

            if (latitudeName.isNotBlank() && longitudeName.isNotBlank()) {
                 // save to firebase location
                viewModel.updateHomeLocation(this, latitudeName.toDouble(), longitudeName.toDouble())
                builder.dismiss()
            } else {
                Toast.makeText(this, "Please enter latitude and longitude", Toast.LENGTH_SHORT
                ).show()
            }
        }


        val layoutParams = WindowManager.LayoutParams()
        layoutParams.copyFrom(builder.window?.attributes)
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT
        builder.window?.attributes = layoutParams
        builder.show()
    }

}
