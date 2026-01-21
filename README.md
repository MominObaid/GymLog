GymLog - Simple Android Workout Logger

GymLog is a lightweight and straightforward Android application designed to help users log their daily workout sessions. 

It provides a clean interface to add, view, update, and delete workout entries, making it easy to track your fitness progress over time.

This project is built using modern Android development practices and serves as a great example of a database-driven mobile application.

🛠️ Tech Stack & Architecture

This project follows the official Android recommended architecture and utilizes 

the following technologies:

•Language: Kotlin - The officially recommended language for Android development.

•Architecture: MVVM (Model-View-ViewModel) - A robust architectural pattern that separates the UI from the business logic.

•UI:•XML Layouts with ViewBinding: To safely and easily interact with views.

•Fragments: For creating modular and reusable UI components.

•RecyclerView: To efficiently display a long list of workouts.

•Android Jetpack Components:

•Navigation Component: To handle all in-app navigation and pass data between screens in a type-safe manner using Safe Args.

•Room Database: For robust, local persistence of workout data.

•ViewModel: To manage UI-related data in a lifecycle-conscious way.

•LiveData: To build data objects that notify views of any database changes, ensuring the UI is always up-to-date.

•Coroutines: For managing background threads and performing database operations asynchronously without blocking the main UI thread.
