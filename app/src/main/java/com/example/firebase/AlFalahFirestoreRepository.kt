package com.example.firebase

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.annotation.Keep
import com.example.R
import com.example.model.*
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

@Keep
@IgnoreExtraProperties
data class UserProfileDoc(
    val userId: String = "",
    val displayName: String = "",
    val email: String = "",
    val phone: String = "",
    val roleView: String = "student",
    val hadithAr: String = DefaultAlFalahData.hadith.ar,
    val hadithTr: String = DefaultAlFalahData.hadith.tr,
    val hadithSrc: String = DefaultAlFalahData.hadith.src,
    val bannerCaption: String = "",
    val bannerImagePath: String? = null,
    val customLogoPath: String? = null,
    val seeded: Boolean = false
)

data class AiChatMessage(
    val docId: String,
    val sender: String, // "user" or "assistant"
    val text: String,
    val topic: String = "General",
    val timestampMillis: Long = 0L
)

data class SyncedWorkspaceSnapshot(
    val profile: UserProfileDoc?,
    val appState: InstituteAppState,
    val aiMessages: List<AiChatMessage>
)

class AlFalahFirestoreRepository(private val appContext: Context) {

    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(
        appContext.getString(R.string.firestore_database_id)
    )

    private fun requireUserId(): String =
        Firebase.auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google")

    private fun userDoc(uid: String) = db.collection("users").document(uid)

    private inline fun <T> runSafeWrite(
        op: OperationType,
        path: String,
        block: () -> T
    ): Result<T> {
        return try {
            Result.success(block())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            try {
                handleFirestoreError(e, op, path)
            } catch (wrapped: RuntimeException) {
                Result.failure(wrapped)
            }
        }
    }

    private suspend fun runSafeSuspendWrite(
        op: OperationType,
        path: String,
        block: suspend () -> Unit
    ): Result<Unit> {
        return try {
            block()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            try {
                handleFirestoreError(e, op, path)
            } catch (wrapped: RuntimeException) {
                Result.failure(wrapped)
            }
        }
    }

    suspend fun ensureWorkspaceSeeded(
        uid: String,
        displayName: String,
        email: String,
        initialRole: UserRole
    ): Result<Unit> = runSafeSuspendWrite(OperationType.WRITE, "users/$uid") {
        val docRef = userDoc(uid)
        val snap = docRef.get().await()
        val alreadySeeded = snap.exists() && (snap.getBoolean("seeded") == true)
        if (alreadySeeded) return@runSafeSuspendWrite

        val safeName = displayName.trim().ifEmpty { "Student" }.take(120)
        val safeEmail = email.trim().take(160)
        val batch = db.batch()

        val profileData = mapOf(
            "userId" to uid,
            "displayName" to safeName,
            "email" to safeEmail,
            "phone" to "+92 300 1234567",
            "roleView" to initialRole.key,
            "hadithAr" to DefaultAlFalahData.hadith.ar,
            "hadithTr" to DefaultAlFalahData.hadith.tr,
            "hadithSrc" to DefaultAlFalahData.hadith.src,
            "bannerCaption" to "",
            "seeded" to true,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        batch.set(docRef, profileData)

        DefaultAlFalahData.courses.forEach { c ->
            val ref = docRef.collection("courses").document(c.id.toString())
            batch.set(
                ref,
                mapOf(
                    "userId" to uid,
                    "title" to c.title,
                    "teacher" to c.teacher,
                    "desc" to c.desc,
                    "duration" to c.duration,
                    "timing" to c.timing,
                    "students" to c.students,
                    "progress" to c.progress,
                    "icon" to c.icon,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
        }

        DefaultAlFalahData.classes.forEach { cls ->
            val ref = docRef.collection("classes").document(cls.id.toString())
            batch.set(
                ref,
                mapOf(
                    "userId" to uid,
                    "course" to cls.course,
                    "teacher" to cls.teacher,
                    "date" to cls.date,
                    "time" to cls.time,
                    "status" to cls.status,
                    "link" to cls.link,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
        }

        DefaultAlFalahData.videos.forEach { v ->
            val ref = docRef.collection("videos").document(v.id.toString())
            batch.set(
                ref,
                mapOf(
                    "userId" to uid,
                    "title" to v.title,
                    "teacher" to v.teacher,
                    "desc" to v.desc,
                    "duration" to v.duration,
                    "url" to v.url,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
        }

        DefaultAlFalahData.materials.forEach { m ->
            val ref = docRef.collection("materials").document(m.id.toString())
            batch.set(
                ref,
                mapOf(
                    "userId" to uid,
                    "title" to m.title,
                    "course" to m.course,
                    "type" to m.type,
                    "uploaderName" to (m.uploader?.name ?: "Admin"),
                    "uploaderRole" to (m.uploader?.role ?: "admin"),
                    "uploaderEmail" to (m.uploader?.email ?: safeEmail),
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
        }

        DefaultAlFalahData.assignments.forEach { a ->
            val ref = docRef.collection("assignments").document(a.id.toString())
            val data = mutableMapOf<String, Any>(
                "userId" to uid,
                "title" to a.title,
                "course" to a.course,
                "due" to a.due,
                "status" to a.status,
                "description" to a.description,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            a.feedback?.let { data["feedback"] = it }
            batch.set(ref, data)
        }

        DefaultAlFalahData.students.forEach { s ->
            val ref = docRef.collection("students").document(s.id.toString())
            batch.set(
                ref,
                mapOf(
                    "userId" to uid,
                    "name" to s.name,
                    "email" to s.email,
                    "city" to s.city,
                    "status" to s.status,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
        }

        DefaultAlFalahData.teachers.forEach { t ->
            val ref = docRef.collection("teachers").document(t.id.toString())
            batch.set(
                ref,
                mapOf(
                    "userId" to uid,
                    "name" to t.name,
                    "subject" to t.subject,
                    "status" to t.status,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
        }

        DefaultAlFalahData.islamicUpdates.forEach { u ->
            val ref = docRef.collection("islamic_updates").document(u.id.toString())
            batch.set(
                ref,
                mapOf(
                    "userId" to uid,
                    "title" to u.title,
                    "category" to u.category,
                    "link" to u.link,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
        }

        DefaultAlFalahData.notifications.forEach { n ->
            val ref = docRef.collection("notifications").document(n.id.toString())
            batch.set(
                ref,
                mapOf(
                    "userId" to uid,
                    "title" to n.title,
                    "body" to n.body,
                    "createdAt" to FieldValue.serverTimestamp()
                )
            )
        }

        batch.commit().await()
    }

    fun observeWorkspace(uid: String): Flow<SyncedWorkspaceSnapshot> {
        val root = userDoc(uid)
        val est = DocumentSnapshot.ServerTimestampBehavior.ESTIMATE

        val profileFlow: Flow<UserProfileDoc?> = root.snapshots().map { snap ->
            if (!snap.exists()) null
            else UserProfileDoc(
                userId = snap.getString("userId") ?: uid,
                displayName = snap.getString("displayName") ?: "Student",
                email = snap.getString("email") ?: "",
                phone = snap.getString("phone") ?: "",
                roleView = snap.getString("roleView") ?: "student",
                hadithAr = snap.getString("hadithAr") ?: DefaultAlFalahData.hadith.ar,
                hadithTr = snap.getString("hadithTr") ?: DefaultAlFalahData.hadith.tr,
                hadithSrc = snap.getString("hadithSrc") ?: DefaultAlFalahData.hadith.src,
                bannerCaption = snap.getString("bannerCaption") ?: "",
                bannerImagePath = snap.getString("bannerImagePath"),
                customLogoPath = snap.getString("customLogoPath"),
                seeded = snap.getBoolean("seeded") ?: false
            )
        }

        val coursesFlow = root.collection("courses")
            .orderBy("createdAt", Query.Direction.ASCENDING)
            .snapshots()
            .map { qs ->
                qs.documents.map { doc ->
                    val data = doc.getData(est) ?: emptyMap()
                    Course(
                        id = doc.id.toLongOrNull() ?: doc.id.hashCode().toLong(),
                        title = data["title"] as? String ?: "",
                        teacher = data["teacher"] as? String ?: "",
                        desc = data["desc"] as? String ?: "",
                        duration = data["duration"] as? String ?: "",
                        timing = data["timing"] as? String ?: "",
                        students = (data["students"] as? Number)?.toInt() ?: 0,
                        progress = (data["progress"] as? Number)?.toInt() ?: 0,
                        icon = data["icon"] as? String ?: "book"
                    )
                }
            }

        val classesFlow = root.collection("classes")
            .orderBy("createdAt", Query.Direction.ASCENDING)
            .snapshots()
            .map { qs ->
                qs.documents.map { doc ->
                    val data = doc.getData(est) ?: emptyMap()
                    OnlineClass(
                        id = doc.id.toLongOrNull() ?: doc.id.hashCode().toLong(),
                        course = data["course"] as? String ?: "",
                        teacher = data["teacher"] as? String ?: "",
                        date = data["date"] as? String ?: "",
                        time = data["time"] as? String ?: "",
                        status = data["status"] as? String ?: "upcoming",
                        link = data["link"] as? String ?: ""
                    )
                }
            }

        val videosFlow = root.collection("videos")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .snapshots()
            .map { qs ->
                qs.documents.map { doc ->
                    val data = doc.getData(est) ?: emptyMap()
                    VideoLesson(
                        id = doc.id.toLongOrNull() ?: doc.id.hashCode().toLong(),
                        title = data["title"] as? String ?: "",
                        teacher = data["teacher"] as? String ?: "",
                        desc = data["desc"] as? String ?: "",
                        duration = data["duration"] as? String ?: "0:00",
                        url = data["url"] as? String ?: ""
                    )
                }
            }

        val materialsFlow = root.collection("materials")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .snapshots()
            .map { qs ->
                qs.documents.map { doc ->
                    val data = doc.getData(est) ?: emptyMap()
                    StudyMaterial(
                        id = doc.id.toLongOrNull() ?: doc.id.hashCode().toLong(),
                        title = data["title"] as? String ?: "",
                        course = data["course"] as? String ?: "",
                        type = data["type"] as? String ?: "Notes",
                        uploader = UploaderInfo(
                            name = data["uploaderName"] as? String ?: "Admin",
                            role = data["uploaderRole"] as? String ?: "admin",
                            email = data["uploaderEmail"] as? String ?: ""
                        ),
                        filePath = data["filePath"] as? String,
                        fileName = data["fileName"] as? String
                    )
                }
            }

        val assignmentsFlow = root.collection("assignments")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .snapshots()
            .map { qs ->
                qs.documents.map { doc ->
                    val data = doc.getData(est) ?: emptyMap()
                    Assignment(
                        id = doc.id.toLongOrNull() ?: doc.id.hashCode().toLong(),
                        title = data["title"] as? String ?: "",
                        course = data["course"] as? String ?: "",
                        due = data["due"] as? String ?: "",
                        status = data["status"] as? String ?: "pending",
                        feedback = data["feedback"] as? String,
                        description = data["description"] as? String ?: ""
                    )
                }
            }

        val studentsFlow = root.collection("students")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .snapshots()
            .map { qs ->
                qs.documents.map { doc ->
                    val data = doc.getData(est) ?: emptyMap()
                    StudentRecord(
                        id = doc.id.toLongOrNull() ?: doc.id.hashCode().toLong(),
                        name = data["name"] as? String ?: "",
                        email = data["email"] as? String ?: "",
                        city = data["city"] as? String ?: "",
                        status = data["status"] as? String ?: "pending"
                    )
                }
            }

        val teachersFlow = root.collection("teachers")
            .orderBy("createdAt", Query.Direction.ASCENDING)
            .snapshots()
            .map { qs ->
                qs.documents.map { doc ->
                    val data = doc.getData(est) ?: emptyMap()
                    TeacherRecord(
                        id = doc.id.toLongOrNull() ?: doc.id.hashCode().toLong(),
                        name = data["name"] as? String ?: "",
                        subject = data["subject"] as? String ?: "",
                        status = data["status"] as? String ?: "active"
                    )
                }
            }

        val updatesFlow = root.collection("islamic_updates")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .snapshots()
            .map { qs ->
                qs.documents.map { doc ->
                    val data = doc.getData(est) ?: emptyMap()
                    IslamicUpdate(
                        id = doc.id.toLongOrNull() ?: doc.id.hashCode().toLong(),
                        title = data["title"] as? String ?: "",
                        category = data["category"] as? String ?: "Wallpaper",
                        thumbPath = data["thumbPath"] as? String,
                        link = data["link"] as? String ?: ""
                    )
                }
            }

        val notificationsFlow = root.collection("notifications")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .snapshots()
            .map { qs ->
                qs.documents.map { doc ->
                    val data = doc.getData(est) ?: emptyMap()
                    NotificationItem(
                        id = doc.id.toLongOrNull() ?: doc.id.hashCode().toLong(),
                        title = data["title"] as? String ?: "",
                        body = data["body"] as? String ?: ""
                    )
                }
            }

        val aiChatsFlow = root.collection("ai_chats")
            .orderBy("createdAt", Query.Direction.ASCENDING)
            .snapshots()
            .map { qs ->
                qs.documents.map { doc ->
                    val data = doc.getData(est) ?: emptyMap()
                    val ts = data["createdAt"] as? Timestamp
                    AiChatMessage(
                        docId = doc.id,
                        sender = data["sender"] as? String ?: "user",
                        text = data["text"] as? String ?: "",
                        topic = data["topic"] as? String ?: "General",
                        timestampMillis = ts?.toDate()?.time ?: 0L
                    )
                }
            }

        val part1 = combine(profileFlow, coursesFlow, classesFlow, videosFlow, materialsFlow) { p, c, cls, v, m ->
            Quintuple(p, c, cls, v, m)
        }
        val part2 = combine(assignmentsFlow, studentsFlow, teachersFlow, updatesFlow, notificationsFlow) { a, s, t, u, n ->
            Quintuple(a, s, t, u, n)
        }

        return combine(part1, part2, aiChatsFlow) { p1, p2, aiChats ->
            val profile = p1.first
            val state = InstituteAppState(
                customLogoPath = profile?.customLogoPath,
                hadith = Hadith(
                    ar = profile?.hadithAr ?: DefaultAlFalahData.hadith.ar,
                    tr = profile?.hadithTr ?: DefaultAlFalahData.hadith.tr,
                    src = profile?.hadithSrc ?: DefaultAlFalahData.hadith.src
                ),
                courses = p1.second,
                classes = p1.third,
                videos = p1.fourth,
                materials = p1.fifth,
                assignments = p2.first,
                students = p2.second,
                teachers = p2.third,
                islamicUpdates = p2.fourth,
                banner = HomeBanner(
                    imagePath = profile?.bannerImagePath,
                    caption = profile?.bannerCaption ?: ""
                ),
                notifications = p2.fifth
            )
            SyncedWorkspaceSnapshot(
                profile = profile,
                appState = state,
                aiMessages = aiChats
            )
        }
    }

    // ---------- Profile & Settings Mutations ----------

    suspend fun updateProfile(name: String, phone: String, email: String): Result<Unit> {
        val uid = requireUserId()
        val path = "users/$uid"
        return runSafeSuspendWrite(OperationType.UPDATE, path) {
            userDoc(uid).update(
                mapOf(
                    "displayName" to name.trim().ifEmpty { "Student" }.take(120),
                    "phone" to phone.trim().take(40),
                    "email" to email.trim().take(160),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        }
    }

    suspend fun updateRoleView(role: UserRole): Result<Unit> {
        val uid = requireUserId()
        val path = "users/$uid"
        return runSafeSuspendWrite(OperationType.UPDATE, path) {
            userDoc(uid).update(
                mapOf(
                    "roleView" to role.key,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        }
    }

    suspend fun saveHadith(ar: String, tr: String, src: String): Result<Unit> {
        val uid = requireUserId()
        val path = "users/$uid"
        return runSafeSuspendWrite(OperationType.UPDATE, path) {
            userDoc(uid).update(
                mapOf(
                    "hadithAr" to ar.trim().take(1000),
                    "hadithTr" to tr.trim().take(1000),
                    "hadithSrc" to src.trim().take(200),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        }
    }

    suspend fun saveCustomLogoPath(localPath: String): Result<Unit> {
        val uid = requireUserId()
        val path = "users/$uid"
        return runSafeSuspendWrite(OperationType.UPDATE, path) {
            userDoc(uid).update(
                mapOf(
                    "customLogoPath" to localPath.take(500),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        }
    }

    suspend fun saveBanner(localPath: String?, caption: String): Result<Unit> {
        val uid = requireUserId()
        val path = "users/$uid"
        return runSafeSuspendWrite(OperationType.UPDATE, path) {
            val payload = mutableMapOf<String, Any?>(
                "bannerCaption" to caption.trim().take(300),
                "bannerImagePath" to localPath?.take(500),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            userDoc(uid).update(payload).await()
        }
    }

    // ---------- Courses Mutations ----------

    suspend fun enrollInCourse(course: Course): Result<Unit> {
        val uid = requireUserId()
        val path = "users/$uid/courses/${course.id}"
        return runSafeSuspendWrite(OperationType.UPDATE, path) {
            userDoc(uid).collection("courses").document(course.id.toString()).update(
                mapOf(
                    "students" to (course.students + 1).coerceAtMost(100000),
                    "progress" to (if (course.progress == 0) 5 else course.progress).coerceIn(0, 100),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        }
    }

    suspend fun addCourse(course: Course): Result<Unit> {
        val uid = requireUserId()
        val path = "users/$uid/courses/${course.id}"
        return runSafeSuspendWrite(OperationType.CREATE, path) {
            userDoc(uid).collection("courses").document(course.id.toString()).set(
                mapOf(
                    "userId" to uid,
                    "title" to course.title.trim().take(150),
                    "teacher" to course.teacher.trim().take(120),
                    "desc" to course.desc.trim().take(1000),
                    "duration" to course.duration.trim().take(80),
                    "timing" to course.timing.trim().take(120),
                    "students" to course.students.coerceIn(0, 100000),
                    "progress" to course.progress.coerceIn(0, 100),
                    "icon" to course.icon.take(40).ifEmpty { "book" },
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        }
    }

    suspend fun updateCourse(course: Course): Result<Unit> {
        val uid = requireUserId()
        val path = "users/$uid/courses/${course.id}"
        return runSafeSuspendWrite(OperationType.UPDATE, path) {
            userDoc(uid).collection("courses").document(course.id.toString()).update(
                mapOf(
                    "title" to course.title.trim().ifEmpty { "Course" }.take(150),
                    "teacher" to course.teacher.trim().ifEmpty { "Teacher" }.take(120),
                    "desc" to course.desc.trim().take(1000),
                    "duration" to course.duration.trim().take(80),
                    "timing" to course.timing.trim().take(120),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        }
    }

    suspend fun deleteCourse(id: Long): Result<Unit> {
        val uid = requireUserId()
        val path = "users/$uid/courses/$id"
        return runSafeSuspendWrite(OperationType.DELETE, path) {
            userDoc(uid).collection("courses").document(id.toString()).delete().await()
        }
    }

    // ---------- Materials / Books Mutations ----------

    suspend fun addStudyMaterial(item: StudyMaterial): Result<Unit> {
        val uid = requireUserId()
        val path = "users/$uid/materials/${item.id}"
        return runSafeSuspendWrite(OperationType.CREATE, path) {
            val payload = mapOf(
                "userId" to uid,
                "title" to item.title.trim().take(200),
                "course" to item.course.trim().ifEmpty { "General" }.take(150),
                "type" to item.type,
                "uploaderName" to (item.uploader?.name ?: "User").take(120),
                "uploaderRole" to (item.uploader?.role ?: "student").take(40),
                "uploaderEmail" to (item.uploader?.email ?: "").take(160),
                "filePath" to item.filePath?.take(500),
                "fileName" to item.fileName?.take(260),
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            ).filterValues { it != null }
            userDoc(uid).collection("materials").document(item.id.toString()).set(payload).await()
        }
    }

    suspend fun updateStudyMaterial(item: StudyMaterial): Result<Unit> {
        val uid = requireUserId()
        val path = "users/$uid/materials/${item.id}"
        return runSafeSuspendWrite(OperationType.UPDATE, path) {
            userDoc(uid).collection("materials").document(item.id.toString()).update(
                mapOf(
                    "title" to item.title.trim().ifEmpty { "Book" }.take(200),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        }
    }

    suspend fun deleteStudyMaterial(id: Long): Result<Unit> {
        val uid = requireUserId()
        val path = "users/$uid/materials/$id"
        return runSafeSuspendWrite(OperationType.DELETE, path) {
            userDoc(uid).collection("materials").document(id.toString()).delete().await()
        }
    }

    // ---------- Assignments Mutations ----------

    suspend fun createAssignment(item: Assignment): Result<Unit> {
        val uid = requireUserId()
        val path = "users/$uid/assignments/${item.id}"
        return runSafeSuspendWrite(OperationType.CREATE, path) {
            userDoc(uid).collection("assignments").document(item.id.toString()).set(
                mapOf(
                    "userId" to uid,
                    "title" to item.title.trim().take(200),
                    "course" to item.course.trim().ifEmpty { "General" }.take(150),
                    "due" to item.due.trim().ifEmpty { "Next Week" }.take(60),
                    "status" to "pending",
                    "description" to item.description.trim().take(1000),
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        }
    }

    suspend fun submitAssignment(id: Long): Result<Unit> {
        val uid = requireUserId()
        val path = "users/$uid/assignments/$id"
        return runSafeSuspendWrite(OperationType.UPDATE, path) {
            userDoc(uid).collection("assignments").document(id.toString()).update(
                mapOf(
                    "status" to "submitted",
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        }
    }

    suspend fun sendAssignmentFeedback(id: Long, feedback: String): Result<Unit> {
        val uid = requireUserId()
        val path = "users/$uid/assignments/$id"
        return runSafeSuspendWrite(OperationType.UPDATE, path) {
            userDoc(uid).collection("assignments").document(id.toString()).update(
                mapOf(
                    "status" to "reviewed",
                    "feedback" to feedback.trim().ifEmpty { "Reviewed by teacher." }.take(1000),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        }
    }

    // ---------- Videos Mutations ----------

    suspend fun addVideo(video: VideoLesson): Result<Unit> {
        val uid = requireUserId()
        val path = "users/$uid/videos/${video.id}"
        return runSafeSuspendWrite(OperationType.CREATE, path) {
            userDoc(uid).collection("videos").document(video.id.toString()).set(
                mapOf(
                    "userId" to uid,
                    "title" to video.title.trim().take(200),
                    "teacher" to video.teacher.trim().ifEmpty { "Teacher" }.take(120),
                    "desc" to video.desc.trim().take(1000),
                    "duration" to video.duration.take(30),
                    "url" to video.url.trim().take(500),
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        }
    }

    suspend fun updateVideo(video: VideoLesson): Result<Unit> {
        val uid = requireUserId()
        val path = "users/$uid/videos/${video.id}"
        return runSafeSuspendWrite(OperationType.UPDATE, path) {
            userDoc(uid).collection("videos").document(video.id.toString()).update(
                mapOf(
                    "title" to video.title.trim().ifEmpty { "Video" }.take(200),
                    "desc" to video.desc.trim().take(1000),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        }
    }

    // ---------- Students & Teachers Mutations ----------

    suspend fun approveStudent(id: Long): Result<Unit> {
        val uid = requireUserId()
        val path = "users/$uid/students/$id"
        return runSafeSuspendWrite(OperationType.UPDATE, path) {
            userDoc(uid).collection("students").document(id.toString()).update(
                mapOf(
                    "status" to "approved",
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        }
    }

    suspend fun updateStudent(student: StudentRecord): Result<Unit> {
        val uid = requireUserId()
        val path = "users/$uid/students/${student.id}"
        return runSafeSuspendWrite(OperationType.UPDATE, path) {
            userDoc(uid).collection("students").document(student.id.toString()).update(
                mapOf(
                    "name" to student.name.trim().ifEmpty { "Student" }.take(120),
                    "city" to student.city.trim().take(120),
                    "status" to student.status,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        }
    }

    suspend fun removeStudent(id: Long): Result<Unit> {
        val uid = requireUserId()
        val path = "users/$uid/students/$id"
        return runSafeSuspendWrite(OperationType.DELETE, path) {
            userDoc(uid).collection("students").document(id.toString()).delete().await()
        }
    }

    suspend fun addTeacher(teacher: TeacherRecord): Result<Unit> {
        val uid = requireUserId()
        val path = "users/$uid/teachers/${teacher.id}"
        return runSafeSuspendWrite(OperationType.CREATE, path) {
            userDoc(uid).collection("teachers").document(teacher.id.toString()).set(
                mapOf(
                    "userId" to uid,
                    "name" to teacher.name.trim().take(120),
                    "subject" to teacher.subject.trim().ifEmpty { "General" }.take(150),
                    "status" to "active",
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        }
    }

    suspend fun updateTeacher(teacher: TeacherRecord): Result<Unit> {
        val uid = requireUserId()
        val path = "users/$uid/teachers/${teacher.id}"
        return runSafeSuspendWrite(OperationType.UPDATE, path) {
            userDoc(uid).collection("teachers").document(teacher.id.toString()).update(
                mapOf(
                    "name" to teacher.name.trim().ifEmpty { "Teacher" }.take(120),
                    "subject" to teacher.subject.trim().ifEmpty { "General" }.take(150),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        }
    }

    suspend fun removeTeacher(id: Long): Result<Unit> {
        val uid = requireUserId()
        val path = "users/$uid/teachers/$id"
        return runSafeSuspendWrite(OperationType.DELETE, path) {
            userDoc(uid).collection("teachers").document(id.toString()).delete().await()
        }
    }

    // ---------- Islamic Updates & Notifications ----------

    suspend fun addIslamicUpdate(item: IslamicUpdate): Result<Unit> {
        val uid = requireUserId()
        val path = "users/$uid/islamic_updates/${item.id}"
        return runSafeSuspendWrite(OperationType.CREATE, path) {
            val payload = mapOf(
                "userId" to uid,
                "title" to item.title.trim().take(200),
                "category" to item.category,
                "thumbPath" to item.thumbPath?.take(500),
                "link" to item.link.take(500),
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            ).filterValues { it != null }
            userDoc(uid).collection("islamic_updates").document(item.id.toString()).set(payload).await()
        }
    }

    suspend fun deleteIslamicUpdate(id: Long): Result<Unit> {
        val uid = requireUserId()
        val path = "users/$uid/islamic_updates/$id"
        return runSafeSuspendWrite(OperationType.DELETE, path) {
            userDoc(uid).collection("islamic_updates").document(id.toString()).delete().await()
        }
    }

    suspend fun publishAnnouncement(title: String, body: String): Result<Unit> {
        val uid = requireUserId()
        val docId = System.currentTimeMillis().toString()
        val path = "users/$uid/notifications/$docId"
        return runSafeSuspendWrite(OperationType.CREATE, path) {
            userDoc(uid).collection("notifications").document(docId).set(
                mapOf(
                    "userId" to uid,
                    "title" to title.trim().ifEmpty { "Announcement" }.take(150),
                    "body" to body.trim().ifEmpty { "New institute update posted." }.take(1000),
                    "createdAt" to FieldValue.serverTimestamp()
                )
            ).await()
        }
    }

    // ---------- AI Study Assistant Chat Persistence ----------

    suspend fun saveAiChatMessage(sender: String, text: String, topic: String = "Study"): Result<Unit> {
        val uid = requireUserId()
        val docRef = userDoc(uid).collection("ai_chats").document()
        val path = "users/$uid/ai_chats/${docRef.id}"
        return runSafeSuspendWrite(OperationType.CREATE, path) {
            docRef.set(
                mapOf(
                    "userId" to uid,
                    "sender" to sender,
                    "text" to text.trim().take(8000),
                    "topic" to topic.trim().take(120),
                    "createdAt" to FieldValue.serverTimestamp()
                )
            ).await()
        }
    }

    suspend fun clearAiChatHistory(docIds: List<String>): Result<Unit> {
        val uid = requireUserId()
        val path = "users/$uid/ai_chats"
        return runSafeSuspendWrite(OperationType.DELETE, path) {
            if (docIds.isEmpty()) return@runSafeSuspendWrite
            val batch = db.batch()
            docIds.forEach { docId ->
                batch.delete(userDoc(uid).collection("ai_chats").document(docId))
            }
            batch.commit().await()
        }
    }

    // ---------- Local Media Picker Helper ----------

    suspend fun copyUriToInternalStorage(uri: Uri, prefix: String): Pair<String, String>? =
        withContext(Dispatchers.IO) {
            try {
                val displayName = resolveDisplayName(uri) ?: "${prefix}_${System.currentTimeMillis()}"
                val safeName = displayName.replace(Regex("[^a-zA-Z0-9._-]"), "_")
                val dir = File(appContext.filesDir, "alfalah_media").apply { mkdirs() }
                val dest = File(dir, "${System.currentTimeMillis()}_$safeName")
                appContext.contentResolver.openInputStream(uri)?.use { input ->
                    FileOutputStream(dest).use { output -> input.copyTo(output) }
                } ?: return@withContext null
                dest.absolutePath to displayName
            } catch (_: Exception) {
                null
            }
        }

    private fun resolveDisplayName(uri: Uri): String? {
        return try {
            appContext.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex >= 0 && cursor.moveToFirst()) cursor.getString(nameIndex) else null
            }
        } catch (_: Exception) {
            null
        }
    }
}

private data class Quintuple<A, B, C, D, E>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
    val fifth: E
)
