package app.smarthomeapp

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.widget.SwitchCompat
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
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
            showDataSelectionDialog()
        }

        roomTabsLayout = view.findViewById(R.id.room_tabs)
        addRoom = view.findViewById(R.id.add_button)
        addRoom.setOnClickListener {
            showAddButtonDialog()
        }

        val temperatureButton: LinearLayout = view.findViewById(R.id.temperature_button)
        temperatureButton.setOnClickListener {

            Toast.makeText(requireContext(), "Temperature button clicked", Toast.LENGTH_SHORT).show()
            // new view with graphics
            val intent = Intent(requireContext(), GraphicsActivity::class.java)
            intent.putExtra("metric", "Temperature")
            intent.putExtra("timeRange", "Day")
            startActivity(intent)
        }

        val humidityButton: LinearLayout = view.findViewById(R.id.humidity_button)

        humidityButton.setOnClickListener {

            Toast.makeText(requireContext(), "Humidity button clicked", Toast.LENGTH_SHORT).show()
            val intent = Intent(requireContext(), GraphicsActivity::class.java)
            intent.putExtra("metric", "Humidity")
            intent.putExtra("timeRange", "Day")
            startActivity(intent)
        }

        val lightButton: LinearLayout = view.findViewById(R.id.lighting_button)

        lightButton.setOnClickListener {

            Toast.makeText(requireContext(), "Light button clicked", Toast.LENGTH_SHORT).show()
            val intent = Intent(requireContext(), GraphicsActivity::class.java)
            intent.putExtra("metric", "Luminosity")
            intent.putExtra("timeRange", "Day")
            startActivity(intent)
        }

        val powerButton: LinearLayout = view.findViewById(R.id.power_button)

        powerButton.setOnClickListener {
            // Your logic here
            Toast.makeText(requireContext(), "Power button clicked", Toast.LENGTH_SHORT).show()
            val intent = Intent(requireContext(), GraphicsActivity::class.java)
            intent.putExtra("metric", "Power Usage")
            intent.putExtra("timeRange", "Day")
            startActivity(intent)
        }

        // Load widgets from Firebase when the view is created
        loadWidgetsFromFirebase()
        loadButtonsFromFirebase()
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

    private var selectedBoxId: String? = null  // Store selected room's box_id

    private fun listenToRealtimeUpdates(boxId: String, roomId: String) {
        // Only listen if this boxId matches the selected boxId
        if (selectedBoxId == boxId) {
            val databaseReference = FirebaseDatabase.getInstance("https://smart-home-app-7c709-default-rtdb.europe-west1.firebasedatabase.app/").reference

            databaseReference.child("box_id").child(boxId).child("temperature").addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val newTemperature = snapshot.getValue(Int::class.java) ?: 0
                    // Update your UI with the new temperature value
                    temperatureText.text = "$newTemperature °C"
                    Log.d("RealtimeDatabase", "Temperature updated for selected boxId: $newTemperature")
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("RealtimeDatabase", "Error listening for temperature updates: ${error.message}")
                }
            })
        } else {
            Log.d("RealtimeDatabase", "Skipping update for non-selected room (boxId: $boxId).")
        }
    }

    private fun showAddButtonDialog() {
        val builder = Dialog(requireContext())
        builder.setContentView(R.layout.dialog_add_room)

        val roomNameInputField = builder.findViewById<EditText>(R.id.add_room_input)  // For room name
        val boxIdInputField = builder.findViewById<EditText>(R.id.add_box_id_input)  // For box ID
        val saveButton = builder.findViewById<Button>(R.id.save_room_button)

        // Set background color for the dialog
        val gradientDrawable = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 30f
            setColor(Color.parseColor("#1c1d23"))
        }
        builder.window?.setBackgroundDrawable(gradientDrawable)

        saveButton.setOnClickListener {
            val roomName = roomNameInputField.text.toString()
            val boxId = boxIdInputField.text.toString()

            // Check if both fields are not blank
            if (roomName.isNotBlank() && boxId.isNotBlank()) {
                addRoom(roomName, boxId)  // Pass both room name and box ID to addRoom
                builder.dismiss()
            } else {
                Toast.makeText(requireContext(), "Please enter both room name and box ID", Toast.LENGTH_SHORT).show()
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

                setColor(Color.parseColor("#8A67D1"))  // Selected color
                selectedBoxId = boxId  // Store the selected room's box_id
                listenToRealtimeUpdates(boxId, buttonName)
            } else {
                setColor(Color.parseColor("#30B0B0C4")) // Default background color
            }
        }

        val button = Button(requireContext()).apply {
            text = buttonName
            setTextColor(Color.parseColor("#FFFFFF"))
            background = roundedBackground
            layoutParams = GridLayout.LayoutParams().apply {
                width = GridLayout.LayoutParams.WRAP_CONTENT
                height = 120  // Set fixed height to maintain consistency
                setPadding(30, 0, 30, 0)
                setMargins(10, 0, 20, 0)
            }
        }

        button.setOnClickListener {
            val newColor = Color.parseColor("#8A67D1")  // Selected color
            (button.background as GradientDrawable).setColor(newColor)

            // Mark the current room as selected in Firebase
            val selectedRoomData = mapOf(
                "name" to buttonName,
                "box_id" to boxId,  // Keep its unique box_id
                "isSelected" to true
            )
            selectedBoxId = boxId  // Store selected room's box_id
            listenToRealtimeUpdates(boxId, buttonName)

            db.collection("users")
                .document(auth.currentUser!!.uid)
                .collection("rooms")
                .document(buttonName)
                .set(selectedRoomData)

            // Unselect all other buttons in the UI and update only their isSelected status
            roomTabsLayout.children.forEach {
                if (it is Button && it != button) {
                    (it.background as GradientDrawable).setColor(Color.parseColor("#30B0B0C4"))

                    // Update the "isSelected" status in Firebase, but keep their original box_id
                    val otherRoomName = it.text.toString()
                    db.collection("users")
                        .document(auth.currentUser!!.uid)
                        .collection("rooms")
                        .document(otherRoomName)
                        .update("isSelected", false)
                }
            }

            // Clear the grid and reload widgets for the newly selected room
            gridLayout.removeAllViews()
            loadWidgetsFromFirebase()
        }

        roomTabsLayout.addView(button)
        // Listen for real-time updates for the selected room

    }

    private fun showDataSelectionDialog(existingWidget: Widget? = null) {
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_select_data)

        val nameInput = dialog.findViewById<EditText>(R.id.name_input)
        val portSpinner = dialog.findViewById<Spinner>(R.id.port_spinner)
        val typeSpinner = dialog.findViewById<Spinner>(R.id.type_spinner)
        val saveButton = dialog.findViewById<Button>(R.id.save_button)
        val modifyButton = dialog.findViewById<Button>(R.id.modify_button)
        val portOptions = (1..9).map { "Port $it" }
        val portAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, portOptions)
        portAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        portSpinner.adapter = portAdapter

        val typeOptions = listOf("Outlet", "Light", "Dimmer")
        val typeAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, typeOptions)
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        typeSpinner.adapter = typeAdapter

        existingWidget?.let {
            nameInput.setText(it.name)
            portSpinner.setSelection(portOptions.indexOf(it.port))
            typeSpinner.setSelection(typeOptions.indexOf(it.type))
        }

        saveButton.setOnClickListener {
            val widgetName = nameInput.text.toString()
            val selectedPort = portSpinner.selectedItem.toString()
            val selectedType = typeSpinner.selectedItem.toString()

            if (widgetName.isNotBlank()) {
                if (existingWidget == null) {
                    addNewWidget(widgetName, selectedPort, selectedType)
                } else {
                    updateWidget(existingWidget.copy(name = widgetName, port = selectedPort, type = selectedType))
                }
                dialog.dismiss()
            } else {
                Toast.makeText(requireContext(), "Please enter a name", Toast.LENGTH_SHORT).show()
            }
        }

        modifyButton.setOnClickListener {
            toggleEditMode()
            dialog.dismiss()
            if (isEditMode) {
                editButton.visibility = View.VISIBLE
                editButton.setOnClickListener {
                    toggleEditMode()
                    editButton.visibility = View.GONE
                }
            }
        }

        val gradientDrawable = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 30f  // Adjust this for the rounded effect
            setColor(Color.parseColor("#1c1d23")) // Default background color
        }

        val displayMetrics = DisplayMetrics()
        val windowMetrics = (context?.getSystemService(Context.WINDOW_SERVICE) as WindowManager).currentWindowMetrics
        val bounds = windowMetrics.bounds

        displayMetrics.widthPixels = bounds.width()
        displayMetrics.heightPixels = bounds.height()

        val screenWidth = displayMetrics.widthPixels
        val screenHeight = displayMetrics.heightPixels
        val halfScreenHeight = screenHeight / 1.5

        dialog.window?.setBackgroundDrawable(gradientDrawable)

        val layoutParams = WindowManager.LayoutParams()
        layoutParams.copyFrom(dialog.window?.attributes)
        layoutParams.width = screenWidth
        layoutParams.height = halfScreenHeight.toInt()
        layoutParams.gravity = Gravity.BOTTOM

        dialog.window?.attributes = layoutParams
        dialog.show()
    }

    private fun toggleEditMode() {
        isEditMode = !isEditMode
        gridLayout.removeAllViews()
        loadWidgetsFromFirebase()
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
                        db.collection("users").document(auth.currentUser!!.uid).collection("rooms").document(roomName).collection("widgets").document(name).set(widget)
                            .addOnSuccessListener {
                                Toast.makeText(requireContext(), "Widget added to Firebase", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener {
                                Toast.makeText(requireContext(), "Failed to add widget", Toast.LENGTH_SHORT).show()
                            }
                        addWidgetToGridLayout(widget)
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
                        db.collection("users").document(auth.currentUser!!.uid).collection("rooms").document(roomName).collection("widgets").document(widget.name).set(widget)
                            .addOnSuccessListener {
                                Toast.makeText(requireContext(), "Widget updated", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener {
                                Toast.makeText(requireContext(), "Failed to update widget", Toast.LENGTH_SHORT).show()
                            }
                        gridLayout.removeAllViews()
                        loadWidgetsFromFirebase()
                    }
                }
            }
    }

    private fun addWidgetToGridLayout(widget: Widget) {
        val cardView = CardView(requireContext()).apply {
            setBackgroundResource(R.drawable.rounded_background)
        }

        val params = GridLayout.LayoutParams().apply {
            width = 0
            height = GridLayout.LayoutParams.WRAP_CONTENT
            columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
            setMargins(16, 16, 16, 16)
        }
        cardView.layoutParams = params

        val innerLayout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16, 16, 16, 16)
            setBackgroundResource(R.drawable.rounded_background)
        }

        val title = TextView(requireContext()).apply {
            text = widget.name.replaceFirstChar { it.uppercase() }
            textSize = 18f
            setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
            typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
            setPadding(30, 0, 0, 0)
        }

        val portText = TextView(requireContext()).apply {
            text = widget.port
            textSize = 12f
            setTextColor(ContextCompat.getColor(requireContext(), android.R.color.darker_gray))
            typeface = Typeface.create("sans-serif", Typeface.NORMAL)
            setPadding(30, 0, 0, 0)
        }

        val typeText = TextView(requireContext()).apply {
            text = widget.type
            textSize = 12f
            setTextColor(ContextCompat.getColor(requireContext(), android.R.color.darker_gray))
            typeface = Typeface.create("sans-serif", Typeface.NORMAL)
            setPadding(30, 0, 0, 0)
        }

        val iconAndTextLayout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.START
            setPadding(0, 20, 20, 0)
        }

        val textLayout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            weightSum = 1f
            setPadding(0, 0, 20, 0)
        }

        val toggle = SwitchCompat(requireContext()).apply {
            isChecked = widget.isEnabled
            tag = widget.name // Use the widget's name as a unique identifier
            setOnCheckedChangeListener { _, newCheckedState ->
                db.collection("users").document(auth.currentUser!!.uid).collection("widgets").document(widget.name)
                    .update("isEnabled", newCheckedState)
                    .addOnSuccessListener {
                        Log.d("Widget", "Widget state updated: ${widget.name} = $newCheckedState")
                    }
                widget.isEnabled = newCheckedState
            }
        }

        widgetViews[widget.name] = toggle
        textLayout.addView(portText)
        textLayout.addView(typeText)
        textLayout.addView(toggle)

        val icon = ImageView(requireContext()).apply {
            setImageResource(
                when (widget.type) {
                    "Outlet" -> R.drawable.ic_outlet
                    "Dimmer" -> R.drawable.ic_dimmer
                    "Light" -> R.drawable.ic_light
                    else -> R.drawable.ic_add
                }
            )
            layoutParams = LinearLayout.LayoutParams(172, 172).apply {
                marginStart = 26
                gravity = Gravity.CENTER_VERTICAL
            }
            clipToOutline = true
            outlineProvider = ViewOutlineProvider.BACKGROUND
        }

        iconAndTextLayout.addView(textLayout)
        iconAndTextLayout.addView(icon)

        if (isEditMode) {
            val deleteButton = Button(requireContext()).apply {
                text = "X"
                setOnClickListener {
                    db.collection("users").document(auth.currentUser!!.uid).collection("rooms").get()
                        .addOnSuccessListener { result ->
                            for (document in result) {
                                val room = document.data
                                val roomName = room["name"] as String
                                val isSelected = room["isSelected"] as Boolean
                                if (isSelected) {
                                    db.collection("users").document(auth.currentUser!!.uid).collection("rooms").document(roomName).collection("widgets").document(widget.name).delete()
                                        .addOnSuccessListener {
                                            Toast.makeText(requireContext(), "Widget deleted", Toast.LENGTH_SHORT).show()
                                            gridLayout.removeView(cardView)
                                        }
                                        .addOnFailureListener {
                                            Toast.makeText(requireContext(), "Failed to delete widget", Toast.LENGTH_SHORT).show()
                                        }
                                }
                            }
                        }
                }
            }
            innerLayout.addView(deleteButton)
        }

        innerLayout.addView(title)
        innerLayout.addView(iconAndTextLayout)
        cardView.addView(innerLayout)
        gridLayout.addView(cardView)
    }

    private fun loadWidgetsFromFirebase() {
        db.collection("users").document(auth.currentUser!!.uid).collection("rooms").get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val room = document.data
                    val roomName = room["name"] as String
                    val isSelected = room["isSelected"] as Boolean
                    if (isSelected) {
                        db.collection("users").document(auth.currentUser!!.uid).collection("rooms")
                            .document(roomName).collection("widgets").get()
                            .addOnSuccessListener { res ->
                                for (doc in res) {
                                    val widget = doc.toObject(Widget::class.java)
                                    addWidgetToGridLayout(widget)
                                }
                            }
                    }
                }
            }
    }
}
