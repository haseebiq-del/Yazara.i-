package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.R
import com.example.data.model.AttendanceRecord
import com.example.data.model.Homework
import com.example.data.model.MarkRecord
import com.example.data.model.SchoolMessage
import com.example.data.model.UserProfile
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class SchoolRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    // Secondary constructor for dependency injection using context
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private fun requireUserId(): String {
        return auth.currentUser?.uid ?: error("User is not authenticated")
    }

    suspend fun getUserProfile(userId: String): UserProfile? {
        return try {
            val doc = db.collection("users").document(userId).get().await()
            if (doc.exists()) {
                UserProfile(
                    id = doc.getString("id") ?: userId,
                    name = doc.getString("name") ?: "",
                    email = doc.getString("email") ?: "",
                    role = doc.getString("role") ?: "student",
                    isVerified = doc.getBoolean("isVerified") ?: false,
                    verificationCode = doc.getString("verificationCode") ?: "",
                    photoUrl = doc.getString("photoUrl") ?: "",
                    studentId = doc.getString("studentId") ?: "",
                    schoolCode = doc.getString("schoolCode") ?: "SCH-2026",
                    createdAt = doc.getTimestamp("createdAt"),
                    updatedAt = doc.getTimestamp("updatedAt")
                )
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("SchoolRepository", "Error fetching user profile", e)
            null
        }
    }

    suspend fun saveUserProfile(profile: UserProfile): Result<Unit> {
        return runCatching {
            val uid = requireUserId()
            val data = hashMapOf<String, Any>(
                "id" to uid,
                "name" to profile.name.trim().ifEmpty { "School Member" },
                "email" to profile.email.trim(),
                "role" to profile.role,
                "isVerified" to profile.isVerified,
                "verificationCode" to profile.verificationCode,
                "photoUrl" to profile.photoUrl,
                "studentId" to profile.studentId,
                "schoolCode" to profile.schoolCode,
                "createdAt" to (profile.createdAt ?: Timestamp.now()),
                "updatedAt" to Timestamp.now()
            )
            db.collection("users").document(uid).set(data).await()
        }
    }

    suspend fun markEmailVerified(userId: String): Result<Unit> {
        return runCatching {
            db.collection("users").document(userId).update(
                mapOf(
                    "isVerified" to true,
                    "updatedAt" to Timestamp.now()
                )
            ).await()
        }
    }

    // Homework Stream
    fun getHomeworkStream(): Flow<List<Homework>> = callbackFlow {
        val listener = db.collection("homework")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("SchoolRepository", "Error listening to homework", error)
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val items = snapshot?.documents?.mapNotNull { doc ->
                    Homework(
                        id = doc.getString("id") ?: doc.id,
                        title = doc.getString("title") ?: "",
                        subject = doc.getString("subject") ?: "",
                        description = doc.getString("description") ?: "",
                        dueDate = doc.getString("dueDate") ?: "",
                        className = doc.getString("className") ?: "Class 10A",
                        teacherId = doc.getString("teacherId") ?: "",
                        teacherName = doc.getString("teacherName") ?: "",
                        createdAt = doc.getTimestamp("createdAt")
                    )
                } ?: emptyList()
                trySend(items)
            }
        awaitClose { listener.remove() }
    }

    suspend fun addHomework(homework: Homework): Result<Unit> {
        return runCatching {
            val docRef = if (homework.id.isNotEmpty()) {
                db.collection("homework").document(homework.id)
            } else {
                db.collection("homework").document()
            }
            val data = hashMapOf<String, Any>(
                "id" to docRef.id,
                "title" to homework.title,
                "subject" to homework.subject,
                "description" to homework.description,
                "dueDate" to homework.dueDate,
                "className" to homework.className,
                "teacherId" to (homework.teacherId.ifEmpty { requireUserId() }),
                "teacherName" to homework.teacherName,
                "createdAt" to (homework.createdAt ?: Timestamp.now())
            )
            docRef.set(data).await()
        }
    }

    // Attendance Stream
    fun getAttendanceStream(filterStudentId: String? = null): Flow<List<AttendanceRecord>> = callbackFlow {
        var query: Query = db.collection("attendance")
            .orderBy("createdAt", Query.Direction.DESCENDING)
        if (!filterStudentId.isNullOrEmpty()) {
            query = query.whereEqualTo("studentId", filterStudentId)
        }

        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e("SchoolRepository", "Error listening to attendance", error)
                trySend(emptyList())
                return@addSnapshotListener
            }
            val items = snapshot?.documents?.mapNotNull { doc ->
                AttendanceRecord(
                    id = doc.getString("id") ?: doc.id,
                    studentId = doc.getString("studentId") ?: "",
                    studentName = doc.getString("studentName") ?: "",
                    date = doc.getString("date") ?: "",
                    status = doc.getString("status") ?: "Present",
                    recordedBy = doc.getString("recordedBy") ?: "",
                    notes = doc.getString("notes") ?: "",
                    createdAt = doc.getTimestamp("createdAt")
                )
            } ?: emptyList()
            trySend(items)
        }
        awaitClose { listener.remove() }
    }

    suspend fun addAttendanceRecord(record: AttendanceRecord): Result<Unit> {
        return runCatching {
            val docRef = if (record.id.isNotEmpty()) {
                db.collection("attendance").document(record.id)
            } else {
                db.collection("attendance").document()
            }
            val data = hashMapOf<String, Any>(
                "id" to docRef.id,
                "studentId" to record.studentId,
                "studentName" to record.studentName,
                "date" to record.date,
                "status" to record.status,
                "recordedBy" to (record.recordedBy.ifEmpty { requireUserId() }),
                "notes" to record.notes,
                "createdAt" to (record.createdAt ?: Timestamp.now())
            )
            docRef.set(data).await()
        }
    }

    // Marks Stream
    fun getMarksStream(filterStudentId: String? = null): Flow<List<MarkRecord>> = callbackFlow {
        var query: Query = db.collection("marks")
            .orderBy("createdAt", Query.Direction.DESCENDING)
        if (!filterStudentId.isNullOrEmpty()) {
            query = query.whereEqualTo("studentId", filterStudentId)
        }

        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e("SchoolRepository", "Error listening to marks", error)
                trySend(emptyList())
                return@addSnapshotListener
            }
            val items = snapshot?.documents?.mapNotNull { doc ->
                MarkRecord(
                    id = doc.getString("id") ?: doc.id,
                    studentId = doc.getString("studentId") ?: "",
                    studentName = doc.getString("studentName") ?: "",
                    subject = doc.getString("subject") ?: "",
                    examTitle = doc.getString("examTitle") ?: "",
                    score = doc.getDouble("score") ?: 0.0,
                    totalMarks = doc.getDouble("totalMarks") ?: 100.0,
                    grade = doc.getString("grade") ?: "A",
                    feedback = doc.getString("feedback") ?: "",
                    recordedBy = doc.getString("recordedBy") ?: "",
                    createdAt = doc.getTimestamp("createdAt")
                )
            } ?: emptyList()
            trySend(items)
        }
        awaitClose { listener.remove() }
    }

    suspend fun addMarkRecord(record: MarkRecord): Result<Unit> {
        return runCatching {
            val docRef = if (record.id.isNotEmpty()) {
                db.collection("marks").document(record.id)
            } else {
                db.collection("marks").document()
            }
            val data = hashMapOf<String, Any>(
                "id" to docRef.id,
                "studentId" to record.studentId,
                "studentName" to record.studentName,
                "subject" to record.subject,
                "examTitle" to record.examTitle,
                "score" to record.score,
                "totalMarks" to record.totalMarks,
                "grade" to record.grade,
                "feedback" to record.feedback,
                "recordedBy" to (record.recordedBy.ifEmpty { requireUserId() }),
                "createdAt" to (record.createdAt ?: Timestamp.now())
            )
            docRef.set(data).await()
        }
    }

    // Messages Stream
    fun getMessagesStream(): Flow<List<SchoolMessage>> = callbackFlow {
        val listener = db.collection("messages")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("SchoolRepository", "Error listening to messages", error)
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val items = snapshot?.documents?.mapNotNull { doc ->
                    SchoolMessage(
                        id = doc.getString("id") ?: doc.id,
                        senderId = doc.getString("senderId") ?: "",
                        senderName = doc.getString("senderName") ?: "",
                        senderRole = doc.getString("senderRole") ?: "teacher",
                        targetRole = doc.getString("targetRole") ?: "all",
                        title = doc.getString("title") ?: "",
                        content = doc.getString("content") ?: "",
                        createdAt = doc.getTimestamp("createdAt")
                    )
                } ?: emptyList()
                trySend(items)
            }
        awaitClose { listener.remove() }
    }

    suspend fun addMessage(message: SchoolMessage): Result<Unit> {
        return runCatching {
            val docRef = if (message.id.isNotEmpty()) {
                db.collection("messages").document(message.id)
            } else {
                db.collection("messages").document()
            }
            val data = hashMapOf<String, Any>(
                "id" to docRef.id,
                "senderId" to (message.senderId.ifEmpty { requireUserId() }),
                "senderName" to message.senderName,
                "senderRole" to message.senderRole,
                "targetRole" to message.targetRole,
                "title" to message.title,
                "content" to message.content,
                "createdAt" to (message.createdAt ?: Timestamp.now())
            )
            docRef.set(data).await()
        }
    }
}
