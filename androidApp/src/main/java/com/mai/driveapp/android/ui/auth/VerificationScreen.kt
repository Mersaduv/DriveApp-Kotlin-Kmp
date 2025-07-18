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
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mai.driveapp.auth.viewmodels.VerificationUiState
import com.mai.driveapp.auth.viewmodels.VerificationViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.TextRange
import com.mai.driveapp.android.LocalizedStrings

@Composable
fun VerificationScreen(
    viewModel: VerificationViewModel = koinViewModel(),
    prefillCode: String? = null,
    onNavigateToRegistration: () -> Unit,
    onBackToPhoneScreen: () -> Unit = {}
) {
    // Force LTR layout direction for the entire screen
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
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
                        text = LocalizedStrings.changeNumber,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable { onBackToPhoneScreen() }
                    )
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Header
                Text(
                    text = LocalizedStrings.verificationTitle,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = LocalizedStrings.verificationSubtitle,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 32.dp)
                )
                
                Spacer(modifier = Modifier.height(36.dp))
                Spacer(modifier = Modifier.height(32.dp))

                // Verification code input - NEW IMPLEMENTATION
                ImprovedVerificationCodeInput(
                    code = verificationCode,
                    onCodeChange = { viewModel.updateVerificationCode(it) },
                    length = 6,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                )
                
                Spacer(modifier = Modifier.height(32.dp))
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
                            LocalizedStrings.submitCode,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Resend code button and countdown
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isCountdownActive) {
                        Text(
                            LocalizedStrings.resendCodeIn(resendCountdown),
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
                                LocalizedStrings.resendCode,
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
}

@Composable
fun ImprovedVerificationCodeInput(
    code: String,
    onCodeChange: (String) -> Unit,
    length: Int = 6,
    modifier: Modifier = Modifier
) {
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    
    // Keep an internal TextFieldValue to control selection (cursor) position
    var textFieldValue by remember(code) {
        mutableStateOf(
            TextFieldValue(
                text = code,
                selection = TextRange(code.length)
            )
        )
    }
    
    // Update internal value when external code changes (e.g., auto-fill)
    LaunchedEffect(code) {
        textFieldValue = TextFieldValue(code, TextRange(code.length))
    }
    
    // Always force focus and show keyboard when composable appears
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        keyboardController?.show()
    }
    
    // Determine cursor position for indicator
    val cursorPosition = if (code.length >= length) length - 1 else code.length
    
    BasicTextField(
        value = textFieldValue,
        onValueChange = { newValue ->
            val filteredText = newValue.text.filter { it.isDigit() }.take(length)
            // Update the internal text field value with cursor at end
            textFieldValue = TextFieldValue(filteredText, TextRange(filteredText.length))
            onCodeChange(filteredText)
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(0.dp)
            .focusRequester(focusRequester),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        cursorBrush = SolidColor(Color.Transparent),
        textStyle = LocalTextStyle.current.copy(color = Color.Transparent),
        decorationBox = { innerTextField ->
            // Visual digit boxes
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        focusRequester.requestFocus()
                        keyboardController?.show()
                    },
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                for (i in 0 until length) {
                    val digit = if (i < code.length) code[i].toString() else ""
                    val isFilled = digit.isNotEmpty()
                    val showCursor = i == cursorPosition && code.length < length || (code.length == length && i == length - 1)
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
                                width = if (showCursor) 2.dp else 1.dp,
                                color = if (isFilled || showCursor) MaterialTheme.colorScheme.primary else Color.LightGray,
                                shape = RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isFilled) {
                            Text(
                                text = digit,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        } else if (showCursor) {
                            Box(
                                modifier = Modifier
                                    .width(2.dp)
                                    .height(24.dp)
                                    .alpha(0.7f)
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

@Preview(showBackground = true)
@Composable
fun VerificationScreenPreview() {
    MaterialTheme {
        VerificationScreen(onNavigateToRegistration = {})
    }
} 