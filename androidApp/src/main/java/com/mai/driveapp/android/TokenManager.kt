package com.mai.driveapp.android

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.util.Date

private const val TAG = "TokenManager"
private const val PREFS_FILE_NAME = "auth_prefs"
private const val KEY_TOKEN = "jwt_token"
private const val KEY_EXPIRY = "token_expiry"

/**
 * مدیریت کننده توکن JWT در برنامه
 * شامل قابلیت‌های ذخیره‌سازی، بازیابی و بررسی انقضاء توکن
 */
class TokenManager(private val context: Context) {

    private val sharedPreferences: SharedPreferences by lazy {
        try {
            // تلاش برای استفاده از EncryptedSharedPreferences برای امنیت بیشتر
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            EncryptedSharedPreferences.create(
                context,
                PREFS_FILE_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            // در صورت خطا، از SharedPreferences معمولی استفاده می‌کنیم
            Log.e(TAG, "Error creating EncryptedSharedPreferences, falling back to regular SharedPreferences", e)
            context.getSharedPreferences(PREFS_FILE_NAME, Context.MODE_PRIVATE)
        }
    }

    /**
     * ذخیره توکن JWT و زمان انقضای آن
     * @param token توکن JWT
     * @param expiryTimeMillis زمان انقضا به میلی‌ثانیه
     */
    fun saveToken(token: String, expiryTimeMillis: Long = System.currentTimeMillis() + DEFAULT_EXPIRY_TIME) {
        sharedPreferences.edit().apply {
            putString(KEY_TOKEN, token)
            putLong(KEY_EXPIRY, expiryTimeMillis)
            apply()
        }
        Log.d(TAG, "Token saved with expiry at ${Date(expiryTimeMillis)}")
    }

    /**
     * بازیابی توکن JWT اگر معتبر باشد
     * @return توکن JWT اگر وجود داشته باشد و منقضی نشده باشد، در غیر این صورت null
     */
    fun getValidToken(): String? {
        val token = sharedPreferences.getString(KEY_TOKEN, null)
        val expiryTime = sharedPreferences.getLong(KEY_EXPIRY, 0)

        return if (token != null && expiryTime > System.currentTimeMillis()) {
            Log.d(TAG, "Valid token found, expires at ${Date(expiryTime)}")
            token
        } else {
            if (token != null) {
                Log.d(TAG, "Token found but expired at ${Date(expiryTime)}")
            }
            null
        }
    }

    /**
     * حذف توکن ذخیره شده (برای خروج از سیستم)
     */
    fun clearToken() {
        sharedPreferences.edit().apply {
            remove(KEY_TOKEN)
            remove(KEY_EXPIRY)
            apply()
        }
        Log.d(TAG, "Token cleared")
    }

    /**
     * بررسی اینکه آیا توکن معتبر است یا خیر
     * @return true اگر توکن وجود داشته باشد و منقضی نشده باشد
     */
    fun hasValidToken(): Boolean {
        return getValidToken() != null
    }

    companion object {
        // زمان انقضای پیش‌فرض: 7 روز
        const val DEFAULT_EXPIRY_TIME = 7 * 24 * 60 * 60 * 1000L
    }
} 