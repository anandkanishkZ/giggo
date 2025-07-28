package com.natrajtechnology.giggo.presentation.screen

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.ContactMail
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.natrajtechnology.giggo.data.model.Notification
import com.natrajtechnology.giggo.data.model.NotificationCategory
import com.natrajtechnology.giggo.ui.theme.Gray300
import com.natrajtechnology.giggo.ui.theme.Gray500
import com.natrajtechnology.giggo.ui.theme.Primary
import com.natrajtechnology.giggo.ui.theme.PrimaryLight
import com.natrajtechnology.giggo.ui.theme.Success
import com.natrajtechnology.giggo.ui.viewmodel.NotificationViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun NotificationsScreen(
    notificationViewModel: NotificationViewModel = hiltViewModel()
) {
    val notifications by notificationViewModel.notifications.collectAsState()
    val isLoading by notificationViewModel.isLoading.collectAsState()
    val errorMessage by notificationViewModel.errorMessage.collectAsState()
    val showContactDetailsDialog by notificationViewModel.showContactDetailsDialog.collectAsState()
    val contactDetails by notificationViewModel.contactDetails.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
    ) {
        // Header
        NotificationHeader(
            unreadCount = notificationViewModel.getUnreadNotificationCount(),
            onRefresh = { notificationViewModel.refreshNotifications() },
            onMarkAllAsRead = { notificationViewModel.markAllNotificationsAsRead() }
        )

        when {
            isLoading && notifications.isEmpty() -> {
                // Loading state for initial load
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = Primary)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Loading requests...",
                            color = Gray500,
                            fontSize = 14.sp
                        )
                    }
                }
            }
            
            errorMessage != null && notifications.isEmpty() -> {
                // Error state
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Text(
                            text = "⚠️",
                            fontSize = 48.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = errorMessage ?: "Failed to load requests",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { notificationViewModel.loadNotifications() },
                            colors = ButtonDefaults.buttonColors(containerColor = Primary)
                        ) {
                            Text("Retry")
                        }
                    }
                }
            }
            
            notifications.isEmpty() -> {
                // Empty state
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = Gray300
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No requests yet",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            color = Gray500
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "When someone contacts you about your gigs, you'll see requests here.",
                            fontSize = 14.sp,
                            color = Gray500,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            
            else -> {
                // Notifications list
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        horizontal = 16.dp,
                        vertical = 8.dp
                    )
                ) {
                    items(notifications) { notification ->
                        NotificationCard(
                            notification = notification,
                            isLoadingContacts = isLoading,
                            onClick = {
                                if (!notification.isRead) {
                                    notificationViewModel.markNotificationAsRead(notification.id)
                                }
                            },
                            onSenderClick = { senderId ->
                                println("Debug: NotificationsScreen - Sender clicked: $senderId")
                                notificationViewModel.loadContactDetails(senderId)
                            }
                        )
                    }
                    
                    // Bottom spacing
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
    
    // Contact Details Dialog
    println("Debug: NotificationsScreen - Dialog state - showContactDetailsDialog: $showContactDetailsDialog, contactDetails: $contactDetails")
    
    if (showContactDetailsDialog) {
        contactDetails?.let { details ->
            println("Debug: NotificationsScreen - Showing ContactDetailsDialog for: ${details.name}")
            ContactDetailsDialog(
                contactDetails = details,
                onDismiss = { 
                    println("Debug: NotificationsScreen - Dialog dismissed")
                    notificationViewModel.hideContactDetailsDialog() 
                }
            )
        } ?: run {
            println("Debug: NotificationsScreen - showContactDetailsDialog is true but contactDetails is null")
        }
    }
}
@Composable
fun NotificationHeader(
    unreadCount: Int,
    onRefresh: () -> Unit,
    onMarkAllAsRead: () -> Unit
) {
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
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Requests",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = onRefresh
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = Primary
                        )
                    }
                    
                    if (unreadCount > 0) {
                        Text(
                            text = "$unreadCount new",
                            fontSize = 14.sp,
                            color = Primary,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier
                                .background(
                                    Primary.copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                .clickable { onMarkAllAsRead() }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationCard(
    notification: Notification,
    isLoadingContacts: Boolean = false,
    onClick: () -> Unit,
    onSenderClick: (String) -> Unit = {}
) {
    val timeFormatted = formatTimestamp(notification.createdAt.toDate().time)
    val notificationIcon = when (notification.type) {
        NotificationCategory.CONTACT_REQUEST -> Icons.Default.ContactMail
        NotificationCategory.MESSAGE -> Icons.AutoMirrored.Filled.Message
        NotificationCategory.JOB_ALERT -> Icons.Default.Work
        NotificationCategory.SYSTEM -> Icons.Default.Notifications
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = if (notification.isRead) 2.dp else 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (notification.isRead) Color.White else Color.White
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Icon container
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = when (notification.type) {
                                NotificationCategory.CONTACT_REQUEST -> listOf(Primary.copy(alpha = 0.2f), PrimaryLight.copy(alpha = 0.1f))
                                NotificationCategory.MESSAGE -> listOf(Primary.copy(alpha = 0.2f), PrimaryLight.copy(alpha = 0.1f))
                                NotificationCategory.JOB_ALERT -> listOf(Success.copy(alpha = 0.2f), Success.copy(alpha = 0.1f))
                                NotificationCategory.SYSTEM -> listOf(Gray500.copy(alpha = 0.2f), Gray500.copy(alpha = 0.1f))
                            }
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = notificationIcon,
                    contentDescription = null,
                    tint = when (notification.type) {
                        NotificationCategory.CONTACT_REQUEST -> Primary
                        NotificationCategory.MESSAGE -> Primary
                        NotificationCategory.JOB_ALERT -> Success
                        NotificationCategory.SYSTEM -> Gray500
                    },
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = notification.title,
                        fontSize = 16.sp,
                        fontWeight = if (notification.isRead) FontWeight.Medium else FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.weight(1f)
                    )
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (!notification.isRead) {
                            Icon(
                                imageVector = Icons.Default.Circle,
                                contentDescription = "Unread",
                                tint = Primary,
                                modifier = Modifier.size(8.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        
                        Text(
                            text = timeFormatted,
                            fontSize = 12.sp,
                            color = Gray500
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = notification.message,
                    fontSize = 14.sp,
                    color = Gray500,
                    lineHeight = 20.sp
                )
                
                // Show sender and gig info for contact requests
                if (notification.type == NotificationCategory.CONTACT_REQUEST) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = Gray500
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        
                        // Loading indicator or clickable name
                        if (isLoadingContacts) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .background(
                                        Primary.copy(alpha = 0.1f),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(12.dp),
                                    strokeWidth = 1.dp,
                                    color = Primary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Loading...",
                                    fontSize = 12.sp,
                                    color = Primary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        } else {
                            Text(
                                text = notification.senderName,
                                fontSize = 12.sp,
                                color = Primary,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier
                                    .clickable { 
                                        println("Debug: NotificationCard - Buyer name clicked: ${notification.senderName} (ID: ${notification.senderUserId})")
                                        onSenderClick(notification.senderUserId) 
                                    }
                                    .background(
                                        Primary.copy(alpha = 0.1f),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isLoadingContacts) "" else "• Tap to view contact",
                            fontSize = 10.sp,
                            color = Gray500,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )
                    }
                }
            }
        }
    }
}

/**
 * Helper function to format timestamp for display
 */
private fun formatTimestamp(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    
    return when {
        diff < 60 * 1000 -> "Just now"
        diff < 60 * 60 * 1000 -> "${diff / (60 * 1000)} min ago"
        diff < 24 * 60 * 60 * 1000 -> "${diff / (60 * 60 * 1000)} hour ago"
        diff < 7 * 24 * 60 * 60 * 1000 -> "${diff / (24 * 60 * 60 * 1000)} day ago"
        else -> {
            val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
            dateFormat.format(Date(timestamp))
        }
    }
}
