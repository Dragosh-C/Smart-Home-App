package app.smarthomeapp

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import app.smarthomeapp.viewmodels.ScenariosViewModel

class AddNewRoutineActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_routine)

        val ifButton = findViewById<LinearLayout>(R.id.if_button)
        ifButton.setOnClickListener {
             val intent = Intent(this, AddNewRoutineIfActivity::class.java)
        startActivity(intent)
        }
        val thenButton = findViewById<LinearLayout>(R.id.then_button)
        thenButton.setOnClickListener {
            val intent = Intent(this, AddNewRoutineThenActivity::class.java)
            startActivity(intent)
        }

    }
}