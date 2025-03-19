package app.smarthomeapp

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

object FirebaseUtils {
    val databaseRef: DatabaseReference by lazy {
        FirebaseDatabase.getInstance("https://smart-home-app-7c709-default-rtdb.europe-west1.firebasedatabase.app")
            .reference
    }
}
