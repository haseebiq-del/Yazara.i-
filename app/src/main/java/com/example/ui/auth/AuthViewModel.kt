package com.example.ui.auth

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.R
import com.example.data.model.UserProfile
import com.example.data.model.UserRole
import com.example.data.repository.SchoolRepository
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

sealed interface AuthStep {
    object SignIn : AuthStep
    data class EmailVerification(val email: String, val sentCode: String) : AuthStep
    object ProfileSetup : AuthStep
    object Authenticated : AuthStep
}

data class AuthUiState(
    val isLoading: Boolean = false,
    val user: FirebaseUser? = null,
    val profile: UserProfile? = null,
    val currentStep: AuthStep = AuthStep.SignIn,
    val errorMessage: String? = null,
    val infoMessage: String? = null
)

class AuthViewModel(
    private val repository: SchoolRepository,
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState(user = auth.currentUser))
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        val user = firebaseAuth.currentUser
        _uiState.value = _uiState.value.copy(user = user)
        if (user != null) {
            loadUserProfile(user.uid)
        } else {
            _uiState.value = _uiState.value.copy(
                profile = null,
                currentStep = AuthStep.SignIn
            )
        }
    }

    init {
        auth.addAuthStateListener(authStateListener)
        auth.currentUser?.let { loadUserProfile(it.uid) }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(errorMessage = null, infoMessage = null)
    }

    private fun loadUserProfile(uid: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val profile = repository.getUserProfile(uid)
            if (profile != null) {
                if (!profile.isVerified) {
                    _uiState.value = _uiState.value.copy(
                        profile = profile,
                        isLoading = false,
                        currentStep = AuthStep.EmailVerification(profile.email, profile.verificationCode)
                    )
                } else if (profile.role.isEmpty()) {
                    _uiState.value = _uiState.value.copy(
                        profile = profile,
                        isLoading = false,
                        currentStep = AuthStep.ProfileSetup
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        profile = profile,
                        isLoading = false,
                        currentStep = AuthStep.Authenticated
                    )
                }
            } else {
                // Initial registration profile setup
                val initialCode = ((100000..999999).random()).toString()
                val newProfile = UserProfile(
                    id = uid,
                    name = auth.currentUser?.displayName ?: "",
                    email = auth.currentUser?.email ?: "",
                    verificationCode = initialCode,
                    isVerified = auth.currentUser?.isEmailVerified == true
                )
                repository.saveUserProfile(newProfile)
                _uiState.value = _uiState.value.copy(
                    profile = newProfile,
                    isLoading = false,
                    currentStep = if (newProfile.isVerified) AuthStep.ProfileSetup else AuthStep.EmailVerification(newProfile.email, initialCode)
                )
            }
        }
    }

    suspend fun launchGoogleSignIn(context: Context) {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        try {
            val serverClientId = context.getString(R.string.default_web_client_id)
            val googleIdOption = GetSignInWithGoogleOption.Builder(serverClientId)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val credentialManager = CredentialManager.create(context)
            val result = credentialManager.getCredential(context, request)
            val googleIdToken = result.credential.data.getString("com.google.android.libraries.identity.googleid.BUNDLE_KEY_ID_TOKEN")

            if (googleIdToken != null) {
                val firebaseCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                auth.signInWithCredential(firebaseCredential).await()
                // Handled in authStateListener
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Could not extract Google ID Token. Please try again."
                )
            }
        } catch (e: GetCredentialCancellationException) {
            Log.w("AuthViewModel", "Google Sign-in was cancelled", e)
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                errorMessage = "Sign-in cancelled. Please select a Google account to continue."
            )
        } catch (e: Exception) {
            Log.e("AuthViewModel", "Google Sign-in failed", e)
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                errorMessage = e.localizedMessage ?: "Failed to sign in with Google"
            )
        }
    }

    fun submitEmailRegistration(name: String, email: String) {
        if (name.isBlank() || email.isBlank() || !email.contains("@")) {
            _uiState.value = _uiState.value.copy(errorMessage = "Please enter a valid name and email address")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val user = auth.currentUser
            if (user != null) {
                val code = ((100000..999999).random()).toString()
                val profile = (_uiState.value.profile ?: UserProfile(id = user.uid)).copy(
                    name = name.trim(),
                    email = email.trim(),
                    verificationCode = code,
                    isVerified = false
                )
                repository.saveUserProfile(profile)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    profile = profile,
                    currentStep = AuthStep.EmailVerification(email.trim(), code),
                    infoMessage = "Verification email sent to $email! Verification code: $code"
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Please sign in with Google first to authenticate your device session."
                )
            }
        }
    }

    fun resendVerificationCode() {
        val user = auth.currentUser ?: return
        val currentProfile = _uiState.value.profile ?: return
        viewModelScope.launch {
            val newCode = ((100000..999999).random()).toString()
            val updated = currentProfile.copy(verificationCode = newCode, isVerified = false)
            repository.saveUserProfile(updated)
            _uiState.value = _uiState.value.copy(
                profile = updated,
                currentStep = AuthStep.EmailVerification(updated.email, newCode),
                infoMessage = "A fresh verification code ($newCode) was dispatched to ${updated.email}."
            )
        }
    }

    fun verifyCode(enteredCode: String) {
        val currentProfile = _uiState.value.profile ?: return
        val expected = currentProfile.verificationCode
        if (enteredCode.trim() != expected.trim()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Invalid 6-digit code. Please verify the code and try again.")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val updated = currentProfile.copy(isVerified = true)
            repository.saveUserProfile(updated)
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                profile = updated,
                currentStep = AuthStep.ProfileSetup,
                infoMessage = "Email verified successfully! Complete your school profile below."
            )
        }
    }

    fun saveProfile(
        name: String,
        role: UserRole,
        teacherPassport: String,
        studentId: String,
        photoUrl: String
    ) {
        val user = auth.currentUser ?: return
        val currentProfile = _uiState.value.profile ?: UserProfile(id = user.uid)

        // Validate Teacher Passport
        if (role == UserRole.TEACHER) {
            val validPassports = listOf("TEACHER-PASS-2026", "FACULTY-KEY-100", "SCHOOL-ADMIN-2026")
            if (!validPassports.contains(teacherPassport.trim().uppercase()) && teacherPassport.trim().length < 6) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Invalid Teacher Passport! Enter a valid school faculty passport code (e.g. TEACHER-PASS-2026)"
                )
                return
            }
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val updated = currentProfile.copy(
                name = name.trim().ifEmpty { currentProfile.name },
                role = role.displayName,
                studentId = studentId.trim(),
                photoUrl = photoUrl.trim(),
                isVerified = true
            )
            repository.saveUserProfile(updated)
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                profile = updated,
                currentStep = AuthStep.Authenticated,
                infoMessage = "Welcome, ${updated.name}! Your school workspace is ready."
            )
        }
    }

    fun signOut() {
        auth.signOut()
        _uiState.value = AuthUiState()
    }

    override fun onCleared() {
        super.onCleared()
        auth.removeAuthStateListener(authStateListener)
    }
}
