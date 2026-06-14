package com.example.budgetnyuku

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

class SettingsActivity : AppCompatActivity() {

    private lateinit var db: DatabaseHelper
    private var userId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        db = DatabaseHelper(this)
        userId = intent.getIntExtra("USER_ID", -1)

        if (userId == -1) {
            finish()
            return
        }

        val spinnerCurrency = findViewById<Spinner>(R.id.spinnerCurrency)
        val currencies = listOf("ZAR (South African Rand)", "BRL (Brazilian Real)", "RUB (Russian Ruble)", "CNY (Chinese Yuan)")
        val currencyCodes = listOf("ZAR", "BRL", "RUB", "CNY")
        
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, currencies)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCurrency.adapter = adapter

        val currentCurrency = db.getCurrency(userId)
        val index = currencyCodes.indexOf(currentCurrency)
        if (index >= 0) spinnerCurrency.setSelection(index)

        findViewById<Button>(R.id.btnSave).setOnClickListener {
            val selectedCurrency = currencyCodes[spinnerCurrency.selectedItemPosition]
            val isNotificationEnabled = findViewById<Switch>(R.id.switchNotifications).isChecked
            
            if (db.updateCurrency(userId, selectedCurrency)) {
                if (isNotificationEnabled) {
                    scheduleDailyReminder()
                } else {
                    cancelDailyReminder()
                }
                Toast.makeText(this, "Settings saved!", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }

        findViewById<Button>(R.id.btnFAQ).setOnClickListener {
            startActivity(Intent(this, FAQActivity::class.java))
        }
    }

    private fun scheduleDailyReminder() {
        val workRequest = PeriodicWorkRequestBuilder<ReminderWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(12, TimeUnit.HOURS) // Start in 12 hours
            .build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "daily_reminder",
            androidx.work.ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )
    }

    private fun cancelDailyReminder() {
        WorkManager.getInstance(this).cancelUniqueWork("daily_reminder")
    }
}
