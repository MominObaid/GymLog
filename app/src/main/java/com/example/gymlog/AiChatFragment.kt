package com.example.gymlog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.example.gymlog.databinding.FragmentAiChatBinding
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import dagger.hilt.android.AndroidEntryPoint

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

        val chatOverlay = binding.root
        val btnSendChat = chatOverlay.findViewById<MaterialButton>(R.id.btnSendChat)
        val etInput = chatOverlay.findViewById<EditText>(R.id.etChatInput)
        val tvContent = chatOverlay.findViewById<TextView>(R.id.tvChatContent)
        val toolbar = chatOverlay.findViewById<MaterialToolbar>(R.id.chatToolbar)

        btnSendChat.setOnClickListener {
            val message = etInput.text.toString()
            if (message.isNotEmpty()) {
                tvContent.append("\n\nYou: $message")
                tvContent.append("\n\nAI: Thinking...")
                workoutViewModel.askAi(message)
                etInput.text.clear()
            }
        }

        toolbar.setNavigationOnClickListener {
            closeChat()
        }

        workoutViewModel.aiResponse.observe(viewLifecycleOwner, Observer { response ->
            response?.let {
                val currentText = tvContent.text.toString()
                if (currentText.contains("AI: Thinking...")) {
                    val updateText = currentText.replace("AI: Thinking...", "AI: $it")
                    tvContent.text = updateText
                } else {
                    tvContent.append("\n\nAI: $it")
                }
                workoutViewModel.clearAiResponse()
            }
        })
    }

    private fun closeChat() {
        activity?.onBackPressedDispatcher?.onBackPressed()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
