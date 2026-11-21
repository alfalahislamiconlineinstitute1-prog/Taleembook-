package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.model.*
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.AlFalahUiState
import java.io.File

@Composable
fun AdminDashboardScreen(
    uiState: AlFalahUiState,
    onSelectTab: (AdminTab) -> Unit,
    onNavigate: (AppScreen) -> Unit,
    onDismissBanner: () -> Unit,
    onApproveStudent: (Long) -> Unit,
    onEditStudent: (StudentRecord) -> Unit,
    onRemoveStudent: (Long) -> Unit,
    onAddTeacher: () -> Unit,
    onEditTeacher: (TeacherRecord) -> Unit,
    onRemoveTeacher: (Long) -> Unit,
    onAddCourse: () -> Unit,
    onEditCourse: (Course) -> Unit,
    onDeleteCourse: (Long) -> Unit,
    onAddBook: () -> Unit,
    onEditBook: (StudyMaterial) -> Unit,
    onDeleteContent: (Long) -> Unit,
    onSaveHadith: (String, String, String) -> Unit,
    onOpenSheet: (ActiveSheet) -> Unit,
    onToast: (String) -> Unit
) {
    val colors = AlFalahTheme.colors
    val appState = uiState.appState

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bg)
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding()
    ) {
        AlFalahAppHeader(
            customLogoPath = appState.customLogoPath,
            subtitleUnderBrand = "Admin Control Panel",
            greetingTitle = "Assalamu Alaikum, Admin",
            greetingSubtitle = "Full control over the institute platform",
            roleBadge = "ADMINISTRATOR",
            showNotificationBell = false,
            showFounderBlink = false,
            bottomPadding = 28.dp,
            onIslamicUpdatesClick = { onNavigate(AppScreen.ISLAMIC_UPDATES) },
            onProfileClick = { onNavigate(AppScreen.PROFILE) }
        )

        AnnouncementBannerCard(
            banner = appState.banner,
            dismissed = uiState.bannerDismissed,
            onDismiss = onDismissBanner,
            modifier = Modifier.padding(start = 18.dp, end = 18.dp, top = 14.dp)
        )

        // Role Tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(start = 18.dp, end = 18.dp, top = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AdminTab.entries.forEach { tab ->
                val active = uiState.adminTab == tab
                RoleTabChip(
                    label = tab.label,
                    active = active,
                    testTag = "admin_tab_${tab.name.lowercase()}",
                    onClick = { onSelectTab(tab) }
                )
            }
        }

        Column(
            modifier = Modifier.padding(start = 18.dp, end = 18.dp, top = 10.dp, bottom = 80.dp)
        ) {
            when (uiState.adminTab) {
                AdminTab.OVERVIEW -> AdminOverviewContent(
                    appState = appState,
                    onApproveStudent = onApproveStudent,
                    onRemoveStudent = onRemoveStudent,
                    onGoHadith = { onSelectTab(AdminTab.HADITH) },
                    onGoCourses = { onSelectTab(AdminTab.COURSES) },
                    onOpenAnnouncement = { onOpenSheet(ActiveSheet.Announcement) }
                )

                AdminTab.USERS -> AdminUsersContent(
                    students = appState.students,
                    onEdit = onEditStudent,
                    onRemove = onRemoveStudent
                )

                AdminTab.TEACHERS -> AdminTeachersContent(
                    teachers = appState.teachers,
                    onAdd = onAddTeacher,
                    onEdit = onEditTeacher,
                    onRemove = onRemoveTeacher
                )

                AdminTab.COURSES -> AdminCoursesContent(
                    courses = appState.courses,
                    onAdd = onAddCourse,
                    onEdit = onEditCourse,
                    onDelete = onDeleteCourse
                )

                AdminTab.BOOKS -> AdminBooksContent(
                    materials = appState.materials,
                    onAdd = onAddBook,
                    onEdit = onEditBook,
                    onDelete = onDeleteContent
                )

                AdminTab.HADITH -> AdminHadithContent(
                    hadith = appState.hadith,
                    onSave = onSaveHadith
                )

                AdminTab.CONTENT -> AdminAppContentTab(
                    appState = appState,
                    onChangeLogo = { onOpenSheet(ActiveSheet.ChangeLogo) },
                    onChangeBanner = { onOpenSheet(ActiveSheet.ChangeBanner) },
                    onGoCourses = { onSelectTab(AdminTab.COURSES) },
                    onOpenAbout = { onNavigate(AppScreen.ABOUT) },
                    onOpenProfile = { onNavigate(AppScreen.PROFILE) },
                    onToast = onToast
                )

                AdminTab.UPDATES -> Unit
            }
        }
    }
}

@Composable
private fun AdminOverviewContent(
    appState: InstituteAppState,
    onApproveStudent: (Long) -> Unit,
    onRemoveStudent: (Long) -> Unit,
    onGoHadith: () -> Unit,
    onGoCourses: () -> Unit,
    onOpenAnnouncement: () -> Unit
) {
    val colors = AlFalahTheme.colors
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatCard("${appState.students.size}", "Total Students", Modifier.weight(1f))
            StatCard("${appState.teachers.size}", "Active Teachers", Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatCard("${appState.courses.size}", "Courses", Modifier.weight(1f))
            StatCard("${appState.classes.size}", "Classes Scheduled", Modifier.weight(1f))
        }
    }

    SectionHeader(title = "Pending Approvals")
    val pending = appState.students.filter { it.status == "pending" }
    InstituteCard {
        if (pending.isEmpty()) {
            Text(
                text = "No pending approvals",
                color = colors.textMuted,
                fontSize = 12.sp,
                modifier = Modifier.padding(vertical = 6.dp)
            )
        } else {
            pending.forEachIndexed { idx, s ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(s.name, color = colors.textPrimary, fontSize = 12.5.sp, fontWeight = FontWeight.ExtraBold)
                        Text("${s.email} · ${s.city}", color = colors.textMuted, fontSize = 10.5.sp)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        MiniActionButton(
                            icon = Icons.Default.Check,
                            contentDescription = stringResource(R.string.cd_approve),
                            testTag = "approve_student_${s.id}",
                            onClick = { onApproveStudent(s.id) }
                        )
                        MiniActionButton(
                            icon = Icons.Default.Close,
                            contentDescription = stringResource(R.string.cd_delete),
                            isDanger = true,
                            testTag = "reject_student_${s.id}",
                            onClick = { onRemoveStudent(s.id) }
                        )
                    }
                }
                if (idx < pending.lastIndex) HorizontalDivider(color = colors.line)
            }
        }
    }

    SectionHeader(title = "Quick Actions")
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
    ) {
        GoldPrimaryButton(text = "+ Add Hadith", compact = true, onClick = onGoHadith)
        GhostButton(text = "+ Add Course", compact = true, onClick = onGoCourses)
        GhostButton(text = "📢 Announcement", compact = true, onClick = onOpenAnnouncement)
    }
}

@Composable
private fun StatCard(value: String, label: String, modifier: Modifier = Modifier) {
    val colors = AlFalahTheme.colors
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = colors.card),
        border = BorderStroke(1.dp, colors.cardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = if (colors.isDark) 4.dp else 2.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = value,
                color = if (colors.isDark) Gold400 else Purple900,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                color = colors.textMuted,
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun AdminUsersContent(
    students: List<StudentRecord>,
    onEdit: (StudentRecord) -> Unit,
    onRemove: (Long) -> Unit
) {
    val colors = AlFalahTheme.colors
    SectionHeader(title = "Students (${students.size})")
    InstituteCard {
        students.forEachIndexed { idx, s ->
            val dotColor = when (s.status) {
                "approved" -> SuccessGreen
                "blocked" -> DangerRed
                else -> Gold500
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(dotColor)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(s.name, color = colors.textPrimary, fontSize = 12.5.sp, fontWeight = FontWeight.ExtraBold)
                    Text("${s.email} · ${s.city}", color = colors.textMuted, fontSize = 10.5.sp)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    MiniActionButton(
                        icon = Icons.Default.Edit,
                        contentDescription = stringResource(R.string.cd_edit),
                        onClick = { onEdit(s) }
                    )
                    MiniActionButton(
                        icon = Icons.Default.Close,
                        contentDescription = stringResource(R.string.cd_delete),
                        isDanger = true,
                        onClick = { onRemove(s.id) }
                    )
                }
            }
            if (idx < students.lastIndex) HorizontalDivider(color = colors.line)
        }
    }
}

@Composable
private fun AdminTeachersContent(
    teachers: List<TeacherRecord>,
    onAdd: () -> Unit,
    onEdit: (TeacherRecord) -> Unit,
    onRemove: (Long) -> Unit
) {
    val colors = AlFalahTheme.colors
    SectionHeader(title = "Teachers (${teachers.size})", actionText = "+ Add", onActionClick = onAdd)
    InstituteCard {
        teachers.forEachIndexed { idx, tc ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(SuccessGreen)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(tc.name, color = colors.textPrimary, fontSize = 12.5.sp, fontWeight = FontWeight.ExtraBold)
                    Text(tc.subject, color = colors.textMuted, fontSize = 10.5.sp)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    MiniActionButton(
                        icon = Icons.Default.Edit,
                        contentDescription = stringResource(R.string.cd_edit),
                        onClick = { onEdit(tc) }
                    )
                    MiniActionButton(
                        icon = Icons.Default.Close,
                        contentDescription = stringResource(R.string.cd_delete),
                        isDanger = true,
                        onClick = { onRemove(tc.id) }
                    )
                }
            }
            if (idx < teachers.lastIndex) HorizontalDivider(color = colors.line)
        }
    }
}

@Composable
private fun AdminCoursesContent(
    courses: List<Course>,
    onAdd: () -> Unit,
    onEdit: (Course) -> Unit,
    onDelete: (Long) -> Unit
) {
    val colors = AlFalahTheme.colors
    SectionHeader(title = "Courses (${courses.size})", actionText = "+ Add", onActionClick = onAdd)
    InstituteCard {
        courses.forEachIndexed { idx, c ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(c.title, color = colors.textPrimary, fontSize = 12.5.sp, fontWeight = FontWeight.ExtraBold)
                    Text("${c.teacher} · ${c.students} students", color = colors.textMuted, fontSize = 10.5.sp)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    MiniActionButton(
                        icon = Icons.Default.Edit,
                        contentDescription = stringResource(R.string.cd_edit),
                        onClick = { onEdit(c) }
                    )
                    MiniActionButton(
                        icon = Icons.Default.Close,
                        contentDescription = stringResource(R.string.cd_delete),
                        isDanger = true,
                        onClick = { onDelete(c.id) }
                    )
                }
            }
            if (idx < courses.lastIndex) HorizontalDivider(color = colors.line)
        }
    }
}

@Composable
private fun AdminBooksContent(
    materials: List<StudyMaterial>,
    onAdd: () -> Unit,
    onEdit: (StudyMaterial) -> Unit,
    onDelete: (Long) -> Unit
) {
    val colors = AlFalahTheme.colors
    SectionHeader(title = "Books & Content (${materials.size})", actionText = "+ Add", onActionClick = onAdd)
    Text(
        text = "No limit. Includes books plus notes/photos/documents/audio uploaded by students & teachers — Admin can remove any of it.",
        color = colors.textMuted,
        fontSize = 11.5.sp,
        modifier = Modifier.padding(bottom = 12.dp)
    )
    InstituteCard {
        if (materials.isEmpty()) {
            Text(
                text = "No books yet — add your first one",
                color = colors.textMuted,
                fontSize = 12.sp,
                modifier = Modifier.padding(vertical = 6.dp)
            )
        } else {
            materials.forEachIndexed { idx, m ->
                val byText = m.uploader?.let { " · by ${it.name} (${it.role})" }.orEmpty()
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Purple900),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.AutoStories,
                            contentDescription = null,
                            tint = Gold400,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(m.title, color = colors.textPrimary, fontSize = 12.5.sp, fontWeight = FontWeight.ExtraBold)
                        Text("${m.course} · ${m.type}$byText", color = colors.textMuted, fontSize = 10.5.sp)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        MiniActionButton(
                            icon = Icons.Default.Edit,
                            contentDescription = stringResource(R.string.cd_edit),
                            onClick = { onEdit(m) }
                        )
                        MiniActionButton(
                            icon = Icons.Default.Close,
                            contentDescription = stringResource(R.string.cd_delete),
                            isDanger = true,
                            onClick = { onDelete(m.id) }
                        )
                    }
                }
                if (idx < materials.lastIndex) HorizontalDivider(color = colors.line)
            }
        }
    }
}

@Composable
private fun AdminHadithContent(
    hadith: Hadith,
    onSave: (String, String, String) -> Unit
) {
    var ar by remember(hadith.ar) { mutableStateOf(hadith.ar) }
    var tr by remember(hadith.tr) { mutableStateOf(hadith.tr) }
    var src by remember(hadith.src) { mutableStateOf(hadith.src) }

    SectionHeader(title = "Daily Hadith")
    InstituteCard {
        StyledTextField(
            label = "Arabic Text",
            value = ar,
            onValueChange = { ar = it },
            singleLine = false,
            minLines = 3,
            isRtlArabic = true,
            testTag = "admin_hadith_ar"
        )
        Spacer(modifier = Modifier.height(12.dp))
        StyledTextField(
            label = "Translation",
            value = tr,
            onValueChange = { tr = it },
            singleLine = false,
            minLines = 3,
            testTag = "admin_hadith_tr"
        )
        Spacer(modifier = Modifier.height(12.dp))
        StyledTextField(
            label = "Reference / Source",
            value = src,
            onValueChange = { src = it },
            testTag = "admin_hadith_src"
        )
        Spacer(modifier = Modifier.height(14.dp))
        GoldPrimaryButton(
            text = "Save Hadith",
            testTag = "save_hadith_btn",
            onClick = { onSave(ar, tr, src) }
        )
    }
}

@Composable
private fun AdminAppContentTab(
    appState: InstituteAppState,
    onChangeLogo: () -> Unit,
    onChangeBanner: () -> Unit,
    onGoCourses: () -> Unit,
    onOpenAbout: () -> Unit,
    onOpenProfile: () -> Unit,
    onToast: (String) -> Unit
) {
    val colors = AlFalahTheme.colors
    val bannerFile = appState.banner.imagePath?.let { File(it) }?.takeIf { it.exists() }

    SectionHeader(title = "App Content")
    InstituteCard {
        // App Logo row
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp)
        ) {
            InstituteLogoImage(
                customLogoPath = appState.customLogoPath,
                size = 38.dp,
                clipCircle = false,
                cornerRadius = 12.dp
            )
            Column(modifier = Modifier.weight(1f)) {
                Text("App Logo", color = colors.textPrimary, fontSize = 12.5.sp, fontWeight = FontWeight.ExtraBold)
                Text("Shown on splash, login and every dashboard", color = colors.textMuted, fontSize = 10.5.sp)
            }
            GhostButton(text = "Change", compact = true, testTag = "admin_change_logo_btn", onClick = onChangeLogo)
        }
        HorizontalDivider(color = colors.line)

        // Home Poster row
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp)
        ) {
            if (bannerFile != null) {
                AsyncImage(
                    model = bannerFile,
                    contentDescription = "Current poster",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(11.dp))
                        .background(Purple900),
                    contentAlignment = Alignment.Center
                ) {
                    Text("📢", fontSize = 16.sp)
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text("Home Poster / Announcement", color = colors.textPrimary, fontSize = 12.5.sp, fontWeight = FontWeight.ExtraBold)
                Text("Reel-size poster shown on Home for everyone", color = colors.textMuted, fontSize = 10.5.sp)
            }
            GhostButton(
                text = if (bannerFile != null) "Change" else "Upload",
                compact = true,
                testTag = "admin_change_banner_btn",
                onClick = onChangeBanner
            )
        }
        HorizontalDivider(color = colors.line)

        ContentSettingRow("Testimonials", "Manage homepage testimonials", "Manage") {
            onToast("Manage testimonials (demo)")
        }
        HorizontalDivider(color = colors.line)
        ContentSettingRow("FAQ", "Frequently asked questions", "Manage") {
            onToast("Manage FAQ (demo)")
        }
        HorizontalDivider(color = colors.line)
        ContentSettingRow("About Section", "Institute description", "View", onOpenAbout)
        HorizontalDivider(color = colors.line)
        ContentSettingRow("Contact Info", "Phone, email, address", "View", onOpenProfile)
        HorizontalDivider(color = colors.line)
        ContentSettingRow("WhatsApp / Instagram / Telegram", "Social links", "View", onOpenProfile)
        HorizontalDivider(color = colors.line)
        ContentSettingRow("Class Meeting Links", "Update Zoom / Google Meet links", "Manage", onGoCourses)
    }
}

@Composable
private fun ContentSettingRow(
    title: String,
    subtitle: String,
    buttonLabel: String,
    onClick: () -> Unit
) {
    val colors = AlFalahTheme.colors
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = colors.textPrimary, fontSize = 12.5.sp, fontWeight = FontWeight.ExtraBold)
            Text(subtitle, color = colors.textMuted, fontSize = 10.5.sp)
        }
        GhostButton(text = buttonLabel, compact = true, onClick = onClick)
    }
}

@Composable
fun TeacherDashboardScreen(
    uiState: AlFalahUiState,
    onSelectTab: (TeacherTab) -> Unit,
    onNavigate: (AppScreen) -> Unit,
    onDismissBanner: () -> Unit,
    onJoinClass: (OnlineClass) -> Unit,
    onOpenSheet: (ActiveSheet) -> Unit,
    canManageMaterial: (StudyMaterial) -> Boolean,
    onOpenMaterialFile: (StudyMaterial) -> Unit,
    onDeleteMaterial: (Long) -> Unit,
    onPlayVideo: (VideoLesson) -> Unit,
    onToast: (String) -> Unit
) {
    val colors = AlFalahTheme.colors
    val appState = uiState.appState

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bg)
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding()
    ) {
        AlFalahAppHeader(
            customLogoPath = appState.customLogoPath,
            subtitleUnderBrand = "Teacher Dashboard",
            greetingTitle = "Assalamu Alaikum, Ustadh",
            greetingSubtitle = "Manage your classes and students",
            roleBadge = "TEACHER",
            showNotificationBell = false,
            showFounderBlink = false,
            bottomPadding = 28.dp,
            onIslamicUpdatesClick = { onNavigate(AppScreen.ISLAMIC_UPDATES) },
            onProfileClick = { onNavigate(AppScreen.PROFILE) }
        )

        AnnouncementBannerCard(
            banner = appState.banner,
            dismissed = uiState.bannerDismissed,
            onDismiss = onDismissBanner,
            modifier = Modifier.padding(start = 18.dp, end = 18.dp, top = 14.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(start = 18.dp, end = 18.dp, top = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TeacherTab.entries.forEach { tab ->
                RoleTabChip(
                    label = tab.label,
                    active = uiState.teacherTab == tab,
                    testTag = "teacher_tab_${tab.name.lowercase()}",
                    onClick = { onSelectTab(tab) }
                )
            }
        }

        Column(
            modifier = Modifier.padding(start = 18.dp, end = 18.dp, top = 10.dp, bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            when (uiState.teacherTab) {
                TeacherTab.CLASSES -> {
                    SectionHeader(title = "Assigned Classes")
                    appState.classes.take(3).forEach { onlineClass ->
                        OnlineClassCard(
                            item = onlineClass,
                            onJoinClick = { onJoinClass(onlineClass) },
                            secondaryButtonText = "Change Link",
                            onSecondaryClick = { onToast("Link change requires Admin permission") }
                        )
                    }
                }

                TeacherTab.MATERIAL -> {
                    SectionHeader(
                        title = "Books & Content",
                        actionText = "+ Upload",
                        onActionClick = { onOpenSheet(ActiveSheet.UploadContent) }
                    )
                    Text(
                        text = "You can manage items you uploaded. Admin can manage all of it.",
                        color = colors.textMuted,
                        fontSize = 11.sp
                    )
                    if (appState.materials.isEmpty()) {
                        EmptyStateView("Nothing uploaded yet")
                    } else {
                        appState.materials.forEach { item ->
                            StudyMaterialCard(
                                material = item,
                                canManage = canManageMaterial(item),
                                showInlinePreview = false,
                                onOpenFile = onOpenMaterialFile,
                                onDelete = { onDeleteMaterial(item.id) }
                            )
                        }
                    }
                }

                TeacherTab.ASSIGNMENTS -> {
                    SectionHeader(
                        title = "Student Submissions",
                        actionText = "+ New",
                        onActionClick = { onOpenSheet(ActiveSheet.CreateAssignment) }
                    )
                    appState.assignments.forEach { a ->
                        InstituteCard {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Text(
                                    text = a.title,
                                    color = colors.textPrimary,
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.weight(1f)
                                )
                                PillBadge(
                                    text = a.status.uppercase(),
                                    style = if (a.status == "reviewed") "default" else "upcoming"
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text("📘 ${a.course}", color = colors.textMuted, fontSize = 10.5.sp)
                                Text("📅 Due ${a.due}", color = colors.textMuted, fontSize = 10.5.sp)
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            if (a.status != "reviewed") {
                                GoldPrimaryButton(
                                    text = "Review & Give Feedback",
                                    compact = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    onClick = { onOpenSheet(ActiveSheet.GiveFeedback(a)) }
                                )
                            } else if (!a.feedback.isNullOrBlank()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(9.dp))
                                        .background(colors.goldTint)
                                        .padding(horizontal = 10.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        text = "💬 ${a.feedback}",
                                        color = colors.textSecondary,
                                        fontSize = 11.5.sp
                                    )
                                }
                            }
                        }
                    }
                }

                TeacherTab.VIDEOS -> {
                    SectionHeader(
                        title = "My Videos",
                        actionText = "+ Upload",
                        onActionClick = { onOpenSheet(ActiveSheet.UploadVideo) }
                    )
                    appState.videos.forEach { video ->
                        VideoLessonCard(
                            video = video,
                            onClick = { onPlayVideo(video) },
                            trailing = {
                                MiniActionButton(
                                    icon = Icons.Default.Edit,
                                    contentDescription = stringResource(R.string.cd_edit),
                                    onClick = { onOpenSheet(ActiveSheet.EditVideo(video)) }
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RoleTabChip(
    label: String,
    active: Boolean,
    testTag: String,
    onClick: () -> Unit
) {
    val colors = AlFalahTheme.colors
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (active) Purple900 else colors.card)
            .border(
                1.5.dp,
                if (active) Purple900 else colors.line,
                RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 15.dp, vertical = 9.dp)
            .testTag(testTag)
    ) {
        Text(
            text = label,
            color = if (active) PureWhite else colors.textSecondary,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}
