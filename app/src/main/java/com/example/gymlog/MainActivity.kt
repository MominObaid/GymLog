package com.example.gymlog

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.os.Bundle
import android.view.View
import android.view.ViewAnimationUtils
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.gymlog.databinding.ActivityMainBinding
import com.example.gymlog.notifications.SmartReminderWorker
import com.example.gymlog.notifications.WorkoutReminderWorker
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            supportFragmentManager.commit {
                replace(R.id.fragment_container, WorkoutListFragment())
            }
            scheduleWorkoutReminders()
        }

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            if (binding.aiChatContainer.visibility == View.VISIBLE) {
                hideChat()
            }
            
            when (item.itemId) {
                R.id.nav_workouts -> {
                    showFragment(WorkoutListFragment())
                    true
                }
                R.id.nav_routines -> {
                    showFragment(RoutineListFragment())
                    true
                }
                R.id.nav_stats -> {
                    showFragment(AnalyticsFragment())
                    true
                }
                R.id.nav_profile -> {
                    showFragment(ProfileFragment())
                    true
                }
                else -> false
            }
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (binding.aiChatContainer.visibility == View.VISIBLE) {
                    hideChat()
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                    isEnabled = true
                }
            }
        })
    }

    private fun showFragment(fragment: Fragment) {
        supportFragmentManager.commit {
            replace(R.id.fragment_container, fragment)
        }
    }

    fun configureFab(iconRes: Int?, text: String?, onClick: View.OnClickListener?) {
        if (iconRes == null || text == null || onClick == null) {
            binding.primaryFab.hide()
        } else {
            binding.primaryFab.setIconResource(iconRes)
            binding.primaryFab.text = text
            binding.primaryFab.setOnClickListener(onClick)
            binding.primaryFab.show()
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
        supportFragmentManager.commit {
            replace(R.id.ai_chat_container, AiChatFragment(), "AI_CHAT_TAG")
        }

        val cx = binding.primaryFab.x.toInt() + binding.primaryFab.width / 2
        val cy = binding.primaryFab.y.toInt() + binding.primaryFab.height / 2

        val finalRadius = Math.hypot(chatView.width.toDouble(), chatView.height.toDouble()).toFloat()
        val anim = ViewAnimationUtils.createCircularReveal(chatView, cx, cy, 0f, finalRadius)
        
        chatView.visibility = View.VISIBLE
        binding.primaryFab.hide()
        
        anim.duration = 500
        anim.start()
    }

    fun hideChat() {
        val chatView = binding.aiChatContainer
        val cx = binding.primaryFab.x.toInt() + binding.primaryFab.width / 2
        val cy = binding.primaryFab.y.toInt() + binding.primaryFab.height / 2

        val initialRadius = Math.hypot(chatView.width.toDouble(), chatView.height.toDouble()).toFloat()
        val anim = ViewAnimationUtils.createCircularReveal(chatView, cx, cy, initialRadius, 0f)
        
        anim.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                chatView.visibility = View.INVISIBLE
                binding.primaryFab.show()
                // Remove fragment to save resources
                supportFragmentManager.findFragmentByTag("AI_CHAT_TAG")?.let {
                    supportFragmentManager.commit { remove(it) }
                }
            }
        })
        anim.start()
    }
}
