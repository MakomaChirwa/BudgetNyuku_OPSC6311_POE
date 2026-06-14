package com.example.budgetnyuku

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, "BudgetDB", null, 7) {

    override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        onUpgrade(db, oldVersion, newVersion)
    }

    override fun onCreate(db: SQLiteDatabase) {
        // Users table
        db.execSQL("""
            CREATE TABLE users(
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT,
                email TEXT UNIQUE,
                password TEXT
            )
        """)

        // Categories table
        db.execSQL("""
            CREATE TABLE categories(
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER,
                name TEXT,
                color TEXT,
                FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE
            )
        """)

        // Expenses table
        db.execSQL("""
            CREATE TABLE expenses(
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER,
                category_id INTEGER,
                amount REAL,
                date TEXT,
                start_time TEXT,
                end_time TEXT,
                description TEXT,
                photo BLOB,
                FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE,
                FOREIGN KEY(category_id) REFERENCES categories(id) ON DELETE CASCADE
            )
        """)

        // Goals table
        db.execSQL("""
            CREATE TABLE goals(
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER,
                name TEXT,
                target_amount REAL,
                current_amount REAL DEFAULT 0,
                deadline TEXT,
                FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE
            )
        """)

        // User settings table
        db.execSQL("""
            CREATE TABLE user_settings(
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER UNIQUE,
                currency TEXT DEFAULT 'ZAR',
                FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE
            )
        """)

        // Budget settings table
        db.execSQL("""
            CREATE TABLE budget_settings(
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER UNIQUE,
                monthly_min_goal REAL,
                monthly_max_goal REAL,
                FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE
            )
        """)

        // User stats table for gamification
        db.execSQL("""
            CREATE TABLE user_stats(
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER UNIQUE,
                points INTEGER DEFAULT 0,
                FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE
            )
        """)

        // User badges table
        db.execSQL("""
            CREATE TABLE user_badges(
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER,
                badge_name TEXT,
                earned_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE
            )
        """)

        // Insert default categories
        val defaultCategories = listOf(
            "Food", "Transport", "Shopping", "Entertainment",
            "Bills", "Healthcare", "Education", "Other"
        )
        defaultCategories.forEach { category ->
            db.execSQL("INSERT INTO categories (user_id, name, color) VALUES (0, '$category', '#9E9E9E')")
        }
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS expenses")
        db.execSQL("DROP TABLE IF EXISTS categories")
        db.execSQL("DROP TABLE IF EXISTS budget_settings")
        db.execSQL("DROP TABLE IF EXISTS user_stats")
        db.execSQL("DROP TABLE IF EXISTS user_badges")
        db.execSQL("DROP TABLE IF EXISTS users")
        db.execSQL("DROP TABLE IF EXISTS goals")
        db.execSQL("DROP TABLE IF EXISTS user_settings")
        onCreate(db)
    }

    fun registerUser(name: String, email: String, password: String): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("name", name)
            put("email", email)
            put("password", password)
        }
        val result = db.insert("users", null, values)
        if (result != -1L) {
            // Initialize user stats
            val userId = result.toInt()
            val statsValues = ContentValues().apply {
                put("user_id", userId)
                put("points", 0)
            }
            db.insert("user_stats", null, statsValues)
        }
        return result != -1L
    }

    fun loginUser(email: String, password: String): Int? {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT id FROM users WHERE email=? AND password=?",
            arrayOf(email, password)
        )
        return if (cursor.moveToFirst()) {
            val userId = cursor.getInt(0)
            cursor.close()
            userId
        } else {
            cursor.close()
            null
        }
    }

    fun getUserName(userId: Int): String {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT name FROM users WHERE id=?", arrayOf(userId.toString()))
        return if (cursor.moveToFirst()) {
            val name = cursor.getString(0)
            cursor.close()
            name
        } else {
            cursor.close()
            ""
        }
    }

    fun addCategory(userId: Int, name: String, color: String): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("user_id", userId)
            put("name", name)
            put("color", color)
        }
        val result = db.insert("categories", null, values)
        return result != -1L
    }

    fun getCategories(userId: Int): List<Category> {
        val categories = mutableListOf<Category>()
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM categories WHERE user_id=? OR user_id=0 ORDER BY name",
            arrayOf(userId.toString())
        )

        while (cursor.moveToNext()) {
            categories.add(
                Category(
                    id = cursor.getInt(0),
                    userId = cursor.getInt(1),
                    name = cursor.getString(2),
                    color = cursor.getString(3)
                )
            )
        }
        cursor.close()
        return categories
    }

    fun updateCategory(categoryId: Int, name: String, color: String): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("name", name)
            put("color", color)
        }
        val result = db.update("categories", values, "id=?", arrayOf(categoryId.toString()))
        return result > 0
    }

    fun deleteCategory(categoryId: Int): Boolean {
        val db = writableDatabase
        val result = db.delete("categories", "id=?", arrayOf(categoryId.toString()))
        return result > 0
    }

    fun addExpense(expense: Expense): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("user_id", expense.userId)
            put("category_id", expense.categoryId)
            put("amount", expense.amount)
            put("date", expense.date)
            put("start_time", expense.startTime)
            put("end_time", expense.endTime)
            put("description", expense.description)
            expense.photo?.let { put("photo", it) }
        }
        val result = db.insert("expenses", null, values)
        if (result != -1L) {
            addUserPoints(expense.userId, 5)
        }
        return result != -1L
    }

    fun getExpensesByDateRange(userId: Int, startDate: String, endDate: String): List<Expense> {
        val expenses = mutableListOf<Expense>()
        val db = readableDatabase
        val cursor = db.rawQuery(
            """SELECT e.*, c.name as category_name, c.color as category_color 
               FROM expenses e
               JOIN categories c ON e.category_id = c.id
               WHERE e.user_id=? AND e.date BETWEEN ? AND ?
               ORDER BY e.date DESC, e.start_time DESC""",
            arrayOf(userId.toString(), startDate, endDate)
        )

        while (cursor.moveToNext()) {
            expenses.add(
                Expense(
                    id = cursor.getInt(0),
                    userId = cursor.getInt(1),
                    categoryId = cursor.getInt(2),
                    amount = cursor.getDouble(3),
                    date = cursor.getString(4),
                    startTime = cursor.getString(5),
                    endTime = cursor.getString(6),
                    description = cursor.getString(7),
                    photo = cursor.getBlob(8),
                    categoryName = cursor.getString(9),
                    categoryColor = cursor.getString(10)
                )
            )
        }
        cursor.close()
        return expenses
    }

    fun getCategoryTotals(userId: Int, startDate: String, endDate: String): List<CategoryTotal> {
        val totals = mutableListOf<CategoryTotal>()
        val db = readableDatabase
        val cursor = db.rawQuery(
            """SELECT c.id, c.name, c.color, COALESCE(SUM(e.amount), 0) as total
               FROM categories c
               LEFT JOIN expenses e ON c.id = e.category_id 
                   AND e.user_id=? AND e.date BETWEEN ? AND ?
               WHERE c.user_id=? OR c.user_id=0
               GROUP BY c.id
               HAVING total > 0
               ORDER BY total DESC""",
            arrayOf(userId.toString(), startDate, endDate, userId.toString())
        )

        while (cursor.moveToNext()) {
            totals.add(
                CategoryTotal(
                    categoryId = cursor.getInt(0),
                    categoryName = cursor.getString(1),
                    categoryColor = cursor.getString(2),
                    totalAmount = cursor.getDouble(3)
                )
            )
        }
        cursor.close()
        return totals
    }

    fun deleteExpense(expenseId: Int): Boolean {
        val db = writableDatabase
        val result = db.delete("expenses", "id=?", arrayOf(expenseId.toString()))
        return result > 0
    }

    fun saveBudgetSettings(userId: Int, minGoal: Double, maxGoal: Double): Boolean {
        val db = writableDatabase

        val cursor = db.rawQuery(
            "SELECT id FROM budget_settings WHERE user_id=?",
            arrayOf(userId.toString())
        )

        val result = if (cursor.moveToFirst()) {
            val values = ContentValues().apply {
                put("monthly_min_goal", minGoal)
                put("monthly_max_goal", maxGoal)
            }
            cursor.close()
            db.update("budget_settings", values, "user_id=?", arrayOf(userId.toString())) > 0
        } else {
            cursor.close()
            val values = ContentValues().apply {
                put("user_id", userId)
                put("monthly_min_goal", minGoal)
                put("monthly_max_goal", maxGoal)
            }
            db.insert("budget_settings", null, values) != -1L
        }
        return result
    }

    fun getBudgetSettings(userId: Int): Pair<Double, Double>? {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT monthly_min_goal, monthly_max_goal FROM budget_settings WHERE user_id=?",
            arrayOf(userId.toString())
        )
        return if (cursor.moveToFirst()) {
            val minGoal = cursor.getDouble(0)
            val maxGoal = cursor.getDouble(1)
            cursor.close()
            Pair(minGoal, maxGoal)
        } else {
            cursor.close()
            null
        }
    }

    fun getMonthlyTotal(userId: Int, yearMonth: String): Double {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT COALESCE(SUM(amount), 0) FROM expenses WHERE user_id=? AND substr(date,1,7)=?",
            arrayOf(userId.toString(), yearMonth)
        )
        return if (cursor.moveToFirst()) {
            val total = cursor.getDouble(0)
            cursor.close()
            total
        } else {
            cursor.close()
            0.0
        }
    }

    fun addUserPoints(userId: Int, points: Int) {
        val db = writableDatabase
        val cursor = db.rawQuery("SELECT points FROM user_stats WHERE user_id=?", arrayOf(userId.toString()))

        if (cursor.moveToFirst()) {
            val currentPoints = cursor.getInt(0)
            cursor.close()
            val values = ContentValues().apply {
                put("points", currentPoints + points)
            }
            db.update("user_stats", values, "user_id=?", arrayOf(userId.toString()))
        } else {
            cursor.close()
            val values = ContentValues().apply {
                put("user_id", userId)
                put("points", points)
            }
            db.insert("user_stats", null, values)
        }
    }

    fun getUserPoints(userId: Int): Int {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT points FROM user_stats WHERE user_id=?", arrayOf(userId.toString()))
        return if (cursor.moveToFirst()) {
            val points = cursor.getInt(0)
            cursor.close()
            points
        } else {
            cursor.close()
            0
        }
    }

    fun saveUserBadge(userId: Int, badgeName: String) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("user_id", userId)
            put("badge_name", badgeName)
        }
        db.insert("user_badges", null, values)
    }

    fun getUserBadges(userId: Int): List<String> {
        val badges = mutableListOf<String>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT badge_name FROM user_badges WHERE user_id=?", arrayOf(userId.toString()))
        while (cursor.moveToNext()) {
            badges.add(cursor.getString(0))
        }
        cursor.close()
        return badges
    }

    // --- Goal Methods ---
    fun addGoal(userId: Int, name: String, targetAmount: Double, deadline: String): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("user_id", userId)
            put("name", name)
            put("target_amount", targetAmount)
            put("current_amount", 0.0)
            put("deadline", deadline)
        }
        return db.insert("goals", null, values) != -1L
    }

    fun getGoals(userId: Int): List<Goal> {
        val goals = mutableListOf<Goal>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM goals WHERE user_id=?", arrayOf(userId.toString()))
        while (cursor.moveToNext()) {
            goals.add(Goal(
                id = cursor.getInt(0),
                userId = cursor.getInt(1),
                name = cursor.getString(2),
                targetAmount = cursor.getDouble(3),
                currentAmount = cursor.getDouble(4),
                deadline = cursor.getString(5)
            ))
        }
        cursor.close()
        return goals
    }

    fun updateGoalProgress(goalId: Int, newAmount: Double): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("current_amount", newAmount)
        }
        return db.update("goals", values, "id=?", arrayOf(goalId.toString())) > 0
    }

    fun deleteGoal(goalId: Int): Boolean {
        val db = writableDatabase
        return db.delete("goals", "id=?", arrayOf(goalId.toString())) > 0
    }

    // --- Currency Methods ---
    fun getCurrency(userId: Int): String {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT currency FROM user_settings WHERE user_id=?", arrayOf(userId.toString()))
        var currency = "ZAR"
        if (cursor.moveToFirst()) {
            currency = cursor.getString(0)
        }
        cursor.close()
        return currency
    }

    fun updateCurrency(userId: Int, currency: String): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("user_id", userId)
            put("currency", currency)
        }
        val result = db.insertWithOnConflict("user_settings", null, values, SQLiteDatabase.CONFLICT_REPLACE)
        return result != -1L
    }

    fun getCurrencySymbol(userId: Int): String {
        return when (getCurrency(userId)) {
            "BRL" -> "R$"
            "RUB" -> "₽"
            "CNY" -> "¥"
            else -> "R"
        }
    }
}