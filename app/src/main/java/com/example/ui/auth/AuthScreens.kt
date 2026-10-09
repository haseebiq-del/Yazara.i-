package com.example.ui.auth

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.FamilyRestroom
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.SupervisorAccount
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.UserRole
import com.example.ui.theme.SchoolEmerald
import com.example.ui.theme.SchoolGold
import com.example.ui.theme.SchoolNavy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun SignInScreen(
    uiState: AuthUiState,
    viewModel: AuthViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var inputName by remember { mutableStateOf("") }
    var inputEmail by remember { mutableStateOf("") }
    var isSignUpMode by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 480.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // School Header
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(SchoolNavy),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.School,
                    contentDescription = "School Logo",
                    tint = Color.White,
                    modifier = Modifier.size(48.dp)
                )
            }

            Text(
                text = "SchoolConnect",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            )

            Text(
                text = "Cloud Campus Portal for Students, Teachers & Parents",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )

            if (uiState.errorMessage != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = uiState.errorMessage,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            if (uiState.infoMessage != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = uiState.infoMessage,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            // Google Sign-In Card (Primary Authentication)
            Card(
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Sign in to your School Account",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    Text(
                        text = "Secure Google Sign-In connected to your cloud school database.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )

                    Button(
                        onClick = {
                            CoroutineScope(Dispatchers.Main).launch {
                                viewModel.launchGoogleSignIn(context)
                            }
                        },
                        enabled = !uiState.isLoading,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("google_signin_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Sign in",
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Sign in with Google",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // User Registration / Email signup section
            Card(
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isSignUpMode) "Register New User" else "School Email Registration",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        TextButton(onClick = { isSignUpMode = !isSignUpMode }) {
                            Text(if (isSignUpMode) "Hide Form" else "Register Form")
                        }
                    }

                    AnimatedVisibility(visible = isSignUpMode) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = inputName,
                                onValueChange = { inputName = it },
                                label = { Text("Full Name") },
                                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("registration_name_input"),
                                singleLine = true
                            )

                            OutlinedTextField(
                                value = inputEmail,
                                onValueChange = { inputEmail = it },
                                label = { Text("School Email Address") },
                                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("registration_email_input"),
                                singleLine = true
                            )

                            Button(
                                onClick = {
                                    viewModel.submitEmailRegistration(inputName, inputEmail)
                                },
                                enabled = !uiState.isLoading && inputName.isNotBlank() && inputEmail.isNotBlank(),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("register_and_verify_button")
                            ) {
                                Icon(Icons.Default.MarkEmailRead, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Send Verification Email")
                            }
                        }
                    }
                }
            }

            // Role Information Cards
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "School Access Types",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )

                RoleInfoRow(
                    icon = Icons.Default.School,
                    title = "Students",
                    description = "Join classes, track homework assignments, and receive teacher messages."
                )

                RoleInfoRow(
                    icon = Icons.Default.VpnKey,
                    title = "Teachers (Special Passport)",
                    description = "Requires teacher passport code (e.g. TEACHER-PASS-2026) to record attendance, post homework & update marks."
                )

                RoleInfoRow(
                    icon = Icons.Default.FamilyRestroom,
                    title = "Parents",
                    description = "Monitor student attendance and check real-time marks, GPA & academic feedback."
                )
            }
        }
    }
}

@Composable
private fun RoleInfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(text = title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
fun EmailVerificationScreen(
    email: String,
    sentCode: String,
    uiState: AuthUiState,
    viewModel: AuthViewModel,
    modifier: Modifier = Modifier
) {
    var codeInput by remember { mutableStateOf("") }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            modifier = Modifier
                .widthIn(max = 440.dp)
                .fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(SchoolEmerald.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MarkEmailRead,
                        contentDescription = "Verify Email",
                        tint = SchoolEmerald,
                        modifier = Modifier.size(40.dp)
                    )
                }

                Text(
                    text = "Verify Your Email",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "A 6-digit verification code has been dispatched to:",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = email.ifEmpty { "your registered school email" },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                // Simulated email notification chip with real code
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = SchoolGold.copy(alpha = 0.15f)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "School Mailbox Simulator",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                        Text(
                            text = "Your verification PIN is: $sentCode",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp,
                            letterSpacing = 4.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                if (uiState.errorMessage != null) {
                    Text(
                        text = uiState.errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center
                    )
                }

                OutlinedTextField(
                    value = codeInput,
                    onValueChange = { if (it.length <= 6) codeInput = it },
                    label = { Text("Enter 6-digit Code") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("verification_code_input")
                )

                Button(
                    onClick = { viewModel.verifyCode(codeInput) },
                    enabled = codeInput.length == 6 && !uiState.isLoading,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("verify_code_button")
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                    } else {
                        Text("Confirm & Verify Email", fontWeight = FontWeight.Bold)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = { viewModel.resendVerificationCode() }) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Resend Code")
                    }

                    TextButton(onClick = { viewModel.signOut() }) {
                        Text("Switch Account")
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileSetupScreen(
    uiState: AuthUiState,
    viewModel: AuthViewModel,
    modifier: Modifier = Modifier
) {
    val profile = uiState.profile
    var name by remember { mutableStateOf(profile?.name ?: "") }
    var selectedRole by remember { mutableStateOf(UserRole.STUDENT) }
    var teacherPassport by remember { mutableStateOf("") }
    var studentId by remember { mutableStateOf(profile?.studentId ?: "STU-101") }
    var selectedAvatar by remember {
        mutableStateOf(
            profile?.photoUrl?.ifEmpty { "avatar_scholar" } ?: "avatar_scholar"
        )
    }

    val avatarOptions = listOf(
        "avatar_scholar" to "🎓 Scholar",
        "avatar_scientist" to "🔬 Scientist",
        "avatar_artist" to "🎨 Artist",
        "avatar_book" to "📚 Reader",
        "avatar_star" to "⭐ Star"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 520.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Complete Your School Profile",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Text(
                        text = "Set up your school role and credentials to access features.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )

                    if (uiState.errorMessage != null) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = uiState.errorMessage,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.padding(10.dp),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }

                    // Name Input
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Display Name") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("profile_name_input"),
                        singleLine = true
                    )

                    // Role Picker
                    Text(
                        text = "Select Your Role",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        UserRole.values().forEach { role ->
                            val isSelected = selectedRole == role
                            val roleBorderColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent

                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .border(2.dp, roleBorderColor, RoundedCornerShape(12.dp))
                                    .clickable { selectedRole = role }
                                    .testTag("role_option_${role.displayName}")
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = when (role) {
                                            UserRole.STUDENT -> Icons.Default.School
                                            UserRole.TEACHER -> Icons.Default.VpnKey
                                            UserRole.PARENT -> Icons.Default.FamilyRestroom
                                        },
                                        contentDescription = null,
                                        tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = role.name.lowercase().replaceFirstChar { it.uppercase() },
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }

                    // Special Teacher Passport input (when teacher is selected)
                    AnimatedVisibility(visible = selectedRole == UserRole.TEACHER) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = SchoolGold.copy(alpha = 0.15f)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Key, contentDescription = null, tint = SchoolGold)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Teacher Passport Required",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.titleSmall
                                    )
                                }
                                Text(
                                    text = "To prevent students from creating teacher accounts, enter your faculty passport code (Demo code: TEACHER-PASS-2026).",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                OutlinedTextField(
                                    value = teacherPassport,
                                    onValueChange = { teacherPassport = it },
                                    label = { Text("Teacher Passport / Access Code") },
                                    placeholder = { Text("e.g. TEACHER-PASS-2026") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("teacher_passport_input"),
                                    singleLine = true
                                )
                            }
                        }
                    }

                    // Parent Child Link input (when parent is selected)
                    AnimatedVisibility(visible = selectedRole == UserRole.PARENT) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "Link to Student / Child",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Enter your child's Student ID to monitor their real-time attendance and exam marks.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                            OutlinedTextField(
                                value = studentId,
                                onValueChange = { studentId = it },
                                label = { Text("Child's Student ID") },
                                placeholder = { Text("e.g. STU-101") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("child_student_id_input"),
                                singleLine = true
                            )
                        }
                    }

                    // Student ID input (when student is selected)
                    AnimatedVisibility(visible = selectedRole == UserRole.STUDENT) {
                        OutlinedTextField(
                            value = studentId,
                            onValueChange = { studentId = it },
                            label = { Text("Student ID / Roll No.") },
                            placeholder = { Text("e.g. STU-101") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("student_id_input"),
                            singleLine = true
                        )
                    }

                    // Avatar Picker
                    Text(
                        text = "Choose Profile Avatar",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        avatarOptions.forEach { (avatarKey, label) ->
                            val isChosen = selectedAvatar == avatarKey
                            Surface(
                                shape = CircleShape,
                                color = if (isChosen) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                border = if (isChosen) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
                                modifier = Modifier
                                    .size(54.dp)
                                    .clickable { selectedAvatar = avatarKey }
                                    .testTag("avatar_$avatarKey")
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(text = label.split(" ").first(), fontSize = 24.sp)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            viewModel.saveProfile(
                                name = name,
                                role = selectedRole,
                                teacherPassport = teacherPassport,
                                studentId = studentId,
                                photoUrl = selectedAvatar
                            )
                        },
                        enabled = name.isNotBlank() && !uiState.isLoading,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("save_profile_button")
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp))
                        } else {
                            Icon(Icons.Default.CheckCircle, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Complete Profile & Enter School", fontWeight = FontWeight.Bold)
                        }
                    }

                    TextButton(
                        onClick = { viewModel.signOut() },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text("Sign Out")
                    }
                }
            }
        }
    }
}
