package app.smarthomeapp.mainpage

import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import app.smarthomeapp.FirebaseUtils
import app.smarthomeapp.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class WidgetDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {

        enableEdgeToEdge()

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_widget_detail_dimmer)

        val layout = findViewById<LinearLayout>(R.id.dimmer_layout)
        val gradientDrawable = ResourcesCompat.getDrawable(resources,
            R.drawable.background_gardient, null) as? GradientDrawable

        gradientDrawable?.setDither(true)
        layout.background = gradientDrawable



        val seekBar: SeekBar = findViewById(R.id.dimmer_seekbar)
        val seekBarValueTextView: TextView = findViewById(R.id.dimmer_percentage)
        
        val database = FirebaseUtils.databaseRef
        val devicePort = intent.getStringExtra("widget_port")

        val dimmerValueListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val dimmerValue = snapshot.value as Long
                seekBar.progress = dimmerValue.toInt()
                seekBarValueTextView.text = "$dimmerValue%"
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("WidgetDetailActivity", "Failed to read value.", error.toException())
            }
        }
        database.child("devices/ports/$devicePort/dimmer").addValueEventListener(dimmerValueListener)

        seekBarValueTextView.text = "${seekBar.progress}%"

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                seekBarValueTextView.text = "$progress%"
                // update the value in the database
                database.child("devices/ports/$devicePort/dimmer").setValue(progress)

            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })




//        val widgetName = intent.getStringExtra("widget_name")
//        val widgetType = intent.getStringExtra("widget_type")
//        val widgetPort = intent.getStringExtra("widget_port")

//        val titleTextView: TextView = findViewById(R.id.widgetTitle)
//        titleTextView.text = "Widget: $widgetName"
//
//        val typeTextView: TextView = findViewById(R.id.widgetType)
//        typeTextView.text = "Type: $widgetType"
//
//        val portTextView: TextView = findViewById(R.id.widgetPort)
//        portTextView.text = "Port: $widgetPort"
    }
}
