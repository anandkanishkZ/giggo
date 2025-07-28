package com.natrajtechnology.giggo.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.firebase.auth.FirebaseAuth
import com.natrajtechnology.giggo.data.model.ContactSellerRequest
import com.natrajtechnology.giggo.data.model.Gig
import com.natrajtechnology.giggo.presentation.viewmodel.GigDetailViewModel
import com.natrajtechnology.giggo.ui.theme.*
import com.natrajtechnology.giggo.ui.viewmodel.NotificationViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GigDetailScreen(
    gigId: String,
    onNavigateBack: () -> Unit,
    gigDetailViewModel: GigDetailViewModel = hiltViewModel(),
    notificationViewModel: NotificationViewModel = hiltViewModel()
) {
    println("Debug: GigDetailScreen - Composable started with gigId: '$gigId'")
    
    val uiState by gigDetailViewModel.uiState.collectAsState()
    val notificationResponse by notificationViewModel.contactSellerResponse.collectAsState()
    val notificationError by notificationViewModel.errorMessage.collectAsState()
    
    var showContactDialog by remember { mutableStateOf(false) }
    var contactMessage by remember { mutableStateOf("") }
    
    // Handle notification response
    LaunchedEffect(notificationResponse) {
        notificationResponse?.let { response ->
            if (response.success) {
                showContactDialog = false
                contactMessage = ""
                // You can show a success message here if needed
                println("Debug: GigDetailScreen - Contact seller request sent successfully")
            }
            // Clear the response after handling
            notificationViewModel.clearContactSellerResponse()
        }
    }
    
    // Handle notification error
    LaunchedEffect(notificationError) {
        notificationError?.let { error ->
            println("Debug: GigDetailScreen - Contact seller error: $error")
            // You can show an error snackbar here if needed
        }
    }
    
    LaunchedEffect(gigId) {
        println("Debug: GigDetailScreen - LaunchedEffect triggered with gigId: '$gigId'")
        try {
            gigDetailViewModel.loadGigDetails(gigId)
        } catch (e: Exception) {
            println("Debug: GigDetailScreen - Exception in LaunchedEffect: ${e.message}")
            e.printStackTrace()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Gig Details",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            try {
                                println("Debug: GigDetailScreen - Back button pressed")
                                onNavigateBack()
                            } catch (e: Exception) {
                                println("Debug: GigDetailScreen - Error in onNavigateBack: ${e.message}")
                                e.printStackTrace()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Primary)
                    }
                }
                
                uiState.errorMessage != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "❌",
                                fontSize = 48.sp
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = uiState.errorMessage ?: "Unknown error",
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { gigDetailViewModel.loadGigDetails(gigId) },
                                colors = ButtonDefaults.buttonColors(containerColor = Primary)
                            ) {
                                Text("Retry")
                            }
                        }
                    }
                }
                
                uiState.gig != null -> {
                    val currentGig = uiState.gig
                    if (currentGig != null) {
                        GigDetailContent(
                            gig = currentGig,
                            onContactSeller = { showContactDialog = true },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
    
    // Contact Seller Dialog
    if (showContactDialog) {
        ContactSellerDialog(
            gig = uiState.gig,
            message = contactMessage,
            onMessageChange = { contactMessage = it },
            onSendRequest = { message ->
                uiState.gig?.let { gig ->
                    val currentUser = FirebaseAuth.getInstance().currentUser
                    if (currentUser != null) {
                        val request = ContactSellerRequest(
                            sellerUserId = gig.userId,
                            buyerUserId = currentUser.uid,
                            buyerName = currentUser.displayName ?: "Anonymous User",
                            gigId = gig.id,
                            gigTitle = gig.title,
                            message = message
                        )
                        notificationViewModel.sendContactSellerRequest(request)
                    }
                }
            },
            onDismiss = { 
                showContactDialog = false
                contactMessage = ""
            },
            isLoading = notificationViewModel.isLoading.collectAsState().value
        )
    }
}

@Composable
fun GigDetailContent(
    gig: Gig,
    onContactSeller: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Header Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                // Title and Category
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = gig.title,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.weight(1f)
                    )
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Surface(
                        color = Primary.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Category,
                                contentDescription = null,
                                tint = Primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = gig.category,
                                fontSize = 14.sp,
                                color = Primary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Price and Delivery Info
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    DetailInfoItem(
                        icon = Icons.Default.AttachMoney,
                        label = "Price",
                        value = "₹${gig.price.toInt()}",
                        valueColor = Primary
                    )
                    
                    DetailInfoItem(
                        icon = Icons.Default.Schedule,
                        label = "Delivery",
                        value = "${gig.deliveryTimeInDays} days",
                        valueColor = MaterialTheme.colorScheme.onBackground
                    )
                    
                    DetailInfoItem(
                        icon = Icons.Default.CheckCircle,
                        label = "Status",
                        value = gig.status,
                        valueColor = if (gig.status == "published") Color(0xFF4CAF50) else Gray500
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        
        // Description Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = null,
                        tint = Primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Description",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = gig.description,
                    fontSize = 16.sp,
                    color = Gray500,
                    lineHeight = 24.sp
                )
            }
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        
        // Tags Card (if tags exist)
        if (gig.tags.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tag,
                            contentDescription = null,
                            tint = Primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Tags",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Tags Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        gig.tags.take(6).forEach { tag ->
                            Surface(
                                color = Primary.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Text(
                                    text = "#$tag",
                                    fontSize = 12.sp,
                                    color = Primary,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
        }
        
        // Additional Details Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = Primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Additional Information",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                DetailRow(
                    label = "Gig ID",
                    value = gig.id.takeLast(8).uppercase()
                )
                
                DetailRow(
                    label = "Active Status",
                    value = if (gig.isActive) "Active" else "Inactive"
                )
                
                DetailRow(
                    label = "Created",
                    value = "Recently" // You can format the timestamp if needed
                )
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Action Button
        Button(
            onClick = onContactSeller,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Primary),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Message,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Contact Seller",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
        
        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
fun DetailInfoItem(
    icon: ImageVector,
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onBackground
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            color = Gray500
        )
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = valueColor
        )
    }
}

@Composable
fun DetailRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = Gray500
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
fun ContactSellerDialog(
    gig: Gig?,
    message: String,
    onMessageChange: (String) -> Unit,
    onSendRequest: (String) -> Unit,
    onDismiss: () -> Unit,
    isLoading: Boolean
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Contact Seller",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Gray500
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Gig Info
                gig?.let {
                    Surface(
                        color = Primary.copy(alpha = 0.05f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Text(
                                text = it.title,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "₹${it.price.toInt()}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Primary
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Message Input
                Text(
                    text = "Your Message (Optional)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = message,
                    onValueChange = onMessageChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    placeholder = {
                        Text(
                            text = "Hi, I'm interested in your gig. Please let me know more details...",
                            color = Gray500
                        )
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Primary,
                        unfocusedBorderColor = Gray300
                    ),
                    maxLines = 4
                )
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Gray500
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Gray300),
                        enabled = !isLoading
                    ) {
                        Text("Cancel")
                    }
                    
                    Button(
                        onClick = { onSendRequest(message) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Send Request")
                        }
                    }
                }
            }
        }
    }
}
