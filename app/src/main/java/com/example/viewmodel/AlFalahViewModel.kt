package com.example.viewmodel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.firebase.AiChatMessage
import com.example.firebase.AiHistoryTurn
import com.example.firebase.AiStreamEvent
import com.example.firebase.AlFalahFirestoreRepository
import com.example.firebase.GeminiIslamicAssistantRepository
import com.example.model.*
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface AiChatUiState {
    data object Idle : AiChatUiState
    data object Loading : AiChatUiState
    data class Streaming(val partialText: String) : AiChatUiState
    data class Error(val message: String) : AiChatUiState
}

data class AlFalahUiState(
    val currentScreen: AppScreen = AppScreen.HOME,
    val appState: InstituteAppState = InstituteAppState(),
    val currentUser: CurrentUser = CurrentUser(),
    val courseSearch: String = "",
    val classFilter: String = "all",
    val updateFilter: String = "all",
    val adminTab: AdminTab = AdminTab.OVERVIEW,
    val teacherTab: TeacherTab = TeacherTab.CLASSES,
    val bannerDismissed: Boolean = false,
    val activeSheet: ActiveSheet? = null,
    val toastMessage: String? = null,
    val isSyncing: Boolean = true,
    val syncError: String? = null,
    val aiMessages: List<AiChatMessage> = emptyList(),
    val aiUiState: AiChatUiState = AiChatUiState.Idle
)

class AlFalahViewModel(
    private val uid: String,
    private val initialDisplayName: String,
    private val initialEmail: String,
    private val initialRole: UserRole,
    private val firestoreRepo: AlFalahFirestoreRepository,
    private val aiRepo: GeminiIslamicAssistantRepository
) : ViewModel() {

    companion object {
        private const val TAG = "AlFalahViewModel"

        fun factory(
            uid: String,
            initialDisplayName: String,
            initialEmail: String,
            initialRole: UserRole,
            firestoreRepo: AlFalahFirestoreRepository,
            aiRepo: GeminiIslamicAssistantRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return AlFalahViewModel(
                    uid = uid,
                    initialDisplayName = initialDisplayName,
                    initialEmail = initialEmail,
                    initialRole = initialRole,
                    firestoreRepo = firestoreRepo,
                    aiRepo = aiRepo
                ) as T
            }
        }
    }

    private val _uiState = MutableStateFlow(
        AlFalahUiState(
            currentScreen = homeScreenFor(initialRole),
            currentUser = CurrentUser(
                name = initialDisplayName.ifBlank { "Student" },
                email = initialEmail,
                role = initialRole
            )
        )
    )
    val uiState: StateFlow<AlFalahUiState> = _uiState.asStateFlow()

    private var toastJob: Job? = null
    private var aiJob: Job? = null
    private var hasAppliedInitialRoleSync = false

    init {
        viewModelScope.launch {
            val seedResult = firestoreRepo.ensureWorkspaceSeeded(
                uid = uid,
                displayName = initialDisplayName,
                email = initialEmail,
                initialRole = initialRole
            )
            seedResult.onFailure { err ->
                Log.e(TAG, "Failed to seed Firestore workspace", err)
                _uiState.update {
                    it.copy(
                        isSyncing = false,
                        syncError = err.localizedMessage ?: "Failed to initialize cloud workspace."
                    )
                }
            }
        }

        viewModelScope.launch {
            firestoreRepo.observeWorkspace(uid)
                .catch { error ->
                    val firestoreEx = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreEx == null && error is CancellationException) throw error
                    Log.e(TAG, "Firestore workspace stream failed", error)
                    _uiState.update {
                        it.copy(
                            isSyncing = false,
                            syncError = firestoreEx?.localizedMessage
                                ?: error.localizedMessage
                                ?: "Unable to sync data with Firestore."
                        )
                    }
                }
                .collect { snapshot ->
                    val profile = snapshot.profile
                    val syncedRole = profile?.roleView?.let { UserRole.fromKey(it) } ?: initialRole
                    _uiState.update { prev ->
                        val updatedUser = CurrentUser(
                            name = profile?.displayName?.ifBlank { prev.currentUser.name }
                                ?: prev.currentUser.name,
                            email = profile?.email?.ifBlank { prev.currentUser.email }
                                ?: prev.currentUser.email,
                            phone = profile?.phone ?: prev.currentUser.phone,
                            role = syncedRole
                        )
                        val updatedScreen = if (!hasAppliedInitialRoleSync && profile != null) {
                            hasAppliedInitialRoleSync = true
                            homeScreenFor(syncedRole)
                        } else {
                            prev.currentScreen
                        }
                        prev.copy(
                            currentScreen = updatedScreen,
                            currentUser = updatedUser,
                            appState = snapshot.appState,
                            aiMessages = snapshot.aiMessages,
                            isSyncing = false,
                            syncError = null
                        )
                    }
                }
        }
    }

    private fun homeScreenFor(role: UserRole): AppScreen = when (role) {
        UserRole.ADMIN -> AppScreen.ADMIN_HOME
        UserRole.TEACHER -> AppScreen.TEACHER_HOME
        UserRole.STUDENT -> AppScreen.HOME
    }

    fun showToast(message: String) {
        toastJob?.cancel()
        _uiState.update { it.copy(toastMessage = message) }
        toastJob = viewModelScope.launch {
            delay(2600L)
            _uiState.update { it.copy(toastMessage = null) }
        }
    }

    fun openSheet(sheet: ActiveSheet) {
        _uiState.update { it.copy(activeSheet = sheet) }
    }

    fun closeSheet() {
        _uiState.update { it.copy(activeSheet = null) }
    }

    private fun launchWrite(
        successToast: String? = null,
        closeModalOnSuccess: Boolean = false,
        block: suspend () -> Result<Unit>
    ) {
        viewModelScope.launch {
            val result = block()
            result.onSuccess {
                if (closeModalOnSuccess) closeSheet()
                if (!successToast.isNullOrBlank()) showToast(successToast)
            }.onFailure { err ->
                showToast(err.localizedMessage ?: "Cloud sync failed. Please try again.")
            }
        }
    }

    // ---------- Navigation & Role Workspace Switching ----------
    fun goTo(screen: AppScreen) {
        _uiState.update { it.copy(currentScreen = screen) }
    }

    fun goHome() {
        val home = homeScreenFor(_uiState.value.currentUser.role)
        _uiState.update { it.copy(currentScreen = home) }
    }

    fun switchWorkspaceRole(role: UserRole) {
        _uiState.update {
            it.copy(
                currentUser = it.currentUser.copy(role = role),
                currentScreen = homeScreenFor(role),
                adminTab = AdminTab.OVERVIEW,
                teacherTab = TeacherTab.CLASSES
            )
        }
        launchWrite(successToast = "Switched to ${role.label} dashboard") {
            firestoreRepo.updateRoleView(role)
        }
    }

    fun handleBack(): Boolean {
        val state = _uiState.value
        if (state.activeSheet != null) {
            closeSheet()
            return true
        }
        return when (state.currentScreen) {
            AppScreen.ABOUT -> {
                goTo(AppScreen.PROFILE)
                true
            }
            AppScreen.PROFILE,
            AppScreen.ISLAMIC_UPDATES,
            AppScreen.AI_ASSISTANT,
            AppScreen.COURSES,
            AppScreen.CLASSES,
            AppScreen.VIDEOS,
            AppScreen.MATERIAL,
            AppScreen.ASSIGNMENTS -> {
                goHome()
                true
            }
            else -> false
        }
    }

    // ---------- Gemini AI Study Assistant ----------
    fun sendAiPrompt(rawPrompt: String, topic: String = "Study") {
        val prompt = rawPrompt.trim()
        if (prompt.isEmpty()) return

        aiJob?.cancel()
        val historySnapshot = _uiState.value.aiMessages.map {
            AiHistoryTurn(sender = it.sender, text = it.text)
        }

        aiJob = viewModelScope.launch {
            _uiState.update { it.copy(aiUiState = AiChatUiState.Loading) }

            val saveUserResult = firestoreRepo.saveAiChatMessage(
                sender = "user",
                text = prompt,
                topic = topic
            )
            saveUserResult.onFailure { err ->
                Log.e(TAG, "Failed to save user AI prompt to Firestore", err)
            }

            aiRepo.streamStudyReply(prompt, historySnapshot).collect { event ->
                when (event) {
                    is AiStreamEvent.Loading -> {
                        _uiState.update { it.copy(aiUiState = AiChatUiState.Loading) }
                    }
                    is AiStreamEvent.Streaming -> {
                        _uiState.update { it.copy(aiUiState = AiChatUiState.Streaming(event.text)) }
                    }
                    is AiStreamEvent.Success -> {
                        val saveReply = firestoreRepo.saveAiChatMessage(
                            sender = "assistant",
                            text = event.text,
                            topic = topic
                        )
                        saveReply.onFailure { err ->
                            Log.e(TAG, "Failed to save AI reply to Firestore", err)
                        }
                        _uiState.update { it.copy(aiUiState = AiChatUiState.Idle) }
                    }
                    is AiStreamEvent.Error -> {
                        _uiState.update { it.copy(aiUiState = AiChatUiState.Error(event.message)) }
                    }
                }
            }
        }
    }

    fun explainDailyHadith() {
        val hadith = _uiState.value.appState.hadith
        goTo(AppScreen.AI_ASSISTANT)
        val prompt = "Please explain Today's Hadith simply with key practical lessons for students:\n" +
            "Arabic: ${hadith.ar}\n" +
            "Translation: ${hadith.tr}\n" +
            "Source: ${hadith.src}"
        sendAiPrompt(prompt, topic = "Hadith")
    }

    fun clearAiChat() {
        val ids = _uiState.value.aiMessages.map { it.docId }
        if (ids.isEmpty()) return
        launchWrite(successToast = "AI chat history cleared") {
            firestoreRepo.clearAiChatHistory(ids)
        }
    }

    fun dismissAiError() {
        _uiState.update { it.copy(aiUiState = AiChatUiState.Idle) }
    }

    // ---------- Student / Shared Actions ----------
    fun updateCourseSearch(query: String) {
        _uiState.update { it.copy(courseSearch = query) }
    }

    fun setClassFilter(filter: String) {
        _uiState.update { it.copy(classFilter = filter) }
    }

    fun setUpdateFilter(category: String) {
        _uiState.update { it.copy(updateFilter = category) }
    }

    fun dismissBanner() {
        _uiState.update { it.copy(bannerDismissed = true) }
    }

    fun updateProfile(name: String, phone: String, email: String) {
        launchWrite(successToast = "Profile synced to cloud") {
            firestoreRepo.updateProfile(name, phone, email)
        }
    }

    fun enrollInCourse(course: Course) {
        launchWrite(
            successToast = "Enrolled / Joined ${course.title}",
            closeModalOnSuccess = true
        ) {
            firestoreRepo.enrollInCourse(course)
        }
    }

    fun submitAssignment(assignmentId: Long) {
        launchWrite(successToast = "Assignment submitted") {
            firestoreRepo.submitAssignment(assignmentId)
        }
    }

    fun canManageContent(item: StudyMaterial): Boolean {
        val user = _uiState.value.currentUser
        if (user.role == UserRole.ADMIN) return true
        return item.uploader?.email == user.email
    }

    fun uploadStudyContent(title: String, course: String, type: String, fileUri: Uri?) {
        val cleanTitle = title.trim()
        if (cleanTitle.isEmpty()) {
            showToast("Enter a title")
            return
        }
        viewModelScope.launch {
            val copied = fileUri?.let { firestoreRepo.copyUriToInternalStorage(it, "content") }
            val user = _uiState.value.currentUser
            val item = StudyMaterial(
                id = System.currentTimeMillis(),
                title = cleanTitle,
                course = course,
                type = type,
                uploader = UploaderInfo(user.name, user.role.key, user.email),
                filePath = copied?.first,
                fileName = copied?.second
            )
            firestoreRepo.addStudyMaterial(item)
                .onSuccess {
                    closeSheet()
                    showToast("Uploaded & synced to cloud")
                }
                .onFailure { err ->
                    showToast(err.localizedMessage ?: "Upload failed")
                }
        }
    }

    fun deleteContent(id: Long) {
        val item = _uiState.value.appState.materials.find { it.id == id }
        if (item != null && !canManageContent(item)) {
            showToast("Only the uploader or Admin can remove this")
            return
        }
        launchWrite(successToast = "Removed") {
            firestoreRepo.deleteStudyMaterial(id)
        }
    }

    // ---------- Islamic Updates ----------
    fun addIslamicUpdate(title: String, category: String, imageUri: Uri?) {
        val cleanTitle = title.trim()
        if (cleanTitle.isEmpty()) {
            showToast("Enter a title")
            return
        }
        viewModelScope.launch {
            val copied = imageUri?.let { firestoreRepo.copyUriToInternalStorage(it, "update") }
            val item = IslamicUpdate(
                id = System.currentTimeMillis(),
                title = cleanTitle,
                category = category,
                thumbPath = copied?.first
            )
            firestoreRepo.addIslamicUpdate(item)
                .onSuccess {
                    _uiState.update { it.copy(updateFilter = "all") }
                    closeSheet()
                    showToast("Uploaded & synced to cloud")
                }
                .onFailure { err ->
                    showToast(err.localizedMessage ?: "Upload failed")
                }
        }
    }

    fun deleteIslamicUpdate(id: Long) {
        launchWrite(successToast = "Removed") {
            firestoreRepo.deleteIslamicUpdate(id)
        }
    }

    // ---------- Admin Actions ----------
    fun setAdminTab(tab: AdminTab) {
        if (tab == AdminTab.UPDATES) {
            goTo(AppScreen.ISLAMIC_UPDATES)
            return
        }
        _uiState.update { it.copy(adminTab = tab) }
    }

    fun approveStudent(id: Long) {
        val target = _uiState.value.appState.students.find { it.id == id } ?: return
        launchWrite(successToast = "${target.name} approved") {
            firestoreRepo.approveStudent(id)
        }
    }

    fun updateStudent(student: StudentRecord) {
        launchWrite(successToast = "Student updated", closeModalOnSuccess = true) {
            firestoreRepo.updateStudent(student)
        }
    }

    fun removeStudent(id: Long) {
        launchWrite(successToast = "Student removed") {
            firestoreRepo.removeStudent(id)
        }
    }

    fun addTeacher(name: String, subject: String) {
        if (name.isBlank()) {
            showToast("Enter teacher name")
            return
        }
        val record = TeacherRecord(
            id = System.currentTimeMillis(),
            name = name.trim(),
            subject = subject.trim().ifEmpty { "General" },
            status = "active"
        )
        launchWrite(successToast = "Teacher account created", closeModalOnSuccess = true) {
            firestoreRepo.addTeacher(record)
        }
    }

    fun updateTeacher(teacher: TeacherRecord) {
        launchWrite(successToast = "Teacher updated", closeModalOnSuccess = true) {
            firestoreRepo.updateTeacher(teacher)
        }
    }

    fun removeTeacher(id: Long) {
        launchWrite(successToast = "Teacher removed") {
            firestoreRepo.removeTeacher(id)
        }
    }

    fun addCourse(title: String, teacher: String, desc: String, duration: String, timing: String) {
        if (title.isBlank()) {
            showToast("Enter course title")
            return
        }
        val course = Course(
            id = System.currentTimeMillis(),
            title = title.trim(),
            teacher = teacher.trim().ifEmpty { "Qari Bilal Ahmed" },
            desc = desc.trim().ifEmpty { "Structured Islamic learning program." },
            duration = duration.trim().ifEmpty { "3 months" },
            timing = timing.trim().ifEmpty { "Mon–Fri, 5:00 PM" },
            students = 0,
            progress = 0,
            icon = "book"
        )
        launchWrite(successToast = "Course added", closeModalOnSuccess = true) {
            firestoreRepo.addCourse(course)
        }
    }

    fun updateCourse(course: Course) {
        launchWrite(successToast = "Course updated", closeModalOnSuccess = true) {
            firestoreRepo.updateCourse(course)
        }
    }

    fun deleteCourse(id: Long) {
        launchWrite(successToast = "Course removed") {
            firestoreRepo.deleteCourse(id)
        }
    }

    fun addBookFromAdmin(title: String, course: String, type: String) {
        val cleanTitle = title.trim()
        if (cleanTitle.isEmpty()) {
            showToast("Enter a book title")
            return
        }
        val user = _uiState.value.currentUser
        val item = StudyMaterial(
            id = System.currentTimeMillis(),
            title = cleanTitle,
            course = course,
            type = type,
            uploader = UploaderInfo(user.name, "admin", user.email)
        )
        launchWrite(
            successToast = "Book added & synced to cloud",
            closeModalOnSuccess = true
        ) {
            firestoreRepo.addStudyMaterial(item)
        }
    }

    fun updateBook(material: StudyMaterial) {
        launchWrite(successToast = "Book updated", closeModalOnSuccess = true) {
            firestoreRepo.updateStudyMaterial(material)
        }
    }

    fun saveHadith(ar: String, tr: String, src: String) {
        launchWrite(successToast = "Daily Hadith synced to cloud") {
            firestoreRepo.saveHadith(ar, tr, src)
        }
    }

    fun saveCustomLogo(uri: Uri?) {
        if (uri == null) {
            showToast("Choose an image first")
            return
        }
        viewModelScope.launch {
            val copied = firestoreRepo.copyUriToInternalStorage(uri, "logo")
            if (copied == null) {
                showToast("Could not read that image")
                return@launch
            }
            firestoreRepo.saveCustomLogoPath(copied.first)
                .onSuccess {
                    closeSheet()
                    showToast("Logo updated across the app")
                }
                .onFailure { err ->
                    showToast(err.localizedMessage ?: "Could not save logo")
                }
        }
    }

    fun saveBanner(uri: Uri?, caption: String) {
        if (uri == null) {
            showToast("Choose a poster image first")
            return
        }
        viewModelScope.launch {
            val copied = firestoreRepo.copyUriToInternalStorage(uri, "banner")
            if (copied == null) {
                showToast("Could not read that image")
                return@launch
            }
            firestoreRepo.saveBanner(copied.first, caption)
                .onSuccess {
                    _uiState.update { it.copy(bannerDismissed = false) }
                    closeSheet()
                    showToast("Poster published to Home")
                }
                .onFailure { err ->
                    showToast(err.localizedMessage ?: "Could not save poster")
                }
        }
    }

    fun removeBanner() {
        launchWrite(successToast = "Poster removed from Home", closeModalOnSuccess = true) {
            firestoreRepo.saveBanner(null, "")
        }
    }

    fun publishAnnouncement(title: String, body: String) {
        launchWrite(successToast = "Announcement published", closeModalOnSuccess = true) {
            firestoreRepo.publishAnnouncement(title, body)
        }
    }

    // ---------- Teacher Actions ----------
    fun setTeacherTab(tab: TeacherTab) {
        _uiState.update { it.copy(teacherTab = tab) }
    }

    fun createAssignment(title: String, course: String, description: String, due: String) {
        val cleanTitle = title.trim()
        if (cleanTitle.isEmpty()) {
            showToast("Enter assignment title")
            return
        }
        val item = Assignment(
            id = System.currentTimeMillis(),
            title = cleanTitle,
            course = course,
            due = due.trim().ifEmpty { "Next Week" },
            status = "pending",
            description = description.trim()
        )
        launchWrite(
            successToast = "Assignment created & synced",
            closeModalOnSuccess = true
        ) {
            firestoreRepo.createAssignment(item)
        }
    }

    fun sendAssignmentFeedback(assignmentId: Long, feedback: String) {
        launchWrite(successToast = "Feedback sent to student", closeModalOnSuccess = true) {
            firestoreRepo.sendAssignmentFeedback(assignmentId, feedback)
        }
    }

    fun uploadVideo(title: String, desc: String, url: String) {
        val cleanTitle = title.trim()
        if (cleanTitle.isEmpty()) {
            showToast("Enter video title")
            return
        }
        val video = VideoLesson(
            id = System.currentTimeMillis(),
            title = cleanTitle,
            teacher = _uiState.value.currentUser.name,
            desc = desc.trim(),
            duration = "0:00",
            url = url.trim()
        )
        launchWrite(successToast = "Video uploaded & synced", closeModalOnSuccess = true) {
            firestoreRepo.addVideo(video)
        }
    }

    fun updateVideo(video: VideoLesson) {
        launchWrite(successToast = "Video updated", closeModalOnSuccess = true) {
            firestoreRepo.updateVideo(video)
        }
    }
}
