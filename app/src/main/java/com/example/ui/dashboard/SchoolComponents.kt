package com.example.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Grade
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AttendanceRecord
import com.example.data.model.Homework
import com.example.data.model.MarkRecord
import com.example.data.model.SchoolMessage
import com.example.ui.theme.SchoolEmerald
import com.example.ui.theme.SchoolGold
import com.example.ui.theme.SchoolNavy
import com.example.ui.theme.SchoolRose

@Composable
fun HomeworkCard(
    homework: Homework,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = homework.subject.ifEmpty { "General" },
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Due: ${homework.dueDate.ifEmpty { "TBA" }}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Text(
                text = homework.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            if (homework.description.isNotEmpty()) {
                Text(
                    text = homework.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Teacher: ${homework.teacherName.ifEmpty { "Faculty" }} (${homework.className})",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
fun AttendanceItemCard(
    record: AttendanceRecord,
    modifier: Modifier = Modifier
) {
    val (statusColor, statusBg) = when (record.status) {
        "Present" -> SchoolEmerald to SchoolEmerald.copy(alpha = 0.15f)
        "Absent" -> SchoolRose to SchoolRose.copy(alpha = 0.15f)
        "Late" -> SchoolGold to SchoolGold.copy(alpha = 0.15f)
        else -> MaterialTheme.colorScheme.primary to MaterialTheme.colorScheme.primaryContainer
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = record.studentName.ifEmpty { record.studentId },
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "[${record.studentId}]",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = record.date,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }

                if (record.notes.isNotEmpty()) {
                    Text(
                        text = "Note: ${record.notes}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = statusBg
            ) {
                Text(
                    text = record.status,
                    color = statusColor,
                    fontWeight = FontWeight.ExtraBold,
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
fun MarkItemCard(
    record: MarkRecord,
    modifier: Modifier = Modifier
) {
    val gradeColor = when (record.grade) {
        "A+", "A" -> SchoolEmerald
        "B" -> MaterialTheme.colorScheme.primary
        "C" -> SchoolGold
        else -> SchoolRose
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(gradeColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = record.grade,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = gradeColor
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = record.subject,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${record.examTitle} • Student: ${record.studentName} (${record.studentId})",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                if (record.feedback.isNotEmpty()) {
                    Text(
                        text = "Feedback: \"${record.feedback}\"",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${record.score.toInt()} / ${record.totalMarks.toInt()}",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
                val pct = if (record.totalMarks > 0) (record.score / record.totalMarks) * 100 else 0.0
                Text(
                    text = "${String.format("%.1f", pct)}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
fun SchoolMessageCard(
    message: SchoolMessage,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = message.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Text(
                        text = "For: ${message.targetRole.uppercase()}",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            Text(
                text = message.content,
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = "Posted by ${message.senderName} (${message.senderRole})",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
    }
}

// Dialog for adding homework
@Composable
fun AddHomeworkDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, subject: String, description: String, dueDate: String, className: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("Mathematics") }
    var description by remember { mutableStateOf("") }
    var dueDate by remember { mutableStateOf("Tomorrow, 5 PM") }
    var className by remember { mutableStateOf("Class 10A") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Assign New Homework", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Assignment Title") },
                    modifier = Modifier.fillMaxWidth().testTag("homework_title_input"),
                    singleLine = true
                )
                OutlinedTextField(
                    value = subject,
                    onValueChange = { subject = it },
                    label = { Text("Subject") },
                    modifier = Modifier.fillMaxWidth().testTag("homework_subject_input"),
                    singleLine = true
                )
                OutlinedTextField(
                    value = dueDate,
                    onValueChange = { dueDate = it },
                    label = { Text("Due Date / Deadline") },
                    modifier = Modifier.fillMaxWidth().testTag("homework_duedate_input"),
                    singleLine = true
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Instructions / Exercises") },
                    modifier = Modifier.fillMaxWidth().testTag("homework_desc_input"),
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onConfirm(title, subject, description, dueDate, className)
                    }
                },
                enabled = title.isNotBlank(),
                modifier = Modifier.testTag("save_homework_button")
            ) {
                Text("Publish Homework")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

// Dialog for recording attendance
@Composable
fun RecordAttendanceDialog(
    onDismiss: () -> Unit,
    onConfirm: (studentId: String, studentName: String, status: String, notes: String) -> Unit
) {
    var studentId by remember { mutableStateOf("STU-101") }
    var studentName by remember { mutableStateOf("John Doe") }
    var status by remember { mutableStateOf("Present") }
    var notes by remember { mutableStateOf("") }

    val statusOptions = listOf("Present", "Absent", "Late", "Excused")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Record Student Attendance", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = studentId,
                    onValueChange = { studentId = it },
                    label = { Text("Student ID") },
                    modifier = Modifier.fillMaxWidth().testTag("attendance_studentid_input"),
                    singleLine = true
                )
                OutlinedTextField(
                    value = studentName,
                    onValueChange = { studentName = it },
                    label = { Text("Student Name") },
                    modifier = Modifier.fillMaxWidth().testTag("attendance_studentname_input"),
                    singleLine = true
                )

                Text("Status:", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    statusOptions.forEach { opt ->
                        FilterChip(
                            selected = status == opt,
                            onClick = { status = opt },
                            label = { Text(opt, fontSize = 11.sp) },
                            modifier = Modifier.testTag("status_chip_$opt")
                        )
                    }
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Teacher Notes (Optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(studentId, studentName, status, notes)
                },
                enabled = studentId.isNotBlank() && studentName.isNotBlank(),
                modifier = Modifier.testTag("save_attendance_button")
            ) {
                Text("Save Attendance")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

// Dialog for recording marks
@Composable
fun RecordMarkDialog(
    onDismiss: () -> Unit,
    onConfirm: (studentId: String, studentName: String, subject: String, examTitle: String, score: Double, totalMarks: Double, feedback: String) -> Unit
) {
    var studentId by remember { mutableStateOf("STU-101") }
    var studentName by remember { mutableStateOf("John Doe") }
    var subject by remember { mutableStateOf("Mathematics") }
    var examTitle by remember { mutableStateOf("Midterm Exam") }
    var scoreStr by remember { mutableStateOf("92") }
    var totalMarksStr by remember { mutableStateOf("100") }
    var feedback by remember { mutableStateOf("Outstanding performance on algebraic problems!") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Update Student Marks", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = studentId,
                    onValueChange = { studentId = it },
                    label = { Text("Student ID") },
                    modifier = Modifier.fillMaxWidth().testTag("mark_studentid_input"),
                    singleLine = true
                )
                OutlinedTextField(
                    value = studentName,
                    onValueChange = { studentName = it },
                    label = { Text("Student Name") },
                    modifier = Modifier.fillMaxWidth().testTag("mark_studentname_input"),
                    singleLine = true
                )
                OutlinedTextField(
                    value = subject,
                    onValueChange = { subject = it },
                    label = { Text("Subject") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = examTitle,
                    onValueChange = { examTitle = it },
                    label = { Text("Exam / Test Title") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = scoreStr,
                        onValueChange = { scoreStr = it },
                        label = { Text("Score") },
                        modifier = Modifier.weight(1f).testTag("mark_score_input"),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = totalMarksStr,
                        onValueChange = { totalMarksStr = it },
                        label = { Text("Total") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
                OutlinedTextField(
                    value = feedback,
                    onValueChange = { feedback = it },
                    label = { Text("Feedback / Commendation") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val s = scoreStr.toDoubleOrNull() ?: 0.0
                    val t = totalMarksStr.toDoubleOrNull() ?: 100.0
                    onConfirm(studentId, studentName, subject, examTitle, s, t, feedback)
                },
                enabled = studentId.isNotBlank() && studentName.isNotBlank(),
                modifier = Modifier.testTag("save_mark_button")
            ) {
                Text("Post Marks")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
