package app.smarthomeapp.routinespage

import FirebaseHelper
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.TimePicker
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.view.children
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import app.smarthomeapp.R
import app.smarthomeapp.viewmodels.ScenariosViewModel
import com.google.android.material.switchmaterial.SwitchMaterial
import kotlinx.coroutines.launch
import java.util.Calendar

class OtherTypeRoutines : AppCompatActivity() {

    private lateinit var viewModel: ScenariosViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.other_type_routines)
        viewModel = ScenariosViewModel()

        val otherTypeRoutine = findViewById<Button>(R.id.set_alarm_button)
        otherTypeRoutine.setOnClickListener {
            showTimePickerDialog()
        }
    }

    private fun showTimePickerDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.time_picker_dialog)
        dialog.setTitle("Smart Home Alarm")

        dialog.window?.setBackgroundDrawableResource(R.drawable.rounded_background)

        val timePicker: TimePicker = dialog.findViewById(R.id.timePicker)
        val adaptiveLight: SwitchMaterial = dialog.findViewById(R.id.enableAdaptiveLightSwitch)
        val repeatSwitch: SwitchMaterial = dialog.findViewById(R.id.repeatSwitch)
        val saveButton: Button = dialog.findViewById(R.id.alarm_save_button)
        timePicker.setIs24HourView(true)

        val calendar = Calendar.getInstance()
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        val currentMinute = calendar.get(Calendar.MINUTE)

        timePicker.hour = currentHour
        timePicker.minute = currentMinute

        saveButton.setOnClickListener {
            val selectedHour = timePicker.hour
            val selectedMinute = timePicker.minute
            val isAdaptiveLight = adaptiveLight.isChecked
            val isRepeat = repeatSwitch.isChecked

            viewModel.setAlarm(this, selectedHour, selectedMinute, isAdaptiveLight, isRepeat)

            // create a widget and save it to the database
            // Convert the widget type to an enum or string

            // selected hours and minutes shoud have leading zero in front of them in case they are less than 10
            val selectedHourString = selectedHour.toString().padStart(2, '0')
            val selectedMinuteString = selectedMinute.toString().padStart(2, '0')

            val widget = Widget1(
                id = lastIdvalue,
                title = "Adaptive Alarm",
                type = "Alarm",
                ifCondition = "Alarm set for $selectedHourString:$selectedMinuteString",
                thenAction = "Alarm triggered",
                repeatEveryDay = isRepeat,
                deviceID = "deviceId",

            )
            // Save the widget to firebase
            val firebaseHelper = FirebaseHelper()
            firebaseHelper.addWidgetToDatabase(widget, isAdaptiveLight)



            lifecycleScope.launch {
                val widgetDatabase = WidgetDatabase.getDatabase(this@OtherTypeRoutines)
                widgetDatabase.widgetDao().insertWidget(widget)
                finish()
            }

            dialog.dismiss()
            finish()
        }
        dialog.show()
    }
}
