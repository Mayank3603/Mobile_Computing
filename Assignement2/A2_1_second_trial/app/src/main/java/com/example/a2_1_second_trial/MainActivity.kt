package com.example.a2_1_second_trial



import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.example.a2_1_second_trial.R

class MainActivity : AppCompatActivity() {

    private lateinit var etFlightNumber: EditText
    private lateinit var btnFetchDetails: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize views
        etFlightNumber = findViewById(R.id.etFlightNumber)
        btnFetchDetails = findViewById(R.id.btnFetchDetails)

        // Set button click listener
        btnFetchDetails.setOnClickListener {
            val flightNumber = etFlightNumber.text.toString().trim()

            // Validate user input
            if (flightNumber.isEmpty()) {
                Toast.makeText(this, "Please enter a flight number.", Toast.LENGTH_SHORT).show()
            } else {
                // Pass the flight number to FlightDetailsActivity
                val intent = Intent(this, FlightDetailsActivity::class.java)
                intent.putExtra("flightNumber", flightNumber)
                startActivity(intent)
            }
        }
    }
}