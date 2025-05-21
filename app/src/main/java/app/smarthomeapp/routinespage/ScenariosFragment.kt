package app.smarthomeapp.routinespage

import FirebaseHelper
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.smarthomeapp.R
import app.smarthomeapp.SettingsActivity
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

        observeViewModel()

        //read last_id from firebase

        val firebaseHelper = FirebaseHelper()
        firebaseHelper.getLastIdFromFirebase { lastId ->
           lastIdvalue = lastId
        }

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
        widgetAdapter = WidgetAdapter(emptyList()) { widget ->
            val intent = Intent(requireContext(), WidgetDetailsActivity::class.java).apply {
                putExtra("widget_id", widget.id)
                putExtra("widget_title", widget.title)
                putExtra("widget_type", widget.type)
            }

            startActivity(intent)
        }



        recyclerView.adapter = widgetAdapter

        widgetDatabase = WidgetDatabase.getDatabase(requireContext())


        widgetDatabase.widgetDao().getAllWidgets().observe(viewLifecycleOwner) { widgets ->
                widgetAdapter.updateData(widgets)
        }

        firebaseHelper.listenToEnabledChanges(widgetDatabase.widgetDao(), viewLifecycleOwner.lifecycleScope)

        return view
    }

    private fun observeViewModel() {
        viewModel.alarmSetMessage.observe(viewLifecycleOwner) { message ->
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }
    }
}


