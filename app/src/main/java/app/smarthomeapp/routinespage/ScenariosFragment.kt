package app.smarthomeapp.routinespage

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TimePicker
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.smarthomeapp.R
import app.smarthomeapp.SettingsActivity
import com.google.android.material.switchmaterial.SwitchMaterial
import java.util.Calendar
import app.smarthomeapp.viewmodels.ScenariosViewModel


class ScenariosFragment : Fragment() {

    private lateinit var viewModel: ScenariosViewModel

    private lateinit var recyclerView: RecyclerView
    private lateinit var widgetAdapter: WidgetAdapter
    private lateinit var widgetDatabase: WidgetDatabase

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

        val settingsButton = view.findViewById<View>(R.id.settings_button_routines)
        settingsButton.setOnClickListener {
            val intent = Intent(activity, SettingsActivity::class.java)
            startActivity(intent)
        }

        val addNewRoutineButton = view.findViewById<ImageButton>(R.id.add_button_routines)
        addNewRoutineButton.setOnClickListener {

                val intent = Intent(activity, AddNewRoutineActivity::class.java)
                startActivity(intent)

        }
        Log.d("ScenariosFragment", "onCreateView called")

        // Initialize RecyclerView and Adapter
        recyclerView = view.findViewById(R.id.widgetRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        widgetAdapter = WidgetAdapter(emptyList())
        recyclerView.adapter = widgetAdapter


        widgetDatabase = WidgetDatabase.getDatabase(requireContext())

        // clear db
//        lifecycleScope.launch {
//            widgetDatabase.widgetDao().deleteAllWidgets()
//        }

        widgetDatabase.widgetDao().getAllWidgets().observe(viewLifecycleOwner) { widgets ->
                widgetAdapter.updateData(widgets)
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


