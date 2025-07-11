package com.mai.driveapp.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.mai.driveapp.android.navigation.AuthNavGraph
import com.mai.driveapp.android.navigation.MainNavGraph
import com.mai.driveapp.auth.SessionManager
import com.mai.driveapp.di.initKoin
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.compose.koinViewModel

class MainActivity : ComponentActivity() {
    
    // Get session manager to check if user is logged in
    private lateinit var sessionManager: SessionManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize Koin AFTER super.onCreate()
        val koinApp = initKoin {
            androidContext(this@MainActivity.applicationContext)
        }
        
        // Now inject SessionManager after Koin is initialized
        sessionManager = inject<SessionManager>().value
        
        setContent {
            val isLoggedIn by sessionManager.isLoggedIn.collectAsState()
            val needsRegistration by sessionManager.needsRegistration.collectAsState()
            
            DriveAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (isLoggedIn && !needsRegistration) {
                        // User is fully logged in, show main app
                        MainNavGraph(
                            onLogout = {
                                sessionManager.logout()
                            }
                        )
                    } else {
                        // User needs authentication or registration completion
                        AuthNavGraph(
                            sessionManager = sessionManager,
                            onAuthenticationComplete = {
                                // این تابع فقط زمانی صدا زده می‌شود که کاربر کاملاً ثبت‌نام کرده باشد
                                // اطمینان حاصل می‌کنیم که فرآیند ثبت‌نام کامل شده است
                                sessionManager.completeRegistration()
                            }
                        )
                    }
                }
            }
        }
    }
}
