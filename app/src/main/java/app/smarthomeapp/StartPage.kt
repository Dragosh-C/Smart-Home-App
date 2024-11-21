package app.smarthomeapp

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.AlarmClock
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import app.smarthomeapp.databinding.ActivityStartPageBinding
import app.smarthomeapp.sign_in.Login
import app.smarthomeapp.sign_in.Register
import com.google.firebase.auth.FirebaseAuth


class StartPage : AppCompatActivity() {

    private lateinit var binding: ActivityStartPageBinding
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_start_page)

        binding = ActivityStartPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.signupButton.setOnClickListener {
            val intent = Intent(this, Register::class.java)
            startActivity(intent)
        }

        binding.loginButton.setOnClickListener {
            val intent = Intent(this, Login::class.java)
            startActivity(intent)


//            val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
//                putExtra(AlarmClock.EXTRA_HOUR, 7)
//                putExtra(AlarmClock.EXTRA_MINUTES, 30)
//                putExtra(AlarmClock.EXTRA_MESSAGE, "Test Alarm")
//            }
//
//            if (intent.resolveActivity(packageManager) != null) {
//                startActivity(intent)
//            } else {
//                Toast.makeText(this, "No compatible alarm app found!", Toast.LENGTH_SHORT).show()
//            }


        }

    }

    override fun onStart() {
        super.onStart()
        auth = FirebaseAuth.getInstance()
        if (auth.currentUser != null) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }
}