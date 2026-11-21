package com.example.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.theme.*
import com.example.viewmodel.AlFalahViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppModalSheetHost(
    activeSheet: ActiveSheet?,
    appState: InstituteAppState,
    viewModel: AlFalahViewModel
) {
    if (activeSheet == null) return
    val colors = AlFalahTheme.colors
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = { viewModel.closeSheet() },
        sheetState = sheetState,
        containerColor = colors.card,
        shape = RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 8.dp)
                    .width(38.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(colors.line)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            when (activeSheet) {
                is ActiveSheet.CourseDetail -> {
                    val c = activeSheet.course
                    SheetTitle(c.title)
                    Text(c.desc, color = colors.textMuted, fontSize = 12.5.sp, lineHeight = 19.sp)
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("👤 ${c.teacher}   ·   ⏱ ${c.duration}", color = colors.textSecondary, fontSize = 11.5.sp)
                        Text("🕒 ${c.timing}   ·   👥 ${c.students} students", color = colors.textSecondary, fontSize = 11.5.sp)
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(colors.line)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth((c.progress / 100f).coerceIn(0f, 1f))
                                .clip(RoundedCornerShape(6.dp))
                                .background(Gold500)
                        )
                    }
                    GoldPrimaryButton(
                        text = "Join Course",
                        testTag = "sheet_join_course_btn",
                        onClick = { viewModel.enrollInCourse(c) }
                    )
                }

                is ActiveSheet.Notifications -> {
                    SheetTitle("Notifications")
                    appState.notifications.forEachIndexed { idx, notif ->
                        Column(modifier = Modifier.padding(vertical = 4.dp)) {
                            Text(notif.title, color = colors.textPrimary, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(notif.body, color = colors.textMuted, fontSize = 11.5.sp)
                        }
                        if (idx < appState.notifications.lastIndex) HorizontalDivider(color = colors.line)
                    }
                }

                is ActiveSheet.Progress -> {
                    SheetTitle("My Progress")
                    val activeCourses = appState.courses.filter { it.progress > 0 }
                    activeCourses.forEachIndexed { idx, c ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(c.title, color = colors.textPrimary, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
                                Text("${c.progress}% complete", color = colors.textMuted, fontSize = 11.sp)
                            }
                            PillBadge(text = "${c.progress}%")
                        }
                        if (idx < activeCourses.lastIndex) HorizontalDivider(color = colors.line)
                    }
                }

                is ActiveSheet.UploadContent -> {
                    val courseNames = appState.courses.map { it.title }.ifEmpty { listOf("Noorani Qaida") }
                    val types = listOf("Notes", "Photo", "Document", "Audio", "Book", "PDF")
                    var title by remember { mutableStateOf("") }
                    var selectedCourse by remember { mutableStateOf(courseNames.first()) }
                    var selectedType by remember { mutableStateOf(types.first()) }
                    var pickedUri by remember { mutableStateOf<Uri?>(null) }

                    val photoPicker = rememberLauncherForActivityResult(
                        ActivityResultContracts.PickVisualMedia()
                    ) { uri -> if (uri != null) pickedUri = uri }

                    val docPicker = rememberLauncherForActivityResult(
                        ActivityResultContracts.OpenDocument()
                    ) { uri -> if (uri != null) pickedUri = uri }

                    SheetTitle("Upload Content")
                    Text(
                        text = "Notes, photos, documents or audio — visible to everyone. Admin can manage/remove any upload; you can manage your own.",
                        color = colors.textMuted,
                        fontSize = 11.5.sp
                    )
                    StyledTextField(
                        label = "Title",
                        value = title,
                        onValueChange = { title = it },
                        placeholder = "e.g. My Tajweed Notes",
                        testTag = "upload_content_title_input"
                    )
                    StyledDropdownField(
                        label = "Course (optional)",
                        options = courseNames,
                        selected = selectedCourse,
                        onSelect = { selectedCourse = it }
                    )
                    StyledDropdownField(
                        label = "Type",
                        options = types,
                        selected = selectedType,
                        onSelect = { selectedType = it }
                    )
                    GhostButton(
                        text = if (pickedUri != null) "✓ File Attached (Tap to change)" else "Attach File (optional)",
                        onClick = {
                            if (selectedType == "Photo") {
                                photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            } else {
                                docPicker.launch(arrayOf("image/*", "audio/*", "application/pdf", "text/*", "*/*"))
                            }
                        }
                    )
                    GoldPrimaryButton(
                        text = "Upload",
                        testTag = "upload_content_submit_btn",
                        onClick = { viewModel.uploadStudyContent(title, selectedCourse, selectedType, pickedUri) }
                    )
                }

                is ActiveSheet.AddIslamicUpdate -> {
                    val categories = listOf("Wallpaper", "Status", "Reel", "Poster", "Ringtone")
                    var title by remember { mutableStateOf("") }
                    var category by remember { mutableStateOf(categories.first()) }
                    var pickedImage by remember { mutableStateOf<Uri?>(null) }

                    val photoPicker = rememberLauncherForActivityResult(
                        ActivityResultContracts.PickVisualMedia()
                    ) { uri -> if (uri != null) pickedImage = uri }

                    SheetTitle("Upload Islamic Update")
                    StyledTextField(
                        label = "Title",
                        value = title,
                        onValueChange = { title = it },
                        placeholder = "e.g. Eid Mubarak Wallpaper"
                    )
                    StyledDropdownField(
                        label = "Category",
                        options = categories,
                        selected = category,
                        onSelect = { category = it }
                    )
                    GhostButton(
                        text = if (pickedImage != null) "✓ Thumbnail Selected" else "Choose Thumbnail Image (optional)",
                        onClick = {
                            photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        }
                    )
                    GoldPrimaryButton(text = "Upload", onClick = {
                        viewModel.addIslamicUpdate(title, category, pickedImage)
                    })
                    Text(
                        text = "Visible instantly to every Student and Teacher. Only Admin can upload or delete.",
                        color = colors.textMuted,
                        fontSize = 11.sp
                    )
                }

                is ActiveSheet.AddTeacher -> {
                    var name by remember { mutableStateOf("") }
                    var subject by remember { mutableStateOf("") }
                    SheetTitle("Add Teacher")
                    StyledTextField(label = "Full Name", value = name, onValueChange = { name = it }, placeholder = "Ustadh name")
                    StyledTextField(label = "Subject", value = subject, onValueChange = { subject = it }, placeholder = "e.g. Tajweed")
                    GoldPrimaryButton(text = "Create Teacher Account", onClick = {
                        viewModel.addTeacher(name, subject)
                    })
                }

                is ActiveSheet.EditTeacher -> {
                    var name by remember { mutableStateOf(activeSheet.teacher.name) }
                    var subject by remember { mutableStateOf(activeSheet.teacher.subject) }
                    SheetTitle("Edit Teacher")
                    StyledTextField(label = "Full Name", value = name, onValueChange = { name = it })
                    StyledTextField(label = "Subject", value = subject, onValueChange = { subject = it })
                    GoldPrimaryButton(text = "Save Teacher", onClick = {
                        viewModel.updateTeacher(activeSheet.teacher.copy(name = name.trim(), subject = subject.trim()))
                    })
                }

                is ActiveSheet.EditStudent -> {
                    val statuses = listOf("approved", "pending", "blocked")
                    var name by remember { mutableStateOf(activeSheet.student.name) }
                    var city by remember { mutableStateOf(activeSheet.student.city) }
                    var status by remember { mutableStateOf(activeSheet.student.status) }
                    SheetTitle("Edit Student")
                    StyledTextField(label = "Full Name", value = name, onValueChange = { name = it })
                    StyledTextField(label = "City", value = city, onValueChange = { city = it })
                    StyledDropdownField(label = "Status", options = statuses, selected = status, onSelect = { status = it })
                    GoldPrimaryButton(text = "Save Student", onClick = {
                        viewModel.updateStudent(
                            activeSheet.student.copy(name = name.trim(), city = city.trim(), status = status)
                        )
                    })
                }

                is ActiveSheet.AddCourse -> {
                    var title by remember { mutableStateOf("") }
                    var teacher by remember { mutableStateOf("") }
                    var desc by remember { mutableStateOf("") }
                    var duration by remember { mutableStateOf("3 months") }
                    var timing by remember { mutableStateOf("Mon–Fri, 5:00 PM") }
                    SheetTitle("Add Course")
                    StyledTextField(label = "Course Title", value = title, onValueChange = { title = it }, placeholder = "e.g. Tafseer-ul-Quran")
                    StyledTextField(label = "Teacher", value = teacher, onValueChange = { teacher = it }, placeholder = "e.g. Mufti Zubair")
                    StyledTextField(label = "Description", value = desc, onValueChange = { desc = it }, singleLine = false, minLines = 2)
                    StyledTextField(label = "Duration", value = duration, onValueChange = { duration = it })
                    StyledTextField(label = "Class Timing", value = timing, onValueChange = { timing = it })
                    GoldPrimaryButton(text = "Create Course", onClick = {
                        viewModel.addCourse(title, teacher, desc, duration, timing)
                    })
                }

                is ActiveSheet.EditCourse -> {
                    var title by remember { mutableStateOf(activeSheet.course.title) }
                    var teacher by remember { mutableStateOf(activeSheet.course.teacher) }
                    var desc by remember { mutableStateOf(activeSheet.course.desc) }
                    var timing by remember { mutableStateOf(activeSheet.course.timing) }
                    SheetTitle("Edit Course")
                    StyledTextField(label = "Course Title", value = title, onValueChange = { title = it })
                    StyledTextField(label = "Teacher", value = teacher, onValueChange = { teacher = it })
                    StyledTextField(label = "Description", value = desc, onValueChange = { desc = it }, singleLine = false, minLines = 2)
                    StyledTextField(label = "Class Timing", value = timing, onValueChange = { timing = it })
                    GoldPrimaryButton(text = "Save Course", onClick = {
                        viewModel.updateCourse(
                            activeSheet.course.copy(
                                title = title.trim(),
                                teacher = teacher.trim(),
                                desc = desc.trim(),
                                timing = timing.trim()
                            )
                        )
                    })
                }

                is ActiveSheet.ChangeLogo -> {
                    var pickedLogo by remember { mutableStateOf<Uri?>(null) }
                    val photoPicker = rememberLauncherForActivityResult(
                        ActivityResultContracts.PickVisualMedia()
                    ) { uri -> if (uri != null) pickedLogo = uri }

                    SheetTitle("Change App Logo")
                    Text(
                        text = "Upload a square image. It will replace the logo on the splash screen, login page and every dashboard.",
                        color = colors.textMuted,
                        fontSize = 11.5.sp
                    )
                    GhostButton(
                        text = if (pickedLogo != null) "✓ Image Selected (Tap to change)" else "Choose Logo Image",
                        onClick = {
                            photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        }
                    )
                    GoldPrimaryButton(text = "Save Logo", onClick = { viewModel.saveCustomLogo(pickedLogo) })
                }

                is ActiveSheet.ChangeBanner -> {
                    var pickedPoster by remember { mutableStateOf<Uri?>(null) }
                    var caption by remember { mutableStateOf(appState.banner.caption) }
                    val photoPicker = rememberLauncherForActivityResult(
                        ActivityResultContracts.PickVisualMedia()
                    ) { uri -> if (uri != null) pickedPoster = uri }

                    SheetTitle("Home Poster / Announcement")
                    Text(
                        text = "Upload a reel/story-sized poster (vertical, e.g. 1080×1920). It appears on the Home screen for every Student, Teacher and Admin.",
                        color = colors.textMuted,
                        fontSize = 11.5.sp
                    )
                    GhostButton(
                        text = if (pickedPoster != null) "✓ Poster Selected (Tap to change)" else "Choose Poster Image",
                        onClick = {
                            photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        }
                    )
                    StyledTextField(
                        label = "Caption (optional)",
                        value = caption,
                        onValueChange = { caption = it },
                        placeholder = "e.g. Eid holidays announcement"
                    )
                    GoldPrimaryButton(text = "Publish to Home", onClick = { viewModel.saveBanner(pickedPoster, caption) })
                    if (!appState.banner.imagePath.isNullOrBlank()) {
                        GhostButton(text = "Remove Current Poster", onClick = { viewModel.removeBanner() })
                    }
                }

                is ActiveSheet.Announcement -> {
                    var title by remember { mutableStateOf("") }
                    var body by remember { mutableStateOf("") }
                    SheetTitle("Publish Announcement")
                    StyledTextField(label = "Title", value = title, onValueChange = { title = it }, placeholder = "Announcement title")
                    StyledTextField(
                        label = "Message",
                        value = body,
                        onValueChange = { body = it },
                        placeholder = "Write your announcement...",
                        singleLine = false,
                        minLines = 3
                    )
                    GoldPrimaryButton(text = "Publish", onClick = { viewModel.publishAnnouncement(title, body) })
                }

                is ActiveSheet.AdminAddBook -> {
                    val courseNames = appState.courses.map { it.title }.ifEmpty { listOf("Noorani Qaida") }
                    val types = listOf("Book", "PDF", "Notes", "Audiobook")
                    var title by remember { mutableStateOf("") }
                    var course by remember { mutableStateOf(courseNames.first()) }
                    var type by remember { mutableStateOf(types.first()) }

                    SheetTitle("Add Book")
                    Text("Unlimited books can be added — no cap.", color = colors.textMuted, fontSize = 11.5.sp)
                    StyledTextField(label = "Book Title", value = title, onValueChange = { title = it }, placeholder = "e.g. Bahishti Zewar")
                    StyledDropdownField(label = "Related Course", options = courseNames, selected = course, onSelect = { course = it })
                    StyledDropdownField(label = "Type", options = types, selected = type, onSelect = { type = it })
                    GoldPrimaryButton(text = "Add Book", onClick = { viewModel.addBookFromAdmin(title, course, type) })
                }

                is ActiveSheet.EditBook -> {
                    var title by remember { mutableStateOf(activeSheet.material.title) }
                    SheetTitle("Edit Book")
                    StyledTextField(label = "Book Title", value = title, onValueChange = { title = it })
                    GoldPrimaryButton(text = "Save Book", onClick = {
                        viewModel.updateBook(activeSheet.material.copy(title = title.trim()))
                    })
                }

                is ActiveSheet.CreateAssignment -> {
                    val courseNames = appState.courses.map { it.title }.ifEmpty { listOf("Noorani Qaida") }
                    var title by remember { mutableStateOf("") }
                    var course by remember { mutableStateOf(courseNames.first()) }
                    var desc by remember { mutableStateOf("") }
                    var due by remember { mutableStateOf("") }

                    SheetTitle("Create Assignment")
                    StyledTextField(label = "Title", value = title, onValueChange = { title = it }, placeholder = "Assignment title")
                    StyledDropdownField(label = "Course", options = courseNames, selected = course, onSelect = { course = it })
                    StyledTextField(label = "Description", value = desc, onValueChange = { desc = it }, placeholder = "Instructions...", singleLine = false, minLines = 3)
                    StyledTextField(label = "Submission Date", value = due, onValueChange = { due = it }, placeholder = "e.g. Oct 05")
                    GoldPrimaryButton(text = "Create Assignment", onClick = {
                        viewModel.createAssignment(title, course, desc, due)
                    })
                }

                is ActiveSheet.GiveFeedback -> {
                    var feedback by remember { mutableStateOf("") }
                    SheetTitle("Review: ${activeSheet.assignment.title}")
                    StyledTextField(
                        label = "Feedback",
                        value = feedback,
                        onValueChange = { feedback = it },
                        placeholder = "Write feedback for the student...",
                        singleLine = false,
                        minLines = 3
                    )
                    GoldPrimaryButton(text = "Send Feedback", onClick = {
                        viewModel.sendAssignmentFeedback(activeSheet.assignment.id, feedback)
                    })
                }

                is ActiveSheet.UploadVideo -> {
                    var title by remember { mutableStateOf("") }
                    var desc by remember { mutableStateOf("") }
                    var url by remember { mutableStateOf("") }
                    SheetTitle("Upload Video")
                    StyledTextField(label = "Title", value = title, onValueChange = { title = it }, placeholder = "Video title")
                    StyledTextField(label = "Description", value = desc, onValueChange = { desc = it }, placeholder = "Short description", singleLine = false, minLines = 2)
                    StyledTextField(label = "Video URL", value = url, onValueChange = { url = it }, placeholder = "https://...")
                    GoldPrimaryButton(text = "Upload Video", onClick = { viewModel.uploadVideo(title, desc, url) })
                }

                is ActiveSheet.EditVideo -> {
                    var title by remember { mutableStateOf(activeSheet.video.title) }
                    var desc by remember { mutableStateOf(activeSheet.video.desc) }
                    SheetTitle("Edit Video")
                    StyledTextField(label = "Title", value = title, onValueChange = { title = it })
                    StyledTextField(label = "Description", value = desc, onValueChange = { desc = it }, singleLine = false, minLines = 2)
                    GoldPrimaryButton(text = "Save Video", onClick = {
                        viewModel.updateVideo(activeSheet.video.copy(title = title.trim(), desc = desc.trim()))
                    })
                }
            }
        }
    }
}

@Composable
private fun SheetTitle(text: String) {
    val colors = AlFalahTheme.colors
    Text(
        text = text,
        color = colors.textPrimary,
        fontSize = 16.sp,
        fontWeight = FontWeight.ExtraBold
    )
}
