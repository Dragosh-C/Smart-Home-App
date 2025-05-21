import android.util.Log
import app.smarthomeapp.routinespage.Widget1
import app.smarthomeapp.routinespage.WidgetDao
import app.smarthomeapp.routinespage.lastIdvalue
import com.google.android.gms.tasks.Task
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FirebaseHelper {
    // Initialize Firebase Database reference
    private val database = FirebaseDatabase.getInstance().getReference("routines")

    // Method to send widget data to Firebase
    fun addWidgetToDatabase(widget: Widget1, adaptiveLight: Boolean? = null) {
        // Generate a unique key for each widget (for example, using Firebase's push method)
        val widgetId = lastIdvalue.toString()
        database.child(lastIdvalue.toString()).setValue(widget)
            .addOnCompleteListener { task: Task<Void?> ->
                if (task.isSuccessful) {
                    // Data saved successfully
                    println("Widget data saved to Firebase")
                } else {
                    // Handle failure
                    println("Failed to save widget data: " + task.exception)
                }
            }
        if (adaptiveLight != null) {
            database.child(widgetId).child("adaptiveLight").setValue(adaptiveLight)
                .addOnCompleteListener { task: Task<Void?> ->
                    if (task.isSuccessful) {
                        // Data saved successfully
                        println("Adaptive light data saved to Firebase")
                    } else {
                        // Handle failure
                        println("Failed to save adaptive light data: " + task.exception)
                    }
                }
        }


        lastIdvalue ++
        // save id to the database
        database.child("last_id").setValue(lastIdvalue)
            .addOnCompleteListener { task: Task<Void?> ->
                if (task.isSuccessful) {
                    // Data saved successfully
                    println("Widget ID saved to Firebase")
                } else {
                    // Handle failure
                    println("Failed to save widget ID: " + task.exception)
                }
            }

    }

    // Method to get the last ID from Firebase
    fun getLastIdFromFirebase(onSuccess: (Int) -> Unit) {
        database.child("last_id").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val lastId = dataSnapshot.getValue(Int::class.java)
                if (lastId != null) {
                    onSuccess(lastId)
                } else {
                    onSuccess(0) // Default value if not found
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                println("Failed to read last ID: " + databaseError.message)
            }
        })
    }

    fun listenToEnabledChanges(widgetDao: WidgetDao, lifecycleScope: CoroutineScope) {
        database.addChildEventListener(object : ChildEventListener {
            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val id = snapshot.child("id").getValue(Int::class.java)
                val enabled = snapshot.child("enabled").getValue(Boolean::class.java)

                if (id != null && enabled != null) {
                    lifecycleScope.launch(Dispatchers.IO) {
                        val widget = widgetDao.getWidgetById(id)
                        if (widget != null) {
                            widget.isEnabled = enabled
                            widgetDao.updateWidget(widget)
                            Log.d("FirebaseHelper", "Updated widget $id enabled to $enabled")
                        }
                    }
                }
            }

            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildRemoved(snapshot: DataSnapshot) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseHelper", "Listener cancelled: ${error.message}")
            }
        })
    }

    fun enableWidget(widgetId: Int, enabled: Boolean) {
        database.child(widgetId.toString()).child("enabled").setValue(enabled)
    }


    fun deleteWidgetFromFirebase(widgetId: Int) {
        database.child(widgetId.toString()).removeValue()
            .addOnCompleteListener { task: Task<Void?> ->
                if (task.isSuccessful) {
                    // Data deleted successfully
                    println("Widget data deleted from Firebase")
                } else {
                    // Handle failure
                    println("Failed to delete widget data: " + task.exception)
                }
            }

    }
}