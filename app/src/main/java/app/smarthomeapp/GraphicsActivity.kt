package app.smarthomeapp

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.icu.util.Calendar
import android.os.Bundle
import android.widget.ImageView
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import java.text.SimpleDateFormat
import java.util.Locale

class GraphicsActivity : AppCompatActivity() {
    private lateinit var currentMetric: String
    private lateinit var currentTimeRange: String
    private lateinit var lineChart: LineChart

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.graphics_layout)

        currentMetric = intent.getStringExtra("metric") ?: "Temperature"
        currentTimeRange = intent.getStringExtra("timeRange") ?: "Day"

        // change grahics_layout title
        val title = findViewById<TextView>(R.id.titleText)
        title.text = currentMetric

        val infoLabel = findViewById<TextView>(R.id.temperatureLabel)
        infoLabel.text = currentMetric

        val valueLabel = findViewById<TextView>(R.id.temperatureValue)

        if (currentMetric == "Temperature") {
            valueLabel.text = "25°C"
        } else if (currentMetric == "Humidity") {
            valueLabel.text = "50%"
        } else if (currentMetric == "Luminosity") {
            valueLabel.text = "500 lux"
        } else if (currentMetric == "Power Usage") {
            valueLabel.text = "100 W"
        } else if (currentMetric == "Air Quality") {
            valueLabel.text = "50 AQI"
        }

         lineChart = findViewById(R.id.lineChart)

        // Top tabs for metrics
        val temperatureTab = findViewById<TextView>(R.id.temperatureTab)
        val humidityTab = findViewById<TextView>(R.id.humidityTab)
        val luminosityTab = findViewById<TextView>(R.id.luminosityTab)
        val powerUsageTab = findViewById<TextView>(R.id.powerUsageTab)
        val airQualityTab = findViewById<TextView>(R.id.airQualityTab)

        val metricTabs = listOf(temperatureTab, humidityTab, luminosityTab, powerUsageTab, airQualityTab)

        val bottomTabs = findViewById<RadioGroup>(R.id.bottomTabs)
        val backButton = findViewById<ImageView>(R.id.backButton)

        temperatureTab.setOnClickListener { selectMetric("Temperature", metricTabs, it as TextView) }
        humidityTab.setOnClickListener { selectMetric("Humidity", metricTabs, it as TextView) }
        luminosityTab.setOnClickListener { selectMetric("Luminosity", metricTabs, it as TextView) }
        powerUsageTab.setOnClickListener { selectMetric("Power Usage", metricTabs, it as TextView) }
        airQualityTab.setOnClickListener { selectMetric("Air Quality", metricTabs, it as TextView) }

        // bottom tabs
        bottomTabs.setOnCheckedChangeListener { _, checkedId ->
            currentTimeRange = when (checkedId) {
                R.id.btnDay -> "Day"
                R.id.btnWeek -> "Week"
                R.id.btnMonth -> "Month"
                R.id.btnYear -> "Year"
                else -> "Day"
            }
            updateChart()
        }

        // Back button
        backButton.setOnClickListener {
            finish()
        }

        updateChart()
    }

    private fun selectMetric(metric: String, tabs: List<TextView>, selectedTab: TextView) {
        currentMetric = metric

        val title = findViewById<TextView>(R.id.titleText)
        title.text = currentMetric

        val infoLabel = findViewById<TextView>(R.id.temperatureLabel)
        infoLabel.text = currentMetric

        val valueLabel = findViewById<TextView>(R.id.temperatureValue)

        if (currentMetric == "Temperature") {
            valueLabel.text = "25°C"
        } else if (currentMetric == "Humidity") {
            valueLabel.text = "50%"
        } else if (currentMetric == "Luminosity") {
            valueLabel.text = "500 lux"
        } else if (currentMetric == "Power Usage") {
            valueLabel.text = "100 W"
        } else if (currentMetric == "Air Quality") {
            valueLabel.text = "50 AQI"
        }

        // Highlight the selected tab
        tabs.forEach { tab ->
            tab.setBackgroundResource(R.drawable.tab_background)
            tab.setTextColor(Color.WHITE)
        }
        selectedTab.setBackgroundResource(R.drawable.tab_selected_background) // Highlight selected
        selectedTab.setTextColor(Color.BLACK)

        updateChart()
    }

    private fun configureLineDataSet(
        entries: List<Entry>, label: String, fillDrawable: Drawable
    ): LineDataSet {
        return LineDataSet(entries, label).apply {
            color = when (label) {
                "Temperature" -> Color.RED
                "Luminosity" -> Color.YELLOW
                "Power Usage" -> Color.BLUE
                "Air Quality" -> Color.GREEN
                else -> Color.CYAN
            }
            setCircleColor(Color.WHITE)
            valueTextColor = Color.TRANSPARENT
            setDrawFilled(true)
            mode = LineDataSet.Mode.CUBIC_BEZIER
            this.fillDrawable = fillDrawable
        }
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
            axisMaximum = when (currentMetric) {
                "Temperature" -> 50f
                "Luminosity" -> 1000f
                "Power Usage" -> 500f
                "Air Quality" -> 100f
                else -> 100f
            }
            labelCount = 6
            setDrawGridLines(true)
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return when (currentMetric) {
                        "Temperature" -> "${value.toInt()}°C"
                        "Luminosity" -> "${value.toInt()} lux"
                        "Power Usage" -> "${value.toInt()} W"
                        "Air Quality" -> "${value.toInt()} AQI"
                        else -> "${value.toInt()}%"
                    }
                }
            }
        }
    }

    private fun updateChart() {
        val entries: List<Entry>
        val labels: List<String>
        val labelCount: Int

        when (currentTimeRange) {
            "Day" -> {
                val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
                entries = generateRandomData(
                    getRangeForMetric(), 24
                )
                labels = (0 until 24).map { hour ->
                    "${(currentHour - 23 + hour + 24) % 24}:00"
                }
                labelCount = 6
            }
            "Week" -> {
                entries = generateRandomData(getRangeForMetric(), 7)
                labels = (0 until 7).map { offset ->
                    SimpleDateFormat("EEE", Locale.getDefault()).format(Calendar.getInstance().apply {
                        add(Calendar.DAY_OF_WEEK, offset - 6)
                    }.time)
                }
                labelCount = 7
            }
            "Month" -> {
                entries = generateRandomData(getRangeForMetric(), 30)
                labels = (1..30).map { "Day $it" }
                labelCount = 7
            }
            "Year" -> {
                entries = generateRandomData(getRangeForMetric(), 12)
                labels = (0 until 12).map { offset ->
                    SimpleDateFormat("MMM", Locale.getDefault()).format(Calendar.getInstance().apply {
                        add(Calendar.MONTH, offset - 11)
                    }.time)
                }
                labelCount = 12
            }
            else -> return
        }

        updateChartView(entries, labels, labelCount)
    }

    private fun generateRandomData(range: IntRange, count: Int): List<Entry> {
        return (0 until count).map { index ->
            Entry(index.toFloat(), range.random().toFloat())
        }
    }

    private fun getRangeForMetric(): IntRange {
        return when (currentMetric) {
            "Temperature" -> 15..35
            "Luminosity" -> 200..800
            "Power Usage" -> 10..200
            "Air Quality" -> 10..90
            else -> 50..80
        }
    }

    private fun updateChartView(entries: List<Entry>, labels: List<String>, labelCount: Int) {

        val fillDrawable = when (currentMetric) {
            "Temperature" -> getDrawable(R.drawable.gradient_fill_red)!!
            "Luminosity" -> getDrawable(R.drawable.gradient_fill_yellow)!!
//            "Power Usage" -> getDrawable(R.drawable.gradient_fill_blue)!!
//            "Air Quality" -> getDrawable(R.drawable.gradient_fill_green)!!
            else -> getDrawable(R.drawable.gradient_fill_yellow)!!
        }
        val lineDataSet = configureLineDataSet(entries, currentMetric, fillDrawable)

        val lineData = LineData(lineDataSet)

        lineChart.data = lineData
        configureXAxis(lineChart.xAxis, labels, labelCount)
        configureYAxis(lineChart.axisLeft)
        lineChart.axisRight.isEnabled = false
        lineChart.description.isEnabled = false
        lineChart.legend.isEnabled = false
        lineChart.invalidate()
    }
}