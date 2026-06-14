package com.example.budgetnyuku

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNav: BottomNavigationView
    private var userId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        userId = intent.getIntExtra("USER_ID", -1)

        if (userId == -1) {
            finish()
            return
        }

        bottomNav = findViewById(R.id.bottom_navigation)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (bottomNav.selectedItemId != R.id.nav_dashboard) {
                    bottomNav.selectedItemId = R.id.nav_dashboard
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                    isEnabled = true
                }
            }
        })

        loadFragment(DashboardFragment.newInstance(userId))

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_dashboard -> {
                    loadFragment(DashboardFragment.newInstance(userId))
                    true
                }
                R.id.nav_add_transaction -> {
                    loadFragment(AddTransactionFragment.newInstance(userId))
                    true
                }
                R.id.nav_categories -> {
                    loadFragment(CategoriesFragment.newInstance(userId))
                    true
                }
                R.id.nav_budget -> {
                    loadFragment(BudgetFragment.newInstance(userId))
                    true
                }
                R.id.nav_goals -> {
                    loadFragment(GoalFragment.newInstance(userId))
                    true
                }
                else -> false
            }
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}
