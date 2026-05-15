package com.example.gymlog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.gymlog.databinding.FragmentAiChatBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AiChatFragment : Fragment() {

    private var _binding: FragmentAiChatBinding? = null
    private val binding get() = _binding!!

    private val workoutViewModel: WorkoutViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAiChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val chatContent = binding.chatContent

        chatContent.btnSendChat.setOnClickListener {
            val message = chatContent.etChatInput.text.toString()
            if (message.isNotEmpty()) {
                chatContent.tvChatContent.append("\n\nYou: $message")
                chatContent.tvChatContent.append("\n\nAI: Thinking...")
                workoutViewModel.askAi(message)
                chatContent.etChatInput.text.clear()
            }
        }

        chatContent.chatToolbar.setNavigationOnClickListener {
            closeChat()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                workoutViewModel.aiResponseEvent.collect { response ->
                    val currentText = chatContent.tvChatContent.text.toString()
                    if (currentText.contains("AI: Thinking...")) {
                        val updateText = currentText.replace("AI: Thinking...", "AI: $response")
                        chatContent.tvChatContent.text = updateText
                    } else {
                        chatContent.tvChatContent.append("\n\nAI: $response")
                    }
                }
            }
        }
    }

    private fun closeChat() {
        activity?.onBackPressedDispatcher?.onBackPressed()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
