package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.firebase.AiChatMessage
import com.example.ui.components.PageTopHeader
import com.example.ui.theme.*
import com.example.viewmodel.AiChatUiState

@Composable
fun AiAssistantScreen(
    messages: List<AiChatMessage>,
    aiUiState: AiChatUiState,
    onBack: () -> Unit,
    onSendPrompt: (String, String) -> Unit,
    onExplainHadith: () -> Unit,
    onClearChat: () -> Unit,
    onDismissError: () -> Unit
) {
    val colors = AlFalahTheme.colors
    var promptText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val isBusy = aiUiState is AiChatUiState.Loading || aiUiState is AiChatUiState.Streaming

    LaunchedEffect(messages.size, aiUiState) {
        val totalItems = messages.size + if (isBusy) 1 else 0
        if (totalItems > 0) {
            listState.animateScrollToItem(totalItems - 1)
        }
    }

    val quickPrompts = remember {
        listOf(
            "Hadith" to "Explain Today's Hadith",
            "Tajweed" to "Explain the 4 rules of Noon Sakinah in simple steps",
            "Duas" to "Share 3 authentic daily Duas for seeking knowledge",
            "Hifz" to "Give me a practical daily Hifz revision plan",
            "Quiz" to "Create a 3-question Islamic knowledge quiz for me"
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bg)
            .imePadding()
    ) {
        PageTopHeader(
            title = "AI Study Tutor",
            subtitle = "Powered by Gemini · Islamic Learning Helper",
            showBackButton = true,
            onBackClick = onBack,
            trailing = {
                if (messages.isNotEmpty() && !isBusy) {
                    IconButton(
                        onClick = onClearChat,
                        modifier = Modifier.testTag("ai_clear_chat_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.DeleteOutline,
                            contentDescription = "Clear AI chat history",
                            tint = PureWhite
                        )
                    }
                }
            }
        )

        // Quick prompt chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            quickPrompts.forEachIndexed { idx, (topic, prompt) ->
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = if (colors.isDark) DarkCard else colors.goldTint,
                    border = BorderStroke(1.dp, Gold500.copy(alpha = 0.45f)),
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .clickable(enabled = !isBusy) {
                            if (idx == 0) onExplainHadith() else onSendPrompt(prompt, topic)
                        }
                        .testTag("ai_chip_${topic.lowercase()}")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.AutoAwesome,
                            contentDescription = null,
                            tint = Gold500,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = if (idx == 0) "Explain Today's Hadith" else topic,
                            color = if (colors.isDark) Gold400 else Purple900,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Transparent AI error banner
        if (aiUiState is AiChatUiState.Error) {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = colors.dangerText.copy(alpha = 0.12f)),
                border = BorderStroke(1.dp, colors.dangerText.copy(alpha = 0.35f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .testTag("ai_error_banner")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.padding(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ErrorOutline,
                        contentDescription = "AI Error",
                        tint = colors.dangerText,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = aiUiState.message,
                        color = colors.dangerText,
                        fontSize = 12.sp,
                        lineHeight = 17.sp,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = onDismissError) {
                        Text("Dismiss", color = colors.dangerText, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Messages list
        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (messages.isEmpty() && !isBusy) {
                item(key = "welcome_card") {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Purple900),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Brush.linearGradient(listOf(Purple700, Purple900)))
                                .padding(20.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.AutoAwesome,
                                    contentDescription = null,
                                    tint = Gold400,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "ASSALAMU ALAIKUM",
                                    color = Gold400,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 0.5.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Ask your Al Falah AI Study Tutor",
                                color = PureWhite,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Get clear explanations on Noorani Qaida, Tajweed rules, daily Duas & Salah, Stories of the Prophets, or tap a topic above to begin. Your conversations sync automatically to your cloud account.",
                                color = PureWhite.copy(alpha = 0.85f),
                                fontSize = 12.5.sp,
                                lineHeight = 19.sp
                            )
                        }
                    }
                }
            }

            items(messages, key = { it.docId }) { msg ->
                ChatBubble(
                    isUser = msg.sender == "user",
                    text = msg.text,
                    topic = msg.topic
                )
            }

            when (aiUiState) {
                is AiChatUiState.Loading -> {
                    item(key = "ai_loading") {
                        ChatBubble(
                            isUser = false,
                            text = "Thinking with Gemini...",
                            topic = "Gemini",
                            isStreaming = true
                        )
                    }
                }
                is AiChatUiState.Streaming -> {
                    item(key = "ai_streaming") {
                        ChatBubble(
                            isUser = false,
                            text = aiUiState.partialText,
                            topic = "Gemini",
                            isStreaming = true
                        )
                    }
                }
                else -> Unit
            }
        }

        // Bottom input bar
        Surface(
            color = colors.card,
            tonalElevation = 4.dp,
            border = BorderStroke(1.dp, colors.cardBorder),
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                OutlinedTextField(
                    value = promptText,
                    onValueChange = { promptText = it },
                    placeholder = {
                        Text(
                            "Ask about Tajweed, Hadith, Duas...",
                            color = colors.textMuted,
                            fontSize = 13.sp
                        )
                    },
                    singleLine = false,
                    maxLines = 3,
                    enabled = !isBusy,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Gold500,
                        unfocusedBorderColor = colors.line,
                        focusedTextColor = colors.textPrimary,
                        unfocusedTextColor = colors.textPrimary
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("ai_prompt_input")
                )
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            if (promptText.isNotBlank() && !isBusy) {
                                Brush.linearGradient(listOf(Gold400, Gold500))
                            } else {
                                Brush.linearGradient(listOf(colors.line, colors.line))
                            }
                        )
                        .clickable(enabled = promptText.isNotBlank() && !isBusy) {
                            val toSend = promptText.trim()
                            promptText = ""
                            onSendPrompt(toSend, "Study")
                        }
                        .testTag("ai_send_btn"),
                    contentAlignment = Alignment.Center
                ) {
                    if (isBusy) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = Purple900
                        )
                    } else {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send message to AI Tutor",
                            tint = Purple900,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatBubble(
    isUser: Boolean,
    text: String,
    topic: String,
    isStreaming: Boolean = false
) {
    val colors = AlFalahTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Card(
            shape = RoundedCornerShape(
                topStart = 18.dp,
                topEnd = 18.dp,
                bottomStart = if (isUser) 18.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 18.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = if (isUser) Purple900 else colors.card
            ),
            border = if (isUser) null else BorderStroke(1.dp, colors.cardBorder),
            modifier = Modifier.widthIn(max = 310.dp)
        ) {
            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                if (!isUser) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(bottom = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.AutoAwesome,
                            contentDescription = null,
                            tint = Gold500,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = if (isStreaming) "AL FALAH AI · STREAMING" else "AL FALAH AI · ${topic.uppercase()}",
                            color = Gold500,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.4.sp
                        )
                    }
                }
                Text(
                    text = text,
                    color = if (isUser) PureWhite else colors.textPrimary,
                    fontSize = 13.sp,
                    lineHeight = 20.sp
                )
            }
        }
    }
}
