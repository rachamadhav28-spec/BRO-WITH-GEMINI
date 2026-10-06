package com.bro.assistant.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bro.assistant.model.AssistantState
import com.bro.assistant.viewmodel.ChatMessage
import com.bro.assistant.viewmodel.MainViewModel

@Composable
fun MainScreen(viewModel: MainViewModel) {
    val currentState by viewModel.currentState.collectAsState()
    val messages by viewModel.messages.collectAsState()
    var inputText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0B0C10))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Action Bar with Title and New Chat
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "NAVI",
                color = Color(0xFF00E5FF),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            TextButton(
                onClick = { viewModel.startNewChat() },
                colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF00E5FF))
            ) {
                Text("+ New Chat", fontSize = 14.sp)
            }
        }

        // Center Visualizer Orb with dynamic color cycling
        Box(
            modifier = Modifier
                .weight(0.35f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            NaviVisualizerOrb(state = currentState)
        }

        // Chat Viewport
        LazyColumn(
            modifier = Modifier
                .weight(0.45f)
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            reverseLayout = false
        ) {
            items(messages, key = { it.id }) { msg ->
                ChatBubble(message = msg)
            }
        }

        // Bottom Input Controls with Upward Arrow Send Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = inputText,
                onValueChange = { inputText = it },
                placeholder = { Text("Command NAVI...", color = Color.Gray) },
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(24.dp)),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFF1F2833),
                    unfocusedContainerColor = Color(0xFF1F2833),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Upward Arrow Send Button
            Button(
                onClick = {
                    if (inputText.isNotBlank()) {
                        viewModel.sendMessage(inputText)
                        inputText = ""
                    }
                },
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier.size(48.dp)
            ) {
                Text("▲", color = Color.Black, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun NaviVisualizerOrb(state: AssistantState) {
    val infiniteTransition = rememberInfiniteTransition(label = "OrbTransitions")

    // Dynamic scale pulse
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Pulse"
    )

    // Smooth color cycling for IDLE mode (shifting through purple, cyan, violet, magenta)
    val idleColorCycle by infiniteTransition.animateColor(
        initialValue = Color(0xFF8A2BE2), // Deep Purple
        targetValue = Color(0xFF00E5FF),  // Neon Cyan
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "IdleColorCycle"
    )

    val orbColor by animateColorAsState(
        targetValue = when (state) {
            AssistantState.IDLE -> idleColorCycle
            AssistantState.LISTENING -> Color(0xFF00FF66)  // Bright Green
            AssistantState.THINKING -> Color(0xFFFFB700)   // Amber
            AssistantState.EXECUTING -> Color(0xFF9D00FF)  // Electric Purple
            AssistantState.SPEAKING -> Color(0xFF00B2FF)   // Sky Blue
            AssistantState.SUCCESS -> Color(0xFF00FFAD)    // Emerald
            AssistantState.ERROR -> Color(0xFFFF0055)      // Red
        },
        animationSpec = tween(500),
        label = "StateColor"
    )

    // Display "NAVI" when in IDLE mode
    val orbLabel = if (state == AssistantState.IDLE) "NAVI" else state.name

    Box(
        modifier = Modifier
            .size(140.dp)
            .scale(if (state == AssistantState.LISTENING || state == AssistantState.SPEAKING) pulseScale else 1.0f)
            .clip(CircleShape)
            .background(orbColor),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = orbLabel,
            color = Color.Black,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun ChatBubble(message: ChatMessage) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (message.isUser) 16.dp else 0.dp,
                        bottomEnd = if (message.isUser) 0.dp else 16.dp
                    )
                )
                .background(if (message.isUser) Color(0xFF00E5FF) else Color(0xFF1F2833))
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Text(
                text = message.text,
                color = if (message.isUser) Color.Black else Color.White,
                fontSize = 14.sp
            )
        }
    }
}
