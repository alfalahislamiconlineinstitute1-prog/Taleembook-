package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.AlFalahUiState

@Composable
fun StudentHomeScreen(
    uiState: AlFalahUiState,
    onNavigate: (AppScreen) -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenProgress: () -> Unit,
    onCourseClick: (Course) -> Unit,
    onJoinClass: (OnlineClass) -> Unit,
    onDismissBanner: () -> Unit,
    onExplainHadith: () -> Unit
) {
    val colors = AlFalahTheme.colors
    val firstName = uiState.currentUser.name.substringBefore(" ")
    val appState = uiState.appState

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bg)
            .verticalScroll(rememberScrollState())
    ) {
        AlFalahAppHeader(
            customLogoPath = appState.customLogoPath,
            subtitleUnderBrand = "Islamic Online Institute",
            greetingTitle = "Assalamu Alaikum, $firstName",
            greetingSubtitle = "Ready to continue your Dini Taleem today?",
            roleBadge = "STUDENT",
            showNotificationBell = true,
            showFounderBlink = true,
            bottomPadding = 46.dp,
            onIslamicUpdatesClick = { onNavigate(AppScreen.ISLAMIC_UPDATES) },
            onNotificationsClick = onOpenNotifications,
            onProfileClick = { onNavigate(AppScreen.PROFILE) }
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = (-28).dp)
                .padding(horizontal = 18.dp)
        ) {
            AnnouncementBannerCard(
                banner = appState.banner,
                dismissed = uiState.bannerDismissed,
                onDismiss = onDismissBanner,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Quick Grid (3 columns, 7 cards matching HTML exact order)
            val quickItems = listOf(
                Triple("My Classes", Icons.Outlined.Videocam) { onNavigate(AppScreen.CLASSES) },
                Triple("Courses", Icons.Outlined.MenuBook) { onNavigate(AppScreen.COURSES) },
                Triple("Assignments", Icons.Outlined.AssignmentTurnedIn) { onNavigate(AppScreen.ASSIGNMENTS) },
                Triple("Videos", Icons.Outlined.SmartDisplay) { onNavigate(AppScreen.VIDEOS) },
                Triple("Books", Icons.Outlined.Description) { onNavigate(AppScreen.MATERIAL) },
                Triple("Progress", Icons.Outlined.BarChart) { onOpenProgress() },
                Triple("Islamic Updates", Icons.Outlined.AutoAwesome) { onNavigate(AppScreen.ISLAMIC_UPDATES) },
                Triple("AI Study Tutor", Icons.Outlined.Psychology) { onNavigate(AppScreen.AI_ASSISTANT) }
            )

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                quickItems.chunked(3).forEach { rowItems ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        rowItems.forEach { (label, icon, action) ->
                            QuickTileCard(
                                label = label,
                                icon = icon,
                                onClick = action,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        repeat(3 - rowItems.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            // Daily Hadith Card
            SectionHeader(title = "آج کی حدیث · Daily Hadith")
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Purple900),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Brush.linearGradient(listOf(Purple700, Purple900)))
                        .padding(20.dp)
                ) {
                    Text(
                        text = "TODAY'S HADITH",
                        color = Gold400,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.4.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = appState.hadith.ar,
                        color = PureWhite,
                        fontSize = 20.sp,
                        lineHeight = 36.sp,
                        fontFamily = AmiriFontFamily,
                        textAlign = TextAlign.End,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = appState.hadith.tr,
                        color = PureWhite.copy(alpha = 0.87f),
                        fontSize = 12.5.sp,
                        lineHeight = 21.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "— ${appState.hadith.src}",
                            color = Gold400,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = Gold500.copy(alpha = 0.2f),
                            border = BorderStroke(1.dp, Gold400.copy(alpha = 0.55f)),
                            modifier = Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .clickable(onClick = onExplainHadith)
                                .testTag("explain_hadith_ai_btn")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.AutoAwesome,
                                    contentDescription = "Explain Hadith with Gemini AI",
                                    tint = Gold400,
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = "Explain with AI",
                                    color = Gold400,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }
                }
            }

            // Continue Learning
            SectionHeader(
                title = "Continue Learning",
                actionText = "See all",
                onActionClick = { onNavigate(AppScreen.COURSES) }
            )
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                appState.courses.take(2).forEach { course ->
                    CourseItemCard(course = course, onClick = { onCourseClick(course) })
                }
            }

            // Upcoming Classes
            SectionHeader(
                title = "Upcoming Classes",
                actionText = "See all",
                onActionClick = { onNavigate(AppScreen.CLASSES) }
            )
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                appState.classes.take(2).forEach { onlineClass ->
                    OnlineClassCard(
                        item = onlineClass,
                        onJoinClick = { onJoinClass(onlineClass) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
private fun QuickTileCard(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = AlFalahTheme.colors
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colors.card),
        border = BorderStroke(1.dp, colors.cardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = if (colors.isDark) 4.dp else 2.dp),
        modifier = modifier
            .clickable(onClick = onClick)
            .testTag("quick_tile_${label.lowercase().replace(" ", "_")}")
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(11.dp))
                    .background(Purple900),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = Gold400,
                    modifier = Modifier.size(19.dp)
                )
            }
            Text(
                text = label,
                color = colors.textSecondary,
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                lineHeight = 13.sp,
                maxLines = 2
            )
        }
    }
}

@Composable
fun CoursesScreen(
    courses: List<Course>,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onCourseClick: (Course) -> Unit
) {
    val colors = AlFalahTheme.colors
    val filtered = courses.filter { it.title.contains(searchQuery.trim(), ignoreCase = true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bg)
            .verticalScroll(rememberScrollState())
    ) {
        PageTopHeader(
            title = "Courses",
            subtitle = "Explore our full Islamic curriculum"
        )
        Column(
            modifier = Modifier.padding(start = 18.dp, end = 18.dp, top = 12.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SearchInputBar(
                query = searchQuery,
                placeholder = "Search courses...",
                onQueryChange = onSearchChange,
                testTag = "course_search_input"
            )
            if (filtered.isEmpty()) {
                EmptyStateView("No courses found")
            } else {
                filtered.forEach { course ->
                    CourseItemCard(course = course, onClick = { onCourseClick(course) })
                }
            }
        }
    }
}

@Composable
fun ClassesScreen(
    classes: List<OnlineClass>,
    selectedFilter: String,
    onFilterChange: (String) -> Unit,
    onJoinClass: (OnlineClass) -> Unit
) {
    val colors = AlFalahTheme.colors
    val filtered = if (selectedFilter == "all") classes else classes.filter { it.status == selectedFilter }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bg)
            .verticalScroll(rememberScrollState())
    ) {
        PageTopHeader(
            title = "Online Classes",
            subtitle = "Your live class schedule"
        )
        Column(
            modifier = Modifier.padding(start = 18.dp, end = 18.dp, top = 12.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FilterChipRow(
                options = listOf("all" to "All", "live" to "Live Now", "upcoming" to "Upcoming"),
                selectedKey = selectedFilter,
                onSelect = onFilterChange
            )
            if (filtered.isEmpty()) {
                EmptyStateView("No classes here yet")
            } else {
                filtered.forEach { onlineClass ->
                    OnlineClassCard(
                        item = onlineClass,
                        onJoinClick = { onJoinClass(onlineClass) }
                    )
                }
            }
        }
    }
}

@Composable
fun VideosScreen(
    videos: List<VideoLesson>,
    onPlayVideo: (VideoLesson) -> Unit
) {
    val colors = AlFalahTheme.colors
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bg)
            .verticalScroll(rememberScrollState())
    ) {
        PageTopHeader(
            title = "Video Lessons",
            subtitle = "Watch and listen to educational content"
        )
        Column(
            modifier = Modifier.padding(start = 18.dp, end = 18.dp, top = 12.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (videos.isEmpty()) {
                EmptyStateView("No videos yet")
            } else {
                videos.forEach { video ->
                    VideoLessonCard(video = video, onClick = { onPlayVideo(video) })
                }
            }
        }
    }
}

@Composable
fun MaterialScreen(
    materials: List<StudyMaterial>,
    canManage: (StudyMaterial) -> Boolean,
    onUploadClick: () -> Unit,
    onOpenFile: (StudyMaterial) -> Unit,
    onDelete: (Long) -> Unit
) {
    val colors = AlFalahTheme.colors
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bg)
            .verticalScroll(rememberScrollState())
    ) {
        PageTopHeader(
            title = "Books",
            subtitle = "Notes, photos, documents & audio shared by everyone",
            trailing = {
                GoldPrimaryButton(
                    text = "+ Upload",
                    compact = true,
                    testTag = "books_upload_btn",
                    onClick = onUploadClick
                )
            }
        )
        Column(
            modifier = Modifier.padding(start = 18.dp, end = 18.dp, top = 12.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (materials.isEmpty()) {
                EmptyStateView("No books or notes yet — tap Upload to add one")
            } else {
                materials.forEach { item ->
                    StudyMaterialCard(
                        material = item,
                        canManage = canManage(item),
                        onOpenFile = onOpenFile,
                        onDelete = { onDelete(item.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun AssignmentsScreen(
    assignments: List<Assignment>,
    onSubmitAssignment: (Long) -> Unit
) {
    val colors = AlFalahTheme.colors
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bg)
            .verticalScroll(rememberScrollState())
    ) {
        PageTopHeader(
            title = "Assignments",
            subtitle = "Track submissions and feedback"
        )
        Column(
            modifier = Modifier.padding(start = 18.dp, end = 18.dp, top = 12.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (assignments.isEmpty()) {
                EmptyStateView("No assignments yet")
            } else {
                assignments.forEach { item ->
                    val pillStyle = when (item.status) {
                        "pending" -> "upcoming"
                        "submitted" -> "live"
                        else -> "default"
                    }
                    InstituteCard(modifier = Modifier.testTag("assignment_card_${item.id}")) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = item.title,
                                color = colors.textPrimary,
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.weight(1f)
                            )
                            PillBadge(
                                text = item.status.uppercase(),
                                style = pillStyle
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(
                                text = "📘 ${item.course}",
                                color = colors.textMuted,
                                fontSize = 10.5.sp
                            )
                            Text(
                                text = "📅 Due ${item.due}",
                                color = colors.textMuted,
                                fontSize = 10.5.sp
                            )
                        }
                        if (!item.feedback.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(9.dp))
                                    .background(colors.goldTint)
                                    .padding(horizontal = 10.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = "💬 ${item.feedback}",
                                    color = colors.textSecondary,
                                    fontSize = 11.5.sp
                                )
                            }
                        }
                        if (item.status == "pending") {
                            Spacer(modifier = Modifier.height(10.dp))
                            GoldPrimaryButton(
                                text = "Submit Assignment",
                                compact = true,
                                modifier = Modifier.fillMaxWidth(),
                                testTag = "submit_assignment_btn_${item.id}",
                                onClick = { onSubmitAssignment(item.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}
