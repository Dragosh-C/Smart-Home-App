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
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import app.smarthomeapp.R


class AddNewRoutineThenActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_then_routine)

        val backButton = findViewById<ImageView>(R.id.back_button)
        backButton.setOnClickListener {
            finish()
        }

        val relayButton = findViewById<LinearLayout>(R.id.relay_button)
        relayButton.setOnClickListener {
            showRelayDialog()
        }
    }

    private fun showRelayDialog() {
        val builder = Dialog(this)
        builder.setContentView(R.layout.dialog_relay)

        val conditionField = builder.findViewById<EditText>(R.id.condition_input)

        val relayState = builder.findViewById<EditText>(R.id.relay_state)
        val saveButton = builder.findViewById<Button>(R.id.save_temperature_button)

        val gradientDrawable = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 30f
            setColor(Color.parseColor("#1c1d23"))
        }
        builder.window?.setBackgroundDrawable(gradientDrawable)

        val conditions = listOf("Port 1", "Port 2", "Port 3", "Port 4")

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
        relayState.setOnClickListener {
            val popupMenu = PopupMenu(this, relayState)
            states.forEach { condition ->
                popupMenu.menu.add(condition)
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

            if (port.isNotBlank() && state.isNotBlank()) {
                val selectedCondition = "$port is turned $state"

                Toast.makeText(
                    this,
                    "Selected Condition: $selectedCondition",
                    Toast.LENGTH_SHORT
                ).show()

                val resultIntent = Intent()
                resultIntent.putExtra("selected_action", selectedCondition)
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


}