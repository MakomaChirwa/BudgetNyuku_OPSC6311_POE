package com.example.budgetnyuku

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import java.util.regex.Pattern

class Register : AppCompatActivity() {

    private lateinit var db: DatabaseHelper

    private val PASSWORD_PATTERN = Pattern.compile(
        "^(?=.*[0-9])" +
                "(?=.*[a-z])" +
                "(?=.*[A-Z])" +
                "(?=.*[@#$%^&+=!])" +
                "(?=\\S+$)" +
                ".{8,}"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        db = DatabaseHelper(this)

        val etName = findViewById<EditText>(R.id.etName)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val etConfirmPassword = findViewById<EditText>(R.id.etConfirmPassword)
        val btnRegister = findViewById<Button>(R.id.btnRegister)
        val btnBack = findViewById<ImageButton>(R.id.btnBack)

        btnBack.setOnClickListener { finish() }

        btnRegister.setOnClickListener {
            val name = etName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val confirmPassword = etConfirmPassword.text.toString().trim()

            when {
                name.isEmpty() -> {
                    etName.error = "Please enter your name"
                    Toast.makeText(this, "Please enter your name", Toast.LENGTH_SHORT).show()
                }
                email.isEmpty() -> {
                    etEmail.error = "Please enter your email"
                    Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show()
                }
                !isValidEmail(email) -> {
                    etEmail.error = "Please enter a valid email address"
                    Toast.makeText(this, "Invalid email format", Toast.LENGTH_SHORT).show()
                }
                password.isEmpty() -> {
                    etPassword.error = "Please enter a password"
                    Toast.makeText(this, "Please enter a password", Toast.LENGTH_SHORT).show()
                }
                !isValidPassword(password) -> {
                    etPassword.error = "Password must be 8+ chars with uppercase, lowercase, number & special char"
                    Toast.makeText(this, "Password must be at least 8 characters and contain:\n- Uppercase letter\n- Lowercase letter\n- Number\n- Special character (!@#$%^&+=)", Toast.LENGTH_LONG).show()
                }
                password != confirmPassword -> {
                    etConfirmPassword.error = "Passwords do not match"
                    Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    val success = db.registerUser(name, email, password)
                    if (success) {
                        Toast.makeText(this, "Registration successful! Please login.", Toast.LENGTH_LONG).show()
                        startActivity(Intent(this, LogIn::class.java))
                        finish()
                    } else {
                        Toast.makeText(this, "Email already exists or registration failed", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun isValidEmail(email: String): Boolean {
        val emailPattern = Pattern.compile(
            "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
        )
        return emailPattern.matcher(email).matches()
    }

    private fun isValidPassword(password: String): Boolean {
        return PASSWORD_PATTERN.matcher(password).matches()
    }
}