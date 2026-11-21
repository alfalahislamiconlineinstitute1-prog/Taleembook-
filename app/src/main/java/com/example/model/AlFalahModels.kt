package com.example.model

enum class UserRole(val key: String, val label: String, val badgeText: String) {
    STUDENT("student", "Student", "STUDENT"),
    TEACHER("teacher", "Teacher", "TEACHER"),
    ADMIN("admin", "Admin", "ADMINISTRATOR");

    companion object {
        fun fromKey(key: String): UserRole = entries.find { it.key == key } ?: STUDENT
    }
}

enum class AppScreen {
    SPLASH,
    AUTH,
    HOME,
    COURSES,
    CLASSES,
    VIDEOS,
    MATERIAL,
    ASSIGNMENTS,
    ISLAMIC_UPDATES,
    ABOUT,
    PROFILE,
    AI_ASSISTANT,
    ADMIN_HOME,
    TEACHER_HOME
}

enum class AdminTab(val label: String) {
    OVERVIEW("Overview"),
    USERS("Users"),
    TEACHERS("Teachers"),
    COURSES("Courses"),
    BOOKS("Books"),
    UPDATES("Islamic Updates"),
    HADITH("Hadith"),
    CONTENT("Content")
}

enum class TeacherTab(val label: String) {
    CLASSES("My Classes"),
    MATERIAL("Books"),
    ASSIGNMENTS("Assignments"),
    VIDEOS("Videos")
}

data class CurrentUser(
    val name: String = "Ahmed Khan",
    val email: String = "ahmed@example.com",
    val phone: String = "+92 300 1234567",
    val role: UserRole = UserRole.STUDENT
)

data class Hadith(
    val ar: String,
    val tr: String,
    val src: String
)

data class Course(
    val id: Long,
    val title: String,
    val teacher: String,
    val desc: String,
    val duration: String,
    val timing: String,
    val students: Int,
    val progress: Int,
    val icon: String
)

data class OnlineClass(
    val id: Long,
    val course: String,
    val teacher: String,
    val date: String,
    val time: String,
    val status: String, // "live" or "upcoming"
    val link: String
)

data class VideoLesson(
    val id: Long,
    val title: String,
    val teacher: String,
    val desc: String,
    val duration: String,
    val url: String = ""
)

data class UploaderInfo(
    val name: String,
    val role: String,
    val email: String
)

data class StudyMaterial(
    val id: Long,
    val title: String,
    val course: String,
    val type: String, // "Notes", "Photo", "Document", "Audio", "Book", "PDF", "Audiobook"
    val uploader: UploaderInfo? = null,
    val filePath: String? = null,
    val fileName: String? = null
)

data class Assignment(
    val id: Long,
    val title: String,
    val course: String,
    val due: String,
    val status: String, // "pending", "submitted", "reviewed"
    val feedback: String? = null,
    val description: String = ""
)

data class StudentRecord(
    val id: Long,
    val name: String,
    val email: String,
    val city: String,
    val status: String // "approved", "pending", "blocked"
)

data class TeacherRecord(
    val id: Long,
    val name: String,
    val subject: String,
    val status: String // "active"
)

data class IslamicUpdate(
    val id: Long,
    val title: String,
    val category: String, // "Wallpaper", "Status", "Reel", "Poster", "Ringtone"
    val thumbPath: String? = null,
    val link: String = ""
)

data class HomeBanner(
    val imagePath: String? = null,
    val caption: String = ""
)

data class NotificationItem(
    val id: Long,
    val title: String,
    val body: String
)

data class InstituteAppState(
    val customLogoPath: String? = null,
    val hadith: Hadith = DefaultAlFalahData.hadith,
    val courses: List<Course> = DefaultAlFalahData.courses,
    val classes: List<OnlineClass> = DefaultAlFalahData.classes,
    val videos: List<VideoLesson> = DefaultAlFalahData.videos,
    val materials: List<StudyMaterial> = DefaultAlFalahData.materials,
    val assignments: List<Assignment> = DefaultAlFalahData.assignments,
    val students: List<StudentRecord> = DefaultAlFalahData.students,
    val teachers: List<TeacherRecord> = DefaultAlFalahData.teachers,
    val islamicUpdates: List<IslamicUpdate> = DefaultAlFalahData.islamicUpdates,
    val banner: HomeBanner = HomeBanner(),
    val notifications: List<NotificationItem> = DefaultAlFalahData.notifications
)

sealed interface ActiveSheet {
    data class CourseDetail(val course: Course) : ActiveSheet
    data object Notifications : ActiveSheet
    data object Progress : ActiveSheet
    data object UploadContent : ActiveSheet
    data object AddIslamicUpdate : ActiveSheet
    data object AddTeacher : ActiveSheet
    data class EditTeacher(val teacher: TeacherRecord) : ActiveSheet
    data class EditStudent(val student: StudentRecord) : ActiveSheet
    data object AddCourse : ActiveSheet
    data class EditCourse(val course: Course) : ActiveSheet
    data object ChangeLogo : ActiveSheet
    data object ChangeBanner : ActiveSheet
    data object Announcement : ActiveSheet
    data object AdminAddBook : ActiveSheet
    data class EditBook(val material: StudyMaterial) : ActiveSheet
    data object CreateAssignment : ActiveSheet
    data class GiveFeedback(val assignment: Assignment) : ActiveSheet
    data object UploadVideo : ActiveSheet
    data class EditVideo(val video: VideoLesson) : ActiveSheet
}

object DefaultAlFalahData {
    val hadith = Hadith(
        ar = "طَلَبُ الْعِلْمِ فَرِيضَةٌ عَلَى كُلِّ مُسْلِمٍ",
        tr = "Seeking knowledge is an obligation upon every Muslim.",
        src = "Sunan Ibn Majah"
    )

    val courses = listOf(
        Course(1, "Noorani Qaida", "Qari Bilal Ahmed", "Foundational course for correct Arabic letter recognition and pronunciation.", "3 months", "Mon–Fri, 5:00 PM", 42, 65, "book"),
        Course(2, "Quran Reading", "Qari Bilal Ahmed", "Fluent Quran reading with proper makharij.", "6 months", "Mon–Fri, 6:00 PM", 38, 40, "quran"),
        Course(3, "Quran Tajweed", "Hafiza Sumaira", "Detailed rules of Tajweed for precise recitation.", "8 months", "Tue/Thu, 7:00 PM", 29, 20, "quran"),
        Course(4, "Hifz-ul-Quran", "Hafiz Usman Tariq", "Complete Quran memorization program with revision plan.", "Ongoing", "Daily, 4:00 PM", 17, 12, "quran"),
        Course(5, "Duas & Salah", "Ustadha Ayesha", "Essential daily duas and correct method of Salah.", "2 months", "Sat/Sun, 3:00 PM", 51, 80, "pray"),
        Course(6, "Basic Islamic Knowledge", "Ustadh Imran", "Fundamentals of Islamic beliefs and practices.", "3 months", "Mon/Wed, 5:30 PM", 33, 55, "book"),
        Course(7, "Aqaaid", "Mufti Zubair", "Core Islamic creed and beliefs explained simply.", "3 months", "Wed, 6:30 PM", 22, 0, "star"),
        Course(8, "Islamic Adab & Akhlaaq", "Ustadha Ayesha", "Manners and character building rooted in Sunnah.", "2 months", "Fri, 5:00 PM", 40, 30, "star"),
        Course(9, "Stories of the Prophets", "Ustadh Imran", "Life lessons from the Prophets (peace be upon them).", "4 months", "Sat, 4:00 PM", 47, 10, "book"),
        Course(10, "Naat", "Qari Bilal Ahmed", "Learn beautiful Naats with correct tarannum.", "2 months", "Sun, 3:00 PM", 19, 0, "mic")
    )

    val classes = listOf(
        OnlineClass(1, "Hifz-ul-Quran", "Hafiz Usman Tariq", "Today", "4:00 PM", "live", "https://meet.google.com/demo-link"),
        OnlineClass(2, "Quran Tajweed", "Hafiza Sumaira", "Today", "7:00 PM", "upcoming", "https://zoom.us/j/demo"),
        OnlineClass(3, "Noorani Qaida", "Qari Bilal Ahmed", "Tomorrow", "5:00 PM", "upcoming", "https://meet.google.com/demo-link-2"),
        OnlineClass(4, "Duas & Salah", "Ustadha Ayesha", "Sat", "3:00 PM", "upcoming", "https://zoom.us/j/demo2")
    )

    val videos = listOf(
        VideoLesson(1, "Makharij al-Huroof Explained", "Qari Bilal Ahmed", "Correct articulation points of Arabic letters.", "12:40"),
        VideoLesson(2, "Surah Al-Fatiha — Tajweed Breakdown", "Hafiza Sumaira", "Line-by-line Tajweed rules applied.", "18:05"),
        VideoLesson(3, "How to Pray Salah Correctly", "Ustadha Ayesha", "Step by step guide for beginners.", "9:22"),
        VideoLesson(4, "Story of Prophet Yusuf (AS) — Part 1", "Ustadh Imran", "Lessons in patience and trust in Allah.", "22:15")
    )

    val materials = listOf(
        StudyMaterial(1, "Noorani Qaida — Lesson 1 PDF", "Noorani Qaida", "PDF", UploaderInfo("Admin", "admin", "admin")),
        StudyMaterial(2, "Tajweed Rules Summary Notes", "Quran Tajweed", "Notes", UploaderInfo("Admin", "admin", "admin")),
        StudyMaterial(3, "40 Daily Duas Booklet", "Duas & Salah", "PDF", UploaderInfo("Admin", "admin", "admin")),
        StudyMaterial(4, "Names of Allah — Illustrated", "Basic Islamic Knowledge", "Photo", UploaderInfo("Admin", "admin", "admin"))
    )

    val assignments = listOf(
        Assignment(1, "Tajweed Rule Worksheet 3", "Quran Tajweed", "Sep 22", "pending"),
        Assignment(2, "Memorize Surah Al-Mulk (1-10)", "Hifz-ul-Quran", "Sep 24", "submitted"),
        Assignment(3, "Write 10 Daily Duas", "Duas & Salah", "Sep 18", "reviewed", feedback = "Excellent work, well written!")
    )

    val students = listOf(
        StudentRecord(1, "Ahmed Khan", "ahmed@example.com", "Karachi", "approved"),
        StudentRecord(2, "Fatima Noor", "fatima@example.com", "Lahore", "pending"),
        StudentRecord(3, "Bilal Hussain", "bilal@example.com", "Islamabad", "approved"),
        StudentRecord(4, "Zainab Ali", "zainab@example.com", "Multan", "blocked")
    )

    val teachers = listOf(
        TeacherRecord(1, "Qari Bilal Ahmed", "Quran Reading & Naat", "active"),
        TeacherRecord(2, "Hafiza Sumaira", "Tajweed", "active"),
        TeacherRecord(3, "Hafiz Usman Tariq", "Hifz-ul-Quran", "active")
    )

    val islamicUpdates = listOf(
        IslamicUpdate(1, "Ramadan Kareem Wallpaper", "Wallpaper"),
        IslamicUpdate(2, "Jumma Mubarak Status", "Status"),
        IslamicUpdate(3, "Surah Al-Fajr Recitation Reel", "Reel"),
        IslamicUpdate(4, "Al Falah Announcement Poster", "Poster"),
        IslamicUpdate(5, "Adhan Ringtone — Makkah", "Ringtone"),
        IslamicUpdate(6, "99 Names of Allah Wallpaper", "Wallpaper")
    )

    val notifications = listOf(
        NotificationItem(1, "New class scheduled", "Hifz-ul-Quran class moved to 4:00 PM today"),
        NotificationItem(2, "Assignment reviewed", "Your Duas homework has feedback from Ustadha Ayesha"),
        NotificationItem(3, "Announcement", "Institute will be closed on Eid holidays")
    )
}
