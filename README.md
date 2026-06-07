# 💪 GymLog

A modern Android fitness tracking application built with **Kotlin**, **MVVM**, **Room**, **Hilt**, **Health Connect**, **WorkManager**, and **AI-powered coaching features**.

GymLog helps users track workouts, monitor progress, manage training routines, analyze performance trends, and receive intelligent fitness insights—all within a clean and scalable Android architecture.

---

## 📱 Features

### 🏠 Daily Hub (Dashboard)
- Personalized greetings and dynamic date display
- Glanceable widgets for current workout streak and total sessions
- Health Integration: Sync daily steps and weight directly from **Health Connect**
- Quick Actions: One-tap access to routines or manual logging

### 📋 Routine Management
- Create and edit reusable workout routines
- Organize training splits (Push/Pull/Legs, Upper/Lower, Full Body)
- **AI Routine Generator**: Describe your goal, and the AI builds a structured routine for you
- Custom Rest Timers: Set default rest intervals for each routine

### 🏋️ Active Session Tracking
- Real-time logging of sets, reps, and weight
- Interactive visual rest timer with notifications
- **PR Alerts**: Automatic detection and celebration of new Personal Records
- Safety First: Confirmation dialogs to prevent accidental session discards

### 📈 Progress Analytics
- Visual volume trends with interactive line charts
- Top Strengths: Identification of strongest exercises by max weight
- Plateau Detection: Smart alerts when progress on specific exercises stalls
- Complete session history with swipe-to-delete management

### 🤖 AI Fitness Assistant
- Built-in AI coach that understands your profile and recent stats
- Data-driven recommendations to improve form or break plateaus
- Fitness-related Q&A support

### 👤 User Profile
- Comprehensive storage for age, height, weight, and target weight
- Preferences for experience level, frequency, and available equipment

---

## 🏗️ Architecture

GymLog follows the **MVVM (Model-View-ViewModel)** architecture pattern.

```text
UI (Fragments/Activities)
        ↓
     ViewModel
        ↓
    Repository
        ↓
 Room Database / APIs
```

### Architecture Components

- MVVM
- Repository Pattern
- Dependency Injection (Hilt)
- Kotlin Coroutines & Flow
- ViewBinding
- Navigation Component
- Room Database
- WorkManager
- Health Connect

---

## 🛠️ Tech Stack

- **Language**: [Kotlin](https://kotlinlang.org/)
- **Dependency Injection**: [Hilt](https://developer.android.com/training/dependency-injection/hilt-android)
- **Local Database**: [Room](https://developer.android.com/training/data-storage/room)
- **Networking**: [Retrofit](https://square.github.io/retrofit/) & [Moshi](https://github.com/square/moshi) (Gemini AI API)
- **Background Tasks**: [WorkManager](https://developer.android.com/topic/libraries/architecture/workmanager)
- **Charts**: [MPAndroidChart](https://github.com/PhilJay/MPAndroidChart)
- **Health**: [Health Connect](https://developer.android.com/health-and-fitness/guides/health-connect)

---

## 🚀 Roadmap

### Phase 1 & 2 (Completed)
- [x] Workout & Manual Logging
- [x] Routine Editor & Management
- [x] **Home Dashboard Hub**
- [x] **Active Workout Session Mode**
- [x] **AI Routine Generator & Assistant**
- [x] **Personal Records (PR) Tracking**
- [x] **Milestone & Streak System**
- [x] Health Connect integration
- [x] Progress Analytics & Charts

### Phase 3
- [ ] Advanced Analytics (Volume per Muscle Group)
- [ ] AI-Powered Smart Progression Engine
- [ ] Exercise Video Library Integration
- [ ] Recovery & Fatigue Recommendations

---

## 📸 Screenshots

| Dashboard | Workout Library | Progress Tracking |
| :---: | :---: | :---: |
| ![Dashboard](https://github.com/user-attachments/assets/70070129-cb0f-4d8c-81e8-91aa9389555e) | ![Routines](https://github.com/user-attachments/assets/4ba5a6f1-4219-49cc-bfde-78ea63aa11ec) | ![Progress](#) |

---

## 👨‍💻 Author

**Momin Obaid**
Android Developer

[GitHub Profile](https://github.com/MominObaid)

---
*Built with ❤️ for the fitness community.*
