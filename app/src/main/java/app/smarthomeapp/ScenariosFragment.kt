//package app.smarthomeapp
//import android.app.Dialog
//import android.content.Intent
//import android.os.Bundle
//import android.provider.AlarmClock
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//
//import android.widget.TimePicker
//import android.widget.Toast
//import androidx.fragment.app.Fragment
//import androidx.recyclerview.widget.RecyclerView
//import com.google.firebase.auth.FirebaseAuth
//import com.google.firebase.firestore.FirebaseFirestore
//import com.google.firebase.firestore.ListenerRegistration
//import java.util.Calendar
//import android.widget.Button
//import android.widget.Switch
//import androidx.core.content.ContextCompat
//import com.google.android.material.switchmaterial.SwitchMaterial
//
//
//data class Widget2(
//    val id: String = "",
//    val name: String = "",
//    val color: String = "",
//    val size: Int = 0
//)
//
//class ScenariosFragment : Fragment() {
//
//    private lateinit var db: FirebaseFirestore
//    private lateinit var auth: FirebaseAuth
//    private lateinit var widgetsRecyclerView: RecyclerView
//    private lateinit var widgetAdapter: WidgetAdapter
//    private lateinit var widgetList: MutableList<Widget2>
//    private var widgetListener: ListenerRegistration? = null
//
//
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        val view = inflater.inflate(R.layout.fragment_routines, container, false)
//
//        val setAlarmButton = view.findViewById<View>(R.id.set_alarm_button)
//
//        setAlarmButton.setOnClickListener{
//            showTimePickerDialog()
//        }
//        return view
//    }
//
//
//    private fun showTimePickerDialog() {
//
//        val dialog = Dialog(requireContext())
//        dialog.setContentView(R.layout.time_picker_dialog)
//        dialog.setTitle("Smart Home Alarm")
//
//        val timePicker: TimePicker = dialog.findViewById(R.id.timePicker)
//        val adaptiveLight: SwitchMaterial = dialog.findViewById(R.id.specialFunctionSwitch)
//        val saveButton: Button = dialog.findViewById(R.id.alarm_save_button)
//        timePicker.setIs24HourView(true)
//
//        val calendar = Calendar.getInstance()
//        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
//        val currentMinute = calendar.get(Calendar.MINUTE)
//
//        timePicker.hour = currentHour
//        timePicker.minute = currentMinute
//
//        saveButton.setOnClickListener {
//            val selectedHour = timePicker.hour
//            val selectedMinute = timePicker.minute
//            val isAdaptiveLight = adaptiveLight.isChecked
//
//            // TO DO: Implement adapiptive light for alarm
//
//            setAlarm(selectedHour, selectedMinute)
//
//            dialog.dismiss()
//        }
//
//        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
//        dialog.window?.setBackgroundDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.rounded_background))
//
//        dialog.show()
//    }
//
//
//
//    private fun setAlarm(hour: Int, minute: Int) {
//
//        val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
//            putExtra(AlarmClock.EXTRA_HOUR, hour)
//            putExtra(AlarmClock.EXTRA_MINUTES, minute)
//            putExtra(AlarmClock.EXTRA_MESSAGE, "Smart-Home Alarm")
//        }
//
//        val packageManager = requireContext().packageManager
//
//        if (intent.resolveActivity(packageManager) != null) {
//            startActivity(intent)
//        } else {
//            Toast.makeText(requireContext(), "No compatible alarm app found!", Toast.LENGTH_SHORT).show()
//        }
//    }
//
//
//
//
//
//
//    override fun onDestroyView() {
//        super.onDestroyView()
//        // Detach the listener to avoid memory leaks
//        widgetListener?.remove()
//    }
//}



package app.smarthomeapp

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.provider.AlarmClock
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TimePicker
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.switchmaterial.SwitchMaterial
import java.util.Calendar
import app.smarthomeapp.viewmodels.ScenariosViewModel

class ScenariosFragment : Fragment() {

    private lateinit var viewModel: ScenariosViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_routines, container, false)

        viewModel = ViewModelProvider(this)[ScenariosViewModel::class.java]

        val setAlarmButton = view.findViewById<View>(R.id.set_alarm_button)
        setAlarmButton.setOnClickListener {
            showTimePickerDialog()
        }

        observeViewModel()

        // when I click on the button, I want to update my home location in the database

        val settingsButton = view.findViewById<View>(R.id.settings_button_routines)
        settingsButton.setOnClickListener {
            val intent = Intent(activity, SettingsActivity::class.java)
            startActivity(intent)
        }

        // make read the list with routines from the database
        val addNewRoutineButton = view.findViewById<ImageButton>(R.id.add_button_routines)
        addNewRoutineButton.setOnClickListener {

                val intent = Intent(activity, AddNewRoutineActivity::class.java)
                startActivity(intent)

        }

        return view
    }

    private fun observeViewModel() {
        viewModel.alarmSetMessage.observe(viewLifecycleOwner) { message ->
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun showTimePickerDialog() {
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.time_picker_dialog)
        dialog.setTitle("Smart Home Alarm")

        val timePicker: TimePicker = dialog.findViewById(R.id.timePicker)
        val adaptiveLight: SwitchMaterial = dialog.findViewById(R.id.specialFunctionSwitch)
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

            viewModel.setAlarm(requireContext(), selectedHour, selectedMinute, isAdaptiveLight)
            dialog.dismiss()
        }

        dialog.show()


    }
}


