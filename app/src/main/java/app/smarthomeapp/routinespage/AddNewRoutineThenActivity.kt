package app.smarthomeapp.routinespage

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import app.smarthomeapp.R


class AddNewRoutineThenActivity : AppCompatActivity() {

    // Declare a map to hold the references for the buttons dynamically
    private val buttonActions = mutableMapOf<String, String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_then_routine)

        val backButton = findViewById<ImageView>(R.id.back_button)
        backButton.setOnClickListener {
            finish()
        }

        // Handle relay button click
        val relayButton = findViewById<LinearLayout>(R.id.relay_button)
        relayButton.setOnClickListener {
            showRelayDialog()
        }

        // Handle other buttons (e.g., dimmer, alarm, camera, lockers)
        val dimmerButton = findViewById<LinearLayout>(R.id.dimmer_button)
        dimmerButton.setOnClickListener {
            showDimmerDialog()
        }

        val alarmButton = findViewById<LinearLayout>(R.id.alarm_button)
        alarmButton.setOnClickListener {
            showAlarmDialog()
        }

        // Add more button listeners for other actions as required
    }

    private fun showRelayDialog() {
        val builder = Dialog(this)
        builder.setContentView(R.layout.dialog_relay)

        val conditionField = builder.findViewById<EditText>(R.id.condition_input)
        val relayState = builder.findViewById<EditText>(R.id.relay_state)
        val saveButton = builder.findViewById<Button>(R.id.save_relay_button)
        val deviceIDInput = builder.findViewById<EditText>(R.id.device_id_input)

        val gradientDrawable = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 30f
            setColor(Color.parseColor("#1c1d23"))
        }
        builder.window?.setBackgroundDrawable(gradientDrawable)

        val conditions = listOf("Port 1", "Port 2", "Port 3", "Port 4")

        // Handling conditions
        conditionField.setOnClickListener {
            val popupMenu = PopupMenu(this, conditionField)
            conditions.forEach { condition ->
                popupMenu.menu.add(condition)
            }
            popupMenu.setOnMenuItemClickListener { menuItem ->
                conditionField.setText(menuItem.title)
                true
            }
            popupMenu.show()
        }

        val states = listOf("On", "Off")

        // Handling states
        relayState.setOnClickListener {
            val popupMenu = PopupMenu(this, relayState)
            states.forEach { state ->
                popupMenu.menu.add(state)
            }
            popupMenu.setOnMenuItemClickListener { menuItem ->
                relayState.setText(menuItem.title)
                true
            }
            popupMenu.show()
        }

        saveButton.setOnClickListener {
            val port = conditionField.text.toString()
            val state = relayState.text.toString()

            if ((port.isNotBlank() or deviceIDInput.text.isNotBlank()) && state.isNotBlank()) {

                var selectedCondition = ""
                if (port.isNotBlank()) {
                    selectedCondition = "$port is turned $state"
                }
                else {
                    val deviceID = deviceIDInput.text.toString()
                    selectedCondition = "Device with ID $deviceID is turned $state"
                }


                // Overwriting selected action dynamically
                buttonActions["relay"] = selectedCondition
//
////                // Display the updated selected action in your UI
//                val relayTextView = findViewById<TextView>(R.id.relay_title)
//                relayTextView.text = selectedCondition
//
                Toast.makeText(
                    this,
                    "Selected Condition: $selectedCondition",
                    Toast.LENGTH_SHORT
                ).show()

                val resultIntent = Intent()
                resultIntent.putExtra("selected_action", selectedCondition)
                resultIntent.putExtra("title", "Relay")
                setResult(Activity.RESULT_OK, resultIntent)
                finish()
                builder.dismiss()
            } else {
                Toast.makeText(
                    this,
                    "Please select port and state",
                    Toast.LENGTH_SHORT
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

    private fun showDimmerDialog() {
        val builder = Dialog(this)
        builder.setContentView(R.layout.dialog_dimmer)

        val dimmerValueInput = builder.findViewById<EditText>(R.id.dimmer_value_input)
        val portValueInput = builder.findViewById<EditText>(R.id.port_input)
        val saveButton = builder.findViewById<Button>(R.id.save_dimmer_button)

        val gradientDrawable = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 30f
            setColor(Color.parseColor("#1c1d23"))
        }
        builder.window?.setBackgroundDrawable(gradientDrawable)

        // Validation for input value between 0 and 100
        saveButton.setOnClickListener {
            val dimmerValue = dimmerValueInput.text.toString()
            val portValue = portValueInput.text.toString()

            if (dimmerValue.isNotBlank() && portValue.isNotBlank()) {
                val dimmerValueInt = dimmerValue.toIntOrNull()


                if (dimmerValueInt != null && dimmerValueInt in 0..100) {
                    val selectedCondition = "Set Dimmer to $dimmerValue% on Port $portValue"

                    // Store the action in the buttonActions map
                    buttonActions["dimmer"] = selectedCondition

                    // Update the text view or UI element where the dimmer value is shown
//                    val dimmerTextView = findViewById<TextView>(R.id.dimmer_text_view)
//                    dimmerTextView.text = selectedCondition

                    Toast.makeText(
                        this,
                        "Selected Condition: $selectedCondition",
                        Toast.LENGTH_SHORT
                    ).show()

                    // Returning result to previous activity
                    val resultIntent = Intent()
                    resultIntent.putExtra("selected_action", selectedCondition)
                    resultIntent.putExtra("title", "Dimmer")
                    setResult(Activity.RESULT_OK, resultIntent)
                    finish()
                    builder.dismiss()
                } else {
                    Toast.makeText(
                        this,
                        "Please enter a valid value between 0 and 100",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                Toast.makeText(
                    this,
                    "Please enter a dimmer value",
                    Toast.LENGTH_SHORT
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

    private fun showAlarmDialog() {
        val builder = Dialog(this)
        builder.setContentView(R.layout.dialog_alarm)

        val alarmValueInput = builder.findViewById<EditText>(R.id.alarm_value_input)
        val saveButton = builder.findViewById<Button>(R.id.save_alarm_button)

        val gradientDrawable = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 30f
            setColor(Color.parseColor("#1c1d23"))
        }
        builder.window?.setBackgroundDrawable(gradientDrawable)

        val states = listOf("On", "Off")
        val alarmState = builder.findViewById<EditText>(R.id.alarm_state)

        // Handling states
        alarmState.setOnClickListener {
            val popupMenu = PopupMenu(this, alarmState)
            states.forEach { state ->
                popupMenu.menu.add(state)
            }
            popupMenu.setOnMenuItemClickListener { menuItem ->
                alarmState.setText(menuItem.title)
                true
            }
            popupMenu.show()
        }

        saveButton.setOnClickListener {
            var alarmValue = alarmValueInput.text.toString()
            val alarm = alarmState.text.toString()

            if (alarm.isNotBlank()) {

                if (alarmValue.isBlank()) {
                    alarmValue = "All devices"
                }

                val selectedCondition = "$alarmValue turned $alarm"

                // Overwriting selected action dynamically
                buttonActions["alert"] = selectedCondition
//
////                // Display the updated selected action in your UI
//                val relayTextView = findViewById<TextView>(R.id.relay_title)
//                relayTextView.text = selectedCondition
//
                Toast.makeText(
                    this,
                    "Selected Condition: $selectedCondition",
                    Toast.LENGTH_SHORT
                ).show()

                val resultIntent = Intent()
                resultIntent.putExtra("selected_action", selectedCondition)
                resultIntent.putExtra("title", "Alert")
                setResult(Activity.RESULT_OK, resultIntent)
                finish()
                builder.dismiss()
            } else {
                Toast.makeText(
                    this,
                    "Please select state",
                    Toast.LENGTH_SHORT
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
