package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.UserProfile
import com.example.data.repository.SchoolRepository
import com.example.ui.auth.AuthStep
import com.example.ui.auth.AuthViewModel
import com.example.ui.auth.EmailVerificationScreen
import com.example.ui.auth.ProfileSetupScreen
import com.example.ui.auth.SignInScreen
import com.example.ui.dashboard.MainSchoolScreen
import com.example.ui.dashboard.SchoolViewModel
import com.example.ui.theme.SchoolConnectTheme

class MainActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val repo = SchoolRepository(this@MainActivity)
                @Suppress("UNCHECKED_CAST")
                return AuthViewModel(repo) as T
            }
        }
    }

    private val schoolViewModel: SchoolViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val repo = SchoolRepository(this@MainActivity)
                @Suppress("UNCHECKED_CAST")
                return SchoolViewModel(repo) as T
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            SchoolConnectTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val authState by authViewModel.uiState.collectAsStateWithLifecycle()

                    if (authState.user == null) {
                        SignInScreen(
                            uiState = authState,
                            viewModel = authViewModel
                        )
                    } else {
                        when (val step = authState.currentStep) {
                            is AuthStep.EmailVerification -> {
                                EmailVerificationScreen(
                                    email = step.email,
                                    sentCode = step.sentCode,
                                    uiState = authState,
                                    viewModel = authViewModel
                                )
                            }
                            is AuthStep.ProfileSetup -> {
                                ProfileSetupScreen(
                                    uiState = authState,
                                    viewModel = authViewModel
                                )
                            }
                            else -> {
                                val profile = authState.profile ?: UserProfile(
                                    id = authState.user?.uid ?: "",
                                    name = authState.user?.displayName ?: "",
                                    email = authState.user?.email ?: ""
                                )
                                MainSchoolScreen(
                                    userProfile = profile,
                                    authViewModel = authViewModel,
                                    schoolViewModel = schoolViewModel
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
