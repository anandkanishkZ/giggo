package com.natrajtechnology.giggo.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.natrajtechnology.giggo.data.constants.GigCategories
import com.natrajtechnology.giggo.presentation.viewmodel.AuthViewModel
import com.natrajtechnology.giggo.presentation.viewmodel.GigViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddGigScreen(
    authViewModel: AuthViewModel? = null,
    gigViewModel: GigViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val gigUiState by gigViewModel.uiState.collectAsStateWithLifecycle()
    val formState by gigViewModel.formState.collectAsStateWithLifecycle()

    var isExpanded by remember { mutableStateOf(false) }

    val categories = GigCategories.ALL_CATEGORIES

    val scrollState = rememberScrollState()

    // Handle success state
    LaunchedEffect(gigUiState.isSuccess) {
        if (gigUiState.isSuccess) {
            // Auto-navigate back after successful creation
            kotlinx.coroutines.delay(1500) // Show success message for 1.5 seconds
            onNavigateBack()
        }
    }

    // Show snackbar for messages
    gigUiState.errorMessage?.let { message ->
        LaunchedEffect(message) {
            // Clear the message after showing
            gigViewModel.clearMessages()
        }
    }

    gigUiState.successMessage?.let { message ->
        LaunchedEffect(message) {
            // Auto-clear success message
            kotlinx.coroutines.delay(2000)
            gigViewModel.clearMessages()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                Text(
                    text = "Create New Gig",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface,
                titleContentColor = MaterialTheme.colorScheme.onSurface
            )
        )
        
        // Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Error/Success Message Display
            gigUiState.errorMessage?.let { message ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = "❌ $message",
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        fontSize = 14.sp
                    )
                }
            }

            gigUiState.successMessage?.let { message ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Text(
                        text = "✅ $message",
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Header
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "🚀 Start Your Freelancing Journey",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Create a professional gig to showcase your skills and attract clients",
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
            }
            
            // Gig Title
            CustomTextField(
                value = formState.title,
                onValueChange = { gigViewModel.updateFormField("title", it) },
                label = "Gig Title",
                placeholder = "I will create an amazing website for you",
                icon = Icons.Filled.Title
            )
            
            // Category Selection
            ExposedDropdownMenuBox(
                expanded = isExpanded,
                onExpandedChange = { isExpanded = !isExpanded }
            ) {
                OutlinedTextField(
                    value = formState.category,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Category") },
                    placeholder = { Text("Select a category") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Category,
                            contentDescription = "Category"
                        )
                    },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded)
                    },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                
                ExposedDropdownMenu(
                    expanded = isExpanded,
                    onDismissRequest = { isExpanded = false }
                ) {
                    categories.forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category) },
                            onClick = {
                                gigViewModel.updateFormField("category", category)
                                isExpanded = false
                            }
                        )
                    }
                }
            }
            
            // Description
            OutlinedTextField(
                value = formState.description,
                onValueChange = { gigViewModel.updateFormField("description", it) },
                label = { Text("Gig Description") },
                placeholder = { Text("Describe what you will deliver...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Description,
                        contentDescription = "Description"
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                minLines = 4,
                maxLines = 6
            )
            
            // Price and Delivery Time Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Price
                OutlinedTextField(
                    value = formState.price,
                    onValueChange = { gigViewModel.updateFormField("price", it) },
                    label = { Text("Price (₹)") },
                    placeholder = { Text("500") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.AttachMoney,
                            contentDescription = "Price"
                        )
                    },
                    modifier = Modifier.weight(1f)
                )
                
                // Delivery Time
                OutlinedTextField(
                    value = formState.deliveryTime,
                    onValueChange = { gigViewModel.updateFormField("deliveryTime", it) },
                    label = { Text("Delivery (Days)") },
                    placeholder = { Text("3") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Schedule,
                            contentDescription = "Delivery Time"
                        )
                    },
                    modifier = Modifier.weight(1f)
                )
            }
            
            // Requirements Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "📋 Gig Requirements",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "• Provide detailed requirements from buyers\n" +
                                "• Include any necessary files or information\n" +
                                "• Set clear expectations and deliverables",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }
            }
            
            // Action Buttons
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Create Gig Button
                Button(
                    onClick = {
                        gigViewModel.createGig()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    enabled = formState.isValid && !gigUiState.isLoading,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (gigUiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text(
                            text = "🚀 Create Gig",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Bottom spacing
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = label
            )
        },
        modifier = modifier.fillMaxWidth(),
        singleLine = true
    )
}

@Preview(showBackground = true)
@Composable
private fun AddGigScreenPreview() {
    MaterialTheme {
        AddGigScreen()
    }
}
