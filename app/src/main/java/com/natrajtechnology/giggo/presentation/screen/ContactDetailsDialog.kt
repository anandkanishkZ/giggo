package com.natrajtechnology.giggo.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Launch
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.natrajtechnology.giggo.data.model.ContactDetails
import com.natrajtechnology.giggo.ui.theme.*
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.platform.LocalContext

@Composable
fun ContactDetailsDialog(
    contactDetails: ContactDetails,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .heightIn(max = 600.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Header with profile picture and basic info
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Primary.copy(alpha = 0.1f),
                                    Color.White
                                )
                            )
                        )
                        .padding(24.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Profile Picture Placeholder
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .background(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            Primary.copy(alpha = 0.3f),
                                            Primary.copy(alpha = 0.1f)
                                        )
                                    ),
                                    shape = CircleShape
                                )
                                .border(3.dp, Primary.copy(alpha = 0.2f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                modifier = Modifier.size(40.dp),
                                tint = Primary
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Name and verification
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = contactDetails.name,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            
                            if (contactDetails.isVerified) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = Icons.Default.Verified,
                                    contentDescription = "Verified",
                                    tint = Success,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        
                        // Rating if available
                        if (contactDetails.rating > 0) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = Color(0xFFFFD700),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = String.format("%.1f", contactDetails.rating),
                                    fontSize = 14.sp,
                                    color = Gray500,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = " (${contactDetails.completedGigs} gigs)",
                                    fontSize = 12.sp,
                                    color = Gray500
                                )
                            }
                        }
                    }
                }
                
                // Contact Information
                Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text(
                        text = "Contact Information",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    // Email
                    if (contactDetails.email.isNotBlank() && !contactDetails.email.contains("not available")) {
                        ContactInfoRow(
                            icon = Icons.Default.Email,
                            label = "Email",
                            value = contactDetails.email,
                            onClick = {
                                val intent = Intent(Intent.ACTION_SENDTO).apply {
                                    data = Uri.parse("mailto:${contactDetails.email}")
                                }
                                try {
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    // Handle case where no email app is available
                                    println("No email app available")
                                }
                            }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    } else if (contactDetails.email.contains("not available")) {
                        ContactInfoRow(
                            icon = Icons.Default.Email,
                            label = "Email",
                            value = "Contact information not shared",
                            onClick = null
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                    
                    // Phone
                    if (contactDetails.phone.isNotBlank()) {
                        ContactInfoRow(
                            icon = Icons.Default.Phone,
                            label = "Phone",
                            value = contactDetails.phone,
                            onClick = {
                                val intent = Intent(Intent.ACTION_DIAL).apply {
                                    data = Uri.parse("tel:${contactDetails.phone}")
                                }
                                try {
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    // Handle case where no phone app is available
                                    println("No phone app available")
                                }
                            }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                    
                    // Location
                    if (contactDetails.location.isNotBlank()) {
                        ContactInfoRow(
                            icon = Icons.Default.LocationOn,
                            label = "Location",
                            value = contactDetails.location,
                            onClick = null
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                    
                    // Join Date
                    ContactInfoRow(
                        icon = Icons.Default.CalendarToday,
                        label = "Member Since",
                        value = contactDetails.joinDate,
                        onClick = null
                    )
                    
                    // Bio
                    if (contactDetails.bio.isNotBlank()) {
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = "About",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Text(
                            text = contactDetails.bio,
                            fontSize = 14.sp,
                            color = Gray500,
                            lineHeight = 20.sp
                        )
                    }
                    
                    // Skills
                    if (contactDetails.skills.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = "Skills",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        // Skills chips
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            contactDetails.skills.take(3).forEach { skill ->
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = Primary.copy(alpha = 0.1f)
                                    ),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Text(
                                        text = skill,
                                        fontSize = 12.sp,
                                        color = Primary,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                    
                    // Close Button
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Close",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactInfoRow(
    icon: ImageVector,
    label: String,
    value: String,
    onClick: (() -> Unit)?
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) {
                    Modifier
                } else {
                    Modifier
                }
            ),
        colors = CardDefaults.cardColors(
            containerColor = Gray300.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(12.dp),
        onClick = onClick ?: {}
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Primary,
                modifier = Modifier.size(20.dp)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = label,
                    fontSize = 12.sp,
                    color = Gray500,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = value,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Medium
                )
            }
            
            if (onClick != null) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Launch,
                    contentDescription = "Open",
                    tint = Primary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
