package com.mai.driveapp.android

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.State
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.mai.driveapp.Language
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

/**
 * Provides access to language settings within the Compose hierarchy
 */
val LocalLanguageManager = compositionLocalOf<AppLanguageManager> { 
    error("No LocalLanguageManager provided")
}

/**
 * Android implementation of LanguageManager that persists settings
 */
class AppLanguageManager(private val context: Context) {
    private val languagePreferences = LanguagePreferences(context)
    
    // Initialize with saved language preference
    private val _languageState = mutableStateOf(languagePreferences.getLanguage())
    val languageState: State<Language> = _languageState
    
    val currentLanguage: Language
        get() = _languageState.value
    
    /**
     * Set the application language and save the preference
     */
    fun setLanguage(language: Language) {
        _languageState.value = language
        languagePreferences.saveLanguage(language)
    }
}

/**
 * Provides language manager to the composition hierarchy
 */
@Composable
fun ProvideLanguageManager(content: @Composable () -> Unit) {
    val context = LocalContext.current
    val languageManager = remember { AppLanguageManager(context) }
    
    CompositionLocalProvider(LocalLanguageManager provides languageManager) {
        content()
    }
} 