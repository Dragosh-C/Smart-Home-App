//package app.smarthomeapp.routinespage
//
//import android.app.Activity
//import android.app.Dialog
//import android.content.Intent
//import android.graphics.Color
//import android.graphics.drawable.GradientDrawable
//import android.os.Bundle
//import android.view.WindowManager
//import android.widget.Button
//import android.widget.EditText
//import android.widget.ImageView
//import android.widget.LinearLayout
//import android.widget.PopupMenu
//import android.widget.Toast
//import androidx.activity.enableEdgeToEdge
//import androidx.appcompat.app.AppCompatActivity
//import app.smarthomeapp.R
//
//
//class AddNewRoutineIfActivity : AppCompatActivity() {
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
//        setContentView(R.layout.activity_if_routine)
//
//        val backButton = findViewById<ImageView>(R.id.back_button)
//        backButton.setOnClickListener {
//            finish()
//        }
//
//        val temperatureButton = findViewById<LinearLayout>(R.id.temperature_button)
//        temperatureButton.setOnClickListener {
//            showTemperatureDialog()
//        }
//    }
//
//    private fun showTemperatureDialog() {
//        val builder = Dialog(this)
//        builder.setContentView(R.layout.dialog_temperature)
//
//        val conditionField = builder.findViewById<EditText>(R.id.condition_input)
//        val temperatureInputField = builder.findViewById<EditText>(R.id.temperature_input)
//        val saveButton = builder.findViewById<Button>(R.id.save_temperature_button)
//
//        val gradientDrawable = GradientDrawable().apply {
//            shape = GradientDrawable.RECTANGLE
//            cornerRadius = 30f
//            setColor(Color.parseColor("#1c1d23"))
//        }
//        builder.window?.setBackgroundDrawable(gradientDrawable)
//
//        val conditions = listOf(">", "<", "=")
//
//        conditionField.setOnClickListener {
//            val popupMenu = PopupMenu(this, conditionField)
//            conditions.forEach { condition ->
//                popupMenu.menu.add(condition)
//            }
//            popupMenu.setOnMenuItemClickListener { menuItem ->
//                conditionField.setText(menuItem.title)
//                true
//            }
//            popupMenu.show()
//        }
//
//        saveButton.setOnClickListener {
//            val condition = conditionField.text.toString()
//            val temperatureValue = temperatureInputField.text.toString()
//
//            if (condition.isNotBlank() && temperatureValue.isNotBlank()) {
//                val isGreater = condition == ">"
//                val isEqualTo = condition == "="
//                val temperature = temperatureValue.toFloat()
//                handleTemperatureSelection(isGreater, isEqualTo, temperature)
//                builder.dismiss()
//            } else {
//                Toast.makeText(
//                    this,
//                    "Please select a condition and enter a temperature value",
//                    Toast.LENGTH_SHORT
//                ).show()
//            }
//        }
//
//        val layoutParams = WindowManager.LayoutParams()
//        layoutParams.copyFrom(builder.window?.attributes)
//        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT
//        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT
//        builder.window?.attributes = layoutParams
//        builder.show()
//    }
//
//    private fun handleTemperatureSelection(isGreater: Boolean,isEqualTo: Boolean, value: Float) {
//
//        val selectedCondition: String
//
//        if (isGreater) {
//            Toast.makeText(this, "Temperature > $value °C", Toast.LENGTH_SHORT).show()
//            selectedCondition = "Temperature > $value °C"
//
//        } else if (isEqualTo) {
//            Toast.makeText(this, "Temperature = $value °C", Toast.LENGTH_SHORT).show()
//            selectedCondition = "Temperature = $value °C"
//        }
//        else {
//            Toast.makeText(this, "Temperature < $value °C", Toast.LENGTH_SHORT).show()
//            selectedCondition = "Temperature < $value °C"
//        }
//
//        val resultIntent = Intent()
//        resultIntent.putExtra("selected_condition", selectedCondition)
//        setResult(Activity.RESULT_OK, resultIntent)
//        finish()
//    }
//
//
//}

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
import androidx.appcompat.app.AppCompatActivity
import app.smarthomeapp.R

class AddNewRoutineIfActivity : AppCompatActivity() {
    private lateinit var boxTitle: TextView
    private lateinit var deviceId: EditText
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_if_routine)
        val backButton = findViewById<ImageView>(R.id.back_button)
        backButton.setOnClickListener {
            finish()
        }

        // Set click listeners for all condition buttons
        findViewById<LinearLayout>(R.id.temperature_button).setOnClickListener {
            showConditionDialog("Temperature", "°C")
        }
        findViewById<LinearLayout>(R.id.luminosity_button).setOnClickListener {
            showConditionDialog("Luminosity", "lux")
        }
        findViewById<LinearLayout>(R.id.humidity_button).setOnClickListener {
            showConditionDialog("Humidity", "%")
        }
        findViewById<LinearLayout>(R.id.air_quality_button).setOnClickListener {
            showConditionDialog("Air Quality", "AQI")
        }
        findViewById<LinearLayout>(R.id.power_button).setOnClickListener {
            showConditionDialog("Power", "W")
        }
    }

    private fun showConditionDialog(conditionType: String, unit: String) {
        val builder = Dialog(this)
        builder.setContentView(R.layout.dialog_temperature)

        val conditionField = builder.findViewById<EditText>(R.id.condition_input)
        val valueInputField = builder.findViewById<EditText>(R.id.temperature_input) // Reusing temperature_input
        val saveButton = builder.findViewById<Button>(R.id.save_temperature_button)
        deviceId = builder.findViewById(R.id.device_id_input)

        val gradientDrawable = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 30f
            setColor(Color.parseColor("#1c1d23"))
        }
        builder.window?.setBackgroundDrawable(gradientDrawable)

        boxTitle = builder.findViewById(R.id.temperature_title)
        boxTitle.text = conditionType


        // Set dynamic label and units
        val conditions = listOf(">", "<", "=")
        conditionField.hint = "Select Condition (e.g., >, <, =)"
        valueInputField.hint = "Enter Value ($unit)"

        // Show condition options in a popup menu
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

        saveButton.setOnClickListener {
            val condition = conditionField.text.toString()
            val value = valueInputField.text.toString()

            if (condition.isNotBlank() && value.isNotBlank()) {
                handleConditionSelection(conditionType, condition, value, unit)
                builder.dismiss()
            } else {
                Toast.makeText(
                    this,
                    "Please select a condition and enter a value",
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

    private fun handleConditionSelection(conditionType: String, condition: String, value: String, unit: String) {
        val selectedCondition = "$conditionType $condition $value $unit"

        // Set the title based on the condition type


        // Handle specific condition types, e.g., Temperature > 30°C
        Toast.makeText(this, "$selectedCondition", Toast.LENGTH_SHORT).show()

        // Pass the result back to the main activity
        val resultIntent = Intent()
        resultIntent.putExtra("selected_condition", selectedCondition)
        resultIntent.putExtra("device_id", deviceId.text.toString())
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }
}
