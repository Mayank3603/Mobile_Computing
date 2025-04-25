
package com.example.a3_2

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var wifiManager: WifiManager
    private lateinit var spinner: Spinner
    private lateinit var matrixText: TextView
    private lateinit var summaryText: TextView
    private lateinit var startStopButton: Button

    private var selectedLocation = "Location A"
    private var isLogging = false

    private val rssiData = mutableMapOf(
        "Location A" to mutableListOf<Int>(),
        "Location B" to mutableListOf<Int>(),
        "Location C" to mutableListOf<Int>()
    )

    private val handler = Handler(Looper.getMainLooper())
    private val scanRunnable = object : Runnable {
        override fun run() {
            if (isLogging) {
                startWifiScan()
                handler.postDelayed(this, 2000)
            }
        }
    }

    // 1) BroadcastReceiver for async scan results
    private val wifiScanReceiver = object : BroadcastReceiver() {
        override fun onReceive(ctx: Context?, intent: Intent?) {
            if (intent?.action == WifiManager.SCAN_RESULTS_AVAILABLE_ACTION) {
                val results: List<ScanResult> = wifiManager.scanResults
                val strongest = results.maxByOrNull { it.level }
                strongest?.let { recordRssi(it.level) }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        spinner       = findViewById(R.id.locationSpinner)
        matrixText    = findViewById(R.id.rssiMatrix)
        summaryText   = findViewById(R.id.summaryText)
        startStopButton = findViewById(R.id.startStopButton)

        // Spinner callback
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?, view: android.view.View?, pos: Int, id: Long
            ) {
                selectedLocation = parent?.getItemAtPosition(pos).toString()
                updateMatrixDisplay()
                updateSummary()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        startStopButton.setOnClickListener {
            isLogging = !isLogging
            startStopButton.text = if (isLogging) "Stop Logging" else "Start Logging"
            if (isLogging) handler.post(scanRunnable)
            else          handler.removeCallbacks(scanRunnable)
        }

        requestPermissions()

        // register for scan results
        registerReceiver(
            wifiScanReceiver,
            IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(wifiScanReceiver)
    }

    // 2) Permission handling
    private fun requestPermissions() {
        val perms = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        if (perms.any {
                ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
            }) {
            ActivityCompat.requestPermissions(this, perms, 0)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, results: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, results)
        // you could check results here and alert the user if denied
    }

    // 3) Start a new scan
    private fun startWifiScan() {
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            wifiManager.startScan()
        }
    }

    // 4) Record one RSSI reading into our 100‐slot buffer
    private fun recordRssi(level: Int) {
        val list = rssiData[selectedLocation]!!
        if (list.size < 100) list.add(level)
        else {
            list.removeAt(0)
            list.add(level)
        }
        updateMatrixDisplay()
        updateSummary()
    }

    // 5) Draw the 10×10 matrix
    private fun updateMatrixDisplay() {
        val list = rssiData[selectedLocation]!!
        val sb = StringBuilder()
        list.forEachIndexed { i, v ->
            sb.append(String.format("%4d", v))
            if ((i + 1) % 10 == 0) sb.append("\n")
        }
        matrixText.text = sb.toString()
    }

    // 6) Compute and show min, max, avg
    private fun updateSummary() {
        val (min, max, avg) = computeStats(rssiData[selectedLocation]!!)
        summaryText.text = "$min   /   $max   /   ${"%.1f".format(avg)}"
    }

    private fun computeStats(list: List<Int>): Triple<Int, Int, Double> {
        if (list.isEmpty()) return Triple(0, 0, 0.0)
        return Triple(
            list.minOrNull()!!,
            list.maxOrNull()!!,
            list.average()
        )
    }
}
