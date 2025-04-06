package app.smarthomeapp

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.RadioGroup
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider

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
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class GraphicsActivity : AppCompatActivity() {
    private lateinit var viewModel: GraphicsViewModel
    private lateinit var lineChart: LineChart
    private lateinit var selectedBoxId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.graphics_layout)

        viewModel = ViewModelProvider(this)[GraphicsViewModel::class.java]

        val metric = intent.getStringExtra("metric") ?: "Temperature"
        val timeRange = intent.getStringExtra("timeRange") ?: "Day"
        selectedBoxId = intent.getStringExtra("selectedBoxId") ?: "1212"

        viewModel.setMetric(metric)
        viewModel.setTimeRange(timeRange)

        val title = findViewById<TextView>(R.id.titleText)
        val infoLabel = findViewById<TextView>(R.id.temperatureLabel)
        val valueLabel = findViewById<TextView>(R.id.temperatureValue)
        lineChart = findViewById(R.id.lineChart)

        val metricTabs = listOf(
            findViewById(R.id.temperatureTab),
            findViewById(R.id.humidityTab),
            findViewById(R.id.luminosityTab),
            findViewById(R.id.powerUsageTab),
            findViewById(R.id.airQualityTab),
            findViewById<TextView>(R.id.batteryLevelTab)
        )

        viewModel.currentMetric.observe(this) { m ->
            title.text = m
            infoLabel.text = m
            valueLabel.text = getValueForMetric(m)
            updateTabSelection(m, metricTabs)
            updateLightValue(m)
        }

        viewModel.chartData.observe(this) { entries ->
            viewModel.chartLabels.observe(this) { labels ->
                viewModel.labelCount.observe(this) { count ->
                    updateChart(entries, labels, count)
                }
            }
        }

        metricTabs.forEach { tab ->
            tab.setOnClickListener {
                val newMetric = tab.text.toString()
                viewModel.setMetric(newMetric)
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

        val settingsButton = findViewById<ImageView>(R.id.settingsButton)
        settingsButton.setOnClickListener {
            val intent = intent
            intent.setClass(this, SettingsGraphicsActivity::class.java)
            startActivity(intent)
        }


        findViewById<ImageView>(R.id.backButton).setOnClickListener {
            finish()
        }
    }

    private fun getValueForMetric(metric: String): String {
        return when (metric) {
            "Temperature" -> "25°C"
            "Humidity" -> "50%"
            "Luminosity" -> "500 lux"
            "Power Usage" -> "100 W"
            "Air Quality" -> "50 AQI"
            "Battery Level" -> "4.2 V"
            else -> ""
        }
    }

    private fun updateTabSelection(metric: String, tabs: List<TextView>) {
        tabs.forEach { tab ->
            if (tab.text.toString() == metric) {
                tab.setBackgroundResource(R.drawable.tab_selected_background)
                tab.setTextColor(Color.BLACK)
            } else {
                tab.setBackgroundResource(R.drawable.tab_background)
                tab.setTextColor(Color.WHITE)
            }
        }
    }

    private fun updateChart(entries: List<Entry>, labels: List<String>, labelCount: Int) {

        val (lineColor, fillDrawable) = when (viewModel.currentMetric.value) {
            "Temperature" -> Pair(Color.RED, getDrawable(R.drawable.gradient_fill_red)!!)
            "Luminosity" -> Pair(Color.YELLOW, getDrawable(R.drawable.gradient_fill_yellow)!!)
            else -> Pair(Color.YELLOW, getDrawable(R.drawable.gradient_fill_yellow)!!)
        }

        val lineDataSet = LineDataSet(entries, viewModel.currentMetric.value).apply {
            color = lineColor
            setCircleColor(lineColor)
            setDrawFilled(true)
            mode = LineDataSet.Mode.CUBIC_BEZIER
            this.fillDrawable = fillDrawable
        }

        val lineData = LineData(lineDataSet)
        lineChart.data = lineData

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
            axisMaximum = when (viewModel.currentMetric.value) {
                "Temperature" -> 50f
                "Luminosity" -> 100f
                "Power Usage" -> 300f
                "Battery Level" -> 5f
                else -> 100f
            }
            labelCount = 6
            setDrawGridLines(true)
        }
    }

    private var currentListener: ValueEventListener? = null
    private var currentDatabaseReference: DatabaseReference? = null

    fun updateLightValue(filedName: String) {
        var dataName = ""
        if (filedName == "Temperature") dataName = "temperature"
        if (filedName == "Humidity") dataName = "humidity"
        if (filedName == "Luminosity") dataName = "light_intensity"
        if (filedName == "Power Usage") dataName = "power_usage"
        if (filedName == "Air Quality") dataName = "air_quality"
        if (filedName == "Battery Level") dataName = "battery_voltage"

        currentListener?.let { listener ->
            currentDatabaseReference?.removeEventListener(listener)
            Log.d("Firebase", "Removed old listener for $dataName")
        }

        val database = FirebaseDatabase.getInstance()
        Log.d("Firebase", "Database instance: $database")

        val myRef: DatabaseReference = database.getReference("/box_id/$selectedBoxId/$dataName")

        val newListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val value = snapshot.getValue(Double::class.java)

                val lightText = findViewById<TextView>(R.id.temperatureValue)

                when (filedName) {
                    "Temperature" -> lightText.text = "$value °C"
                    "Humidity" -> lightText.text = "$value %"
                    "Luminosity" -> lightText.text = "$value Lux"
                    "Power Usage" -> lightText.text = "$value W"
                    "Air Quality" -> lightText.text = "$value AQI"
                    "Battery Level" -> lightText.text = "$value V"
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Failed to read value: ${error.message}")
            }
        }

        myRef.addValueEventListener(newListener)
        currentListener = newListener
        currentDatabaseReference = myRef

        Log.d("Firebase", "Added new listener for $dataName")
    }


}
