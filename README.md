# 💪 GymLog — Modern Android Workout & Exercise Companion

GymLog is a feature-packed, modern native Android application built with **Kotlin**, **Material Design 3**, **MVVM Architecture**, **Hilt**, **Room Database**, **Health Connect**, **WorkManager**, and **AI Fitness Coaching**.

Designed specifically for real-world gym environments, GymLog provides **large, high-contrast, gym-friendly controls** for fatigued hands, automatic **Personal Record (PR) detection**, **Exercise Technique Guides with Video Demonstrations**, **Streak Tracking**, and **Data-Driven Progress Analytics**.

---

## ✨ Key Features & Capabilities

### 📱 1. Gym-Friendly Active Session Logging
- **Frictionless Controls**: Large `+2.5kg` / `-2.5kg` and `+1` / `-1 rep` quick-adjust buttons so athletes don't need to type on small software keyboards while fatigued.
- **One-Tap DONE Toggle**: High-contrast, tactile set completion button with visual green feedback.
- **Set Removal**: One-tap delete button on individual set rows.
- **Rest Timer**: Integrated rest timer with background notifications and a 1-tap "Skip Rest" action.

### 📚 2. Exercise & Technique Guide Library (5th Navigation Tab)
- **Comprehensive Technique Database**: Searchable, muscle-categorized library covering Chest, Back, Legs, Shoulders, Arms, and Core.
- **Form Guides**: Detailed Setup Instructions, Right Technique Execution, Pro Tips, and Common Mistakes to Avoid.
- **Video Tutorials**: Instant video demonstration launcher opening native YouTube/web video tutorials.

### 🏠 3. Smart Dashboard & Quick Log
- **Today's Workout Suggestion**: Recommends the next uncompleted routine or lets you resume active sessions with 1 tap.
- **Streak & PR Tracking**: Live streak counter and Record Weight / Top Lift display.
- **Unified Quick Log**: Single-exercise logging that automatically syncs into streaks, weekly volume, and analytics.

### 📊 4. Progress Analytics & Milestones
- **Visual Progress Charts**: Interactive strength trends, 1RM estimations, and volume history using `MPAndroidChart`.
- **Plateau Detection**: Smart warnings when progress on specific lifts stalls across multiple sessions.
- **Exercise Breakdown**: Max weight tracking and most frequent exercise statistics.

### 🤖 5. AI Workout Generator & Coach
- **AI Routine Generator**: Generates custom, multi-day workout routines based on user goals, equipment, and experience.
- **Post-Workout Analysis**: AI-powered review evaluating fatigue, volume, and recovery after completed workouts.
- **Exercise Substitutions**: Recommends equipment-matched alternatives for busy gym equipment.

### 🌙 6. 100% Night Mode (Dark Theme) Compatible
- Full **Material 3 Dark Theme** palette (`res/values-night/colors.xml` & `themes.xml`).
- High-contrast, elevated dark surface containers, visible card outlines (`#3D374A`), and crisp `#F4F0F8` typography.

---

## 🏗️ Architecture & Technology Stack

GymLog adheres strictly to **Android Recommended Architecture Guidelines** (MVVM, Clean Architecture, Repository Pattern).

| Layer / Domain | Technology Used |
| :--- | :--- |
| **Language** | [Kotlin 1.9+](https://kotlinlang.org/) |
| **UI Framework** | Material Design 3, ViewBinding, CoordinatorLayout, ConstraintLayout |
| **Navigation** | Jetpack Navigation Component (SafeArgs, BottomNavigationView, 5 Tabs) |
| **Architecture** | MVVM, Clean Architecture (Domain Use-Cases, Repository Pattern) |
| **Dependency Injection** | [Hilt / Dagger 2](https://developer.android.com/training/dependency-injection/hilt-android) |
| **Local Database** | [Room Database](https://developer.android.com/training/data-storage/room) (SQLite, Flow, DAOs, Transactions) |
| **Asynchronous Programming** | Kotlin Coroutines, StateFlow, SharedFlow, LiveData |
| **Networking & AI** | Retrofit 2, Moshi, Gemini AI API integration |
| **Health Integration** | [Health Connect SDK](https://developer.android.com/health-and-fitness/guides/health-connect) (Steps, Weight, Exercise Sessions) |
| **Background Scheduling** | [WorkManager](https://developer.android.com/topic/libraries/architecture/workmanager) (Periodic Reminders, Smart Streak Alerts) |
| **Data Visualization** | [MPAndroidChart](https://github.com/PhilJay/MPAndroidChart) |

---

## 📁 Project Structure

```
com.example.gymlog
├── api                  # Retrofit API interfaces & Gemini AI models
├── auth                 # Authentication Manager
├── di                   # Hilt Dependency Injection Modules (Database, Network)
├── domain
│   └── usecase          # Clean Architecture Use Cases (FinishWorkout, StartWorkout, etc.)
├── health               # Health Connect Manager & Permissions
├── model                # Room Entities, DAOs, and Data Classes
├── notifications        # WorkManager Workers (SmartReminderWorker, WorkoutReminderWorker)
├── service              # RestTimerService (Foreground Service)
├── sync                 # Cloud Sync Manager (Firestore integration)
├── utils                # NotificationHelper, StreakCalculator
├── widget               # Workout Home Screen Widget Provider
└── ui                   # Activity & Fragment UI Screens
    ├── ExerciseGuideFragment.kt
    ├── WorkoutListFragment.kt (Dashboard)
    ├── RoutineListFragment.kt
    ├── WorkoutSessionFragment.kt
    ├── AnalyticsFragment.kt
    ├── ProfileFragment.kt
    └── AiChatFragment.kt
```

---

## 🚀 Getting Started

1. **Clone the Repository**:
   ```bash
   git clone https://github.com/your-username/GymLog.git
   cd GymLog
   ```

2. **Open in Android Studio**:
   - Open Android Studio (Ladybug / Jellyfish or newer).
   - Sync Gradle dependencies (`Sync Project with Gradle Files`).

3. **Build and Run**:
   - Select the `:app` configuration.
   - Run on an Android Emulator or physical device running Android 7.0 (API 24) or higher.

---

## 📄 License

```
Copyright 2026 GymLog

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0
```
