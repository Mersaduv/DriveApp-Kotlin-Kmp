package com.mai.driveapp.android

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import com.mai.driveapp.auth.AuthService
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

private const val TAG = "CustomPhoneAuthScreen"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomPhoneAuthScreen(
    onAuthSuccess: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val languageManager = LocalLanguageManager.current
    
    // AuthService برای ارتباط با API بک‌اند
    val authService = remember { 
        AuthService() 
    }
    
    var phoneNumber by remember { mutableStateOf("") }
    var verificationCode by remember { mutableStateOf("") }
    var isCodeSent by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var fullName by remember { mutableStateOf("") }
    var showNameInput by remember { mutableStateOf(false) }
    var validationError by remember { mutableStateOf<String?>(null) }
    var resendTimer by remember { mutableStateOf(0L) }
    var canResend by remember { mutableStateOf(true) }
    var showLanguageSelector by remember { mutableStateOf(false) }
    var sessionId by remember { mutableStateOf<String?>(null) }

    // Create notification channel for API >= 26
    LaunchedEffect(Unit) {
        createNotificationChannel(context)
    }
    
    // For validation error messages - remember them in composable context
    val phoneNumberMustBe10Digits = LocalizedStrings.phoneNumberMustBe10Digits
    val phoneNumberMustStartWith07 = LocalizedStrings.phoneNumberMustStartWith07
    val verificationCodeSent = LocalizedStrings.verificationCodeSent
    val pleaseEnterFullName = LocalizedStrings.pleaseEnterFullName
    
    // Request notification permission for Android 13+
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Log.d(TAG, "Notification permission granted")
        } else {
            Log.d(TAG, "Notification permission denied")
            Toast.makeText(context, "Notification permission is needed to show verification codes", Toast.LENGTH_LONG).show()
        }
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
    
    // Language selector modal
    LanguageSelectorModal(
        isVisible = showLanguageSelector,
        onDismiss = { showLanguageSelector = false },
        currentLanguage = languageManager.currentLanguage,
        onLanguageSelected = { language ->
            languageManager.setLanguage(language)
        }
    )
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (!isCodeSent) {
            // Phone number input screen
            Text(
                text = LocalizedStrings.phoneAuthentication,
                style = MaterialTheme.typography.headlineMedium
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Language selector button
            Button(
                onClick = { showLanguageSelector = true },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(LocalizedStrings.languageSelection)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = phoneNumber,
                onValueChange = { 
                    // Only allow digits and limit to 10 characters
                    if (it.all { char -> char.isDigit() } && it.length <= 10) {
                        phoneNumber = it
                        validationError = null
                    }
                },
                label = { Text(LocalizedStrings.enterPhoneNumber) },
                placeholder = { Text(LocalizedStrings.phoneNumberHint) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth(),
                isError = validationError != null
            )
            
            if (validationError != null) {
                Text(
                    text = validationError!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .align(Alignment.Start)
                        .padding(start = 16.dp, top = 4.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = LocalizedStrings.afghanistanPrefix,
                style = MaterialTheme.typography.bodySmall
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = {
                    // Validate phone number format
                    if (phoneNumber.length != 10) {
                        validationError = phoneNumberMustBe10Digits
                        return@Button
                    }
                    
                    if (!phoneNumber.startsWith("07")) {
                        validationError = phoneNumberMustStartWith07
                        return@Button
                    }
                    
                    isLoading = true
                    
                    // Call API to send verification code
                    scope.launch {
                        val response = authService.sendPhoneNumber(phoneNumber)
                        isLoading = false
                        
                        if (response.success && response.sessionId != null) {
                            sessionId = response.sessionId
                            isCodeSent = true
                            verificationCode = ""
                            
                            // Start resend timer
                            canResend = false
                            resendTimer = 60L // 60 seconds
                            
                            Toast.makeText(context, verificationCodeSent, Toast.LENGTH_SHORT).show()
                        } else {
                            validationError = response.message
                            Toast.makeText(context, response.message, Toast.LENGTH_LONG).show()
                        }
                    }
                },
                enabled = !isLoading && phoneNumber.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(LocalizedStrings.sendVerificationCode)
                }
            }
        } else if (!showNameInput) {
            // Verification code screen
            Text(
                text = LocalizedStrings.enterVerificationCode,
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = LocalizedStrings.verificationSentToPhone(phoneNumber), 
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = LocalizedStrings.isPhoneNumberWrong,
                    style = MaterialTheme.typography.bodyMedium
                )
                
                TextButton(onClick = {
                    isCodeSent = false
                    verificationCode = ""
                }) {
                    Text(
                        text = LocalizedStrings.edit,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // OTP input fields
            OtpInputField(
                otpText = verificationCode,
                onOtpTextChange = { value, isComplete ->
                    verificationCode = value
                    if (isComplete && value.length == 6) {
                        // Verify code
                        isLoading = true
                        
                        // Call API to verify code
                        scope.launch {
                            // Ensure sessionId is not null
                            val currentSessionId = sessionId
                            if (currentSessionId == null) {
                                Toast.makeText(context, "Session ID is missing, please try again", Toast.LENGTH_LONG).show()
                                isLoading = false
                                return@launch
                            }
                            
                            val response = authService.verifyCode(currentSessionId, value)
                            
                            if (response.success) {
                                if (response.requiresRegistration) {
                                    // Profile creation required
                                    isLoading = false
                                    showNameInput = true
                                } else {
                                    // Already has a profile, get token
                                    val profileResponse = authService.createProfile(currentSessionId, "")
                                    isLoading = false
                                    
                                    if (profileResponse.success && profileResponse.token != null) {
                                        // استفاده از !! برای اطمینان به کامپایلر که مقدار null نیست
                                        onAuthSuccess(profileResponse.token!!)
                                    } else {
                                        Toast.makeText(context, profileResponse.message, Toast.LENGTH_LONG).show()
                                    }
                                }
                            } else {
                                isLoading = false
                                Toast.makeText(context, response.message, Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                }
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Resend code timer
            LaunchedEffect(resendTimer, canResend) {
                if (!canResend && resendTimer > 0) {
                    delay(1000)
                    resendTimer -= 1
                    if (resendTimer == 0L) {
                        canResend = true
                    }
                }
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = LocalizedStrings.resendCodeCountdown,
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Text(
                    text = String.format("%02d:%02d", 
                        TimeUnit.SECONDS.toMinutes(resendTimer) % 60,
                        resendTimer % 60
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            if (canResend) {
                Spacer(modifier = Modifier.height(16.dp))
                
                TextButton(
                    onClick = {
                        // Resend code - call API
                        isLoading = true
                        scope.launch {
                            val response = authService.sendPhoneNumber(phoneNumber)
                            isLoading = false
                            
                            if (response.success && response.sessionId != null) {
                                sessionId = response.sessionId
                                verificationCode = ""
                                
                                // Reset timer
                                canResend = false
                                resendTimer = 60L
                                
                                Toast.makeText(context, verificationCodeSent, Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, response.message, Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                ) {
                    Text(LocalizedStrings.resendCode)
                }
            }
            
            if (isLoading) {
                Spacer(modifier = Modifier.height(16.dp))
                CircularProgressIndicator()
            }
        } else {
            // Full name input screen
            Text(
                text = LocalizedStrings.enterFullName,
                style = MaterialTheme.typography.headlineMedium
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = fullName,
                onValueChange = { fullName = it },
                label = { Text(LocalizedStrings.fullName) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = {
                    if (fullName.isBlank()) {
                        Toast.makeText(context, pleaseEnterFullName, Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    
                    isLoading = true
                    
                    // Call API to create profile and get token
                    scope.launch {
                        // Ensure sessionId is not null
                        val currentSessionId = sessionId
                        if (currentSessionId == null) {
                            Toast.makeText(context, "Session ID is missing, please try again", Toast.LENGTH_LONG).show()
                            isLoading = false
                            return@launch
                        }
                        
                        val response = authService.createProfile(currentSessionId, fullName)
                        isLoading = false
                        
                        if (response.success && response.token != null) {
                            // استفاده از !! برای اطمینان به کامپایلر که مقدار null نیست
                            onAuthSuccess(response.token!!)
                        } else {
                            Toast.makeText(context, response.message, Toast.LENGTH_LONG).show()
                        }
                    }
                },
                enabled = !isLoading && fullName.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(LocalizedStrings.continue_)
                }
            }
        }
    }
}

@Composable
fun OtpInputField(
    otpText: String,
    onOtpTextChange: (String, Boolean) -> Unit
) {
    val maxLength = 6
    var otpTextValue by remember {
        mutableStateOf(
            TextFieldValue(
                text = otpText,
                selection = TextRange(otpText.length)
            )
        )
    }
    
    val focusRequester = remember { FocusRequester() }
    
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
    
    BasicTextField(
        value = otpTextValue,
        onValueChange = { value ->
            if (value.text.length <= maxLength && value.text.all { it.isDigit() }) {
                otpTextValue = value.copy(selection = TextRange(value.text.length))
                onOtpTextChange(value.text, value.text.length == maxLength)
            }
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier.focusRequester(focusRequester),
        textStyle = TextStyle(
            fontSize = 1.sp,
            color = Color.Transparent
        ),
        decorationBox = { innerTextField ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                repeat(maxLength) { index ->
                    val char = when {
                        index >= otpTextValue.text.length -> ""
                        else -> otpTextValue.text[index].toString()
                    }
                    val isFocused = otpTextValue.text.length == index
                    
                    Box(
                        modifier = Modifier
                            .width(40.dp)
                            .height(48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Underline
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                                .height(2.dp)
                                .background(
                                    if (isFocused) MaterialTheme.colorScheme.primary
                                    else Color.Gray
                                )
                        )
                        
                        // Text
                        if (char.isNotEmpty()) {
                            Text(
                                text = char,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        } else if (isFocused) {
                            // Cursor
                            Box(
                                modifier = Modifier
                                    .width(2.dp)
                                    .height(24.dp)
                                    .background(MaterialTheme.colorScheme.primary)
                            )
                        }
                    }
                }
            }
            innerTextField()
        }
    )
}

private fun createNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val name = "Verification Codes"
        val descriptionText = "Channel for verification codes"
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel("VERIFICATION_CHANNEL", name, importance).apply {
            description = descriptionText
        }
        
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
        Log.d(TAG, "Notification channel created")
    }
}

private fun showVerificationCodeNotification(context: Context, code: String) {
    Log.d(TAG, "Attempting to show notification for code: $code")
    
    val builder = NotificationCompat.Builder(context, "VERIFICATION_CHANNEL")
        .setSmallIcon(android.R.drawable.ic_dialog_info)
        .setContentTitle("Verification Code")
        .setContentText("Your verification code is: $code")
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setAutoCancel(true)
    
    try {
        with(NotificationManagerCompat.from(context)) {
            // Check for permission for Android 13+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    Log.e(TAG, "Notification permission not granted")
                    Toast.makeText(context, "Notification permission required", Toast.LENGTH_LONG).show()
                    return
                }
            }
            notify(1001, builder.build())
            Log.d(TAG, "Notification displayed successfully")
        }
    } catch (e: SecurityException) {
        // Handle the permission exception
        Log.e(TAG, "Security exception when showing notification", e)
        Toast.makeText(context, "Notification permission required: ${e.message}", Toast.LENGTH_LONG).show()
    } catch (e: Exception) {
        // Handle any other exceptions
        Log.e(TAG, "Exception when showing notification", e)
        Toast.makeText(context, "Error showing notification: ${e.message}", Toast.LENGTH_LONG).show()
    }
} 