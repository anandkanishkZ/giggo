package com.natrajtechnology.giggo.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.natrajtechnology.giggo.ui.theme.Primary
import com.natrajtechnology.giggo.ui.theme.Gray500

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TermsOfServiceScreen(
    onNavigateBack: () -> Unit
) {
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
                        text = "Terms of Service",
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
                    .padding(24.dp)
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
                        )
                        .align(Alignment.CenterHorizontally),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Gavel,
                        contentDescription = null,
                        tint = Primary,
                        modifier = Modifier.size(40.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = "Terms of Service",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Last updated: July 28, 2025",
                    fontSize = 14.sp,
                    color = Gray500,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Terms of Service Content
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        TermsSection(
                            title = "Acceptance of Terms",
                            content = "By accessing and using GigGO (\"the Service\"), you accept and agree to be bound by the terms and provision of this agreement. If you do not agree to abide by the above, please do not use this service."
                        )
                        
                        TermsSection(
                            title = "Description of Service",
                            content = "GigGO is a platform that connects freelancers and service providers with individuals and businesses seeking their services. We provide the technology platform but are not a party to any agreements between users."
                        )
                        
                        TermsSection(
                            title = "User Accounts",
                            content = "• You must be at least 18 years old to use this service\n• You are responsible for maintaining the confidentiality of your account\n• You agree to provide accurate and complete information\n• You are responsible for all activities that occur under your account\n• You must notify us immediately of any unauthorized use"
                        )
                        
                        TermsSection(
                            title = "User Conduct",
                            content = "You agree not to:\n\n• Use the service for any unlawful purpose\n• Post false, misleading, or fraudulent content\n• Harass, abuse, or harm other users\n• Violate any intellectual property rights\n• Transmit viruses or malicious code\n• Spam or send unsolicited messages\n• Circumvent any security measures"
                        )
                        
                        TermsSection(
                            title = "Content Guidelines",
                            content = "• All content must be original or properly licensed\n• Content must not violate any laws or regulations\n• We reserve the right to remove inappropriate content\n• You retain ownership of your content but grant us license to use it\n• You are responsible for backing up your content"
                        )
                        
                        TermsSection(
                            title = "Payments and Fees",
                            content = "• Users are responsible for all fees and taxes\n• Payment processing is handled by third-party providers\n• We may charge service fees as disclosed in the app\n• Refunds are subject to our refund policy\n• Disputed transactions will be investigated"
                        )
                        
                        TermsSection(
                            title = "Privacy",
                            content = "Your privacy is important to us. Please review our Privacy Policy, which also governs your use of the service, to understand our practices."
                        )
                        
                        TermsSection(
                            title = "Limitation of Liability",
                            content = "GigGO shall not be liable for any indirect, incidental, special, consequential, or punitive damages resulting from your use of the service. Our total liability is limited to the amount you paid us in the last 12 months."
                        )
                        
                        TermsSection(
                            title = "Indemnification",
                            content = "You agree to indemnify and hold harmless GigGO from any claims, damages, or expenses arising from your use of the service or violation of these terms."
                        )
                        
                        TermsSection(
                            title = "Termination",
                            content = "We may terminate or suspend your account at any time for violations of these terms. You may also terminate your account at any time. Termination does not relieve you of any obligations incurred prior to termination."
                        )
                        
                        TermsSection(
                            title = "Modifications",
                            content = "We reserve the right to modify these terms at any time. We will notify users of significant changes. Continued use of the service after changes constitutes acceptance of the new terms."
                        )
                        
                        TermsSection(
                            title = "Governing Law",
                            content = "These terms are governed by the laws of [Your Jurisdiction]. Any disputes will be resolved in the courts of [Your Jurisdiction]."
                        )
                        
                        TermsSection(
                            title = "Contact Information",
                            content = "If you have any questions about these Terms of Service, please contact us at:\n\nEmail: legal@giggo.com\nPhone: +977 9851156852\nAddress: Dillibazar, Kathmandu, Bagmati"
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TermsSection(
    title: String,
    content: String
) {
    Column(
        modifier = Modifier.padding(bottom = 24.dp)
    ) {
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Text(
            text = content,
            fontSize = 14.sp,
            color = Gray500,
            lineHeight = 20.sp
        )
    }
}
