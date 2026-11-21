package com.example

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.firebase.AlFalahFirestoreRepository
import com.example.firebase.GeminiIslamicAssistantRepository
import com.example.firebase.GoogleAuthHelper
import com.example.firebase.GoogleSignInResult
import com.example.firebase.authStateFlow
import com.example.firebase.configureAppCheck
import com.example.model.*
import com.example.ui.components.AppModalSheetHost
import com.example.ui.components.FloatingToastOverlay
import com.example.ui.components.StudentBottomNavigationBar
import com.example.ui.screens.*
import com.example.ui.theme.AlFalahTheme
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.AlFalahViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // STEP 1: Configure Firebase App Check with launch intent BEFORE UI, ViewModels, or Firebase calls
        configureAppCheck(this, intent)

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.Transparent.toArgb()),
            navigationBarStyle = SystemBarStyle.auto(
                Color.Transparent.toArgb(),
                Color.Transparent.toArgb()
            )
        )

        val firestoreRepo = AlFalahFirestoreRepository(applicationContext)
        val aiRepo = GeminiIslamicAssistantRepository()

        setContent {
            MyApplicationTheme {
                AlFalahAuthGate(
                    firestoreRepo = firestoreRepo,
                    aiRepo = aiRepo
                )
            }
        }
    }
}

@Composable
private fun AlFalahAuthGate(
    firestoreRepo: AlFalahFirestoreRepository,
    aiRepo: GeminiIslamicAssistantRepository
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val coroutineScope = rememberCoroutineScope()
    val colors = AlFalahTheme.colors

    val firebaseUser by remember { Firebase.auth.authStateFlow() }
        .collectAsStateWithLifecycle(initialValue = Firebase.auth.currentUser)

    var splashDone by remember { mutableStateOf(false) }
    var selectedRole by remember { mutableStateOf(UserRole.STUDENT) }
    var isSigningIn by remember { mutableStateOf(false) }
    var authError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        val autoSignInJob = launch {
            if (activity != null) {
                GoogleAuthHelper.attemptAutoSignIn(activity)
            }
        }
        delay(2200L)
        autoSignInJob.join()
        splashDone = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bg),
        contentAlignment = Alignment.TopCenter
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 600.dp)
        ) {
            if (!splashDone) {
                SplashScreen(customLogoPath = null)
            } else {
                val activeUser = firebaseUser
                if (activeUser == null) {
                    AuthScreen(
                        customLogoPath = null,
                        selectedRole = selectedRole,
                        isLoading = isSigningIn,
                        errorMessage = authError,
                        onRoleChange = { selectedRole = it },
                        onGoogleSignInClick = {
                            if (activity == null || isSigningIn) return@AuthScreen
                            authError = null
                            isSigningIn = true
                            coroutineScope.launch {
                                when (val result = GoogleAuthHelper.onGoogleSignInClicked(activity)) {
                                    is GoogleSignInResult.Success -> {
                                        isSigningIn = false
                                        authError = null
                                    }
                                    is GoogleSignInResult.Cancelled -> {
                                        isSigningIn = false
                                        authError = result.message
                                    }
                                    is GoogleSignInResult.Error -> {
                                        isSigningIn = false
                                        authError = result.message
                                    }
                                }
                            }
                        }
                    )
                } else {
                    // Key authenticated subtree by UID so account switches cleanly dispose old listeners
                    key(activeUser.uid) {
                        AuthenticatedAlFalahApp(
                            firebaseUser = activeUser,
                            initialRole = selectedRole,
                            firestoreRepo = firestoreRepo,
                            aiRepo = aiRepo,
                            onSignOut = {
                                coroutineScope.launch {
                                    GoogleAuthHelper.signOut(context)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AuthenticatedAlFalahApp(
    firebaseUser: FirebaseUser,
    initialRole: UserRole,
    firestoreRepo: AlFalahFirestoreRepository,
    aiRepo: GeminiIslamicAssistantRepository,
    onSignOut: () -> Unit
) {
    val viewModel: AlFalahViewModel = viewModel(
        key = firebaseUser.uid,
        factory = AlFalahViewModel.factory(
            uid = firebaseUser.uid,
            initialDisplayName = firebaseUser.displayName ?: "Student",
            initialEmail = firebaseUser.email ?: "",
            initialRole = initialRole,
            firestoreRepo = firestoreRepo,
            aiRepo = aiRepo
        )
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val canInterceptBack = uiState.activeSheet != null || uiState.currentScreen in setOf(
        AppScreen.ABOUT,
        AppScreen.PROFILE,
        AppScreen.ISLAMIC_UPDATES,
        AppScreen.AI_ASSISTANT,
        AppScreen.COURSES,
        AppScreen.CLASSES,
        AppScreen.VIDEOS,
        AppScreen.MATERIAL,
        AppScreen.ASSIGNMENTS
    )
    BackHandler(enabled = canInterceptBack) {
        viewModel.handleBack()
    }

    val showStudentBottomNav = uiState.currentUser.role == UserRole.STUDENT &&
        uiState.currentScreen in setOf(
            AppScreen.HOME,
            AppScreen.COURSES,
            AppScreen.CLASSES,
            AppScreen.VIDEOS,
            AppScreen.MATERIAL,
            AppScreen.ASSIGNMENTS,
            AppScreen.ISLAMIC_UPDATES,
            AppScreen.PROFILE,
            AppScreen.ABOUT
        )

    Box(modifier = Modifier.fillMaxSize()) {
        when (uiState.currentScreen) {
            AppScreen.SPLASH, AppScreen.AUTH, AppScreen.HOME -> StudentHomeScreen(
                uiState = uiState,
                onNavigate = { viewModel.goTo(it) },
                onOpenNotifications = { viewModel.openSheet(ActiveSheet.Notifications) },
                onOpenProgress = { viewModel.openSheet(ActiveSheet.Progress) },
                onCourseClick = { viewModel.openSheet(ActiveSheet.CourseDetail(it)) },
                onJoinClass = { cls -> viewModel.showToast("Opening meeting link → ${cls.link}") },
                onDismissBanner = { viewModel.dismissBanner() },
                onExplainHadith = { viewModel.explainDailyHadith() }
            )

            AppScreen.AI_ASSISTANT -> AiAssistantScreen(
                messages = uiState.aiMessages,
                aiUiState = uiState.aiUiState,
                onBack = { viewModel.goHome() },
                onSendPrompt = { prompt, topic -> viewModel.sendAiPrompt(prompt, topic) },
                onExplainHadith = { viewModel.explainDailyHadith() },
                onClearChat = { viewModel.clearAiChat() },
                onDismissError = { viewModel.dismissAiError() }
            )

            AppScreen.COURSES -> CoursesScreen(
                courses = uiState.appState.courses,
                searchQuery = uiState.courseSearch,
                onSearchChange = { viewModel.updateCourseSearch(it) },
                onCourseClick = { viewModel.openSheet(ActiveSheet.CourseDetail(it)) }
            )

            AppScreen.CLASSES -> ClassesScreen(
                classes = uiState.appState.classes,
                selectedFilter = uiState.classFilter,
                onFilterChange = { viewModel.setClassFilter(it) },
                onJoinClass = { cls -> viewModel.showToast("Opening meeting link → ${cls.link}") }
            )

            AppScreen.VIDEOS -> VideosScreen(
                videos = uiState.appState.videos,
                onPlayVideo = { v -> viewModel.showToast("Playing: ${v.title}") }
            )

            AppScreen.MATERIAL -> MaterialScreen(
                materials = uiState.appState.materials,
                canManage = { viewModel.canManageContent(it) },
                onUploadClick = { viewModel.openSheet(ActiveSheet.UploadContent) },
                onOpenFile = { m -> viewModel.showToast("Saved: ${m.fileName ?: m.title}") },
                onDelete = { viewModel.deleteContent(it) }
            )

            AppScreen.ASSIGNMENTS -> AssignmentsScreen(
                assignments = uiState.appState.assignments,
                onSubmitAssignment = { viewModel.submitAssignment(it) }
            )

            AppScreen.ISLAMIC_UPDATES -> IslamicUpdatesScreen(
                updates = uiState.appState.islamicUpdates,
                selectedFilter = uiState.updateFilter,
                isAdmin = uiState.currentUser.role == UserRole.ADMIN,
                onBack = { viewModel.goHome() },
                onFilterChange = { viewModel.setUpdateFilter(it) },
                onAddClick = { viewModel.openSheet(ActiveSheet.AddIslamicUpdate) },
                onDownloadClick = { u -> viewModel.showToast("Downloading ${u.title}") },
                onDeleteClick = { viewModel.deleteIslamicUpdate(it) }
            )

            AppScreen.ABOUT -> AboutScreen(
                customLogoPath = uiState.appState.customLogoPath,
                onBack = { viewModel.goTo(AppScreen.PROFILE) }
            )

            AppScreen.PROFILE -> ProfileScreen(
                user = uiState.currentUser,
                customLogoPath = uiState.appState.customLogoPath,
                onBack = { viewModel.goHome() },
                onSaveProfile = { n, p, e -> viewModel.updateProfile(n, p, e) },
                onOpenAbout = { viewModel.goTo(AppScreen.ABOUT) },
                onSwitchRole = { viewModel.switchWorkspaceRole(it) },
                onLogout = onSignOut,
                onToast = { viewModel.showToast(it) }
            )

            AppScreen.ADMIN_HOME -> AdminDashboardScreen(
                uiState = uiState,
                onSelectTab = { viewModel.setAdminTab(it) },
                onNavigate = { viewModel.goTo(it) },
                onDismissBanner = { viewModel.dismissBanner() },
                onApproveStudent = { viewModel.approveStudent(it) },
                onEditStudent = { viewModel.openSheet(ActiveSheet.EditStudent(it)) },
                onRemoveStudent = { viewModel.removeStudent(it) },
                onAddTeacher = { viewModel.openSheet(ActiveSheet.AddTeacher) },
                onEditTeacher = { viewModel.openSheet(ActiveSheet.EditTeacher(it)) },
                onRemoveTeacher = { viewModel.removeTeacher(it) },
                onAddCourse = { viewModel.openSheet(ActiveSheet.AddCourse) },
                onEditCourse = { viewModel.openSheet(ActiveSheet.EditCourse(it)) },
                onDeleteCourse = { viewModel.deleteCourse(it) },
                onAddBook = { viewModel.openSheet(ActiveSheet.AdminAddBook) },
                onEditBook = { viewModel.openSheet(ActiveSheet.EditBook(it)) },
                onDeleteContent = { viewModel.deleteContent(it) },
                onSaveHadith = { ar, tr, src -> viewModel.saveHadith(ar, tr, src) },
                onOpenSheet = { viewModel.openSheet(it) },
                onToast = { viewModel.showToast(it) }
            )

            AppScreen.TEACHER_HOME -> TeacherDashboardScreen(
                uiState = uiState,
                onSelectTab = { viewModel.setTeacherTab(it) },
                onNavigate = { viewModel.goTo(it) },
                onDismissBanner = { viewModel.dismissBanner() },
                onJoinClass = { cls -> viewModel.showToast("Opening meeting link → ${cls.link}") },
                onOpenSheet = { viewModel.openSheet(it) },
                canManageMaterial = { viewModel.canManageContent(it) },
                onOpenMaterialFile = { m -> viewModel.showToast("Saved: ${m.fileName ?: m.title}") },
                onDeleteMaterial = { viewModel.deleteContent(it) },
                onPlayVideo = { v -> viewModel.showToast("Playing: ${v.title}") },
                onToast = { viewModel.showToast(it) }
            )
        }

        if (showStudentBottomNav) {
            Box(modifier = Modifier.align(Alignment.BottomCenter)) {
                StudentBottomNavigationBar(
                    currentScreen = uiState.currentScreen,
                    onNavigate = { viewModel.goTo(it) }
                )
            }
        }

        FloatingToastOverlay(
            message = uiState.toastMessage ?: uiState.syncError,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = if (showStudentBottomNav) 86.dp else 28.dp)
        )
    }

    AppModalSheetHost(
        activeSheet = uiState.activeSheet,
        appState = uiState.appState,
        viewModel = viewModel
    )
}
