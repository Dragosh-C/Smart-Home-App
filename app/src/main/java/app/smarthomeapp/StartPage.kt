package app.smarthomeapp

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
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