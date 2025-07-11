package com.mai.driveapp.android.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mai.driveapp.android.R
import com.mai.driveapp.auth.viewmodels.PhoneNumberUiState
import com.mai.driveapp.auth.viewmodels.PhoneNumberViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun PhoneNumberScreen(
    viewModel: PhoneNumberViewModel = koinViewModel(),
    onNavigateToVerification: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val phoneNumber by viewModel.phoneNumber.collectAsState()
    val userType by viewModel.userType.collectAsState()
    
    var showNotification by remember { mutableStateOf(false) }
    var notificationMessage by remember { mutableStateOf("") }
    
    // Show notification when verification code is received
    LaunchedEffect(uiState) {
        when (uiState) {
            is PhoneNumberUiState.Success -> {
                val code = (uiState as PhoneNumberUiState.Success).verificationCode
                notificationMessage = "کد تایید شما: $code"
                showNotification = true
                onNavigateToVerification(code)
            }
            is PhoneNumberUiState.Error -> {
                notificationMessage = (uiState as PhoneNumberUiState.Error).message
                showNotification = true
            }
            else -> {
                showNotification = false
            }
        }
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        // Main content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Text(
                text = "به تاپرو خوش آمدید",
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 48.dp, bottom = 24.dp)
            )
            
            Text(
                text = "برای استفاده از تاپرو، شماره تلفن خود را وارد کنید",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 24.dp)
            )
            
            // User type selection
            UserTypeSelector(
                selectedUserType = userType,
                onUserTypeSelected = { viewModel.setUserType(it) }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Phone number input
            OutlinedTextField(
                value = phoneNumber,
                onValueChange = { viewModel.updatePhoneNumber(it) },
                label = { Text("شماره تلفن") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Submit button
            Button(
                onClick = { viewModel.submitPhoneNumber() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = uiState !is PhoneNumberUiState.Loading
            ) {
                if (uiState is PhoneNumberUiState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("ارسال کد تایید")
                }
            }
        }
        
        // Notification
        if (showNotification) {
            NotificationCard(
                message = notificationMessage,
                onDismiss = { showNotification = false }
            )
        }
    }
}

@Composable
fun UserTypeSelector(
    selectedUserType: String,
    onUserTypeSelected: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "می‌خواهید به عنوان چه کسی از تاپرو استفاده کنید؟",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // Passenger option
            UserTypeOption(
                title = "مسافر",
                iconResId = R.drawable.ic_passenger,
                isSelected = selectedUserType == "passenger",
                onClick = { onUserTypeSelected("passenger") }
            )
            
            // Driver option
            UserTypeOption(
                title = "راننده",
                iconResId = R.drawable.ic_driver,
                isSelected = selectedUserType == "driver",
                onClick = { onUserTypeSelected("driver") }
            )
        }
    }
}

@Composable
fun UserTypeOption(
    title: String,
    iconResId: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(120.dp)
    ) {
        Card(
            modifier = Modifier
                .size(100.dp)
                .padding(8.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surfaceVariant
            ),
            onClick = onClick
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = iconResId),
                    contentDescription = title,
                    modifier = Modifier.size(48.dp),
                    tint = if (isSelected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            color = if (isSelected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun NotificationCard(
    message: String,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Card(
            modifier = Modifier
                .padding(top = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )
                
                IconButton(onClick = onDismiss) {
                    Icon(
                        painter = painterResource(id = android.R.drawable.ic_menu_close_clear_cancel),
                        contentDescription = "بستن"
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PhoneNumberScreenPreview() {
    MaterialTheme {
        PhoneNumberScreen(onNavigateToVerification = {})
    }
} 