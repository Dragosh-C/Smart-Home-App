package app.smarthomeapp.mainpage

import android.app.Dialog
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.widget.SwitchCompat
import androidx.core.view.children
import androidx.fragment.app.Fragment
import app.smarthomeapp.GraphicsActivity
import app.smarthomeapp.R
import app.smarthomeapp.SettingsActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

data class Widget(
    val name: String = "",
    val port: String = "",
    val type: String = "",
    var isEnabled: Boolean = false
)

class HomeFragment : Fragment() {

    private lateinit var gridLayout: GridLayout
    private lateinit var editButton: Button
    private val widgetViews = mutableMapOf<String, SwitchCompat>()
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private var isEditMode = false
    private lateinit var addRoom: ImageButton
    private lateinit var roomTabsLayout: LinearLayout

    private lateinit var temperatureText: TextView
    private lateinit var humidityText: TextView
    private lateinit var lightText: TextView
    private lateinit var powerText: TextView
    private lateinit var airQualityText: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        temperatureText = view.findViewById(R.id.temperature_text)
        humidityText = view.findViewById(R.id.humidity_text)
        lightText = view.findViewById(R.id.lighting_text)
        powerText = view.findViewById(R.id.power_text)

        gridLayout = view.findViewById(R.id.grid_layout)
        editButton = view.findViewById(R.id.exit_edit_mode_button)

        val addButton = view.findViewById<LinearLayout>(R.id.add_widget_button)
        addButton.setOnClickListener {

            DialogHelper.showDataSelectionDialog(
                context = requireContext(),
                existingWidget = null,
                isEditMode = isEditMode,
                toggleEditMode = { toggleEditMode() },
                addNewWidget = { widgetName, port, type -> addNewWidget(widgetName, port, type) },
                updateWidget = { widget -> updateWidget(widget) },
                editButton = editButton
            )
        }

        roomTabsLayout = view.findViewById(R.id.room_tabs)
        buttonListener(view)

        val widgetLoader = RealtimeDatabaseListener(this@HomeFragment.requireContext())
        widgetLoader.loadWidgetsFromFirebase(
            requireContext(),
            db,
            auth,
            gridLayout,
            isEditMode,
            widgetViews
        )
        loadButtonsFromFirebase()
    }

    private fun buttonListener(view: View) {
        addRoom = view.findViewById(R.id.add_button)
        addRoom.setOnClickListener {
            showAddButtonDialog()
        }
        val settingsButton: ImageButton = view.findViewById(R.id.settings_button_home)
        settingsButton.setOnClickListener {
            val intent = Intent(requireContext(), SettingsActivity::class.java)
            startActivity(intent)
        }

        val temperatureButton: LinearLayout = view.findViewById(R.id.temperature_button)
        temperatureButton.setOnClickListener {
            navigateToGraphicsActivity("Temperature", "Day")
        }

        val humidityButton: LinearLayout = view.findViewById(R.id.humidity_button)
        humidityButton.setOnClickListener {
            navigateToGraphicsActivity("Humidity", "Day")
        }

        val lightButton: LinearLayout = view.findViewById(R.id.lighting_button)
        lightButton.setOnClickListener {
            navigateToGraphicsActivity("Luminosity", "Day")
        }

        val powerButton: LinearLayout = view.findViewById(R.id.power_button)
        powerButton.setOnClickListener {
            navigateToGraphicsActivity("Power Usage", "Day")
        }
    }

    private fun navigateToGraphicsActivity(metric: String, timeRange: String) {
        val intent = Intent(requireContext(), GraphicsActivity::class.java).apply {
            putExtra("metric", metric)
            putExtra("timeRange", timeRange)
        }
        startActivity(intent)
    }


    private fun loadButtonsFromFirebase() {
        // load the buttons from firebase and read the isSelected status
        db.collection("users").document(auth.currentUser!!.uid).collection("rooms").get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val room = document.data
                    val roomName = room["name"] as String
                    val isSelected = room["isSelected"] as Boolean

                    // read the box_id from the database
                    val boxId = room["box_id"] as String
                    addRoom(roomName, boxId, isSelected)
                }
            }
    }

    private var selectedBoxId: String? = null

    private fun showAddButtonDialog() {
        val builder = Dialog(requireContext())
        builder.setContentView(R.layout.dialog_add_room)

        val roomNameInputField =
            builder.findViewById<EditText>(R.id.add_room_input)
        val boxIdInputField = builder.findViewById<EditText>(R.id.add_box_id_input)
        val saveButton = builder.findViewById<Button>(R.id.save_room_button)

        val gradientDrawable = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 30f
            setColor(Color.parseColor("#1c1d23"))
        }
        builder.window?.setBackgroundDrawable(gradientDrawable)

        saveButton.setOnClickListener {
            val roomName = roomNameInputField.text.toString()
            val boxId = boxIdInputField.text.toString()

            if (roomName.isNotBlank() && boxId.isNotBlank()) {
                addRoom(roomName, boxId)
                builder.dismiss()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Please enter both room name and box ID",
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

    private fun addRoom(buttonName: String, boxId: String, isSelected: Boolean = false) {
        val roundedBackground = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 100f
            if (isSelected) {

                setColor(Color.parseColor("#8A67D1"))
                selectedBoxId = boxId

                val realtimeListener = RealtimeDatabaseListener(this@HomeFragment.requireContext())

                realtimeListener.listenToRealtimeUpdates(
                    boxId = boxId,
                    selectedBoxId = selectedBoxId!!,
                    temperatureText = temperatureText,
                    humidityText = humidityText,
                    lightText = lightText,
                    powerText = powerText
                )
            } else {
                setColor(Color.parseColor("#30B0B0C4"))
            }
        }

        val button = Button(requireContext()).apply {
            text = buttonName
            setTextColor(Color.parseColor("#FFFFFF"))
            background = roundedBackground
            layoutParams = GridLayout.LayoutParams().apply {
                width = GridLayout.LayoutParams.WRAP_CONTENT
                height = 120
                setPadding(30, 0, 30, 0)
                setMargins(10, 0, 20, 0)
            }
        }

        button.setOnClickListener {
            val newColor = Color.parseColor("#8A67D1")
            (button.background as GradientDrawable).setColor(newColor)

            val selectedRoomData = mapOf(
                "name" to buttonName,
                "box_id" to boxId,
                "isSelected" to true
            )
            selectedBoxId = boxId
            val realtimeListener = RealtimeDatabaseListener(this@HomeFragment.requireContext())

            realtimeListener.listenToRealtimeUpdates(
                boxId = boxId,
                selectedBoxId = selectedBoxId!!,
                temperatureText = temperatureText,
                humidityText = humidityText,
                lightText = lightText,
                powerText = powerText
            )

            db.collection("users")
                .document(auth.currentUser!!.uid)
                .collection("rooms")
                .document(buttonName)
                .set(selectedRoomData)


            roomTabsLayout.children.forEach {
                if (it is Button && it != button) {
                    (it.background as GradientDrawable).setColor(Color.parseColor("#30B0B0C4"))

                    val otherRoomName = it.text.toString()
                    db.collection("users")
                        .document(auth.currentUser!!.uid)
                        .collection("rooms")
                        .document(otherRoomName)
                        .update("isSelected", false)
                }
            }

            gridLayout.removeAllViews()

            val widgetLoader = RealtimeDatabaseListener(this@HomeFragment.requireContext())
            widgetLoader.loadWidgetsFromFirebase(
                requireContext(),
                db,
                auth,
                gridLayout,
                isEditMode,
                widgetViews
            )
        }

        roomTabsLayout.addView(button)
    }

    private fun toggleEditMode() {
        isEditMode = !isEditMode
        gridLayout.removeAllViews()
        val widgetLoader = RealtimeDatabaseListener(this@HomeFragment.requireContext())
        widgetLoader.loadWidgetsFromFirebase(
            requireContext(),
            db,
            auth,
            gridLayout,
            isEditMode,
            widgetViews
        )
    }


    private fun addNewWidget(name: String, port: String, type: String) {
        val widget = Widget(name, port, type, false)
        db.collection("users").document(auth.currentUser!!.uid).collection("rooms").get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val room = document.data
                    val roomName = room["name"] as String
                    val isSelected = room["isSelected"] as Boolean
                    if (isSelected) {
                        db.collection("users").document(auth.currentUser!!.uid).collection("rooms")
                            .document(roomName).collection("widgets").document(name).set(widget)
                            .addOnSuccessListener {
                                Toast.makeText(
                                    requireContext(),
                                    "Widget added to Firebase",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            .addOnFailureListener {
                                Toast.makeText(
                                    requireContext(),
                                    "Failed to add widget",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        addWidgetToGridLayout(requireContext(), widget, gridLayout, db, auth, isEditMode, widgetViews)

                    }
                }
            }
    }

    private fun updateWidget(widget: Widget) {
        db.collection("users").document(auth.currentUser!!.uid).collection("rooms").get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val room = document.data
                    val roomName = room["name"] as String
                    val isSelected = room["isSelected"] as Boolean
                    if (isSelected) {
                        db.collection("users").document(auth.currentUser!!.uid).collection("rooms")
                            .document(roomName).collection("widgets").document(widget.name)
                            .set(widget)
                            .addOnSuccessListener {
                                Toast.makeText(
                                    requireContext(),
                                    "Widget updated",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            .addOnFailureListener {
                                Toast.makeText(
                                    requireContext(),
                                    "Failed to update widget",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        gridLayout.removeAllViews()

                        val widgetLoader = RealtimeDatabaseListener(requireContext())
                        widgetLoader.loadWidgetsFromFirebase(
                            requireContext(),
                            db,
                            auth,
                            gridLayout,
                            isEditMode,
                            widgetViews
                        )
                    }
                }
            }
    }
}
