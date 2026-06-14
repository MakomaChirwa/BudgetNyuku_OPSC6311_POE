package com.example.budgetnyuku

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class CategoriesFragment : Fragment() {

    private lateinit var db: DatabaseHelper
    private var userId: Int = -1
    private lateinit var categoriesAdapter: CategoryAdapter
    private val categories = mutableListOf<Category>()

    companion object {
        fun newInstance(userId: Int): CategoriesFragment {
            val fragment = CategoriesFragment()
            val args = Bundle()
            args.putInt("USER_ID", userId)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_categories, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = DatabaseHelper(requireContext())
        userId = arguments?.getInt("USER_ID") ?: -1

        if (userId == -1) {
            Toast.makeText(requireContext(), "Error loading user", Toast.LENGTH_SHORT).show()
            return
        }

        val recyclerView = view.findViewById<RecyclerView>(R.id.rvCategories)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        categoriesAdapter = CategoryAdapter(categories, { category ->
            showDeleteConfirmDialog(category)
        }, { category ->
            showAddEditDialog(category)
        })
        recyclerView.adapter = categoriesAdapter

        view.findViewById<FloatingActionButton>(R.id.fabAddCategory).setOnClickListener {
            showAddEditDialog(null)
        }

        view.findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            (requireActivity() as? MainActivity)?.findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottom_navigation)?.selectedItemId = R.id.nav_dashboard
        }

        loadCategories()
    }

    private fun loadCategories() {
        categories.clear()
        categories.addAll(db.getCategories(userId))
        categoriesAdapter.notifyDataSetChanged()

        if (categories.isEmpty()) {
            view?.findViewById<TextView>(R.id.tvNoCategories)?.visibility = View.VISIBLE
        } else {
            view?.findViewById<TextView>(R.id.tvNoCategories)?.visibility = View.GONE
        }
    }

    private fun showAddEditDialog(category: Category?) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_category, null)
        val etName = dialogView.findViewById<EditText>(R.id.etCategoryName)
        val colorPicker = dialogView.findViewById<Spinner>(R.id.spinnerColor)

        val colors = listOf("#2196F3", "#4CAF50", "#FF9800", "#F44336", "#9C27B0", "#00BCD4", "#795548", "#607D8B")
        val colorNames = listOf("Blue", "Green", "Orange", "Red", "Purple", "Cyan", "Brown", "Gray")

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, colorNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        colorPicker.adapter = adapter

        if (category != null) {
            etName.setText(category.name)
            val index = colors.indexOf(category.color)
            if (index >= 0) colorPicker.setSelection(index)
        }

        AlertDialog.Builder(requireContext())
            .setTitle(if (category == null) "Add Category" else "Edit Category")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val name = etName.text.toString().trim()
                if (name.isEmpty()) {
                    Toast.makeText(requireContext(), "Please enter category name", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val selectedColor = colors[colorPicker.selectedItemPosition]
                if (category == null) {
                    if (db.addCategory(userId, name, selectedColor)) {
                        Toast.makeText(requireContext(), "Category added", Toast.LENGTH_SHORT).show()
                        loadCategories()
                    } else {
                        Toast.makeText(requireContext(), "Failed to add category", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    if (db.updateCategory(category.id, name, selectedColor)) {
                        Toast.makeText(requireContext(), "Category updated", Toast.LENGTH_SHORT).show()
                        loadCategories()
                    } else {
                        Toast.makeText(requireContext(), "Failed to update category", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showDeleteConfirmDialog(category: Category) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Category")
            .setMessage("Are you sure you want to delete '${category.name}'? All expenses in this category will also be deleted.")
            .setPositiveButton("Delete") { _, _ ->
                if (db.deleteCategory(category.id)) {
                    Toast.makeText(requireContext(), "Category deleted", Toast.LENGTH_SHORT).show()
                    loadCategories()
                } else {
                    Toast.makeText(requireContext(), "Failed to delete category", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}