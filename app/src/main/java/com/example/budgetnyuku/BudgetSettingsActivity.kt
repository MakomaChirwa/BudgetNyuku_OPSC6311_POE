package com.example.budgetnyuku

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.*

class BudgetSettingsActivity : AppCompatActivity() {

    private lateinit var db: DatabaseHelper
    private var userId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_budget_settings)

        db = DatabaseHelper(this)
        userId = intent.getIntExtra("USER_ID", -1)

        if (userId == -1) {
            Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val etMinGoal = findViewById<EditText>(R.id.etMinGoal)
        val etMaxGoal = findViewById<EditText>(R.id.etMaxGoal)
        val btnSave = findViewById<Button>(R.id.btnSaveBudget)
        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        
        val currencySymbol = db.getCurrencySymbol(userId)
        findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.tilMinGoal).prefixText = currencySymbol
        findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.tilMaxGoal).prefixText = currencySymbol

        val settings = db.getBudgetSettings(userId)
        if (settings != null) {
            etMinGoal.setText(settings.first.toString())
            etMaxGoal.setText(settings.second.toString())
        }

        btnSave.setOnClickListener {
            val minGoal = etMinGoal.text.toString().toDoubleOrNull()
            val maxGoal = etMaxGoal.text.toString().toDoubleOrNull()

            if (minGoal == null || maxGoal == null) {
                Toast.makeText(this, "Please enter valid amounts", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (minGoal >= maxGoal) {
                Toast.makeText(this, "Minimum goal must be less than maximum goal", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (db.saveBudgetSettings(userId, minGoal, maxGoal)) {
                Toast.makeText(this, "Budget settings saved", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Failed to save settings", Toast.LENGTH_SHORT).show()
            }
        }

        btnBack.setOnClickListener { finish() }
    }
}