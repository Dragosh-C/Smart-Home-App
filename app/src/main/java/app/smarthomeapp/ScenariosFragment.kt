////package app.smarthomeapp
////
////import android.os.Bundle
////import android.view.LayoutInflater
////import android.view.View
////import android.view.ViewGroup
////import androidx.fragment.app.Fragment
////
////class ScenariosFragment : Fragment() {
////
////    override fun onCreateView(
////        inflater: LayoutInflater, container: ViewGroup?,
////        savedInstanceState: Bundle?
////    ): View? {
////
////        return inflater.inflate(R.layout.fragment_routines, container, false)
////    }
////}
//



package app.smarthomeapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

data class Widget2(
    val id: String = "",
    val name: String = "",
    val color: String = "",
    val size: Int = 0
)

class ScenariosFragment : Fragment() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var widgetsRecyclerView: RecyclerView
    private lateinit var widgetAdapter: WidgetAdapter
    private lateinit var widgetList: MutableList<Widget2>
    private var widgetListener: ListenerRegistration? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_routines, container, false)

        // Initialize Firebase Auth and Firestore
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Initialize RecyclerView and Adapter
        widgetsRecyclerView = view.findViewById(R.id.widgetsRecyclerView)
        widgetsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        widgetList = mutableListOf()
        widgetAdapter = WidgetAdapter(widgetList)
        widgetsRecyclerView.adapter = widgetAdapter

        // Retrieve and display widgets with real-time updates
        fetchWidgets()

        // Set up save button to add a new widget
        val inputField = view.findViewById<EditText>(R.id.name_input)
        val button = view.findViewById<View>(R.id.save_button)

        button.setOnClickListener {
            val name = inputField.text.toString()
            if (name.isNotEmpty()) {
                val newWidget = Widget2(
                    id = db.collection("users").document(auth.currentUser!!.uid)
                        .collection("widgets").document().id,
                    name = name,
                    color = "Green",  // You can add more input fields for these properties
                    size = 7
                )
                saveWidget(newWidget)
            } else {
                Toast.makeText(context, "Please enter a widget name", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }

    // Function to save a widget for the current user
    private fun saveWidget(widget: Widget2) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            db.collection("users").document(userId)
                .collection("widgets").document(widget.id)
                .set(widget)
                .addOnSuccessListener {
                    Toast.makeText(context, "Widget added successfully!", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Error adding widget: ${e.message}", Toast.LENGTH_SHORT)
                        .show()
                }
        } else {
            Toast.makeText(context, "No user is logged in", Toast.LENGTH_SHORT).show()
        }
    }

    // Function to retrieve widgets for the logged-in user with real-time updates
    private fun fetchWidgets() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            widgetListener = db.collection("users").document(userId)
                .collection("widgets")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Toast.makeText(
                            context,
                            "Error loading widgets: ${error.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@addSnapshotListener
                    }

                    if (snapshot != null) {
                        val currentWidgets = snapshot.documents.mapNotNull {
                            it.toObject(Widget2::class.java)
                        }

                        // Find new widgets to add (added since the last fetch)
                        val newWidgets = currentWidgets.filter { newWidget ->
                            !widgetList.any { it.id == newWidget.id }
                        }

                        // Find widgets to remove (removed since the last fetch)
                        val removedWidgets = widgetList.filter { oldWidget ->
                            !currentWidgets.any { it.id == oldWidget.id }
                        }

                        // Find updated widgets (modified since the last fetch)
                        val updatedWidgets = currentWidgets.filter { newWidget ->
                            widgetList.any { oldWidget ->
                                oldWidget.id == newWidget.id && oldWidget != newWidget
                            }
                        }

                        // Add new widgets to the list
                        widgetList.addAll(newWidgets)
                        // Notify adapter about the inserted new widgets
                        widgetAdapter.notifyItemRangeInserted(
                            widgetList.size - newWidgets.size,
                            newWidgets.size
                        )

                        // Remove widgets that no longer exist
                        widgetList.removeAll(removedWidgets)
                        // Notify adapter about the removed widgets
                        for (removedWidget in removedWidgets) {
                            val position = widgetList.indexOf(removedWidget)
                            if (position != -1) {
                                widgetAdapter.notifyItemRemoved(position)
                            }
                        }
                        // Update modified widgets in the list
                        for (updatedWidget in updatedWidgets) {
                            val position = widgetList.indexOfFirst { it.id == updatedWidget.id }
                            if (position != -1) {
                                widgetList[position] = updatedWidget
                                widgetAdapter.notifyItemChanged(position) // Notify the adapter that this item has changed
                            }
                        }
                    }
                }
        } else {
            Toast.makeText(context, "No user is logged in", Toast.LENGTH_SHORT).show()
        }
    }



    override fun onDestroyView() {
        super.onDestroyView()
        // Detach the listener to avoid memory leaks
        widgetListener?.remove()
    }
}
