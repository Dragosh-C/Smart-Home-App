package app.smarthomeapp.mainpage

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.RadioGroup
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.lifecycle.ViewModelProvider
import app.smarthomeapp.FirebaseUtils
import app.smarthomeapp.FirebaseUtils.databaseRef
import app.smarthomeapp.R
import app.smarthomeapp.viewmodels.GraphicsViewModel
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener

class WidgetDetailsActivityDevice : AppCompatActivity() {
    private lateinit var viewModel: GraphicsViewModel
    private lateinit var lineChart: LineChart

    private var currentListener: ValueEventListener? = null
    private var currentDatabaseReference: DatabaseReference? = null
    private lateinit var widgetId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_widget_detail_device)

        widgetId = intent.getStringExtra("widget_port") ?: "1001"
//        Log.d("Firebase", "Widget ID: $widgetId")

        viewModel = ViewModelProvider(this)[GraphicsViewModel::class.java]

        viewModel.setMetric("Power Usage")
        viewModel.setTimeRange("Day")

        val title = findViewById<TextView>(R.id.titleText)
        val infoLabel = findViewById<TextView>(R.id.temperatureLabel)
        val valueLabel = findViewById<TextView>(R.id.temperatureValue)
        lineChart = findViewById(R.id.lineChart)

        viewModel.currentMetric.observe(this) { metric ->
            title.text = metric
            infoLabel.text = metric
            valueLabel.text = getValueForMetric(metric)
        }

        viewModel.chartData.observe(this) { entries ->
            viewModel.chartLabels.observe(this) { labels ->
                viewModel.labelCount.observe(this) { count ->
                    updateChart(entries, labels, count)
                }
            }
        }

        val bottomTabs = findViewById<RadioGroup>(R.id.bottomTabs)
        bottomTabs.setOnCheckedChangeListener { _, checkedId ->
            val newTimeRange = when (checkedId) {
                R.id.btnDay -> "Day"
                R.id.btnWeek -> "Week"
                R.id.btnMonth -> "Month"
                R.id.btnYear -> "Year"
                else -> "Day"
            }
            viewModel.setTimeRange(newTimeRange)
        }

        val powerSwitch = findViewById<SwitchCompat>(R.id.powerSwitch)

        // write to the database when the switch is toggled
//        val myRef = databaseRef.child("/devices/$deviceId/actuator")
        val myRef = databaseRef.child("/devices/$widgetId/actuator")

// Read data from Firebase
        myRef.get().addOnSuccessListener {
            val value = it.getValue(Boolean::class.java)
            powerSwitch.isChecked = value ?: false
        }

// Listen for switch changes and update Firebase
        powerSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                Log.d("Firebase", "Power switch is ON")
                myRef.setValue(true)
            } else {
                Log.d("Firebase", "Power switch is OFF")
                myRef.setValue(false)
            }
        }



        findViewById<ImageView>(R.id.backButton).setOnClickListener {
            finish()
        }

        updatePowerUsage(valueLabel)
    }

    private fun getValueForMetric(metric: String): String {
        return when (metric) {
            "Power Usage" -> "100 W"
            else -> ""
        }
    }

    private fun updateChart(entries: List<Entry>, labels: List<String>, labelCount: Int) {
        val lineDataSet = LineDataSet(entries, "Device Power Usage").apply {
            color = Color.YELLOW
            setCircleColor(Color.YELLOW)
            setDrawFilled(true)
            mode = LineDataSet.Mode.CUBIC_BEZIER
            this.fillDrawable = getDrawable(R.drawable.gradient_fill_yellow)!!
        }

        val lineData = LineData(lineDataSet)
        lineChart.data = lineData

        // Disable label on each point on the plot line
        lineDataSet.setDrawValues(false)

        configureXAxis(lineChart.xAxis, labels, labelCount)
        configureYAxis(lineChart.axisLeft)
        lineChart.axisRight.isEnabled = false
        lineChart.description.isEnabled = false
        lineChart.legend.isEnabled = false

        lineChart.animateY(300)
        lineChart.invalidate()
    }

    private fun configureXAxis(xAxis: XAxis, labels: List<String>, labelCount: Int) {
        xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            textColor = Color.WHITE
            valueFormatter = IndexAxisValueFormatter(labels)
            setDrawGridLines(false)
            this.labelCount = labelCount
            granularity = 1f
        }
    }

    private fun configureYAxis(yAxis: YAxis) {
        yAxis.apply {
            textColor = Color.WHITE
            axisMinimum = 0f
            axisMaximum = 300f  // Max for power usage
            labelCount = 6
            setDrawGridLines(true)
        }
    }

    private fun updatePowerUsage(valueLabel: TextView) {
        // Remove the old listener if it exists
        currentListener?.let { listener ->
            currentDatabaseReference?.removeEventListener(listener)
            Log.d("Firebase", "Removed old listener for power_usage")
        }

        val myRef: DatabaseReference = databaseRef.child("/devices/$widgetId/power_usage")

        // New listener to fetch power usage data
        val newListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val value = snapshot.getValue(Double::class.java)
                valueLabel.text = "$value W"
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Failed to read value: ${error.message}")
            }
        }

        myRef.addValueEventListener(newListener)

        currentListener = newListener
        currentDatabaseReference = myRef

        Log.d("Firebase", "Added new listener for power_usage")
    }
}
