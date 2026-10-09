package com.example.data.model

import com.google.firebase.Timestamp

enum class UserRole(val displayName: String, val description: String) {
    STUDENT("student", "Access homework, view attendance, check marks & messages"),
    TEACHER("teacher", "Manage homework, post marks, record attendance & announcements"),
    PARENT("parent", "Track student attendance, view academic marks & reports")
}

data class UserProfile(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val role: String = UserRole.STUDENT.displayName,
    val isVerified: Boolean = false,
    val verificationCode: String = "",
    val photoUrl: String = "",
    val studentId: String = "",
    val schoolCode: String = "SCH-2026",
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
)

data class Homework(
    val id: String = "",
    val title: String = "",
    val subject: String = "",
    val description: String = "",
    val dueDate: String = "",
    val className: String = "Class 10A",
    val teacherId: String = "",
    val teacherName: String = "",
    val createdAt: Timestamp? = null
)

data class AttendanceRecord(
    val id: String = "",
    val studentId: String = "",
    val studentName: String = "",
    val date: String = "",
    val status: String = "Present", // Present, Absent, Late, Excused
    val recordedBy: String = "",
    val notes: String = "",
    val createdAt: Timestamp? = null
)

data class MarkRecord(
    val id: String = "",
    val studentId: String = "",
    val studentName: String = "",
    val subject: String = "",
    val examTitle: String = "",
    val score: Double = 0.0,
    val totalMarks: Double = 100.0,
    val grade: String = "A",
    val feedback: String = "",
    val recordedBy: String = "",
    val createdAt: Timestamp? = null
)

data class SchoolMessage(
    val id: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val senderRole: String = "teacher",
    val targetRole: String = "all", // all, students, parents, teachers
    val title: String = "",
    val content: String = "",
    val createdAt: Timestamp? = null
)
