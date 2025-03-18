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
