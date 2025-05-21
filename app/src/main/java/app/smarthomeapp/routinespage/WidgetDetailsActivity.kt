//package app.smarthomeapp.routinespage
//
//import android.os.Bundle
//import android.widget.Button
//import android.widget.TextView
//import android.widget.Toast
//import androidx.activity.enableEdgeToEdge
//import androidx.appcompat.app.AlertDialog
//import androidx.appcompat.app.AppCompatActivity
//import androidx.appcompat.widget.SwitchCompat
//import androidx.lifecycle.lifecycleScope
//import app.smarthomeapp.R
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.launch
//import kotlinx.coroutines.withContext
//
//class WidgetDetailsActivity : AppCompatActivity() {
//
//    private lateinit var widgetDatabase: WidgetDatabase
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_widget_details)
//        enableEdgeToEdge()
//        widgetDatabase = WidgetDatabase.getDatabase(this)
//
//        val titleText: TextView = findViewById(R.id.widgetTitleText)
//        val typeText: TextView = findViewById(R.id.widgetTypeText)
//        val deleteButton: Button = findViewById(R.id.deleteButton)
//
//        // Get widget data passed from intent
//        val widgetId = intent.getIntExtra("widget_id", -1)
//        val widgetTitle = intent.getStringExtra("widget_title") ?: "Unknown"
//        val widgetType = intent.getStringExtra("widget_type") ?: "Unknown"
//
//        titleText.text = "$widgetTitle"
//        typeText.text = "$widgetType"
//
//        deleteButton.setOnClickListener {
//            deleteWidget(Widget1(widgetId, widgetTitle, widgetType, isEnabled = true, ifCondition = "", thenAction = ""))
//        }
//
//        val enableSwitch: SwitchCompat = findViewById(R.id.widgetEnabledSwitch)
//
//        lifecycleScope.launch(Dispatchers.IO) {
//            val widget = widgetDatabase.widgetDao().getWidgetById(widgetId)
//            if (widget != null) {
//                withContext(Dispatchers.Main) {
//                    enableSwitch.isChecked = widget.isEnabled
//                }
//            }
//        }
//        enableSwitch.setOnCheckedChangeListener { _, isChecked ->
//            lifecycleScope.launch(Dispatchers.IO) {
//                try {
//                    val widget = widgetDatabase.widgetDao().getWidgetById(widgetId)
//                    if (widget != null) {
//                        widget.isEnabled = isChecked
//                        widgetDatabase.widgetDao().updateWidget(widget)
//
//                        withContext(Dispatchers.Main) {
//                            Toast.makeText(
//                                this@WidgetDetailsActivity,
//                                "Widget ${if (isChecked) "enabled" else "disabled"}",
//                                Toast.LENGTH_SHORT
//                            ).show()
//                        }
//                    }
//                } catch (e: Exception) {
//                    e.printStackTrace()
//                    withContext(Dispatchers.Main) {
//                        Toast.makeText(
//                            this@WidgetDetailsActivity,
//                            "Something went wrong: ${e.message}",
//                            Toast.LENGTH_LONG
//                        ).show()
//                    }
//                }
//            }
//        }
//    }
//
//    private fun deleteWidget(widget: Widget1) {
//        AlertDialog.Builder(this)
//            .setTitle("Delete Widget")
//            .setMessage("Are you sure you want to delete '${widget.title}'?")
//            .setPositiveButton("Yes") { _, _ ->
//                lifecycleScope.launch {
//                    widgetDatabase.widgetDao().deleteWidget(widget)
//                    Toast.makeText(this@WidgetDetailsActivity, "Widget deleted", Toast.LENGTH_SHORT).show()
//                    finish()
//                }
//            }
//            .setNegativeButton("Cancel", null)
//            .show()
//    }
//}
package app.smarthomeapp.routinespage

import FirebaseHelper
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.lifecycle.lifecycleScope
import app.smarthomeapp.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WidgetDetailsActivity : AppCompatActivity() {

    private lateinit var widgetDatabase: WidgetDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_widget_details)
        enableEdgeToEdge()
        widgetDatabase = WidgetDatabase.getDatabase(this)

        val titleText: TextView = findViewById(R.id.widgetTitleText)
//        val typeText: TextView = findViewById(R.id.widgetTypeText)
        val ifConditionText: TextView = findViewById(R.id.widgetIfConditionText)
        val thenActionText: TextView = findViewById(R.id.widgetThenActionText)
        val repeatEveryDayText: TextView = findViewById(R.id.widgetRepeatEveryDayText)
        val deleteButton: Button = findViewById(R.id.deleteButton)

        // Get widget data passed from intent
        val widgetId = intent.getIntExtra("widget_id", -1)
        val widgetTitle = intent.getStringExtra("widget_title") ?: "Unknown"
        val widgetType = intent.getStringExtra("widget_type") ?: "Unknown"
        val widgetIfCondition = intent.getStringExtra("if_condition") ?: "Not Set"
        val widgetThenAction = intent.getStringExtra("then_action") ?: "Not Set"
        val widgetRepeatEveryDay = intent.getBooleanExtra("repeat_every_day", false)
        val widgetDeviceID = intent.getStringExtra("device_id") ?: "Unknown"

        titleText.text = "$widgetTitle"
//        typeText.text = "$widgetType"
        ifConditionText.text = "IF: $widgetIfCondition"
        thenActionText.text = "THEN: $widgetThenAction"
        repeatEveryDayText.text = "Repeat Every Day: ${if (widgetRepeatEveryDay) "Yes" else "No"}"

        deleteButton.setOnClickListener {
            deleteWidget(Widget1(widgetId, widgetTitle, widgetType, isEnabled = true, ifCondition = "", thenAction = "", deviceID = widgetDeviceID))
        }

        val enableSwitch: SwitchCompat = findViewById(R.id.widgetEnabledSwitch)

        lifecycleScope.launch(Dispatchers.IO) {
            val widget = widgetDatabase.widgetDao().getWidgetById(widgetId)
            if (widget != null) {
                withContext(Dispatchers.Main) {
                    enableSwitch.isChecked = widget.isEnabled
                }
            }
        }

        enableSwitch.setOnCheckedChangeListener { _, isChecked ->
            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    val widget = widgetDatabase.widgetDao().getWidgetById(widgetId)
                    if (widget != null) {
                        widget.isEnabled = isChecked
                        widgetDatabase.widgetDao().updateWidget(widget)
                        // Update the widget in Firebase
                        val firebaseHelper = FirebaseHelper()
                        firebaseHelper.enableWidget(widgetId, isChecked)

                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                this@WidgetDetailsActivity,
                                "Widget ${if (isChecked) "enabled" else "disabled"}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@WidgetDetailsActivity,
                            "Something went wrong: ${e.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }
    }

    private fun deleteWidget(widget: Widget1) {
        AlertDialog.Builder(this)
            .setTitle("Delete Widget")
            .setMessage("Are you sure you want to delete '${widget.title}'?")
            .setPositiveButton("Yes") { _, _ ->
                lifecycleScope.launch {
                    widgetDatabase.widgetDao().deleteWidget(widget)
                    Toast.makeText(this@WidgetDetailsActivity, "Widget deleted", Toast.LENGTH_SHORT).show()

                    // decrease the last_id in firebase
                    val firebaseHelper = FirebaseHelper()
                    firebaseHelper.deleteWidgetFromFirebase(widget.id)
                    // delete the widget from firebase

                    finish()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
