package com.example.budgetnyuku

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class ManageCategories : AppCompatActivity() {


    private lateinit var db: DatabaseHelper
    private var userId: Int = -1
    private lateinit var categoriesAdapter: CategoryAdapter
    private val categories = mutableListOf<Category>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_categories)

        db = DatabaseHelper(this)
        userId = intent.getIntExtra("USER_ID", -1)

        if (userId == -1) {
            Toast.makeText(this, "Error loading user", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val recyclerView = findViewById<RecyclerView>(R.id.rvCategories)
        recyclerView.layoutManager = LinearLayoutManager(this)
        categoriesAdapter = CategoryAdapter(categories, { category ->
            showDeleteConfirmDialog(category)
        }, { category ->
            showAddEditDialog(category)
        })
        recyclerView.adapter = categoriesAdapter

        findViewById<FloatingActionButton>(R.id.fabAddCategory).setOnClickListener {
            showAddEditDialog(null)
        }

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finish()
        }

        loadCategories()
    }

    private fun loadCategories() {
        categories.clear()
        categories.addAll(db.getCategories(userId))
        categoriesAdapter.notifyDataSetChanged()

        if (categories.isEmpty()) {
            findViewById<TextView>(R.id.tvNoCategories).visibility = android.view.View.VISIBLE
        } else {
            findViewById<TextView>(R.id.tvNoCategories).visibility = android.view.View.GONE
        }
    }

    private fun showAddEditDialog(category: Category?) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_category, null)
        val etName = dialogView.findViewById<EditText>(R.id.etCategoryName)
        val colorPicker = dialogView.findViewById<Spinner>(R.id.spinnerColor)

        val colors = listOf("#2196F3", "#4CAF50", "#FF9800", "#F44336", "#9C27B0", "#00BCD4", "#795548", "#607D8B")
        val colorNames = listOf("Blue", "Green", "Orange", "Red", "Purple", "Cyan", "Brown", "Gray")

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, colorNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        colorPicker.adapter = adapter

        if (category != null) {
            etName.setText(category.name)
            val index = colors.indexOf(category.color)
            if (index >= 0) colorPicker.setSelection(index)
        }

        AlertDialog.Builder(this)
            .setTitle(if (category == null) "Add Category" else "Edit Category")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val name = etName.text.toString().trim()
                if (name.isEmpty()) {
                    Toast.makeText(this, "Please enter category name", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val selectedColor = colors[colorPicker.selectedItemPosition]
                if (category == null) {
                    if (db.addCategory(userId, name, selectedColor)) {
                        Toast.makeText(this, "Category added", Toast.LENGTH_SHORT).show()
                        loadCategories()
                    } else {
                        Toast.makeText(this, "Failed to add category", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    if (db.updateCategory(category.id, name, selectedColor)) {
                        Toast.makeText(this, "Category updated", Toast.LENGTH_SHORT).show()
                        loadCategories()
                    } else {
                        Toast.makeText(this, "Failed to update category", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showDeleteConfirmDialog(category: Category) {
        AlertDialog.Builder(this)
            .setTitle("Delete Category")
            .setMessage("Are you sure you want to delete '${category.name}'? All expenses in this category will also be deleted.")
            .setPositiveButton("Delete") { _, _ ->
                if (db.deleteCategory(category.id)) {
                    Toast.makeText(this, "Category deleted", Toast.LENGTH_SHORT).show()
                    loadCategories()
                } else {
                    Toast.makeText(this, "Failed to delete category", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}