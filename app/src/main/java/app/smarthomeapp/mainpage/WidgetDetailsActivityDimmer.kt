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
import android.os.Handler
import android.os.Looper
import com.google.firebase.perf.FirebasePerformance

class WidgetDetailActivity : AppCompatActivity() {
    private var isUserChanging = false
    private var lastSentValue = -1  // Stores last sent value to prevent redundant updates
    private val handler = Handler(Looper.getMainLooper())
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_widget_detail_dimmer)

        val layout = findViewById<LinearLayout>(R.id.dimmer_layout)
        val gradientDrawable = ResourcesCompat.getDrawable(
            resources, R.drawable.background_gardient, null
        ) as? GradientDrawable
        gradientDrawable?.setDither(true)
        layout.background = gradientDrawable

        val seekBar: SeekBar = findViewById(R.id.dimmer_seekbar)
        val seekBarValueTextView: TextView = findViewById(R.id.dimmer_percentage)

        val database = FirebaseUtils.databaseRef
        val devicePort = intent.getStringExtra("widget_port")

        val dimmerValueListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val dimmerValue = snapshot.value as? Long ?: return

                if (!isUserChanging && seekBar.progress != dimmerValue.toInt()) {
                    seekBar.progress = dimmerValue.toInt()
                    seekBarValueTextView.text = "$dimmerValue%"
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("WidgetDetailActivity", "Failed to read value.", error.toException())
            }
        }

        database.child("devices/ports/$devicePort/dimmer")
            .addValueEventListener(dimmerValueListener)

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    isUserChanging = true
                    seekBarValueTextView.text = "$progress%"

                    // Avoid sending redundant values
                    if (progress != lastSentValue) {
                        lastSentValue = progress
                        handler.removeCallbacksAndMessages(null) // Remove previous update task
//                        handler.postDelayed({
//                            database.child("devices/ports/$devicePort/dimmer").setValue(progress)
//                        }, 10) // Add delay (10ms) to prevent excessive writes

                        val trace = FirebasePerformance.getInstance().newTrace("dimmer_write_trace")
                        trace.start()

                        handler.postDelayed({
                            database.child("devices/ports/$devicePort/dimmer")
                                .setValue(progress)
                                .addOnSuccessListener {
                                    trace.stop()
                                    Log.d("FirebaseWrite", "Write success")
                                }
                                .addOnFailureListener {
                                    trace.stop()
                                    Log.e("FirebaseWrite", "Write failed")
                                }
                        }, 10)
                    }
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                isUserChanging = true
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                isUserChanging = false
            }
        })
    }
}
