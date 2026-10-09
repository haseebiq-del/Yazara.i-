package com.example

import com.example.data.model.AttendanceRecord
import com.example.data.model.Homework
import com.example.data.model.MarkRecord
import com.example.data.model.UserProfile
import com.example.data.model.UserRole
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SchoolModelsTest {

    @Test
    fun testUserProfileDefaults() {
        val user = UserProfile(
            id = "user123",
            name = "Jane Doe",
            email = "jane@school.edu",
            role = UserRole.STUDENT.displayName,
            isVerified = false,
            verificationCode = "123456"
        )
        assertEquals("user123", user.id)
        assertEquals("student", user.role)
        assertFalse(user.isVerified)
        assertEquals("123456", user.verificationCode)
    }

    @Test
    fun testMarkRecordGradeCalculation() {
        val mark = MarkRecord(
            id = "m1",
            studentId = "STU-101",
            studentName = "John",
            subject = "Math",
            examTitle = "Final",
            score = 95.0,
            totalMarks = 100.0,
            grade = "A+"
        )
        assertEquals(95.0, mark.score, 0.01)
        assertEquals("A+", mark.grade)
    }

    @Test
    fun testAttendanceRecordStatus() {
        val att = AttendanceRecord(
            id = "a1",
            studentId = "STU-101",
            studentName = "John",
            date = "2026-10-09",
            status = "Present"
        )
        assertEquals("Present", att.status)
        assertEquals("2026-10-09", att.date)
    }
}
