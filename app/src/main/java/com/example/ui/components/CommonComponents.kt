package com.example.ui.components

import android.media.MediaPlayer
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.model.*
import com.example.ui.theme.*
import java.io.File

@Composable
fun InstituteLogoImage(
    customLogoPath: String?,
    size: Dp,
    modifier: Modifier = Modifier,
    clipCircle: Boolean = true,
    cornerRadius: Dp = 18.dp
) {
    val shape = if (clipCircle) CircleShape else RoundedCornerShape(cornerRadius)
    val file = customLogoPath?.let { File(it) }?.takeIf { it.exists() }
    if (file != null) {
        AsyncImage(
            model = file,
            contentDescription = stringResource(R.string.cd_institute_logo),
            contentScale = ContentScale.Crop,
            modifier = modifier
                .size(size)
                .clip(shape)
        )
    } else {
        Image(
            painter = painterResource(id = R.drawable.al_falah_logo),
            contentDescription = stringResource(R.string.cd_institute_logo),
            contentScale = ContentScale.Crop,
            modifier = modifier
                .size(size)
                .clip(shape)
        )
    }
}

@Composable
fun AlFalahAppHeader(
    customLogoPath: String?,
    subtitleUnderBrand: String,
    greetingTitle: String,
    greetingSubtitle: String,
    roleBadge: String,
    showNotificationBell: Boolean,
    showFounderBlink: Boolean,
    bottomPadding: Dp = 46.dp,
    onIslamicUpdatesClick: () -> Unit,
    onNotificationsClick: () -> Unit = {},
    onProfileClick: () -> Unit
) {
    val colors = AlFalahTheme.colors
    val infiniteTransition = rememberInfiniteTransition(label = "founder_blink")
    val blinkAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 700),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blink_alpha"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.purpleHeader)
            .statusBarsPadding()
            .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = bottomPadding)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(PureWhite)
                        .padding(2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    InstituteLogoImage(customLogoPath = customLogoPath, size = 30.dp)
                }
                Column {
                    Text(
                        text = "AL FALAH",
                        color = PureWhite,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        lineHeight = 13.sp
                    )
                    Text(
                        text = subtitleUnderBrand,
                        color = Gold400,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Normal,
                        lineHeight = 12.sp
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                HeaderIconButton(
                    icon = Icons.Outlined.AutoAwesome,
                    contentDescription = stringResource(R.string.cd_islamic_updates),
                    testTag = "header_islamic_updates_btn",
                    onClick = onIslamicUpdatesClick
                )
                if (showNotificationBell) {
                    HeaderIconButton(
                        icon = Icons.Outlined.Notifications,
                        contentDescription = stringResource(R.string.cd_notifications),
                        showBadgeDot = true,
                        testTag = "header_notifications_btn",
                        onClick = onNotificationsClick
                    )
                }
                HeaderIconButton(
                    icon = Icons.Outlined.Person,
                    contentDescription = stringResource(R.string.cd_profile),
                    testTag = "header_profile_btn",
                    onClick = onProfileClick
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = greetingTitle,
            color = PureWhite,
            fontSize = 19.sp,
            fontWeight = FontWeight.ExtraBold
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = greetingSubtitle,
            color = PureWhite.copy(alpha = 0.68f),
            fontSize = 12.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(Gold500.copy(alpha = 0.18f))
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Text(
                text = roleBadge,
                color = Gold400,
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.3.sp
            )
        }

        if (showFounderBlink) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.alpha(blinkAlpha)
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(Gold400)
                )
                Text(
                    text = stringResource(R.string.founder_label),
                    color = Gold400,
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.2.sp
                )
            }
        }
    }
}

@Composable
private fun HeaderIconButton(
    icon: ImageVector,
    contentDescription: String,
    testTag: String,
    showBadgeDot: Boolean = false,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(11.dp))
            .background(PureWhite.copy(alpha = 0.11f))
            .clickable(onClick = onClick)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = PureWhite,
            modifier = Modifier.size(18.dp)
        )
        if (showBadgeDot) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 7.dp, end = 7.dp)
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(Gold400)
                    .border(1.dp, Purple900, CircleShape)
            )
        }
    }
}

@Composable
fun AnnouncementBannerCard(
    banner: HomeBanner,
    dismissed: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val file = banner.imagePath?.let { File(it) }?.takeIf { it.exists() }
    if (file == null || dismissed) return

    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Purple900),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 160.dp, max = 300.dp)
            ) {
                AsyncImage(
                    model = file,
                    contentDescription = "Institute announcement poster",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxWidth()
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(12.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Gold500)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "📢 ANNOUNCEMENT",
                        color = Purple900,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(10.dp)
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.5f))
                        .clickable(onClick = onDismiss)
                        .testTag("banner_dismiss_btn"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.cd_close),
                        tint = PureWhite,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
            if (banner.caption.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Brush.linearGradient(listOf(Purple700, Purple900)))
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = banner.caption,
                        color = PureWhite,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun SectionHeader(
    title: String,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    val colors = AlFalahTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 20.dp, bottom = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = colors.textPrimary,
            fontSize = 14.5.sp,
            fontWeight = FontWeight.ExtraBold
        )
        if (actionText != null && onActionClick != null) {
            Text(
                text = actionText,
                color = if (colors.isDark) Gold400 else Purple600,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = onActionClick)
                    .padding(horizontal = 6.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
fun PageTopHeader(
    title: String,
    subtitle: String,
    showBackButton: Boolean = false,
    onBackClick: () -> Unit = {},
    trailing: (@Composable () -> Unit)? = null
) {
    val colors = AlFalahTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(start = 18.dp, end = 18.dp, top = 16.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f)
        ) {
            if (showBackButton) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(11.dp))
                        .background(colors.goldTint)
                        .clickable(onClick = onBackClick)
                        .testTag("page_back_btn"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.cd_back),
                        tint = if (colors.isDark) Gold400 else Purple900,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Column {
                Text(
                    text = title,
                    color = colors.textPrimary,
                    fontSize = 18.5.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = subtitle,
                    color = colors.textMuted,
                    fontSize = 12.sp
                )
            }
        }
        trailing?.invoke()
    }
}

@Composable
fun InstituteCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    contentPadding: PaddingValues = PaddingValues(14.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    val colors = AlFalahTheme.colors
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colors.card),
        border = BorderStroke(1.dp, colors.cardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = if (colors.isDark) 4.dp else 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
    ) {
        Column(modifier = Modifier.padding(contentPadding), content = content)
    }
}

@Composable
fun GoldPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = "",
    compact: Boolean = false
) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(if (compact) 10.dp else 13.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Gold500,
            contentColor = Purple900
        ),
        contentPadding = if (compact) {
            PaddingValues(horizontal = 14.dp, vertical = 9.dp)
        } else {
            PaddingValues(horizontal = 16.dp, vertical = 14.dp)
        },
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
        modifier = modifier
            .then(if (!compact) Modifier.fillMaxWidth() else Modifier)
            .defaultMinSize(minHeight = if (compact) 38.dp else 48.dp)
            .then(if (testTag.isNotEmpty()) Modifier.testTag(testTag) else Modifier)
    ) {
        Text(
            text = text,
            fontSize = if (compact) 12.5.sp else 14.5.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

@Composable
fun GhostButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = "",
    compact: Boolean = false
) {
    val colors = AlFalahTheme.colors
    OutlinedButton(
        onClick = onClick,
        shape = RoundedCornerShape(if (compact) 10.dp else 13.dp),
        border = BorderStroke(1.5.dp, colors.line),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = if (colors.isDark) DarkCharcoal700 else Purple700
        ),
        contentPadding = if (compact) {
            PaddingValues(horizontal = 14.dp, vertical = 9.dp)
        } else {
            PaddingValues(horizontal = 16.dp, vertical = 14.dp)
        },
        modifier = modifier
            .then(if (!compact) Modifier.fillMaxWidth() else Modifier)
            .defaultMinSize(minHeight = if (compact) 38.dp else 48.dp)
            .then(if (testTag.isNotEmpty()) Modifier.testTag(testTag) else Modifier)
    ) {
        Text(
            text = text,
            fontSize = if (compact) 12.sp else 14.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun PillBadge(text: String, style: String = "default") {
    val colors = AlFalahTheme.colors
    val (bg, fg) = when (style) {
        "live" -> colors.successTint to colors.successText
        "upcoming" -> colors.purpleTint to colors.purpleTintText
        else -> colors.goldTint to colors.goldTintText
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bg)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = text,
            color = fg,
            fontSize = 9.5.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

@Composable
fun MiniActionButton(
    icon: ImageVector,
    contentDescription: String,
    isDanger: Boolean = false,
    testTag: String = "",
    onClick: () -> Unit
) {
    val colors = AlFalahTheme.colors
    val bg = if (isDanger) colors.dangerTint else colors.goldTint
    val tint = if (isDanger) colors.dangerText else colors.goldTintText
    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(RoundedCornerShape(9.dp))
            .background(bg)
            .clickable(onClick = onClick)
            .then(if (testTag.isNotEmpty()) Modifier.testTag(testTag) else Modifier),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
fun CourseItemCard(
    course: Course,
    onClick: () -> Unit
) {
    val colors = AlFalahTheme.colors
    val iconVector = when (course.icon) {
        "quran" -> Icons.Outlined.MenuBook
        "star" -> Icons.Outlined.StarOutline
        "pray" -> Icons.Outlined.SelfImprovement
        "mic" -> Icons.Outlined.Mic
        else -> Icons.Outlined.AutoStories
    }
    InstituteCard(
        onClick = onClick,
        modifier = Modifier.testTag("course_card_${course.id}")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(colors.goldTint),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = iconVector,
                    contentDescription = course.title,
                    tint = if (colors.isDark) Gold400 else Purple700,
                    modifier = Modifier.size(26.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = course.title,
                    color = colors.textPrimary,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = course.teacher,
                    color = colors.textMuted,
                    fontSize = 11.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "⏱ ${course.duration}",
                        color = colors.textMuted,
                        fontSize = 10.5.sp
                    )
                    Text(
                        text = "👥 ${course.students}",
                        color = colors.textMuted,
                        fontSize = 10.5.sp
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(colors.line)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth((course.progress / 100f).coerceIn(0f, 1f))
                            .clip(RoundedCornerShape(6.dp))
                            .background(Gold500)
                    )
                }
            }
        }
    }
}

@Composable
fun OnlineClassCard(
    item: OnlineClass,
    onJoinClick: () -> Unit,
    secondaryButtonText: String? = null,
    onSecondaryClick: (() -> Unit)? = null
) {
    val colors = AlFalahTheme.colors
    InstituteCard(modifier = Modifier.testTag("class_card_${item.id}")) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = item.course,
                color = colors.textPrimary,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.weight(1f)
            )
            PillBadge(
                text = if (item.status == "live") "LIVE NOW" else "UPCOMING",
                style = item.status
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "👤 ${item.teacher}",
                color = colors.textMuted,
                fontSize = 10.5.sp
            )
            Text(
                text = "📅 ${item.date}, ${item.time}",
                color = colors.textMuted,
                fontSize = 10.5.sp
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        if (secondaryButtonText != null && onSecondaryClick != null) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                GoldPrimaryButton(
                    text = "Join",
                    compact = true,
                    onClick = onJoinClick,
                    modifier = Modifier.weight(1f),
                    testTag = "join_class_btn_${item.id}"
                )
                GhostButton(
                    text = secondaryButtonText,
                    compact = true,
                    onClick = onSecondaryClick,
                    testTag = "change_link_btn_${item.id}"
                )
            }
        } else {
            GoldPrimaryButton(
                text = "Join Class",
                compact = true,
                onClick = onJoinClick,
                modifier = Modifier.fillMaxWidth(),
                testTag = "join_class_btn_${item.id}"
            )
        }
    }
}

@Composable
fun VideoLessonCard(
    video: VideoLesson,
    onClick: () -> Unit,
    trailing: (@Composable () -> Unit)? = null
) {
    val colors = AlFalahTheme.colors
    InstituteCard(
        onClick = onClick,
        modifier = Modifier.testTag("video_card_${video.id}")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(96.dp)
                    .height(66.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Brush.linearGradient(listOf(Purple600, Purple900))),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = video.title,
                    tint = PureWhite,
                    modifier = Modifier.size(28.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = video.title,
                    color = colors.textPrimary,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.ExtraBold,
                    lineHeight = 16.sp
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = "${video.teacher} · ${video.duration}",
                    color = colors.textMuted,
                    fontSize = 10.5.sp
                )
                if (video.desc.isNotBlank()) {
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = video.desc,
                        color = colors.textMuted,
                        fontSize = 10.5.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            trailing?.invoke()
        }
    }
}

fun materialEmoji(type: String): String = when (type) {
    "Photo" -> "🖼️"
    "Audio" -> "🎵"
    "Document" -> "📄"
    "Notes" -> "📝"
    "PDF" -> "📕"
    "Audiobook" -> "🎧"
    else -> "📘"
}

@Composable
fun StudyMaterialCard(
    material: StudyMaterial,
    canManage: Boolean,
    showInlinePreview: Boolean = true,
    onOpenFile: (StudyMaterial) -> Unit,
    onDelete: () -> Unit
) {
    val colors = AlFalahTheme.colors
    val file = material.filePath?.let { File(it) }?.takeIf { it.exists() }
    val uploaderLine = material.uploader?.let {
        "Uploaded by ${it.name} · ${it.role.replaceFirstChar { c -> c.uppercase() }}"
    }

    InstituteCard(modifier = Modifier.testTag("material_card_${material.id}")) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(11.dp))
                    .background(Purple900),
                contentAlignment = Alignment.Center
            ) {
                Text(text = materialEmoji(material.type), fontSize = 18.sp)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = material.title,
                    color = colors.textPrimary,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "${material.course} · ${material.type}",
                    color = colors.textMuted,
                    fontSize = 10.5.sp
                )
                if (!uploaderLine.isNullOrBlank()) {
                    Text(
                        text = uploaderLine,
                        color = colors.textMuted,
                        fontSize = 9.5.sp
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                if (file != null) {
                    MiniActionButton(
                        icon = Icons.Default.Download,
                        contentDescription = stringResource(R.string.cd_download),
                        onClick = { onOpenFile(material) }
                    )
                }
                if (canManage) {
                    MiniActionButton(
                        icon = Icons.Default.Close,
                        contentDescription = stringResource(R.string.cd_delete),
                        isDanger = true,
                        testTag = "delete_material_btn_${material.id}",
                        onClick = onDelete
                    )
                }
            }
        }

        if (showInlinePreview && file != null && material.type == "Photo") {
            Spacer(modifier = Modifier.height(10.dp))
            AsyncImage(
                model = file,
                contentDescription = material.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 165.dp)
                    .clip(RoundedCornerShape(12.dp))
            )
        } else if (showInlinePreview && file != null && (material.type == "Audio" || material.type == "Audiobook")) {
            Spacer(modifier = Modifier.height(10.dp))
            InlineAudioPlayerBar(file = file)
        }
    }
}

@Composable
private fun InlineAudioPlayerBar(file: File) {
    val colors = AlFalahTheme.colors
    var isPlaying by remember { mutableStateOf(false) }
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }

    DisposableEffect(file.absolutePath) {
        onDispose {
            mediaPlayer?.release()
            mediaPlayer = null
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(11.dp))
            .background(colors.fieldBg)
            .border(1.dp, colors.line, RoundedCornerShape(11.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        IconButton(
            onClick = {
                if (isPlaying) {
                    mediaPlayer?.pause()
                    isPlaying = false
                } else {
                    try {
                        if (mediaPlayer == null) {
                            mediaPlayer = MediaPlayer().apply {
                                setDataSource(file.absolutePath)
                                prepare()
                                setOnCompletionListener { isPlaying = false }
                            }
                        }
                        mediaPlayer?.start()
                        isPlaying = true
                    } catch (_: Exception) {
                        isPlaying = false
                    }
                }
            },
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(Purple900)
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (isPlaying) "Pause audio" else "Play audio",
                tint = Gold400
            )
        }
        Text(
            text = if (isPlaying) "Playing audio…" else file.name,
            color = colors.textSecondary,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun SearchInputBar(
    query: String,
    placeholder: String,
    onQueryChange: (String) -> Unit,
    testTag: String = "search_input"
) {
    val colors = AlFalahTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colors.card)
            .border(1.5.dp, colors.line, RoundedCornerShape(12.dp))
            .padding(horizontal = 13.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = null,
            tint = colors.textMuted,
            modifier = Modifier.size(18.dp)
        )
        Box(modifier = Modifier.weight(1f)) {
            if (query.isEmpty()) {
                Text(
                    text = placeholder,
                    color = colors.textMuted,
                    fontSize = 13.sp
                )
            }
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                singleLine = true,
                textStyle = TextStyle(
                    color = colors.textPrimary,
                    fontSize = 13.sp,
                    fontFamily = ManropeFontFamily
                ),
                cursorBrush = SolidColor(Purple500),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(testTag)
            )
        }
    }
}

@Composable
fun FilterChipRow(
    options: List<Pair<String, String>>,
    selectedKey: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = AlFalahTheme.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEach { (key, label) ->
            val active = key == selectedKey
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (active) Purple900 else colors.card)
                    .border(
                        1.5.dp,
                        if (active) Purple900 else colors.line,
                        RoundedCornerShape(20.dp)
                    )
                    .clickable { onSelect(key) }
                    .padding(horizontal = 14.dp, vertical = 8.dp)
                    .testTag("filter_chip_$key")
            ) {
                Text(
                    text = label,
                    color = if (active) PureWhite else colors.textSecondary,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun EmptyStateView(message: String) {
    val colors = AlFalahTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 44.dp, horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(
            imageVector = Icons.Outlined.Inbox,
            contentDescription = null,
            tint = colors.line,
            modifier = Modifier.size(44.dp)
        )
        Text(
            text = message,
            color = colors.textMuted,
            fontSize = 12.5.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun StyledTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "",
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    minLines: Int = 1,
    isPassword: Boolean = false,
    enabled: Boolean = true,
    isRtlArabic: Boolean = false,
    testTag: String = ""
) {
    val colors = AlFalahTheme.colors
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            color = colors.textSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = {
                Text(
                    text = placeholder,
                    color = colors.textMuted,
                    fontSize = 13.5.sp
                )
            },
            enabled = enabled,
            singleLine = singleLine,
            minLines = minLines,
            visualTransformation = if (isPassword) {
                androidx.compose.ui.text.input.PasswordVisualTransformation()
            } else {
                androidx.compose.ui.text.input.VisualTransformation.None
            },
            textStyle = TextStyle(
                color = colors.textPrimary,
                fontSize = if (isRtlArabic) 17.sp else 14.sp,
                fontFamily = if (isRtlArabic) AmiriFontFamily else ManropeFontFamily,
                textAlign = if (isRtlArabic) TextAlign.End else TextAlign.Start
            ),
            shape = RoundedCornerShape(11.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = colors.fieldBg,
                unfocusedContainerColor = colors.fieldBg,
                disabledContainerColor = colors.fieldBg.copy(alpha = 0.7f),
                focusedBorderColor = Purple500,
                unfocusedBorderColor = colors.line,
                disabledBorderColor = colors.line,
                cursorColor = Purple500
            ),
            modifier = Modifier
                .fillMaxWidth()
                .then(if (testTag.isNotEmpty()) Modifier.testTag(testTag) else Modifier)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StyledDropdownField(
    label: String,
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit,
    enabled: Boolean = true
) {
    val colors = AlFalahTheme.colors
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            color = colors.textSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        ExposedDropdownMenuBox(
            expanded = expanded && enabled,
            onExpandedChange = { if (enabled) expanded = !expanded }
        ) {
            OutlinedTextField(
                value = selected,
                onValueChange = {},
                readOnly = true,
                enabled = enabled,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded && enabled) },
                shape = RoundedCornerShape(11.dp),
                textStyle = TextStyle(
                    color = colors.textPrimary,
                    fontSize = 14.sp,
                    fontFamily = ManropeFontFamily
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = colors.fieldBg,
                    unfocusedContainerColor = colors.fieldBg,
                    disabledContainerColor = colors.fieldBg.copy(alpha = 0.7f),
                    focusedBorderColor = Purple500,
                    unfocusedBorderColor = colors.line,
                    disabledBorderColor = colors.line,
                    disabledTextColor = colors.textSecondary
                ),
                modifier = Modifier
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled)
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = expanded && enabled,
                onDismissRequest = { expanded = false },
                containerColor = colors.card
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option, color = colors.textPrimary, fontSize = 13.5.sp) },
                        onClick = {
                            onSelect(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun StudentBottomNavigationBar(
    currentScreen: AppScreen,
    onNavigate: (AppScreen) -> Unit
) {
    val colors = AlFalahTheme.colors
    val items = listOf(
        Triple(AppScreen.HOME, "Home", Icons.Outlined.Home),
        Triple(AppScreen.COURSES, "Courses", Icons.Outlined.MenuBook),
        Triple(AppScreen.CLASSES, "Classes", Icons.Outlined.Videocam),
        Triple(AppScreen.VIDEOS, "Videos", Icons.Outlined.PlayCircle),
        Triple(AppScreen.MATERIAL, "Books", Icons.Outlined.Description)
    )

    Surface(
        color = colors.card,
        tonalElevation = 4.dp,
        shadowElevation = 8.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            HorizontalDivider(color = colors.line, thickness = 1.dp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 6.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEach { (screen, label, icon) ->
                    val active = currentScreen == screen
                    val tint = if (active) {
                        if (colors.isDark) Gold400 else Purple700
                    } else colors.textMuted
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .defaultMinSize(minWidth = 54.dp, minHeight = 48.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onNavigate(screen) }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .testTag("nav_${screen.name.lowercase()}")
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = label,
                            tint = tint,
                            modifier = Modifier.size(21.dp)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = label,
                            color = tint,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FloatingToastOverlay(
    message: String?,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = message != null,
        enter = fadeIn() + slideInVertically { it / 2 },
        exit = fadeOut() + slideOutVertically { it / 2 },
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .shadow(10.dp, RoundedCornerShape(12.dp))
                .clip(RoundedCornerShape(12.dp))
                .background(Purple900)
                .border(1.dp, Gold400.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                .padding(horizontal = 18.dp, vertical = 11.dp)
        ) {
            Text(
                text = message.orEmpty(),
                color = PureWhite,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}
