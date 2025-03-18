package app.smarthomeapp.routinespage

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.children
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import app.smarthomeapp.R
import kotlinx.coroutines.launch

class AddNewRoutineActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var widgetAdapter: WidgetAdapter
    private lateinit var widgetDatabase: WidgetDatabase
    private lateinit var widgetTitle: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_routine)


        val ifContainer = findViewById<LinearLayout>(R.id.if_container)
        val thenContainer = findViewById<LinearLayout>(R.id.then_container)

        val ifButton = findViewById<LinearLayout>(R.id.if_button)
        ifButton.setOnClickListener {
            val intent = Intent(this, AddNewRoutineIfActivity::class.java)
            startActivityForResult(intent, REQUEST_CODE_IF)
        }

        val thenButton = findViewById<LinearLayout>(R.id.then_button)
        thenButton.setOnClickListener {
            val intent = Intent(this, AddNewRoutineThenActivity::class.java)
            startActivityForResult(intent, REQUEST_CODE_THEN)
        }

        val cancelButton = findViewById<Button>(R.id.cancel_button)
        cancelButton.setOnClickListener {
            finish()
        }

        val saveButton = findViewById<Button>(R.id.save_button)
        saveButton.setOnClickListener {

            // save the info to the database
            lifecycleScope.launch {
                val ifCondition = ifContainer.children
                    .filterIsInstance<TextView>()
                    .joinToString(", ") { it.text.toString() }

                val thenAction = thenContainer.children
                    .filterIsInstance<TextView>()
                    .joinToString(", ") { it.text.toString() }



                val widget = Widget1(
                    title = widgetTitle,
                    type = "$ifCondition -> $thenAction"
                )

                // Save widget to db
                lifecycleScope.launch {
                    widgetDatabase = WidgetDatabase.getDatabase(this@AddNewRoutineActivity)
                    widgetDatabase.widgetDao().insertWidget(widget)

                    finish()
                }

            }

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK && data != null) {
            when (requestCode) {
                REQUEST_CODE_IF -> {
                    val condition = data.getStringExtra("selected_condition")
                    addConditionView(condition, findViewById(R.id.if_container))
                }
                REQUEST_CODE_THEN -> {
                    val action = data.getStringExtra("selected_action")
                    widgetTitle = data.getStringExtra("title").toString()
                    addConditionView(action, findViewById(R.id.then_container))
                }
            }
        }
    }

    private fun addConditionView(text: String?, container: LinearLayout) {
        text?.let {
            val textView = TextView(this).apply {
                this.text = it
                setTextColor(getColor(android.R.color.white))
                textSize = 16f
                setPadding(8, 8, 8, 8)
            }
            container.addView(textView)
        }
    }

    companion object {
        private const val REQUEST_CODE_IF = 1
        private const val REQUEST_CODE_THEN = 2
    }
}
