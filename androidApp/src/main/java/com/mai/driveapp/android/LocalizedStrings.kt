package com.mai.driveapp.android

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.getValue
import com.mai.driveapp.Language

/**
 * Provides localized strings based on the current language setting
 */
object LocalizedStrings {
    @Composable
    @ReadOnlyComposable
    private fun currentLanguage(): Language {
        val languageManager = LocalLanguageManager.current
        val language by languageManager.languageState
        return language
    }
    
    // App generic strings
    val appName: String
        @Composable
        @ReadOnlyComposable
        get() = when(currentLanguage()) {
            Language.PERSIAN -> "نرم افزار MAi Drive"
            Language.ENGLISH -> "MAi Drive App"
        }
    
    // Authentication strings
    val phoneAuthentication: String
        @Composable
        @ReadOnlyComposable
        get() = when(currentLanguage()) {
            Language.PERSIAN -> "احراز هویت با تلفن همراه"
            Language.ENGLISH -> "Phone Authentication"
        }
    
    val enterPhoneNumber: String
        @Composable
        @ReadOnlyComposable
        get() = when(currentLanguage()) {
            Language.PERSIAN -> "شماره موبایل خود را وارد کنید"
            Language.ENGLISH -> "Enter your phone number"
        }
    
    val phoneNumberHint: String
        @Composable
        @ReadOnlyComposable
        get() = when(currentLanguage()) {
            Language.PERSIAN -> "مثال: 0700123456"
            Language.ENGLISH -> "Example: 0700123456"
        }
    
    val afghanistanPrefix: String
        @Composable
        @ReadOnlyComposable
        get() = when(currentLanguage()) {
            Language.PERSIAN -> "پیش‌شماره افغانستان (+93) به صورت خودکار اضافه می‌شود"
            Language.ENGLISH -> "Afghanistan (+93) will be added automatically"
        }
    
    val sendVerificationCode: String
        @Composable
        @ReadOnlyComposable
        get() = when(currentLanguage()) {
            Language.PERSIAN -> "ارسال کد تأیید"
            Language.ENGLISH -> "Send Verification Code"
        }
    
    val enterVerificationCode: String
        @Composable
        @ReadOnlyComposable
        get() = when(currentLanguage()) {
            Language.PERSIAN -> "کد تأیید را وارد کنید"
            Language.ENGLISH -> "Enter Verification Code"
        }
    
    @Composable
    @ReadOnlyComposable
    fun verificationSentToPhone(phoneNumber: String): String {
        return when(currentLanguage()) {
            Language.PERSIAN -> "کد تأیید را با پیامک به شمارهٔ $phoneNumber فرستادیم."
            Language.ENGLISH -> "Verification code was sent to $phoneNumber."
        }
    }
    
    val isPhoneNumberWrong: String
        @Composable
        @ReadOnlyComposable
        get() = when(currentLanguage()) {
            Language.PERSIAN -> "شمارهٔ موبایل اشتباه است؟"
            Language.ENGLISH -> "Wrong phone number?"
        }
    
    val edit: String
        @Composable
        @ReadOnlyComposable
        get() = when(currentLanguage()) {
            Language.PERSIAN -> "ویرایش"
            Language.ENGLISH -> "Edit"
        }
    
    val resendCodeCountdown: String
        @Composable
        @ReadOnlyComposable
        get() = when(currentLanguage()) {
            Language.PERSIAN -> "ارسال دوبارهٔ کد تأیید تا "
            Language.ENGLISH -> "Resend code in "
        }
    
    val resendCode: String
        @Composable
        @ReadOnlyComposable
        get() = when(currentLanguage()) {
            Language.PERSIAN -> "ارسال مجدد کد"
            Language.ENGLISH -> "Resend Code"
        }
    
    // Full name strings
    val enterFullName: String
        @Composable
        @ReadOnlyComposable
        get() = when(currentLanguage()) {
            Language.PERSIAN -> "نام و نام خانوادگی خود را وارد کنید"
            Language.ENGLISH -> "Enter Your Full Name"
        }
    
    val fullName: String
        @Composable
        @ReadOnlyComposable
        get() = when(currentLanguage()) {
            Language.PERSIAN -> "نام و نام خانوادگی"
            Language.ENGLISH -> "Full Name"
        }
    
    val continue_: String
        @Composable
        @ReadOnlyComposable
        get() = when(currentLanguage()) {
            Language.PERSIAN -> "ادامه"
            Language.ENGLISH -> "Continue"
        }
    
    // Drawer menu strings
    val languageSelection: String
        @Composable
        @ReadOnlyComposable
        get() = when(currentLanguage()) {
            Language.PERSIAN -> "انتخاب زبان"
            Language.ENGLISH -> "Select Language"
        }
    
    val logout: String
        @Composable
        @ReadOnlyComposable
        get() = when(currentLanguage()) {
            Language.PERSIAN -> "خروج از حساب کاربری"
            Language.ENGLISH -> "Logout"
        }
    
    val welcome: String
        @Composable
        @ReadOnlyComposable
        get() = when(currentLanguage()) {
            Language.PERSIAN -> "به MAi Drive خوش آمدید!"
            Language.ENGLISH -> "Welcome to MAi Drive!"
        }
    
    val loginSuccess: String
        @Composable
        @ReadOnlyComposable
        get() = when(currentLanguage()) {
            Language.PERSIAN -> "شما با موفقیت وارد سیستم شده‌اید"
            Language.ENGLISH -> "You have successfully logged in"
        }
    
    // Validation error messages
    val phoneNumberMustBe10Digits: String
        @Composable
        @ReadOnlyComposable
        get() = when(currentLanguage()) {
            Language.PERSIAN -> "شماره موبایل باید دقیقاً ۱۰ رقم باشد"
            Language.ENGLISH -> "Phone number must be exactly 10 digits"
        }
    
    val phoneNumberMustStartWith07: String
        @Composable
        @ReadOnlyComposable
        get() = when(currentLanguage()) {
            Language.PERSIAN -> "شماره موبایل باید با ۰۷ شروع شود"
            Language.ENGLISH -> "Phone number must start with 07"
        }
    
    val pleaseEnterFullName: String
        @Composable
        @ReadOnlyComposable
        get() = when(currentLanguage()) {
            Language.PERSIAN -> "لطفاً نام و نام خانوادگی خود را وارد کنید"
            Language.ENGLISH -> "Please enter your full name"
        }
    
    // Toast messages
    val verificationCodeSent: String
        @Composable
        @ReadOnlyComposable
        get() = when(currentLanguage()) {
            Language.PERSIAN -> "کد تأیید ارسال شد"
            Language.ENGLISH -> "Verification code sent"
        }
    
    val loginSuccessful: String
        @Composable
        @ReadOnlyComposable
        get() = when(currentLanguage()) {
            Language.PERSIAN -> "ورود موفقیت‌آمیز!"
            Language.ENGLISH -> "Login successful!"
        }
    
    val logoutSuccessful: String
        @Composable
        @ReadOnlyComposable
        get() = when(currentLanguage()) {
            Language.PERSIAN -> "با موفقیت خارج شدید"
            Language.ENGLISH -> "Successfully logged out"
        }
    
    // Button to save language selection
    val save: String
        @Composable
        @ReadOnlyComposable
        get() = when(currentLanguage()) {
            Language.PERSIAN -> "ثبت"
            Language.ENGLISH -> "Apply"
        }
} 