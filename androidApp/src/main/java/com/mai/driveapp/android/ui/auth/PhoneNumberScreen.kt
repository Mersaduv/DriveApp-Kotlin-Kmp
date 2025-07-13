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

    // Access language manager
    val languageManager = LocalLanguageManager.current
    val currentLanguage by languageManager.languageState

    // Determine text alignment and direction based on language
    val textAlign = if (currentLanguage.isRtl) TextAlign.Right else TextAlign.Left
    val textDirection = if (currentLanguage.isRtl) TextDirection.Rtl else TextDirection.Ltr

    // User type label for the main button (dynamic last word)
    val userTypeLabel = when (userType) {
        "passenger" -> if (currentLanguage == Language.PERSIAN) "مسافر" else "Passenger"
        "driver" -> if (currentLanguage == Language.PERSIAN) "مسافربر و راننده" else "Driver"
        else -> if (currentLanguage == Language.PERSIAN) "مسافر" else "Passenger"
    }

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
                .padding(16.dp)
                .statusBarsPadding(), // Add status bar padding to avoid overlap
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
                text = if (currentLanguage == Language.PERSIAN) "خوش آمدید!" else "Welcome!",
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 32.dp, bottom = 24.dp)
            )

            Text(
                text = if (currentLanguage == Language.PERSIAN)
                    "لطفاً شماره موبایلی را وارد کنید که مسافربر یا راننده از طریق آن با شما در تماس یا هماهنگ باشد"
                else
                    "Please enter the phone number that the driver or passenger can use to contact or coordinate with you.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Phone number input with RTL support
            OutlinedTextField(
                value = phoneNumber,
                onValueChange = { viewModel.updatePhoneNumber(it) },
                label = { Text(if (currentLanguage == Language.PERSIAN) "شماره تلفن" else "Phone Number") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true,
                textStyle = LocalTextStyle.current.copy(
                    textDirection = textDirection,
                    textAlign = textAlign
                ),
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
                    Text(
                        if (currentLanguage == Language.PERSIAN)
                            "ورود بعنوان $userTypeLabel"
                        else
                            "Login as $userTypeLabel",
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            }

            // User type selection as small clickable text below the button
            UserTypeSelector(
                selectedUserType = userType,
                onUserTypeSelected = { viewModel.setUserType(it) }
            )
        }

        // Language selector modal
        LanguageSelectorModal(
            isVisible = showLanguageSelector,
            onDismiss = { showLanguageSelector = false },
            currentLanguage = currentLanguage,
            onLanguageSelected = { languageManager.setLanguage(it) }
        )

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
    val languageManager = LocalLanguageManager.current
    val currentLanguage by languageManager.languageState

    val alternateUserType = if (selectedUserType == "passenger") "driver" else "passenger"
    val alternateUserTypeLabel = if (alternateUserType == "passenger") {
        if (currentLanguage == Language.PERSIAN) "مسافر" else "Passenger"
    } else {
        if (currentLanguage == Language.PERSIAN) "مسافربر و راننده" else "Driver"
    }

    val annotatedText = buildAnnotatedString {
        withStyle(style = SpanStyle(fontSize = 18.sp)) {
            append(if (currentLanguage == Language.PERSIAN) "ورود" else "Login")
        }
        withStyle(
            style = SpanStyle(
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        ) {
            append(if (currentLanguage == Language.PERSIAN) " به عنوان $alternateUserTypeLabel" else " as $alternateUserTypeLabel")
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