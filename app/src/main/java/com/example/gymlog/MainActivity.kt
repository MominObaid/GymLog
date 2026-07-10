package com.example.gymlog

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.os.Bundle
import android.view.View
import android.view.ViewAnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.gymlog.databinding.ActivityMainBinding
import com.example.gymlog.notifications.SmartReminderWorker
import com.example.gymlog.notifications.WorkoutReminderWorker
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        
        binding.bottomNavigation.setupWithNavController(navController)

        if (savedInstanceState == null) {
            scheduleWorkoutReminders()
        }

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            if (binding.aiChatContainer.visibility == View.VISIBLE) {
                hideChat()
            }
            // Use navigation component for tab switching
            // The item IDs in bottom_nav_menu.xml match the fragment IDs in nav_graph.xml
            navController.navigate(item.itemId)
            true
        }
    }

    private fun scheduleWorkoutReminders() {
        val reminderRequest = PeriodicWorkRequestBuilder<WorkoutReminderWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(1, TimeUnit.HOURS)
            .build()

        val smartRequest = PeriodicWorkRequestBuilder<SmartReminderWorker>(12, TimeUnit.HOURS)
            .build()

        val workManager = WorkManager.getInstance(this)
        
        workManager.enqueueUniquePeriodicWork(
            "workout_reminder",
            androidx.work.ExistingPeriodicWorkPolicy.KEEP,
            reminderRequest
        )

        workManager.enqueueUniquePeriodicWork(
            "smart_streak_reminder",
            androidx.work.ExistingPeriodicWorkPolicy.KEEP,
            smartRequest
        )
    }

    fun revealChat() {
        val chatView = binding.aiChatContainer
        
        // Add fragment to the container
        supportFragmentManager.beginTransaction()
            .replace(R.id.ai_chat_container, AiChatFragment(), "AI_CHAT_TAG")
            .commit()

        val cx = chatView.width / 2
        val cy = chatView.height / 2

        val finalRadius = Math.hypot(chatView.width.toDouble(), chatView.height.toDouble()).toFloat()
        val anim = ViewAnimationUtils.createCircularReveal(chatView, cx, cy, 0f, finalRadius)
        
        chatView.visibility = View.VISIBLE
        
        anim.duration = 500
        anim.start()
    }

    fun hideChat() {
        val chatView = binding.aiChatContainer
        val cx = chatView.width / 2
        val cy = chatView.height / 2

        val initialRadius = Math.hypot(chatView.width.toDouble(), chatView.height.toDouble()).toFloat()
        val anim = ViewAnimationUtils.createCircularReveal(chatView, cx, cy, initialRadius, 0f)
        
        anim.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                chatView.visibility = View.INVISIBLE
                // Remove fragment to save resources
                supportFragmentManager.findFragmentByTag("AI_CHAT_TAG")?.let {
                    supportFragmentManager.beginTransaction().remove(it).commit()
                }
            }
        })
        anim.start()
    }
}
