package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ChatMessage
import com.example.ui.components.GlassmorphicCard
import com.example.viewmodel.AiCoachUiState
import com.example.viewmodel.LifeSyncViewModel

@Composable
fun CoachScreen(
    viewModel: LifeSyncViewModel,
    modifier: Modifier = Modifier
) {
    var activeSubTab by remember { mutableStateOf("Insights") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // AI Hero Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(
                    androidx.compose.ui.graphics.Brush.horizontalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.secondary
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "AI",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Column {
                    Text(
                        text = "LifeSync AI Coach",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Analyzing habits, hydration, and moods to craft your ultimate routine.",
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }
            }
        }

        // Tab Switcher
        TabRow(
            selectedTabIndex = if (activeSubTab == "Insights") 0 else 1,
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
        ) {
            Tab(
                selected = activeSubTab == "Insights",
                onClick = { activeSubTab = "Insights" },
                text = { Text("Daily Audit Report", fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                icon = { Icon(Icons.Default.Analytics, contentDescription = "Insights", modifier = Modifier.size(18.dp)) },
                modifier = Modifier.testTag("coach_insights_tab")
            )
            Tab(
                selected = activeSubTab == "Chat",
                onClick = { activeSubTab = "Chat" },
                text = { Text("Interactive Chat", fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                icon = { Icon(Icons.Default.Forum, contentDescription = "Chat", modifier = Modifier.size(18.dp)) },
                modifier = Modifier.testTag("coach_chat_tab")
            )
        }

        if (activeSubTab == "Insights") {
            InsightsTabContent(viewModel = viewModel, modifier = Modifier.weight(1f))
        } else {
            ChatTabContent(viewModel = viewModel, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun InsightsTabContent(
    viewModel: LifeSyncViewModel,
    modifier: Modifier = Modifier
) {
    val coachState by viewModel.aiCoachState.collectAsState()
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Active State Card
        when (val state = coachState) {
            is AiCoachUiState.Idle -> {
                GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.TipsAndUpdates,
                            contentDescription = "Tips",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = "Ready for your Personal Audit?",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "LifeSync will compile your logged habits, timetable completion rates, hydration records, and journal moods into a highly customized daily strategy.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            is AiCoachUiState.Loading -> {
                GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = "Synthesizing LifeSync Logs...",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Querying gemini-3.5-flash with your habits data. Please wait...",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            is AiCoachUiState.Success -> {
                GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "Spark",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Personal Growth Plan",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Divider(color = MaterialTheme.colorScheme.surfaceVariant)

                        Text(
                            text = state.advice,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 22.sp
                        )
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Secure",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Secure local check. AI logs remain isolated and private.",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Trigger Audit Button
        Button(
            onClick = { viewModel.queryAiCoach() },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("ai_coach_button"),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            ),
            shape = RoundedCornerShape(25.dp)
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = "Audit",
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (coachState is AiCoachUiState.Success) "Regenerate Advisor Report" else "Request Personal LifeSync Audit",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun ChatTabContent(
    viewModel: LifeSyncViewModel,
    modifier: Modifier = Modifier
) {
    val chatMessages by viewModel.chatMessages.collectAsState()
    val chatRole by viewModel.chatRole.collectAsState()
    val chatModel by viewModel.chatModel.collectAsState()
    val useThinking by viewModel.useThinking.collectAsState()
    val isChatLoading by viewModel.isChatLoading.collectAsState()

    var inputMessage by remember { mutableStateOf("") }
    var showModelMenu by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()

    // Auto-scroll to the bottom when new messages arrive
    LaunchedEffect(chatMessages.size, isChatLoading) {
        if (chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(chatMessages.size - 1)
        }
    }

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // --- 1. CHAT CONFIGURATION HEADER ---
        GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Role FilterChips
                Text(
                    text = "SELECT AI PERSONA",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    viewModel.chatRoles.keys.forEach { role ->
                        val isSelected = chatRole == role
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.setChatRole(role) },
                            label = { Text(role) },
                            leadingIcon = {
                                val icon = when (role) {
                                    "Life Coach" -> Icons.Default.AutoAwesome
                                    "Mindfulness Guide" -> Icons.Default.Spa
                                    "Fitness Trainer" -> Icons.Default.FitnessCenter
                                    "Productivity Master" -> Icons.Default.Timer
                                    else -> Icons.Default.Person
                                }
                                Icon(icon, contentDescription = role, modifier = Modifier.size(16.dp))
                            },
                            modifier = Modifier.testTag("role_chip_$role")
                        )
                    }
                }

                Divider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))

                // Model Selector & Thinking toggle Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Model Dropdown Trigger
                    Box {
                        OutlinedButton(
                            onClick = { showModelMenu = true },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("chat_model_dropdown_trigger")
                        ) {
                            val modelLabel = when (chatModel) {
                                "gemini-3.1-flash-lite-preview" -> "⚡ Lite (Fast)"
                                "gemini-3.5-flash" -> "⚖️ Balanced"
                                "gemini-3.1-pro-preview" -> "🧠 Pro (Complex)"
                                else -> chatModel
                            }
                            Text(modelLabel, fontSize = 12.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Default.ArrowDropDown, contentDescription = "Dropdown")
                        }

                        DropdownMenu(
                            expanded = showModelMenu,
                            onDismissRequest = { showModelMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("⚡ gemini-3.1-flash-lite-preview (Low-Latency)") },
                                onClick = {
                                    viewModel.setChatModel("gemini-3.1-flash-lite-preview")
                                    showModelMenu = false
                                },
                                modifier = Modifier.testTag("model_item_lite")
                            )
                            DropdownMenuItem(
                                text = { Text("⚖️ gemini-3.5-flash (Balanced)") },
                                onClick = {
                                    viewModel.setChatModel("gemini-3.5-flash")
                                    showModelMenu = false
                                },
                                modifier = Modifier.testTag("model_item_flash")
                            )
                            DropdownMenuItem(
                                text = { Text("🧠 gemini-3.1-pro-preview (Pro / Reasoning)") },
                                onClick = {
                                    viewModel.setChatModel("gemini-3.1-pro-preview")
                                    showModelMenu = false
                                },
                                modifier = Modifier.testTag("model_item_pro")
                            )
                        }
                    }

                    // Thinking Mode Toggle (Visible only when gemini-3.1-pro-preview is selected)
                    if (chatModel == "gemini-3.1-pro-preview") {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text("High Thinking", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Switch(
                                checked = useThinking,
                                onCheckedChange = { viewModel.toggleThinking(it) },
                                modifier = Modifier
                                    .scale(0.8f)
                                    .testTag("thinking_mode_switch")
                            )
                        }
                    }
                }
            }
        }

        // --- 2. SCROLLABLE MESSAGES AREA ---
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White.copy(alpha = 0.05f))
        ) {
            if (chatMessages.isEmpty()) {
                // Showcase onboarding suggestions
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Forum,
                        contentDescription = "Chat",
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Chat with your AI Coach!",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Your conversation is private and custom tailored to your active LifeSync habits. Choose a persona to start!",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    Text("TRY ASKING:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(8.dp))

                    val suggestions = listOf(
                        "Give me a high-intensity routine for today.",
                        "Suggest a mindfulness habit to reduce study stress.",
                        "Help me structure a code-learning goal block.",
                        "How can I lock in a solid hydration streak?"
                    )

                    suggestions.forEach { suggestion ->
                        Card(
                            onClick = {
                                inputMessage = suggestion
                                viewModel.sendChatMessage(suggestion)
                                inputMessage = ""
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Text(
                                text = "💬  $suggestion",
                                fontSize = 12.sp,
                                modifier = Modifier.padding(12.dp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(chatMessages) { message ->
                        val isUser = message.role == "user"
                        ChatBubble(message = message, isUser = isUser)
                    }

                    if (isChatLoading) {
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.Start,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = "AI Loading",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Card(
                                    shape = RoundedCornerShape(topStart = 0.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("AI is synthesizing reply", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(14.dp),
                                            strokeWidth = 2.dp,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- 3. INPUT BAR AREA ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Clear Chat Button
            IconButton(
                onClick = { viewModel.clearChat() },
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f))
                    .testTag("chat_clear_button")
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteSweep,
                    contentDescription = "Clear Chat",
                    tint = MaterialTheme.colorScheme.error
                )
            }

            // Input Field
            OutlinedTextField(
                value = inputMessage,
                onValueChange = { inputMessage = it },
                placeholder = { Text("Ask your LifeSync coach...") },
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 48.dp)
                    .testTag("chat_input_field"),
                shape = RoundedCornerShape(24.dp),
                trailingIcon = {
                    IconButton(
                        onClick = {
                            if (inputMessage.isNotBlank()) {
                                viewModel.sendChatMessage(inputMessage)
                                inputMessage = ""
                            }
                        },
                        enabled = inputMessage.isNotBlank() && !isChatLoading,
                        modifier = Modifier.testTag("chat_send_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Send",
                            tint = if (inputMessage.isNotBlank()) MaterialTheme.colorScheme.primary else Color.Gray
                        )
                    }
                }
            )
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage, isUser: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Top
    ) {
        if (!isUser) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "AI Coach Icon",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        val bubbleShape = if (isUser) {
            RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 0.dp)
        } else {
            RoundedCornerShape(topStart = 0.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
        }

        val bubbleBgColor = if (isUser) {
            MaterialTheme.colorScheme.primary
        } else {
            val isLight = MaterialTheme.colorScheme.background == Color(0xFFF3F4F6) // Matches LightBG definition
            if (isLight) Color.White.copy(alpha = 0.85f) else Color(0xFF1E293B).copy(alpha = 0.85f)
        }

        val bubbleBorderColor = if (isUser) {
            Color.Transparent
        } else {
            val isLight = MaterialTheme.colorScheme.background == Color(0xFFF3F4F6)
            if (isLight) Color(0xFFE2E8F0).copy(alpha = 0.6f) else Color(0xFF334155).copy(alpha = 0.6f)
        }

        val textColor = if (isUser) {
            Color.White
        } else {
            MaterialTheme.colorScheme.onSurface
        }

        Card(
            shape = bubbleShape,
            colors = CardDefaults.cardColors(containerColor = bubbleBgColor),
            modifier = Modifier
                .widthIn(max = 280.dp)
                .border(1.dp, bubbleBorderColor, bubbleShape)
        ) {
            Text(
                text = message.text,
                fontSize = 13.5.sp,
                color = textColor,
                modifier = Modifier.padding(12.dp)
            )
        }

        if (isUser) {
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "User Icon",
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

