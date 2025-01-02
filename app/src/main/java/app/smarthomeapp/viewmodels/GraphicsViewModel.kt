//package app.smarthomeapp.viewmodels
//import androidx.lifecycle.LiveData
//import androidx.lifecycle.MutableLiveData
//import androidx.lifecycle.ViewModel
//import com.github.mikephil.charting.data.Entry
//import java.text.SimpleDateFormat
//import java.util.*
//
//class GraphicsViewModel : ViewModel() {
//    private val _currentMetric = MutableLiveData("Temperature")
//    val currentMetric: LiveData<String> get() = _currentMetric
//
//    private val _currentTimeRange = MutableLiveData("Day")
//    val currentTimeRange: LiveData<String> get() = _currentTimeRange
//
//    private val _chartData = MutableLiveData<List<Entry>>()
//    val chartData: LiveData<List<Entry>> get() = _chartData
//
//    private val _chartLabels = MutableLiveData<List<String>>()
//    val chartLabels: LiveData<List<String>> get() = _chartLabels
//
//    private val _labelCount = MutableLiveData(6)
//    val labelCount: LiveData<Int> get() = _labelCount
//
//    fun setMetric(metric: String) {
//        _currentMetric.value = metric
//        updateChart()
//    }
//
//    fun setTimeRange(timeRange: String) {
//        _currentTimeRange.value = timeRange
//        updateChart()
//    }
//
//    private fun updateChart() {
//        val range = getRangeForMetric()
//        val timeRange = _currentTimeRange.value ?: "Day"
//        val (entries, labels, count) = when (timeRange) {
//            "Day" -> generateDayData(range)
//            "Week" -> generateWeekData(range)
//            "Month" -> generateMonthData(range)
//            "Year" -> generateYearData(range)
//            else -> Triple(emptyList(), emptyList(), 0)
//        }
//        _chartData.value = entries
//        _chartLabels.value = labels
//        _labelCount.value = count
//    }
//
//    private fun generateDayData(range: IntRange): Triple<List<Entry>, List<String>, Int> {
//        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
//        val entries = generateRandomData(range, 24)
//        val labels = (0 until 24).map { "${(currentHour - 23 + it + 24) % 24}:00" }
//        return Triple(entries, labels, 6)
//    }
//
//    private fun generateWeekData(range: IntRange): Triple<List<Entry>, List<String>, Int> {
//        val entries = generateRandomData(range, 7)
//        val labels = (0 until 7).map { offset ->
//            SimpleDateFormat("EEE", Locale.getDefault()).format(Calendar.getInstance().apply {
//                add(Calendar.DAY_OF_WEEK, offset - 6)
//            }.time)
//        }
//        return Triple(entries, labels, 7)
//    }
//
//    private fun generateMonthData(range: IntRange): Triple<List<Entry>, List<String>, Int> {
//        val entries = generateRandomData(range, 30)
//        val labels = (1..30).map { "Day $it" }
//        return Triple(entries, labels, 7)
//    }
//
//    private fun generateYearData(range: IntRange): Triple<List<Entry>, List<String>, Int> {
//        val entries = generateRandomData(range, 12)
//        val labels = (0 until 12).map { offset ->
//            SimpleDateFormat("MMM", Locale.getDefault()).format(Calendar.getInstance().apply {
//                add(Calendar.MONTH, offset - 11)
//            }.time)
//        }
//        return Triple(entries, labels, 12)
//    }
//
//    private fun generateRandomData(range: IntRange, count: Int): List<Entry> {
//        return (0 until count).map { index ->
//            Entry(index.toFloat(), range.random().toFloat())
//        }
//    }
//
//    private fun getRangeForMetric(): IntRange {
//        return when (_currentMetric.value) {
//            "Temperature" -> 15..35
//            "Luminosity" -> 200..800
//            "Power Usage" -> 10..200
//            "Air Quality" -> 10..90
//            else -> 50..80
//        }
//    }
//}


package app.smarthomeapp.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.github.mikephil.charting.data.Entry
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*


class GraphicsViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()

    private val _currentMetric = MutableLiveData("Temperature")
    val currentMetric: LiveData<String> get() = _currentMetric

    private val _currentTimeRange = MutableLiveData("Day")
    val currentTimeRange: LiveData<String> get() = _currentTimeRange

    private val _chartData = MutableLiveData<List<Entry>>()
    val chartData: LiveData<List<Entry>> get() = _chartData

    private val _chartLabels = MutableLiveData<List<String>>()
    val chartLabels: LiveData<List<String>> get() = _chartLabels

    private val _labelCount = MutableLiveData(6)
    val labelCount: LiveData<Int> get() = _labelCount

    fun setMetric(metric: String) {
        _currentMetric.value = metric
        fetchChartData()
    }

    fun setTimeRange(timeRange: String) {
        _currentTimeRange.value = timeRange
        fetchChartData()
    }

    private fun fetchChartData() {
        val metric = _currentMetric.value ?: "Temperature"
        val timeRange = _currentTimeRange.value ?: "Day"

        firestore.collection("metrics")
            .document(metric)
            .collection(timeRange)
            .get()
            .addOnSuccessListener { snapshot ->
                val entries = mutableListOf<Entry>()
                val labels = mutableListOf<String>()

                snapshot.documents.forEachIndexed { index, document ->
                    val value = document.getDouble("value") ?: 0.0
                    val timestamp = document.getLong("timestamp") ?: 0L
                    entries.add(Entry(index.toFloat(), value.toFloat()))

                    labels.add(
                        when (timeRange) {
                            "Day" -> SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(timestamp))
                            "Week" -> SimpleDateFormat("EEE", Locale.getDefault()).format(Date(timestamp))
                            "Month" -> SimpleDateFormat("MMM dd", Locale.getDefault()).format(Date(timestamp))
                            "Year" -> SimpleDateFormat("MMM", Locale.getDefault()).format(Date(timestamp))
                            else -> SimpleDateFormat("MMM dd", Locale.getDefault()).format(Date(timestamp))
                        }
                    )
                }

                _chartData.value = entries
                _chartLabels.value = labels
                _labelCount.value = labels.size.coerceAtMost(7)
            }
            .addOnFailureListener { error ->
                _chartData.value = emptyList()
                _chartLabels.value = emptyList()
                _labelCount.value = 0
            }
    }

}
