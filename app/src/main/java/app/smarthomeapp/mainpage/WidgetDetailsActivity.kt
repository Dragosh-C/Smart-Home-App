package app.smarthomeapp.mainpage

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import app.smarthomeapp.R

class WidgetDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_widget_detail) // Create this layout file

        val widgetName = intent.getStringExtra("widget_name")
        val widgetType = intent.getStringExtra("widget_type")
        val widgetPort = intent.getStringExtra("widget_port")

        val titleTextView: TextView = findViewById(R.id.widgetTitle)
        titleTextView.text = "Widget: $widgetName"

        val typeTextView: TextView = findViewById(R.id.widgetType)
        typeTextView.text = "Type: $widgetType"

        val portTextView: TextView = findViewById(R.id.widgetPort)
        portTextView.text = "Port: $widgetPort"
    }
}
