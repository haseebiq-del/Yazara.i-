package com.example.ui.dashboard

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ai.SchoolAiService
import com.example.data.model.AttendanceRecord
import com.example.data.model.Homework
import com.example.data.model.MarkRecord
import com.example.data.model.SchoolMessage
import com.example.data.model.UserProfile
import com.example.data.model.UserRole
import com.example.data.repository.SchoolRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class SchoolUiState(
    val homeworkList: List<Homework> = emptyList(),
    val attendanceList: List<AttendanceRecord> = emptyList(),
    val marksList: List<MarkRecord> = emptyList(),
    val messagesList: List<SchoolMessage> = emptyList(),
    val isLoading: Boolean = false,
    val feedbackMessage: String? = null,
    val errorMessage: String? = null,
    // AI Media Lab states
    val isGeneratingImage: Boolean = false,
    val generatedImageBitmap: Bitmap? = null,
    val selectedImageSize: String = "1K", // 1K, 2K, 4K
    val isGeneratingVideo: Boolean = false,
    val videoStatusMessage: String? = null,
    val selectedVideoAspectRatio: String = "16:9" // 16:9 or 9:16
)

class SchoolViewModel(
    private val repository: SchoolRepository,
    private val aiService: SchoolAiService = SchoolAiService()
) : ViewModel() {

    private val _uiState = MutableStateFlow(SchoolUiState())
    val uiState: StateFlow<SchoolUiState> = _uiState.asStateFlow()

    fun initializeListeners(userProfile: UserProfile) {
        val filterStudentId = when (userProfile.role) {
            UserRole.STUDENT.displayName -> userProfile.studentId.ifEmpty { null }
            UserRole.PARENT.displayName -> userProfile.studentId.ifEmpty { null }
            else -> null // Teachers see all records
        }

        viewModelScope.launch {
            repository.getHomeworkStream()
                .catch { Log.e("SchoolViewModel", "Homework listener error", it) }
                .collect { list ->
                    _uiState.value = _uiState.value.copy(homeworkList = list)
                }
        }

        viewModelScope.launch {
            repository.getAttendanceStream(filterStudentId)
                .catch { Log.e("SchoolViewModel", "Attendance listener error", it) }
                .collect { list ->
                    _uiState.value = _uiState.value.copy(attendanceList = list)
                }
        }

        viewModelScope.launch {
            repository.getMarksStream(filterStudentId)
                .catch { Log.e("SchoolViewModel", "Marks listener error", it) }
                .collect { list ->
                    _uiState.value = _uiState.value.copy(marksList = list)
                }
        }

        viewModelScope.launch {
            repository.getMessagesStream()
                .catch { Log.e("SchoolViewModel", "Messages listener error", it) }
                .collect { list ->
                    _uiState.value = _uiState.value.copy(messagesList = list)
                }
        }
    }

    fun clearFeedback() {
        _uiState.value = _uiState.value.copy(feedbackMessage = null, errorMessage = null)
    }

    fun addHomework(
        title: String,
        subject: String,
        description: String,
        dueDate: String,
        className: String,
        teacherProfile: UserProfile
    ) {
        viewModelScope.launch {
            val item = Homework(
                title = title.trim(),
                subject = subject.trim(),
                description = description.trim(),
                dueDate = dueDate.trim(),
                className = className.trim().ifEmpty { "Class 10A" },
                teacherId = teacherProfile.id,
                teacherName = teacherProfile.name
            )
            val result = repository.addHomework(item)
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(feedbackMessage = "Homework '$title' posted successfully!")
            } else {
                _uiState.value = _uiState.value.copy(errorMessage = "Failed to post homework: ${result.exceptionOrNull()?.message}")
            }
        }
    }

    fun recordAttendance(
        studentId: String,
        studentName: String,
        status: String,
        notes: String,
        teacherProfile: UserProfile
    ) {
        viewModelScope.launch {
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val record = AttendanceRecord(
                studentId = studentId.trim(),
                studentName = studentName.trim(),
                date = today,
                status = status,
                recordedBy = teacherProfile.id,
                notes = notes.trim()
            )
            val result = repository.addAttendanceRecord(record)
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(feedbackMessage = "Attendance recorded for $studentName: $status")
            } else {
                _uiState.value = _uiState.value.copy(errorMessage = "Failed to record attendance: ${result.exceptionOrNull()?.message}")
            }
        }
    }

    fun recordMark(
        studentId: String,
        studentName: String,
        subject: String,
        examTitle: String,
        score: Double,
        totalMarks: Double,
        feedback: String,
        teacherProfile: UserProfile
    ) {
        viewModelScope.launch {
            val percentage = if (totalMarks > 0) (score / totalMarks) * 100 else 0.0
            val grade = when {
                percentage >= 90 -> "A+"
                percentage >= 80 -> "A"
                percentage >= 70 -> "B"
                percentage >= 60 -> "C"
                percentage >= 50 -> "D"
                else -> "F"
            }

            val record = MarkRecord(
                studentId = studentId.trim(),
                studentName = studentName.trim(),
                subject = subject.trim(),
                examTitle = examTitle.trim(),
                score = score,
                totalMarks = totalMarks,
                grade = grade,
                feedback = feedback.trim(),
                recordedBy = teacherProfile.id
            )
            val result = repository.addMarkRecord(record)
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(feedbackMessage = "Marks posted for $studentName ($grade: $score/$totalMarks)")
            } else {
                _uiState.value = _uiState.value.copy(errorMessage = "Failed to post marks: ${result.exceptionOrNull()?.message}")
            }
        }
    }

    fun postSchoolMessage(
        title: String,
        content: String,
        targetRole: String,
        senderProfile: UserProfile
    ) {
        viewModelScope.launch {
            val msg = SchoolMessage(
                senderId = senderProfile.id,
                senderName = senderProfile.name,
                senderRole = senderProfile.role,
                targetRole = targetRole,
                title = title.trim(),
                content = content.trim()
            )
            val result = repository.addMessage(msg)
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(feedbackMessage = "School announcement published!")
            } else {
                _uiState.value = _uiState.value.copy(errorMessage = "Failed to post message: ${result.exceptionOrNull()?.message}")
            }
        }
    }

    // AI Media Lab methods
    fun setImageSize(size: String) {
        _uiState.value = _uiState.value.copy(selectedImageSize = size)
    }

    fun setVideoAspectRatio(ratio: String) {
        _uiState.value = _uiState.value.copy(selectedVideoAspectRatio = ratio)
    }

    fun generateSchoolImage(prompt: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isGeneratingImage = true, errorMessage = null)
            val result = aiService.generateSchoolImage(
                prompt = prompt,
                imageSize = _uiState.value.selectedImageSize
            )
            result.onSuccess { bmp ->
                _uiState.value = _uiState.value.copy(
                    isGeneratingImage = false,
                    generatedImageBitmap = bmp,
                    feedbackMessage = "High-quality ${_uiState.value.selectedImageSize} image generated using gemini-3-pro-image-preview!"
                )
            }.onFailure { err ->
                _uiState.value = _uiState.value.copy(
                    isGeneratingImage = false,
                    errorMessage = "Image generation error: ${err.message}"
                )
            }
        }
    }

    fun animateToVideo(prompt: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isGeneratingVideo = true, errorMessage = null)
            val result = aiService.animateImageToVideo(
                prompt = prompt,
                aspectRatio = _uiState.value.selectedVideoAspectRatio,
                inputBitmap = _uiState.value.generatedImageBitmap
            )
            result.onSuccess { msg ->
                _uiState.value = _uiState.value.copy(
                    isGeneratingVideo = false,
                    videoStatusMessage = msg,
                    feedbackMessage = "Video generated using veo-3.1-fast-generate-preview with ${_uiState.value.selectedVideoAspectRatio} aspect ratio!"
                )
            }.onFailure { err ->
                _uiState.value = _uiState.value.copy(
                    isGeneratingVideo = false,
                    errorMessage = "Video generation error: ${err.message}"
                )
            }
        }
    }
}
