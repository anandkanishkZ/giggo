package com.natrajtechnology.giggo.presentation.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.natrajtechnology.giggo.presentation.components.GigGoButton
import com.natrajtechnology.giggo.presentation.components.GigGoTextField
import com.natrajtechnology.giggo.presentation.viewmodel.AuthViewModel
import com.natrajtechnology.giggo.ui.theme.Gray500
import com.natrajtechnology.giggo.ui.theme.Primary
import com.natrajtechnology.giggo.ui.theme.PrimaryLight
import com.natrajtechnology.giggo.ui.theme.Success

@Composable
fun ForgotPasswordScreen(
    authViewModel: AuthViewModel,
    onNavigateBack: () -> Unit
) {
    val authState by authViewModel.authState.collectAsState()
    val email by authViewModel.forgotPasswordEmail.collectAsState()
    val isEmailSent by authViewModel.isPasswordResetSent.collectAsState()
    
    // Reset state when screen is opened
    LaunchedEffect(Unit) {
        authViewModel.resetPasswordResetState()
    }
    
    fun handleSendResetLink() {
        authViewModel.sendPasswordReset {
            // Success handled by state change
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Primary.copy(alpha = 0.1f),
                        Color.White
                    )
                )
            )
            .systemBarsPadding()
    ) {
        // Back Button - Enhanced with better touch target
        Box(
            modifier = Modifier
                .padding(8.dp)
                .size(48.dp)
                .align(Alignment.TopStart),
            contentAlignment = Alignment.Center
        ) {
            IconButton(
                onClick = {
                    println("ForgotPassword Back button clicked") // Debug log
                    onNavigateBack()
                },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back to Login",
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(80.dp))
            
            if (isEmailSent) {
                // Success State
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(Success, Success.copy(alpha = 0.8f))
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MarkEmailRead,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(40.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Text(
                    text = "Check Your Email",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "We've sent a password reset link to",
                    fontSize = 16.sp,
                    color = Gray500,
                    textAlign = TextAlign.Center
                )
                
                Text(
                    text = email,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp)
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = "Please check your email and click on the link to reset your password. The link will expire in 24 hours.",
                    fontSize = 14.sp,
                    color = Gray500,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                
                Spacer(modifier = Modifier.height(48.dp))
                
                GigGoButton(
                    text = "Back to Login",
                    onClick = onNavigateBack
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                GigGoButton(
                    text = "Resend Email",
                    onClick = {
                        authViewModel.resendPasswordReset()
                    },
                    backgroundColor = Color.Transparent,
                    contentColor = Primary,
                    isLoading = authState.isLoading
                )
                
            } else {
                // Initial State
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(Primary, PrimaryLight)
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(40.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Text(
                    text = "Forgot Password?",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )
                
                Text(
                    text = "Don't worry! Enter your email address and we'll send you a link to reset your password.",
                    fontSize = 16.sp,
                    color = Gray500,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp,
                    modifier = Modifier.padding(top = 8.dp, start = 16.dp, end = 16.dp)
                )
                
                Spacer(modifier = Modifier.height(48.dp))
                
                // Form Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp)
                    ) {
                        GigGoTextField(
                            value = email,
                            onValueChange = { authViewModel.updateForgotPasswordEmail(it) },
                            label = "Email Address",
                            keyboardType = KeyboardType.Email,
                            leadingIcon = Icons.Default.Email,
                            errorMessage = authState.error,
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        GigGoButton(
                            text = "Send Reset Link",
                            onClick = { handleSendResetLink() },
                            isLoading = authState.isLoading,
                            enabled = email.isNotBlank() && !authState.isLoading
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
