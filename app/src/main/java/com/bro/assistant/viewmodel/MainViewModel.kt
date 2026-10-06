package com.bro.assistant.viewmodel

import androidx.lifecycle.ViewModel
import com.bro.assistant.model.AssistantState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class ChatMessage(
    val id: Long = System.currentTimeMillis(),
    val sender: String,
    val text: String,
    val isUser: Boolean
)

class MainViewModel : ViewModel() {

    private val _currentState = MutableStateFlow(AssistantState.IDLE)
    val currentState: StateFlow<AssistantState> = _currentState.asStateFlow()

    private val initialGreeting = listOf(
        ChatMessage(sender = "NAVI", text = "Systems active. How can I help you today?", isUser = false)
    )

    private val _messages = MutableStateFlow<List<ChatMessage>>(initialGreeting)
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    fun updateState(newState: AssistantState) {
        _currentState.value = newState
    }

    fun sendMessage(text: String) {
        if (text.isBlank()) return
        
        val updated = _messages.value.toMutableList()
        updated.add(ChatMessage(sender = "You", text = text, isUser = true))
        _messages.value = updated

        // Simulate state transition for UI verification
        _currentState.value = AssistantState.THINKING
    }

    fun startNewChat() {
        _messages.value = listOf(
            ChatMessage(sender = "NAVI", text = "New conversation started.", isUser = false)
        )
        _currentState.value = AssistantState.IDLE
    }
}
