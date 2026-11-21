package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.model.CurrentUser
import com.example.model.IslamicUpdate
import com.example.model.UserRole
import com.example.ui.components.*
import com.example.ui.theme.*
import java.io.File

@Composable
fun IslamicUpdatesScreen(
    updates: List<IslamicUpdate>,
    selectedFilter: String,
    isAdmin: Boolean,
    onBack: () -> Unit,
    onFilterChange: (String) -> Unit,
    onAddClick: () -> Unit,
    onDownloadClick: (IslamicUpdate) -> Unit,
    onDeleteClick: (Long) -> Unit
) {
    val colors = AlFalahTheme.colors
    val filtered = if (selectedFilter == "all") updates else updates.filter { it.category == selectedFilter }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bg)
            .verticalScroll(rememberScrollState())
    ) {
        PageTopHeader(
            title = "Islamic Updates",
            subtitle = "Wallpapers, Status, Reels, Posters & Ringtones",
            showBackButton = true,
            onBackClick = onBack
        )

        Column(
            modifier = Modifier.padding(start = 18.dp, end = 18.dp, top = 12.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FilterChipRow(
                options = listOf(
                    "all" to "All",
                    "Wallpaper" to "Wallpaper",
                    "Status" to "Status",
                    "Reel" to "Reel",
                    "Poster" to "Instagram Poster",
                    "Ringtone" to "Ringtone"
                ),
                selectedKey = selectedFilter,
                onSelect = onFilterChange
            )

            if (isAdmin) {
                GoldPrimaryButton(
                    text = "+ Upload New",
                    compact = true,
                    testTag = "admin_add_update_btn",
                    onClick = onAddClick
                )
            }

            if (filtered.isEmpty()) {
                EmptyStateView("Nothing here yet")
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    filtered.chunked(2).forEach { pair ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            pair.forEach { item ->
                                IslamicUpdateTile(
                                    item = item,
                                    isAdmin = isAdmin,
                                    onDownload = { onDownloadClick(item) },
                                    onDelete = { onDeleteClick(item.id) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            if (pair.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun IslamicUpdateTile(
    item: IslamicUpdate,
    isAdmin: Boolean,
    onDownload: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = AlFalahTheme.colors
    val (emoji, gradient) = when (item.category) {
        "Status" -> "💬" to listOf(Gold500, GoldTintTextLight)
        "Reel" -> "🎬" to listOf(SuccessGreen, SuccessDark)
        "Poster" -> "📌" to listOf(DangerRed, DangerDark)
        "Ringtone" -> "🎵" to listOf(Purple700, DarkBg)
        else -> "🖼️" to listOf(Purple500, Purple900)
    }
    val thumbFile = item.thumbPath?.let { File(it) }?.takeIf { it.exists() }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colors.card),
        border = BorderStroke(1.dp, colors.cardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = if (colors.isDark) 4.dp else 2.dp),
        modifier = modifier.testTag("islamic_update_card_${item.id}")
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .background(Brush.linearGradient(gradient)),
                contentAlignment = Alignment.Center
            ) {
                if (thumbFile != null) {
                    AsyncImage(
                        model = thumbFile,
                        contentDescription = item.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Text(text = emoji, fontSize = 30.sp)
                }
            }
            Column(modifier = Modifier.padding(10.dp)) {
                PillBadge(text = item.category.uppercase(), style = "upcoming")
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = item.title,
                    color = colors.textPrimary,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.ExtraBold,
                    lineHeight = 15.sp,
                    minLines = 2,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    GoldPrimaryButton(
                        text = "Download",
                        compact = true,
                        onClick = onDownload,
                        modifier = Modifier.weight(1f),
                        testTag = "download_update_btn_${item.id}"
                    )
                    if (isAdmin) {
                        MiniActionButton(
                            icon = Icons.Default.Close,
                            contentDescription = stringResource(R.string.cd_delete),
                            isDanger = true,
                            testTag = "delete_update_btn_${item.id}",
                            onClick = onDelete
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AboutScreen(
    customLogoPath: String?,
    onBack: () -> Unit
) {
    val colors = AlFalahTheme.colors

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bg)
            .verticalScroll(rememberScrollState())
    ) {
        PageTopHeader(
            title = "About Us",
            subtitle = "Our story, mission & founder",
            showBackButton = true,
            onBackClick = onBack
        )

        Column(
            modifier = Modifier.padding(start = 18.dp, end = 18.dp, top = 12.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Logo card
            InstituteCard {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    InstituteLogoImage(
                        customLogoPath = customLogoPath,
                        size = 68.dp,
                        clipCircle = false,
                        cornerRadius = 20.dp,
                        modifier = Modifier.shadow(6.dp, RoundedCornerShape(20.dp))
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "AL FALAH ISLAMIC ONLINE INSTITUTE",
                        color = colors.textPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = stringResource(R.string.institute_tagline_about),
                        color = colors.goldTintText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Full Roman Urdu introduction
            InstituteCard {
                listOf(
                    stringResource(R.string.about_para_1),
                    stringResource(R.string.about_para_2),
                    stringResource(R.string.about_para_3),
                    stringResource(R.string.about_para_4)
                ).forEachIndexed { idx, para ->
                    Text(
                        text = para,
                        color = colors.textSecondary,
                        fontSize = 12.5.sp,
                        lineHeight = 23.sp
                    )
                    if (idx < 3) Spacer(modifier = Modifier.height(14.dp))
                }
            }

            // Hamara Mission purple card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Purple900),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Brush.linearGradient(listOf(Purple700, Purple900)))
                        .padding(16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.about_mission_title),
                        color = Gold400,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.4.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.about_mission_body),
                        color = PureWhite.copy(alpha = 0.92f),
                        fontSize = 12.5.sp,
                        lineHeight = 22.sp
                    )
                }
            }

            // Hamari Pehchan & Hamara Paigham
            InstituteCard {
                Text(
                    text = stringResource(R.string.about_pehchan_title),
                    color = if (colors.isDark) Gold400 else Purple600,
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.4.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = stringResource(R.string.about_pehchan_body),
                    color = if (colors.isDark) colors.textPrimary else Purple700,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 21.sp
                )
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = stringResource(R.string.about_paigham_title),
                    color = if (colors.isDark) Gold400 else Purple600,
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.4.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = stringResource(R.string.about_paigham_body),
                    color = if (colors.isDark) colors.textPrimary else Purple700,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 21.sp
                )
            }

            SectionHeader(title = "Founder")
            InstituteCard {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.founder_photo),
                        contentDescription = stringResource(R.string.cd_founder_photo),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(64.dp)
                            .shadow(6.dp, RoundedCornerShape(18.dp))
                            .clip(RoundedCornerShape(18.dp))
                    )
                    Column {
                        Text(
                            text = stringResource(R.string.founder_name),
                            color = colors.textPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = stringResource(R.string.founder_role),
                            color = colors.textMuted,
                            fontSize = 11.5.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileScreen(
    user: CurrentUser,
    customLogoPath: String?,
    onBack: () -> Unit,
    onSaveProfile: (String, String, String) -> Unit,
    onOpenAbout: () -> Unit,
    onSwitchRole: (UserRole) -> Unit,
    onLogout: () -> Unit,
    onToast: (String) -> Unit
) {
    val colors = AlFalahTheme.colors
    val context = LocalContext.current

    var name by remember(user.name) { mutableStateOf(user.name) }
    var phone by remember(user.phone) { mutableStateOf(user.phone) }
    var email by remember(user.email) { mutableStateOf(user.email) }

    fun openExternal(url: String) {
        try {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        } catch (_: Exception) {
            onToast("Opening $url")
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bg)
            .verticalScroll(rememberScrollState())
    ) {
        PageTopHeader(
            title = "Profile",
            subtitle = "Manage your account",
            showBackButton = true,
            onBackClick = onBack
        )

        Column(
            modifier = Modifier.padding(start = 18.dp, end = 18.dp, top = 12.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Avatar header card
            InstituteCard {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Purple900),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = user.name.firstOrNull()?.uppercase() ?: "A",
                            color = Gold400,
                            fontSize = 19.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = user.name,
                            color = colors.textPrimary,
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = user.email,
                            color = colors.textMuted,
                            fontSize = 11.5.sp
                        )
                    }
                }
            }

            // Edit profile card
            InstituteCard {
                StyledTextField(
                    label = "Full Name",
                    value = name,
                    onValueChange = { name = it },
                    testTag = "profile_name_input"
                )
                Spacer(modifier = Modifier.height(12.dp))
                StyledTextField(
                    label = "Phone / WhatsApp",
                    value = phone,
                    onValueChange = { phone = it },
                    testTag = "profile_phone_input"
                )
                Spacer(modifier = Modifier.height(12.dp))
                StyledTextField(
                    label = "Email",
                    value = email,
                    onValueChange = { email = it },
                    testTag = "profile_email_input"
                )
                Spacer(modifier = Modifier.height(14.dp))
                GoldPrimaryButton(
                    text = "Save Changes",
                    testTag = "profile_save_btn",
                    onClick = { onSaveProfile(name, phone, email) }
                )
            }

            // Switch Dashboard Mode (Synced in Firestore)
            InstituteCard {
                Text(
                    text = "WORKSPACE MODE (CLOUD SYNCED)",
                    color = colors.textMuted,
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    UserRole.entries.forEach { role ->
                        val active = user.role == role
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (active) Purple900 else colors.goldTint,
                            modifier = Modifier
                                .weight(1f)
                                .defaultMinSize(minHeight = 44.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { onSwitchRole(role) }
                                .testTag("profile_role_${role.key}")
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 10.dp)) {
                                Text(
                                    text = role.label,
                                    color = if (active) PureWhite else colors.goldTintText,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Institute summary card
            InstituteCard {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    InstituteLogoImage(
                        customLogoPath = customLogoPath,
                        size = 60.dp,
                        clipCircle = false,
                        cornerRadius = 18.dp,
                        modifier = Modifier.shadow(6.dp, RoundedCornerShape(18.dp))
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = stringResource(R.string.institute_full_name),
                        color = colors.textPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = stringResource(R.string.institute_tagline),
                        color = colors.textMuted,
                        fontSize = 11.5.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    GhostButton(
                        text = "Read Full Introduction & Founder →",
                        compact = true,
                        testTag = "open_about_btn",
                        onClick = onOpenAbout
                    )
                }
            }

            // Contact & About
            SectionHeader(title = "Contact & About")
            InstituteCard(contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)) {
                ContactLinkRow(
                    title = "WhatsApp",
                    subtitle = "7406341264",
                    icon = Icons.Outlined.Chat,
                    iconBrush = Brush.linearGradient(listOf(SuccessGreen, SuccessGreen)),
                    testTag = "contact_whatsapp",
                    onClick = { openExternal("https://wa.me/7406341264") }
                )
                HorizontalDivider(color = colors.line)
                ContactLinkRow(
                    title = "Email",
                    subtitle = "alfalahislamiconlineinstitute1@gmail.com",
                    icon = Icons.Outlined.Email,
                    iconBrush = Brush.linearGradient(listOf(Purple600, Purple600)),
                    testTag = "contact_email",
                    onClick = { openExternal("mailto:alfalahislamiconlineinstitute1@gmail.com") }
                )
                HorizontalDivider(color = colors.line)
                ContactLinkRow(
                    title = "Instagram",
                    subtitle = "@al_falah_islamic_institute1",
                    icon = Icons.Outlined.CameraAlt,
                    iconBrush = Brush.linearGradient(listOf(Gold400, DangerRed)),
                    testTag = "contact_instagram",
                    onClick = { openExternal("https://www.instagram.com/al_falah_islamic_institute1?stkn=cjltNTdvdzFhYjZu") }
                )
                HorizontalDivider(color = colors.line)
                ContactLinkRow(
                    title = "Telegram Channel",
                    subtitle = "t.me/alfalahislamiconlineinstitute1",
                    icon = Icons.AutoMirrored.Filled.Send,
                    iconBrush = Brush.linearGradient(listOf(TelegramBlue, TelegramBlue)),
                    testTag = "contact_telegram",
                    onClick = { openExternal("https://t.me/alfalahislamiconlineinstitute1") }
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            GhostButton(
                text = "Log Out",
                testTag = "logout_btn",
                onClick = onLogout
            )
        }
    }
}

@Composable
private fun ContactLinkRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconBrush: Brush,
    testTag: String,
    onClick: () -> Unit
) {
    val colors = AlFalahTheme.colors
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp)
            .testTag(testTag)
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(11.dp))
                .background(iconBrush),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = PureWhite,
                modifier = Modifier.size(20.dp)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = colors.textPrimary,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = subtitle,
                color = colors.textMuted,
                fontSize = 10.5.sp
            )
        }
    }
}
