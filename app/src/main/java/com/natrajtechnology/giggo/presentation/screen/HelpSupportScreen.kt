package com.natrajtechnology.giggo.presentation.screen

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.natrajtechnology.giggo.ui.theme.Primary
import com.natrajtechnology.giggo.ui.theme.PrimaryLight
import com.natrajtechnology.giggo.ui.theme.Gray500

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpSupportScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Top Bar
            TopAppBar(
                title = {
                    Text(
                        text = "Help & Support",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
            
            // Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Icon
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Primary.copy(alpha = 0.2f),
                                    Primary.copy(alpha = 0.1f)
                                )
                            ),
                            shape = RoundedCornerShape(20.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Help,
                        contentDescription = null,
                        tint = Primary,
                        modifier = Modifier.size(40.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = "How can we help you?",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Choose from the options below to get the support you need",
                    fontSize = 14.sp,
                    color = Gray500,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Support Options
                SupportOption(
                    icon = Icons.Default.Email,
                    title = "Email Support",
                    subtitle = "Get help via email within 24 hours",
                    onClick = {
                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:support@giggo.com")
                            putExtra(Intent.EXTRA_SUBJECT, "GigGO App Support Request")
                            putExtra(Intent.EXTRA_TEXT, "Hi GigGO Support Team,\n\nI need help with:\n\n")
                        }
                        context.startActivity(intent)
                    }
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                SupportOption(
                    icon = Icons.Default.Chat,
                    title = "Live Chat",
                    subtitle = "Chat with our support team (9 AM - 6 PM)",
                    onClick = {
                        // In a real app, this would open a chat widget
                        val intent = Intent(Intent.ACTION_VIEW).apply {
                            data = Uri.parse("https://giggo.com/support/chat")
                        }
                        context.startActivity(intent)
                    }
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                SupportOption(
                    icon = Icons.Default.Call,
                    title = "Phone Support",
                    subtitle = "+977 9851156852 (Mon-Fri, 9 AM - 6 PM)",
                    onClick = {
                        val intent = Intent(Intent.ACTION_DIAL).apply {
                            data = Uri.parse("tel:+9779851156852")
                        }
                        context.startActivity(intent)
                    }
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                SupportOption(
                    icon = Icons.Default.QuestionAnswer,
                    title = "FAQ",
                    subtitle = "Find answers to common questions",
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW).apply {
                            data = Uri.parse("https://giggo.com/faq")
                        }
                        context.startActivity(intent)
                    }
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Quick Help Section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Primary.copy(alpha = 0.05f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Text(
                            text = "Quick Help",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        QuickHelpItem(
                            question = "How do I post a gig?",
                            answer = "Tap the '+' button on the home screen and fill in your gig details."
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        QuickHelpItem(
                            question = "How do I contact a seller?",
                            answer = "Go to the gig details and tap 'Contact Seller' to send a message."
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        QuickHelpItem(
                            question = "How do I edit my profile?",
                            answer = "Go to Settings > Profile to update your information."
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        QuickHelpItem(
                            question = "How do I reset my password?",
                            answer = "Go to Settings > Change Password or use 'Forgot Password' on login."
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // App Info
                Text(
                    text = "GigGO v1.0.0",
                    fontSize = 12.sp,
                    color = Gray500,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}

@Composable
private fun SupportOption(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Primary.copy(alpha = 0.2f),
                                PrimaryLight.copy(alpha = 0.1f)
                            )
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    fontSize = 14.sp,
                    color = Gray500,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
private fun QuickHelpItem(
    question: String,
    answer: String
) {
    Column {
        Text(
            text = question,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = answer,
            fontSize = 13.sp,
            color = Gray500,
            lineHeight = 18.sp
        )
    }
}
