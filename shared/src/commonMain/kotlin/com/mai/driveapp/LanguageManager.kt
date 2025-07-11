package com.mai.driveapp

/**
 * Language options for the application
 */
enum class Language(val code: String, val displayName: String, val isRtl: Boolean) {
    ENGLISH("en", "English", false),
    PERSIAN("fa", "فارسی", true);
    
    companion object {
        fun fromCode(code: String): Language {
            return values().find { it.code == code } ?: PERSIAN // Default to Persian
        }
    }
}

/**
 * A simpler language manager that doesn't use coroutines
 */
class LanguageManager {
    // Current language
    private var _currentLanguage: Language = Language.PERSIAN
    
    // Listeners
    private val listeners = mutableListOf<(Language) -> Unit>()
    
    // Get current language
    val currentLanguage: Language
        get() = _currentLanguage
    
    /**
     * Set the application language
     */
    fun setLanguage(language: Language) {
        _currentLanguage = language
        // Notify listeners
        listeners.forEach { it(language) }
    }
    
    /**
     * Add a listener for language changes
     */
    fun addListener(listener: (Language) -> Unit) {
        listeners.add(listener)
    }
    
    /**
     * Remove a listener
     */
    fun removeListener(listener: (Language) -> Unit) {
        listeners.remove(listener)
    }
} 