package app.smarthomeapp

import android.graphics.Color
import android.os.Bundle
import android.widget.ImageView
import android.widget.RadioGroup
import android.widget.TextView
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

class GraphicsActivity : AppCompatActivity() {
    private lateinit var viewModel: GraphicsViewModel
    private lateinit var lineChart: LineChart

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.graphics_layout)

        viewModel = ViewModelProvider(this)[GraphicsViewModel::class.java]

        val metric = intent.getStringExtra("metric") ?: "Temperature"
        val timeRange = intent.getStringExtra("timeRange") ?: "Day"

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
            findViewById<TextView>(R.id.airQualityTab)
        )

        viewModel.currentMetric.observe(this) { metric ->
            title.text = metric
            infoLabel.text = metric
            valueLabel.text = getValueForMetric(metric)
            updateTabSelection(metric, metricTabs)
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
        val fillDrawable = when (viewModel.currentMetric.value) {
            "Temperature" -> getDrawable(R.drawable.gradient_fill_red)!!
            "Luminosity" -> getDrawable(R.drawable.gradient_fill_yellow)!!
            else -> getDrawable(R.drawable.gradient_fill_yellow)!!
        }

        val lineDataSet = LineDataSet(entries, viewModel.currentMetric.value).apply {
            color = Color.RED
            setCircleColor(Color.WHITE)
            setDrawFilled(true)
            mode = LineDataSet.Mode.CUBIC_BEZIER
            this.fillDrawable = fillDrawable
        }

        val lineData = LineData(lineDataSet)
        lineChart.data = lineData

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
                "Luminosity" -> 1000f
                "Power Usage" -> 1000f
                else -> 100f
            }
            labelCount = 6
            setDrawGridLines(true)
        }
    }
}
