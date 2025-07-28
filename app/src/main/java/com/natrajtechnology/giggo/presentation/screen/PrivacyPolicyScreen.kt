package com.natrajtechnology.giggo.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Shield
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
fun PrivacyPolicyScreen(
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
                        text = "Privacy Policy",
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
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = Primary,
                        modifier = Modifier.size(40.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = "Privacy Policy",
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
                
                // Privacy Policy Content
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        PrivacySection(
                            title = "Introduction",
                            content = "GigGO (\"we,\" \"our,\" or \"us\") is committed to protecting your privacy. This Privacy Policy explains how we collect, use, disclose, and safeguard your information when you use our mobile application and services."
                        )
                        
                        PrivacySection(
                            title = "Information We Collect",
                            content = "We collect information you provide directly to us, such as:\n\n• Personal information (name, email address, phone number)\n• Profile information and photos\n• Gig listings and descriptions\n• Messages and communications\n• Payment information (processed securely by third-party providers)\n• Location data (with your permission)"
                        )
                        
                        PrivacySection(
                            title = "How We Use Your Information",
                            content = "We use the information we collect to:\n\n• Provide, maintain, and improve our services\n• Process transactions and send related information\n• Send technical notices and support messages\n• Communicate with you about products, services, and events\n• Monitor and analyze trends and usage\n• Detect and prevent fraudulent transactions"
                        )
                        
                        PrivacySection(
                            title = "Information Sharing",
                            content = "We do not sell, trade, or otherwise transfer your personal information to third parties except:\n\n• With your consent\n• To service providers who assist us in operating our app\n• To comply with legal obligations\n• To protect our rights and safety\n• In connection with a business transfer or merger"
                        )
                        
                        PrivacySection(
                            title = "Data Security",
                            content = "We implement appropriate security measures to protect your personal information against unauthorized access, alteration, disclosure, or destruction. However, no method of transmission over the internet is 100% secure."
                        )
                        
                        PrivacySection(
                            title = "Your Rights",
                            content = "You have the right to:\n\n• Access and update your personal information\n• Delete your account and associated data\n• Opt-out of marketing communications\n• Request a copy of your data\n• Restrict or object to certain processing activities"
                        )
                        
                        PrivacySection(
                            title = "Data Retention",
                            content = "We retain your personal information for as long as necessary to provide our services and fulfill the purposes outlined in this Privacy Policy, unless a longer retention period is required by law."
                        )
                        
                        PrivacySection(
                            title = "Children's Privacy",
                            content = "Our service is not intended for children under 13 years of age. We do not knowingly collect personal information from children under 13."
                        )
                        
                        PrivacySection(
                            title = "Changes to This Policy",
                            content = "We may update this Privacy Policy from time to time. We will notify you of any changes by posting the new Privacy Policy on this page and updating the \"Last updated\" date."
                        )
                        
                        PrivacySection(
                            title = "Contact Us",
                            content = "If you have any questions about this Privacy Policy, please contact us at:\n\nEmail: privacy@giggo.com\nPhone: +977 9851156852\nAddress: Dillibazar, Kathmandu, Bagmati"
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PrivacySection(
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
