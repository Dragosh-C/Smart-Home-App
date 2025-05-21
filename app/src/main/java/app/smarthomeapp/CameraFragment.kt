package app.smarthomeapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageButton
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class CameraFragment : Fragment() {

    private val handler = Handler(Looper.getMainLooper())
    private var isButtonPressed = false
    private lateinit var database: FirebaseDatabase
    private var x = 90
    private var y = 90

    override fun onDestroyView() {
        super.onDestroyView()
        val webView = view?.findViewById<WebView>(R.id.webview)
        webView?.apply {
            stopLoading()
            clearHistory()
            clearCache(true)
            loadUrl("about:blank")
            removeAllViews()
            destroy()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("CameraFragment", "onCreateView called")

        val view = inflater.inflate(R.layout.fragment_camera, container, false)

        val webView: WebView = view.findViewById(R.id.webview)
        database = FirebaseDatabase.getInstance()


        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            loadWithOverviewMode = true
            useWideViewPort = true
            cacheMode = WebSettings.LOAD_NO_CACHE
        }

        webView.webViewClient = WebViewClient()
        webView.webChromeClient = WebChromeClient()

//      webView.loadUrl("https://rt.ivs.rocks/demos/pk-mode")

        webView.loadUrl("http://192.168.45.124/")

        val leftButton: ImageView = view.findViewById(R.id.joystick_left)
        val rightButton: ImageView = view.findViewById(R.id.joystick_right)
        val upButton: ImageView = view.findViewById(R.id.joystick_up)
        val downButton: ImageView = view.findViewById(R.id.joystick_down)
        val centerButton: ImageView = view.findViewById(R.id.joystick_center)

        // read x and y from firebase
        buttonListener(rightButton, leftButton, upButton, downButton, centerButton, view)

        return view
    }

    private fun buttonListener(
        rightButton: ImageView,
        leftButton: ImageView,
        upButton: ImageView,
        downButton: ImageView,
        centerButton: ImageView,
        view: View
    ) {
        val ref: DatabaseReference = database.reference.child("camera").child("1001").child("coordinates")
        ref.get().addOnSuccessListener {
            if (it.exists()) {
                x = (it.child("x").value as Long).toInt()
                y = (it.child("y").value as Long).toInt()
            }
        }

        rightButton.setOnTouchListener { _, event ->
            handleButtonPress(event.action, {
                if (x < 180) {
                    x+=2
                    vibrate()
                    updateFirebase()
                }
            })
            true
        }

        leftButton.setOnTouchListener { _, event ->
            handleButtonPress(event.action, {
                if (x > 0) {
                    x-=2
                    vibrate()
                    updateFirebase()
                }
            })
            true
        }



        upButton.setOnTouchListener { _, event ->
            handleButtonPress(event.action, {
                if (y < 180) {
                    y+=2
                    vibrate()
                    updateFirebase()
                }

            })
            true
        }


        // Continuous Decrement Y
        downButton.setOnTouchListener { _, event ->
            handleButtonPress(event.action, {
                if (y > 0) {
                    y-=2
                    vibrate()
                    updateFirebase()
                }
            })
            true
        }

        // Center Button

        centerButton.setOnClickListener {
            x = 90
            y = 90
            vibrate()
            updateFirebase()
        }

        val settingsButton = view.findViewById<ImageButton>(R.id.settings_button_camera)
        settingsButton.setOnClickListener {
            val intent = Intent(activity, SettingsActivity::class.java)
            startActivity(intent)
        }

        // Turn on/off camera flashlight
        val flashlightButton = view.findViewById<ImageButton>(R.id.flashlight_button)
        flashlightButton.setOnClickListener {
            val flashlightRef: DatabaseReference = database.reference.child("camera/1001/flashlight")
            flashlightRef.get().addOnSuccessListener {
                if (it.exists()) {
                    val flashlightState = it.value as Boolean
                    flashlightRef.setValue(!flashlightState)
                }
                else {
                    flashlightRef.setValue(true)
                }
            }
            vibrate()
        }
    }

    private fun vibrate() {
        val vibrator = requireContext().getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        val vibrationEffect = VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE)
        vibrator.vibrate(vibrationEffect)
    }

private fun handleButtonPress(action: Int, onIncrement: () -> Unit) {
    when (action) {
        android.view.MotionEvent.ACTION_DOWN -> {
            isButtonPressed = true
            handler.post(object : Runnable {
                override fun run() {
                    if (isButtonPressed) {
                        onIncrement()
                        handler.postDelayed(this, 80) // Repeat every 100ms
                    }
                }
            })
        }
        android.view.MotionEvent.ACTION_UP, android.view.MotionEvent.ACTION_CANCEL -> {
            isButtonPressed = false
        }
    }
}

    private fun updateFirebase() {
        val data = mapOf(
            "x" to x,
            "y" to y
        )
        database.reference.child("camera").child("1001").child("coordinates").setValue(data)
    }

}
