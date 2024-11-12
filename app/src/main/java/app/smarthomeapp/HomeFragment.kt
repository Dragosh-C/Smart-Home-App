package app.smarthomeapp

import android.app.Dialog
import android.graphics.Typeface
import android.os.Bundle
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
import androidx.fragment.app.Fragment
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
//    private val database = FirebaseDatabase.getInstance("https://smart-home-app-7c709-default-rtdb.europe-west1.firebasedatabase.app/")
//    private val widgetsRef = database.getReference("widgets")
    private val widgetViews = mutableMapOf<String, SwitchCompat>()
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val view = inflater.inflate(R.layout.fragment_home, container, false)
        gridLayout = view.findViewById(R.id.grid_layout)

        val addButton = view.findViewById<LinearLayout>(R.id.add_widget_button)
        addButton.setOnClickListener {
            showDataSelectionDialog()
        }

        // Load widgets from Firebase when the view is created
        loadWidgetsFromFirebase()

        return view
    }

    private fun showDataSelectionDialog() {
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_select_data)

        val nameInput = dialog.findViewById<EditText>(R.id.name_input)
        val portSpinner = dialog.findViewById<Spinner>(R.id.port_spinner)
        val typeSpinner = dialog.findViewById<Spinner>(R.id.type_spinner)
        val saveButton = dialog.findViewById<Button>(R.id.save_button)

        val portOptions = (1..9).map { "Port $it" }
        val portAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, portOptions)
        portAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        portSpinner.adapter = portAdapter

        val typeOptions = listOf("Outlet", "Light", "Dimmer")
        val typeAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, typeOptions)
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        typeSpinner.adapter = typeAdapter

        saveButton.setOnClickListener {
            val widgetName = nameInput.text.toString()
            val selectedPort = portSpinner.selectedItem.toString()
            val selectedType = typeSpinner.selectedItem.toString()

            if (widgetName.isNotBlank()) {
                addNewWidget(widgetName, selectedPort, selectedType)
                dialog.dismiss()
            } else {
                Toast.makeText(requireContext(), "Please enter a name", Toast.LENGTH_SHORT).show()
            }
        }

        val layoutParams = WindowManager.LayoutParams()
        layoutParams.copyFrom(dialog.window?.attributes)
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT
        dialog.window?.attributes = layoutParams

        dialog.show()
    }

    private fun addNewWidget(name: String, port: String, type: String) {
        val widget = Widget(name, port, type, false)
//        widgetsRef.child(name).setValue(widget)

        //

        // add the widget to the curent user

        db.collection("users").document(auth.currentUser!!.uid).collection("widgets").document(name).set(widget)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Widget added to Firebase", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to add widget", Toast.LENGTH_SHORT).show()
            }

        addWidgetToGridLayout(widget)
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
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = 8 }
            thumbTintList = ContextCompat.getColorStateList(requireContext(), androidx.cardview.R.color.cardview_dark_background)
            trackTintList = ContextCompat.getColorStateList(requireContext(), R.color.bottom_nav_item_color)
            setPadding(10, 20, 0, 20)

//            setOnCheckedChangeListener { buttonView, isChecked ->
//                val widgetName = buttonView.tag as String
////                widgetsRef.child(widgetName).child("isEnabled").setValue(isChecked)
//            }
        }

        widgetViews[widget.name] = toggle // Store toggle reference by name

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

        innerLayout.addView(title)
        innerLayout.addView(iconAndTextLayout)

        cardView.addView(innerLayout)
        gridLayout.addView(cardView)
    }

    private fun loadWidgetsFromFirebase() {

        db.collection("users").document(auth.currentUser!!.uid).collection("widgets").get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val widget = document.toObject(Widget::class.java)
                    addWidgetToGridLayout(widget)
                    modifyOnChangeWidget(widget)
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(requireContext(), "Failed to load widgets: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }


    // TO DO: Also implement update realtime database when Changing the state (or only update RT database)
    private fun modifyOnChangeWidget(widget: Widget) {
        val toggle = widgetViews[widget.name] ?: return


        toggle.setOnCheckedChangeListener(null)
        toggle.isChecked = widget.isEnabled

        // Re-attach the listener to handle state changes
        toggle.setOnCheckedChangeListener { _, newCheckedState ->
            // Update the widget's isEnabled field in Firebase when toggled
            db.collection("users").document(auth.currentUser!!.uid).collection("widgets").document(widget.name)
                .update("isEnabled", newCheckedState)
                .addOnSuccessListener {

                    Log.d("Widget", "Widget state updated in Firebase: ${widget.name} = $newCheckedState")
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(requireContext(), "Failed to update widget state: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
            widget.isEnabled = newCheckedState
        }
    }


}
