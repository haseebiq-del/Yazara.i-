package com.example.ui.dashboard

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.Grading
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.UserProfile
import com.example.data.model.UserRole
import com.example.ui.auth.AuthViewModel
import com.example.ui.theme.SchoolEmerald
import com.example.ui.theme.SchoolGold
import com.example.ui.theme.SchoolNavy
import com.example.ui.theme.SchoolRose

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainSchoolScreen(
    userProfile: UserProfile,
    authViewModel: AuthViewModel,
    schoolViewModel: SchoolViewModel,
    modifier: Modifier = Modifier
) {
    val schoolUiState by schoolViewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(userProfile) {
        schoolViewModel.initializeListeners(userProfile)
    }

    LaunchedEffect(schoolUiState.feedbackMessage, schoolUiState.errorMessage) {
        schoolUiState.feedbackMessage?.let {
            snackbarHostState.showSnackbar(it)
            schoolViewModel.clearFeedback()
        }
        schoolUiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            schoolViewModel.clearFeedback()
        }
    }

    var selectedTab by remember { mutableIntStateOf(0) }
    val isTeacher = userProfile.role == UserRole.TEACHER.displayName
    val isParent = userProfile.role == UserRole.PARENT.displayName
    val isStudent = userProfile.role == UserRole.STUDENT.displayName

    var showAddHomeworkDialog by remember { mutableStateOf(false) }
    var showRecordAttendanceDialog by remember { mutableStateOf(false) }
    var showRecordMarkDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(SchoolGold),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.School,
                                contentDescription = null,
                                tint = SchoolNavy,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "SchoolConnect",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${userProfile.name.ifEmpty { "Member" }} • ${userProfile.role.uppercase()}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                },
                actions = {
                    // Verified Badge
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (userProfile.isVerified) SchoolEmerald.copy(alpha = 0.2f) else SchoolGold.copy(alpha = 0.2f),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = if (userProfile.isVerified) Icons.Default.CheckCircle else Icons.Default.VpnKey,
                                contentDescription = null,
                                tint = if (userProfile.isVerified) SchoolEmerald else SchoolGold,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (userProfile.isVerified) "Verified" else "Pending",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (userProfile.isVerified) SchoolEmerald else SchoolGold
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Assignment, contentDescription = "Homework") },
                    label = { Text("Homework") },
                    modifier = Modifier.testTag("nav_homework")
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.FactCheck, contentDescription = "Attendance") },
                    label = { Text("Attendance") },
                    modifier = Modifier.testTag("nav_attendance")
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.Grading, contentDescription = "Marks") },
                    label = { Text("Marks") },
                    modifier = Modifier.testTag("nav_marks")
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(Icons.Default.AutoAwesome, contentDescription = "AI Lab") },
                    label = { Text("AI Lab") },
                    modifier = Modifier.testTag("nav_ai_lab")
                )
                NavigationBarItem(
                    selected = selectedTab == 4,
                    onClick = { selectedTab = 4 },
                    icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                    label = { Text("Profile") },
                    modifier = Modifier.testTag("nav_profile")
                )
            }
        },
        floatingActionButton = {
            if (isTeacher) {
                when (selectedTab) {
                    0 -> ExtendedFloatingActionButton(
                        onClick = { showAddHomeworkDialog = true },
                        icon = { Icon(Icons.Default.Add, contentDescription = null) },
                        text = { Text("Assign Homework") },
                        modifier = Modifier.testTag("fab_add_homework")
                    )
                    1 -> ExtendedFloatingActionButton(
                        onClick = { showRecordAttendanceDialog = true },
                        icon = { Icon(Icons.Default.Add, contentDescription = null) },
                        text = { Text("Record Attendance") },
                        modifier = Modifier.testTag("fab_record_attendance")
                    )
                    2 -> ExtendedFloatingActionButton(
                        onClick = { showRecordMarkDialog = true },
                        icon = { Icon(Icons.Default.Add, contentDescription = null) },
                        text = { Text("Update Marks") },
                        modifier = Modifier.testTag("fab_update_marks")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> HomeworkTabContent(
                    userProfile = userProfile,
                    homeworkList = schoolUiState.homeworkList,
                    isTeacher = isTeacher,
                    onOpenAddDialog = { showAddHomeworkDialog = true }
                )
                1 -> AttendanceTabContent(
                    userProfile = userProfile,
                    attendanceList = schoolUiState.attendanceList,
                    isTeacher = isTeacher,
                    isParent = isParent,
                    onOpenRecordDialog = { showRecordAttendanceDialog = true }
                )
                2 -> MarksTabContent(
                    userProfile = userProfile,
                    marksList = schoolUiState.marksList,
                    isTeacher = isTeacher,
                    isParent = isParent,
                    onOpenRecordDialog = { showRecordMarkDialog = true }
                )
                3 -> AiMediaLabTabContent(
                    uiState = schoolUiState,
                    viewModel = schoolViewModel
                )
                4 -> ProfileTabContent(
                    userProfile = userProfile,
                    authViewModel = authViewModel
                )
            }
        }
    }

    if (showAddHomeworkDialog) {
        AddHomeworkDialog(
            onDismiss = { showAddHomeworkDialog = false },
            onConfirm = { title, subject, desc, due, cls ->
                schoolViewModel.addHomework(title, subject, desc, due, cls, userProfile)
                showAddHomeworkDialog = false
            }
        )
    }

    if (showRecordAttendanceDialog) {
        RecordAttendanceDialog(
            onDismiss = { showRecordAttendanceDialog = false },
            onConfirm = { sid, sname, status, notes ->
                schoolViewModel.recordAttendance(sid, sname, status, notes, userProfile)
                showRecordAttendanceDialog = false
            }
        )
    }

    if (showRecordMarkDialog) {
        RecordMarkDialog(
            onDismiss = { showRecordMarkDialog = false },
            onConfirm = { sid, sname, subject, exam, score, total, feedback ->
                schoolViewModel.recordMark(sid, sname, subject, exam, score, total, feedback, userProfile)
                showRecordMarkDialog = false
            }
        )
    }
}

// ----------------- Homework Tab -----------------
@Composable
private fun HomeworkTabContent(
    userProfile: UserProfile,
    homeworkList: List<com.example.data.model.Homework>,
    isTeacher: Boolean,
    onOpenAddDialog: () -> Unit
) {
    var selectedSubjectFilter by remember { mutableStateOf("All") }
    val subjects = listOf("All", "Mathematics", "Science", "English", "History", "Physics")

    val filteredList = if (selectedSubjectFilter == "All") {
        homeworkList
    } else {
        homeworkList.filter { it.subject.equals(selectedSubjectFilter, ignoreCase = true) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Assignment,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = if (isTeacher) "Homework Management" else "Assignments & Homework",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (isTeacher) "Post, edit and schedule student assignments." else "View assigned homework and track upcoming due dates.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        // Subject filters
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            subjects.take(4).forEach { subj ->
                FilterChip(
                    selected = selectedSubjectFilter == subj,
                    onClick = { selectedSubjectFilter = subj },
                    label = { Text(subj, fontSize = 12.sp) }
                )
            }
        }

        if (filteredList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Assignment,
                        contentDescription = null,
                        modifier = Modifier.size(54.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No homework assignments found.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    if (isTeacher) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(onClick = onOpenAddDialog) {
                            Text("Assign First Homework")
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(filteredList) { hw ->
                    HomeworkCard(homework = hw)
                }
            }
        }
    }
}

// ----------------- Attendance Tab -----------------
@Composable
private fun AttendanceTabContent(
    userProfile: UserProfile,
    attendanceList: List<com.example.data.model.AttendanceRecord>,
    isTeacher: Boolean,
    isParent: Boolean,
    onOpenRecordDialog: () -> Unit
) {
    val totalRecords = attendanceList.size
    val presentCount = attendanceList.count { it.status == "Present" }
    val rate = if (totalRecords > 0) (presentCount.toDouble() / totalRecords) * 100 else 100.0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Attendance Overview Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                if (isParent) {
                    Text(
                        text = "Monitoring Child: Student ID [${userProfile.studentId.ifEmpty { "STU-101" }}]",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Attendance Rate",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Text(
                            text = "${String.format("%.1f", rate)}%",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (rate >= 80) SchoolEmerald else SchoolRose
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Present", style = MaterialTheme.typography.labelSmall)
                            Text("$presentCount", fontWeight = FontWeight.Bold, color = SchoolEmerald)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Total", style = MaterialTheme.typography.labelSmall)
                            Text("$totalRecords", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Attendance History",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            if (isTeacher) {
                OutlinedButton(onClick = onOpenRecordDialog) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Record Entry")
                }
            }
        }

        if (attendanceList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No attendance records recorded yet.",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(attendanceList) { record ->
                    AttendanceItemCard(record = record)
                }
            }
        }
    }
}

// ----------------- Marks Tab -----------------
@Composable
private fun MarksTabContent(
    userProfile: UserProfile,
    marksList: List<com.example.data.model.MarkRecord>,
    isTeacher: Boolean,
    isParent: Boolean,
    onOpenRecordDialog: () -> Unit
) {
    val totalScored = marksList.sumOf { it.score }
    val totalPossible = marksList.sumOf { it.totalMarks }
    val averagePct = if (totalPossible > 0) (totalScored / totalPossible) * 100 else 0.0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Marks Performance Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                if (isParent) {
                    Text(
                        text = "Viewing Academic Marks for: ${userProfile.studentId.ifEmpty { "STU-101" }}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Average Academic Score",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Text(
                            text = if (marksList.isNotEmpty()) "${String.format("%.1f", averagePct)}%" else "N/A",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = "${marksList.size} Tests Logged",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Subject Marks & Reports",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            if (isTeacher) {
                OutlinedButton(onClick = onOpenRecordDialog) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Marks")
                }
            }
        }

        if (marksList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No marks published yet for this student.",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(marksList) { record ->
                    MarkItemCard(record = record)
                }
            }
        }
    }
}

// ----------------- AI Media Lab Tab -----------------
@Composable
private fun AiMediaLabTabContent(
    uiState: SchoolUiState,
    viewModel: SchoolViewModel
) {
    var imagePrompt by remember {
        mutableStateOf("Solar system planet orbital simulation educational diagram, vibrant colors")
    }
    var videoPrompt by remember {
        mutableStateOf("Planets revolving smoothly around glowing sun with deep space stars")
    }

    val sizes = listOf("1K", "2K", "4K")
    val aspectRatios = listOf("16:9", "9:16")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "School AI Creative Studio",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Generate high-resolution educational art & animate into videos for school projects.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        // Feature 1: High Quality Image Generation (gemini-3-pro-image-preview)
        Card(
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.School, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Generate High-Quality Image",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = "Powered by gemini-3-pro-image-preview. Select desired resolution size below:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )

                // Affordance to specify image size: 1K, 2K, 4K
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    sizes.forEach { size ->
                        FilterChip(
                            selected = uiState.selectedImageSize == size,
                            onClick = { viewModel.setImageSize(size) },
                            label = { Text("Size: $size", fontWeight = FontWeight.Bold) },
                            modifier = Modifier.testTag("size_chip_$size")
                        )
                    }
                }

                OutlinedTextField(
                    value = imagePrompt,
                    onValueChange = { imagePrompt = it },
                    label = { Text("Image Prompt") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("ai_image_prompt_input"),
                    maxLines = 3
                )

                Button(
                    onClick = { viewModel.generateSchoolImage(imagePrompt) },
                    enabled = !uiState.isGeneratingImage && imagePrompt.isNotBlank(),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("generate_image_button")
                ) {
                    if (uiState.isGeneratingImage) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Generating ${uiState.selectedImageSize} Image...")
                    } else {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Generate ${uiState.selectedImageSize} Image")
                    }
                }

                if (uiState.generatedImageBitmap != null) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Image(
                            bitmap = uiState.generatedImageBitmap.asImageBitmap(),
                            contentDescription = "Generated School Artwork",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .clip(RoundedCornerShape(12.dp))
                        )
                        Text(
                            text = "Generated Art (${uiState.selectedImageSize})",
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }

        // Feature 2: Animate Images into Video (veo-3.1-fast-generate-preview)
        Card(
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Movie, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Animate Image into Video",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = "Powered by veo-3.1-fast-generate-preview. Choose aspect ratio for the video:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )

                // Affordance for aspect ratio: 16:9 (landscape) or 9:16 (portrait)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    aspectRatios.forEach { ratio ->
                        FilterChip(
                            selected = uiState.selectedVideoAspectRatio == ratio,
                            onClick = { viewModel.setVideoAspectRatio(ratio) },
                            label = { Text(if (ratio == "16:9") "Landscape (16:9)" else "Portrait (9:16)", fontWeight = FontWeight.Bold) },
                            modifier = Modifier.testTag("ratio_chip_$ratio")
                        )
                    }
                }

                OutlinedTextField(
                    value = videoPrompt,
                    onValueChange = { videoPrompt = it },
                    label = { Text("Video Motion Instructions") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("ai_video_prompt_input"),
                    maxLines = 2
                )

                Button(
                    onClick = { viewModel.animateToVideo(videoPrompt) },
                    enabled = !uiState.isGeneratingVideo && videoPrompt.isNotBlank(),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("generate_video_button")
                ) {
                    if (uiState.isGeneratingVideo) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Queuing Veo Video Generation...")
                    } else {
                        Icon(Icons.Default.Movie, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Animate Video (${uiState.selectedVideoAspectRatio})")
                    }
                }

                if (uiState.videoStatusMessage != null) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = uiState.videoStatusMessage,
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
        }
    }
}

// ----------------- Profile Tab -----------------
@Composable
private fun ProfileTabContent(
    userProfile: UserProfile,
    authViewModel: AuthViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = when (userProfile.photoUrl) {
                            "avatar_scholar" -> "🎓"
                            "avatar_scientist" -> "🔬"
                            "avatar_artist" -> "🎨"
                            "avatar_book" -> "📚"
                            else -> "👤"
                        },
                        fontSize = 38.sp
                    )
                }

                Text(
                    text = userProfile.name.ifEmpty { "School Member" },
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = userProfile.email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "ROLE: ${userProfile.role.uppercase()}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }

        // Profile Details Card
        Card(
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ProfileDetailRow(label = "Email Verification", value = if (userProfile.isVerified) "Verified (Active ✓)" else "Unverified")
                ProfileDetailRow(label = "School Code", value = userProfile.schoolCode)

                if (userProfile.role == UserRole.TEACHER.displayName) {
                    ProfileDetailRow(label = "Teacher Passport", value = "Authenticated Faculty Key ✓")
                } else if (userProfile.role == UserRole.PARENT.displayName) {
                    ProfileDetailRow(label = "Monitored Student ID", value = userProfile.studentId.ifEmpty { "STU-101" })
                } else {
                    ProfileDetailRow(label = "Student ID", value = userProfile.studentId.ifEmpty { "STU-101" })
                }
            }
        }

        Button(
            onClick = { authViewModel.signOut() },
            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("sign_out_button")
        ) {
            Text("Sign Out of SchoolConnect")
        }
    }
}

@Composable
private fun ProfileDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
    }
}
