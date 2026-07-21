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



_Simple Android Workout Logger_

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


**_🛠️ Tech Stack & Architecture_**

## 🏗️ Architecture

GymLog follows the **MVVM (Model-View-ViewModel)** architecture pattern.


•Language: Kotlin - The officially recommended language for Android development.

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

•**LiveData**: To build data objects that notify views of any database changes, ensuring the UI is always up-to-date.

•**Coroutines**: For managing background threads and performing database operations asynchronously without blocking the main UI thread.





**🚀 Features**

•View All Workouts: A clear, scrollable list of all past workout entries.

•Add New Workouts: A simple form to quickly add a new exercise with its name, sets, reps, and weight.

•Edit Existing Workouts: Click on any workout in the list to open a detail screen where you can modify its details.

•Delete Workouts: Swipe to delete individual workouts or use the menu to delete all entries at once.

•Persistent Storage: All workout data is saved locally on the device using a Room database, so your data is safe even when the app is closed.

•Sort: User can sort workout by chiping on this week's workout, so that only that week's workout will be listed.

•AI: AI chatbot that help user about workouts or form or anything related to workout 




<img width="350"  hspace="250" alt="Screenshot_20260518_150535" src="https://github.com/user-attachments/assets/a38c4cc6-b218-4ec6-96d3-ab49067e0fc7" />






<img src = "https://github.com/user-attachments/assets/65610aff-2b0a-487e-880e-9df499f18bc8" width="350" hspace ="30" vspace="30">














<img src ="https://github.com/user-attachments/assets/a4ed7dc5-e7f8-4921-b168-de0e8583c4ee" width="300" hspace="30" vspace="30">







<img src= "https://github.com/user-attachments/assets/8f2f2fed-df32-412a-9d99-76b1732c2765" width="300"  hspace="30" vspace="30">








<img src = "https://github.com/user-attachments/assets/832e6a9c-9c56-4be7-b6f7-3bc977a85ed4" width = "300"  hspace="30" vspace="30">









<img src= "https://github.com/user-attachments/assets/52d728ba-a645-488e-91e1-c65cd87059dc" width = "300"  hspace="30" vspace="30">









<img src="https://github.com/user-attachments/assets/016054eb-2fa5-4555-a02e-5b30f6e91a03" width="300"  hspace="30" vspace="30">








<img src = "https://github.com/user-attachments/assets/1a687514-20ad-441a-b9da-45008991f60f" width="300"  hspace="30" vspace="30">
