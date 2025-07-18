package com.mai.driveapp.android.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mai.driveapp.android.R
import com.mai.driveapp.auth.viewmodels.PhoneNumberUiState
import com.mai.driveapp.auth.viewmodels.PhoneNumberViewModel
import org.koin.androidx.compose.koinViewModel
import com.mai.driveapp.Language
import com.mai.driveapp.android.LocalLanguageManager
import com.mai.driveapp.android.LocalizedStrings
import com.mai.driveapp.android.LanguageSelectorModal

// Constants moved outside composable to prevent recreations
private val spacerHeight24 = 24.dp
private val buttonHeight = 50.dp
private val iconSize = 24.dp
private val strokeWidth = 2.dp

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
    var showLanguageSelector by remember { mutableStateOf(false) }
    
    // Access language manager - read only once and store in a stable reference
    val languageManager = LocalLanguageManager.current
    val currentLanguage by languageManager.languageState
    
    // Calculate text properties once based on language
    val isRtl = currentLanguage.isRtl
    val textAlign = if (isRtl) TextAlign.Right else TextAlign.Left
    val textDirection = if (isRtl) TextDirection.Rtl else TextDirection.Ltr
    val isPersian = currentLanguage == Language.PERSIAN
 
    // User type label for the main button (dynamic last word)
    val userTypeLabel = when (userType) {
        "passenger" -> if (isPersian) "مسافر" else "Passenger"
        "driver" -> if (isPersian) "مسافربر و راننده" else "Driver"
        else -> if (isPersian) "مسافر" else "Passenger"
    }
    
    // Process UI state changes in a focused effect
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
    
    // Text content based on language - calculate once to avoid recompositions
    val headerText = if (isPersian) "خوش آمدید!" else "Welcome!"
    val subHeaderText = if (isPersian) 
        "لطفاً شماره موبایلی را وارد کنید که مسافربر یا راننده از طریق آن با شما در تماس یا هماهنگ باشد"
    else 
        "Please enter the phone number that the driver or passenger can use to contact or coordinate with you."
    val phoneLabel = if (isPersian) "شماره تلفن" else "Phone Number"
    val buttonText = if (isPersian) "ورود بعنوان $userTypeLabel" else "Login as $userTypeLabel"
    
    // Main content
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .statusBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Language selector button at the top
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(
                    onClick = { showLanguageSelector = true },
                    modifier = Modifier.wrapContentSize()
                ) {
                    Text(currentLanguage.displayName)
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        painter = painterResource(id = android.R.drawable.arrow_down_float),
                        contentDescription = LocalizedStrings.languageSelection
                    )
                }
            }
            
            // Header
            Text(
                text = headerText,
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 32.dp, bottom = 24.dp)
            )
            
            Text(
                text = subHeaderText,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 24.dp)
            )
            
            // Phone number input with RTL support
            OutlinedTextField(
                value = phoneNumber,
                onValueChange = { viewModel.updatePhoneNumber(it) },
                label = { Text(phoneLabel) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true,
                textStyle = LocalTextStyle.current.copy(
                    textDirection = textDirection,
                    textAlign = textAlign
                ),
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(spacerHeight24))
            
            // Submit button
            Button(
                onClick = { viewModel.submitPhoneNumber() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(buttonHeight),
                enabled = uiState !is PhoneNumberUiState.Loading
            ) {
                if (uiState is PhoneNumberUiState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(iconSize),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = strokeWidth
                    )
                } else {
                    Text(
                        text = buttonText,
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            }

            // User type selection
            UserTypeSelector(
                selectedUserType = userType,
                onUserTypeSelected = { viewModel.setUserType(it) }
            )
        }
        
        // Show modals conditionally to avoid unnecessary compositions
        if (showLanguageSelector) {
            LanguageSelectorModal(
                isVisible = true,
                onDismiss = { showLanguageSelector = false },
                currentLanguage = currentLanguage,
                onLanguageSelected = { languageManager.setLanguage(it) }
            )
        }
        
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
    // Access language manager
    val languageManager = LocalLanguageManager.current
    val currentLanguage by languageManager.languageState
    val isPersian = currentLanguage == Language.PERSIAN
    
    val alternateUserType = if (selectedUserType == "passenger") "driver" else "passenger"
    val alternateUserTypeLabel = if (alternateUserType == "passenger") {
        if (isPersian) "مسافر" else "Passenger"
    } else {
        if (isPersian) "مسافربر و راننده" else "Driver"
    }

    val loginText = if (isPersian) "ورود" else "Login"
    val asText = if (isPersian) " به عنوان $alternateUserTypeLabel" else " as $alternateUserTypeLabel"
    
    // Create the annotated string directly - don't wrap in remember with composable functions
    val annotatedText = buildAnnotatedString {
        withStyle(style = SpanStyle(fontSize = 18.sp)) {
            append(loginText)
        }
        withStyle(
            style = SpanStyle(
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        ) {
            append(asText)
        }
    }

    ClickableText(
        text = annotatedText,
        style = MaterialTheme.typography.bodySmall,
        modifier = Modifier.padding(top = 8.dp),
        onClick = { onUserTypeSelected(alternateUserType) }
    )
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