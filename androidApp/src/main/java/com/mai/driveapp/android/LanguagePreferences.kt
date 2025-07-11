package com.mai.driveapp.android

import android.content.Context
import android.content.SharedPreferences
import com.mai.driveapp.Language

/**
 * Handles language preferences storage on Android
 */
class LanguagePreferences(context: Context) {
    private val sharedPreferences: SharedPreferences = 
        context.getSharedPreferences("language_preferences", Context.MODE_PRIVATE)
    
    companion object {
        private const val KEY_LANGUAGE_CODE = "language_code"
    }
    
    /**
     * Save the selected language
     */
    fun saveLanguage(language: Language) {
        sharedPreferences.edit().apply {
            putString(KEY_LANGUAGE_CODE, language.code)
            apply()
        }
    }
    
    /**
     * Get the saved language or return Persian as default
     */
    fun getLanguage(): Language {
        val languageCode = sharedPreferences.getString(KEY_LANGUAGE_CODE, Language.PERSIAN.code) ?: Language.PERSIAN.code
        return Language.fromCode(languageCode)
    }
} 