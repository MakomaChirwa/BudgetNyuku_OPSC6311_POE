# BudgetNyuku

## Overview

Budget Nyuku is a powerful personal finance management app designed to help users take control of their money. 
With features ranging from expense tracking to savings goals, budget planning, and gamification, 
it makes financial management engaging and effective.

BudgetNyuku's Key Highlights are that the app is:
100% Offline - All data stored locally using SQLite
No Subscriptions - Completely free to use
Privacy Focused - Your data never leaves your device
Gamified Experience - Earn points and badges for good financial habits
Multi-Currency Support - Switch between 7 different currencies

## Features

Budget Nyuku's core features include:
1. Expense & Income Tracking
- Add expenses with amount, category, date, time, and description. Add income entries. Optional photo attachment for receipts.
Categorize transactions with color-coded categories. Edit and delete transactions

2. Budget Management
- Set monthly minimum and maximum spending goals. Track spending against budget in real-time.
Visual progress bar showing budget status. Alerts when exceeding or falling below goals

3. Category Management
- Create custom expense categories. Choose from 8 pre-defined colors for categories. Edit and delete categories. 
Default categories included for quick start

4. Goal Tracker
Create savings goals (vacation, emergency fund, shopping, etc.). Set target amount and deadline. Track progress with visual progress bar. 
Add savings manually to goals. Automatic rewards when goals are completed

5. Reporting & Analytics
Bar chart showing spending by category. Filter by week, month, or year. Category spending breakdown with progress bars. 
Expense history with date range filtering. Total spending calculations

6. Gamification Features
Badge Name and How to Earn Points
🏆 Budget Hero - Stay within budget for a month - 50 stars
📝 Consistent Logger - Log 5+ expenses in a month - 20 stars
💰 Savings Master	Complete 3+ savings goals	100 stars
🎯 Goal Getter	Reach first savings goal	30 stars
🔥 Streak Keeper	Log expenses 7 days in a row	25 stars

8. Multi-Currency Support(Currency,Code,Symbol,Country Name)
South African Rand,	ZAR,	R,	South Africa
Brazilian Real,	BRL,	R$,	Brazil
Russian Ruble,	RUB,	₽,	Russia
Chinese Yuan,	CNY,	¥,	China

9. Smart Notifications
Daily reminders to log expenses (customizable time). Budget alerts when approaching limits. 
Goal completion celebrations. Achievement notifications

10. Smart Features
Budget Tips: 10+ practical money-saving tips
FAQ Section: Answers to common questions
Points System: Earn points for financial discipline
Badge Collection: Unlock achievements for milestones
Photo Receipts: Attach images to expenses

## Technologies Used

### Frontend
- Material Design 3

### Backend
- Kotlin

### Database
- SQLite (SQLiteOpenHelper)

### Tools
- Android Studio
- Git
- GitHub
Charts	MPAndroidChart v3.1.0
Minimum SDK	API 24 (Android 7.0)
Target SDK	API 34 (Android 14)

## System Requirements

Before running the application, ensure you have:

- Node.js (v18 or later)
- npm (v9 or later)
- MySQL Server
- Git

---

## Installation

### Clone the Repository

```bash
git clone https://github.com/username/repository-name.git
```

### Navigate to the Project Directory

```bash
cd repository-name
```

### Install Dependencies

```bash
npm install
```

### Configure Environment Variables

Create a `.env` file in the root directory and add:

```env
PORT=3000
DB_HOST=localhost
DB_USER=root
DB_PASSWORD=password
DB_NAME=database_name
JWT_SECRET=your_secret_key
```

### Start the Application

```bash
npm start
```

The application will be available at:

```text
http://localhost:3000
```

---

## Project Structure

BudgetNyuku/
├── app/
│   ├── src/main/
│   │   ├── java/com/example/budgetnyuku/
│   │   │   ├── activities/
│   │   │   │   ├── SplashActivity.kt
│   │   │   │   ├── NyukuLauncher.kt
│   │   │   │   ├── LogIn.kt
│   │   │   │   ├── Register.kt
│   │   │   │   ├── MainActivity.kt
│   │   │   │   ├── AddEditExpense.kt
│   │   │   │   ├── GoalTrackerActivity.kt
│   │   │   │   ├── TipsAndFAQActivity.kt
│   │   │   │   ├── CurrencySettingsActivity.kt
│   │   │   │   ├── NotificationSettingsActivity.kt
│   │   │   │   ├── DashboardFragment.kt
│   │   │   │   ├── AddTransactionFragment.kt
│   │   │   │   ├── CategoriesFragment.kt
│   │   │   │   ├── BudgetFragment.kt
│   │   │   │   ├── CategoryAdapter.kt
│   │   │   │   ├── ExpenseAdapter.kt
│   │   │   │   ├── GoalAdapter.kt
│   │   │   │   ├── TipAdapter.kt
│   │   │   │   ├── Category.kt
│   │   │   │   ├── Expense.kt
│   │   │   │   ├── Goal.kt
│   │   │   │   ├── Currency.kt
│   │   │   │   └── NotificationReceiver.kt
│   │   │   └── DatabaseHelper.kt
│   │   ├── res/
│   │   │   ├── layout/
│   │   │   │   ├── (25+ layout files)
│   │   │   ├── drawable/
│   │   │   │   ├── button_rounded_green.xml
│   │   │   │   └── button_rounded_red.xml
│   │   │   ├── xml/
│   │   │   │   └── file_paths.xml
│   │   │   └── values/
│   │   │       ├── colors.xml
│   │   │       ├── strings.xml
│   │   │       └── themes.xml
│   │   └── AndroidManifest.xml
│   └── build.gradle
└── (gradle files)

## Usage

1. Register a new account.
2. Log in using your credentials.
3. Access the dashboard.
4. Create, update, and manage records.
5. Generate reports and analytics.

---

## API Endpoints (Optional)

### Authentication

| Method | Endpoint | Description |
|----------|----------|-------------|
| POST | /api/auth/register | Register a new user |
| POST | /api/auth/login | Login user |

### Users

| Method | Endpoint | Description |
|----------|----------|-------------|
| GET | /api/users | Get all users |
| GET | /api/users/:id | Get user by ID |
| PUT | /api/users/:id | Update user |

---

## Testing

Run the test suite:

```bash
npm test
```

---

## Screenshots

### Home Page

Insert screenshot here.

### Dashboard

Insert screenshot here.

---

## Deployment

Example deployment platforms:

- Vercel
- Netlify
- Render
- AWS
- Azure

Deployment URL:

```text
https://your-application-url.com
```

---

## Known Issues

- List any current bugs or limitations.
- Mention features under development.

---

## Future Enhancements

- Two-factor authentication
- Dark mode
- Email notifications
- Mobile application support

---

## Contributors

| Name | Role |
|--------|--------|
| Your Name | Developer |
| Team Member | UI/UX Designer |

---

## License

This project is licensed under the MIT License.

---

## Contact

**Developer:** Your Name
**Email:** your.email@example.com
**GitHub:** https://github.com/MakomaChirwa

---

## Acknowledgements

- Open Source Community
- Project Supervisor
- Contributors and Testers

Made with Love for better financial management
Budget Nyuku - Take control of your finances, one expense at a time!
