package com.natrajtechnology.giggo.presentation.screen

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
fun ProfileScreen(
    authViewModel: AuthViewModel,
    onLogout: () -> Unit,
    onNavigateBack: () -> Unit = {}
) {
    val authState by authViewModel.authState.collectAsState()
    val user = authState.user
    
    var isEditMode by remember { mutableStateOf(false) }
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var updateError by remember { mutableStateOf<String?>(null) }
    var updateSuccess by remember { mutableStateOf(false) }
    
    // Initialize fields when user data is loaded
    LaunchedEffect(user) {
        user?.let {
            firstName = it.firstName
            lastName = it.lastName
            phoneNumber = it.phoneNumber
        }
    }
    
    fun handleUpdateProfile() {
        authViewModel.updateUserProfile(
            firstName = firstName.trim(),
            lastName = lastName.trim(),
            phoneNumber = phoneNumber.trim(),
            onSuccess = {
                updateSuccess = true
                isEditMode = false
                updateError = null
            },
            onError = { error ->
                updateError = error
                updateSuccess = false
            }
        )
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
        // Back Button
        Box(
            modifier = Modifier
                .padding(8.dp)
                .size(48.dp)
                .align(Alignment.TopStart),
            contentAlignment = Alignment.Center
        ) {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back to Settings",
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
            Spacer(modifier = Modifier.height(60.dp)) // Extra space for back button
            
            // Profile Header
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Profile Picture
                        Box(
                            contentAlignment = Alignment.BottomEnd
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(120.dp)
                                    .background(
                                        brush = Brush.radialGradient(
                                            colors = listOf(Primary, PrimaryLight)
                                        ),
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(60.dp)
                                )
                            }
                            
                            FloatingActionButton(
                                onClick = { /* TODO: Implement image picker */ },
                                modifier = Modifier.size(40.dp),
                                containerColor = Primary
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Camera,
                                    contentDescription = "Change Profile Picture",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = user?.displayName ?: "User",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        
                        Text(
                            text = user?.email ?: "",
                            fontSize = 16.sp,
                            color = Gray500
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Email verification status
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Email Status: ",
                                fontSize = 14.sp,
                                color = Gray500
                            )
                            Text(
                                text = if (user?.isEmailVerified == true) "Verified ✓" else "Not Verified",
                                fontSize = 14.sp,
                                color = if (user?.isEmailVerified == true) Success else MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Profile Information Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Profile Information",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        
                        GigGoButton(
                            text = if (isEditMode) "Cancel" else "Edit",
                            onClick = {
                                if (isEditMode) {
                                    // Reset fields to original values
                                    firstName = user?.firstName ?: ""
                                    lastName = user?.lastName ?: ""
                                    phoneNumber = user?.phoneNumber ?: ""
                                    updateError = null
                                }
                                isEditMode = !isEditMode
                            },
                            backgroundColor = if (isEditMode) MaterialTheme.colorScheme.error else Primary,
                            modifier = Modifier.height(40.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Show success message
                    if (updateSuccess) {
                        Text(
                            text = "✓ Profile updated successfully!",
                            color = Success,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                    }
                    
                    // Show error message
                    updateError?.let { error ->
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                    }
                    
                    // First Name
                    GigGoTextField(
                        value = firstName,
                        onValueChange = { 
                            firstName = it
                            updateError = null
                            updateSuccess = false
                        },
                        label = "First Name",
                        leadingIcon = Icons.Default.Person,
                        enabled = isEditMode,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Last Name
                    GigGoTextField(
                        value = lastName,
                        onValueChange = { 
                            lastName = it
                            updateError = null
                            updateSuccess = false
                        },
                        label = "Last Name",
                        leadingIcon = Icons.Default.Person,
                        enabled = isEditMode,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Email (Read-only)
                    GigGoTextField(
                        value = user?.email ?: "",
                        onValueChange = { },
                        label = "Email",
                        leadingIcon = Icons.Default.Email,
                        enabled = false,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Phone Number
                    GigGoTextField(
                        value = phoneNumber,
                        onValueChange = { 
                            phoneNumber = it
                            updateError = null
                            updateSuccess = false
                        },
                        label = "Phone Number",
                        leadingIcon = Icons.Default.Phone,
                        keyboardType = KeyboardType.Phone,
                        enabled = isEditMode,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    if (isEditMode) {
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        GigGoButton(
                            text = "Save Changes",
                            onClick = { handleUpdateProfile() },
                            isLoading = authState.isLoading,
                            enabled = firstName.isNotBlank() && lastName.isNotBlank(),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Account Actions Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text(
                        text = "Account Actions",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    if (user?.isEmailVerified == false) {
                        GigGoButton(
                            text = "Resend Email Verification",
                            onClick = {
                                authViewModel.sendEmailVerification()
                            },
                            backgroundColor = Color.Transparent,
                            contentColor = Primary,
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                    
                    GigGoButton(
                        text = "Sign Out",
                        onClick = onLogout,
                        backgroundColor = MaterialTheme.colorScheme.error,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
