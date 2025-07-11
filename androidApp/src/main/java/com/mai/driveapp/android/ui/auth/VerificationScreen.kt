package com.mai.driveapp.android.ui.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mai.driveapp.auth.viewmodels.VerificationUiState
import com.mai.driveapp.auth.viewmodels.VerificationViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
fun VerificationScreen(
    viewModel: VerificationViewModel = koinViewModel(),
    prefillCode: String? = null,
    onNavigateToRegistration: () -> Unit,
    onBackToPhoneScreen: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val verificationCode by viewModel.verificationCode.collectAsState()
    val shouldNavigateToRegistration by viewModel.shouldNavigateToRegistration.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var resendCountdown by remember { mutableStateOf(60) }
    var isCountdownActive by remember { mutableStateOf(false) }
    
    // راه‌حل جدید - پرچم انتقال محلی
    var navigateTriggered by remember { mutableStateOf(false) }
    
    // Countdown for resend button
    LaunchedEffect(isCountdownActive) {
        if (isCountdownActive) {
            while (resendCountdown > 0) {
                delay(1000)
                resendCountdown -= 1
            }
            isCountdownActive = false
        }
    }
    
    // Start countdown when screen is first shown
    LaunchedEffect(Unit) {
        isCountdownActive = true
    }
    
    // Pre-fill code if provided
    LaunchedEffect(prefillCode) {
        prefillCode?.let {
            viewModel.updateVerificationCode(it)
        }
    }
    
    // بهبود منطق ناوبری - برقراری چند مسیر برای اطمینان از انتقال
    
    // 1. با استفاده از پرچم shouldNavigateToRegistration از ViewModel
    LaunchedEffect(shouldNavigateToRegistration) {
        if (shouldNavigateToRegistration) {
            navigateTriggered = true
            onNavigateToRegistration()
            viewModel.onNavigationComplete()
        }
    }
    
    // 2. با استفاده از وضعیت UI
    LaunchedEffect(uiState) {
        when (uiState) {
            is VerificationUiState.Success -> {
                // برای اطمینان از عدم انتقال مکرر
                if (!navigateTriggered) {
                    delay(800) // تاخیر کوتاه
                    navigateTriggered = true
                    onNavigateToRegistration()
                }
            }
            is VerificationUiState.Error -> {
                errorMessage = (uiState as VerificationUiState.Error).message
                showError = true
                delay(3000) // Hide error after 3 seconds
                showError = false
            }
            else -> {
                showError = false
            }
        }
    }
    
    // 3. تلاش دوباره پس از مدتی کوتاه اگر وضعیت Success باشد
    LaunchedEffect(navigateTriggered, uiState) {
        if (uiState is VerificationUiState.Success && !navigateTriggered) {
            delay(1500) // تاخیر طولانی‌تر برای آخرین تلاش
            if (!navigateTriggered && uiState is VerificationUiState.Success) {
                navigateTriggered = true
                viewModel.forceNavigateToRegistration()
                onNavigateToRegistration()
            }
        }
    }
    
    // برای اطمینان از عملکرد صحیح
    DisposableEffect(uiState) {
        if (uiState is VerificationUiState.Success && !navigateTriggered) {
            viewModel.forceNavigateToRegistration()
        }
        onDispose { }
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        // Main content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { onBackToPhoneScreen() }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "بازگشت"
                    )
                }
                
                Spacer(modifier = Modifier.weight(1f))
                
                Text(
                    text = "تغییر شماره",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable { onBackToPhoneScreen() }
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Header
            Text(
                text = "تایید شماره تلفن",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "کد تایید ارسال شده را وارد کنید",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
            
            Spacer(modifier = Modifier.height(36.dp))
            
            // Verification code input
            VerificationCodeInput(
                code = verificationCode,
                onCodeChange = { viewModel.updateVerificationCode(it) },
                length = 6,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Submit button
            Button(
                onClick = { 
                    viewModel.submitVerificationCode()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = uiState !is VerificationUiState.Loading && verificationCode.length == 6,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                )
            ) {
                if (uiState is VerificationUiState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        "تایید کد",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Debug button or alternative navigation button
            if (uiState is VerificationUiState.Success || verificationCode.length == 6) {
                Button(
                    onClick = {
                        if (uiState !is VerificationUiState.Success) {
                            viewModel.submitVerificationCode()
                        }
                        onNavigateToRegistration()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Text(
                        "ادامه به صفحه ثبت‌نام",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // Resend code button and countdown
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isCountdownActive) {
                    Text(
                        text = "ارسال مجدد کد در $resendCountdown ثانیه",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    TextButton(
                        onClick = {
                            coroutineScope.launch {
                                // TODO: Implement resend code functionality
                                resendCountdown = 60
                                isCountdownActive = true
                            }
                        }
                    ) {
                        Text(
                            "ارسال مجدد کد",
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
        
        // Error message
        AnimatedVisibility(
            visible = showError,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 72.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                ),
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

@Composable
fun VerificationCodeInput(
    code: String,
    onCodeChange: (String) -> Unit,
    length: Int = 6,
    modifier: Modifier = Modifier
) {
    val focusRequester = remember { FocusRequester() }
    
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
    
    Column(modifier = modifier) {
        // Hidden text field for actual input
        OutlinedTextField(
            value = code,
            onValueChange = { newValue ->
                if (newValue.length <= length && newValue.all { it.isDigit() }) {
                    onCodeChange(newValue)
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier
                .fillMaxWidth()
                .height(0.dp)
                .focusRequester(focusRequester),
            singleLine = true
        )
        
        // Display boxes for digits
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            for (i in (length - 1) downTo 0) {  // Reversed for RTL layout
                val digitIndex = length - 1 - i
                val digit = if (digitIndex < code.length) code[digitIndex].toString() else ""
                val isFilled = digit.isNotEmpty()
                
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(1f)
                        .padding(horizontal = 4.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isFilled) MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                        .border(
                            width = 1.dp,
                            color = if (isFilled) MaterialTheme.colorScheme.primary 
                                   else MaterialTheme.colorScheme.outline,
                            shape = RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = digit,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isFilled) MaterialTheme.colorScheme.onPrimaryContainer
                               else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun VerificationScreenPreview() {
    MaterialTheme {
        VerificationScreen(onNavigateToRegistration = {})
    }
} 