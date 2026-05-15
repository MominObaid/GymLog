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
import com.example.gymlog.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint

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
        }

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_workouts -> {
                    showFragment(WorkoutListFragment())
                    binding.fabAiChat.show()
                    true
                }
                R.id.nav_stats -> {
                    val intent = android.content.Intent(this, ProgressChartActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.nav_ai -> {
                    if (binding.aiChatContainer.visibility != View.VISIBLE) {
                        revealChat()
                    }
                    true
                }
                else -> false
            }
        }

        binding.fabAiChat.setOnClickListener {
            revealChat()
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

    private fun revealChat() {
        val chatView = binding.aiChatContainer
        
        // Add fragment to the container
        supportFragmentManager.commit {
            replace(R.id.ai_chat_container, AiChatFragment(), "AI_CHAT_TAG")
        }

        val cx = binding.fabAiChat.x.toInt() + binding.fabAiChat.width / 2
        val cy = binding.fabAiChat.y.toInt() + binding.fabAiChat.height / 2

        val finalRadius = Math.hypot(chatView.width.toDouble(), chatView.height.toDouble()).toFloat()
        val anim = ViewAnimationUtils.createCircularReveal(chatView, cx, cy, 0f, finalRadius)
        
        chatView.visibility = View.VISIBLE
        binding.fabAiChat.hide()
        
        anim.duration = 500
        anim.start()
    }
    private fun hideChat() {
        val chatView = binding.aiChatContainer
        val cx = binding.fabAiChat.x.toInt() + binding.fabAiChat.width / 2
        val cy = binding.fabAiChat.y.toInt() + binding.fabAiChat.height / 2

        val initialRadius = Math.hypot(chatView.width.toDouble(), chatView.height.toDouble()).toFloat()
        val anim = ViewAnimationUtils.createCircularReveal(chatView, cx, cy, initialRadius, 0f)
        
        anim.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                chatView.visibility = View.INVISIBLE
                binding.fabAiChat.show()
                // Remove fragment to save resources
                supportFragmentManager.findFragmentByTag("AI_CHAT_TAG")?.let {
                    supportFragmentManager.commit { remove(it) }
                }
            }
        })
        anim.start()
    }
}
