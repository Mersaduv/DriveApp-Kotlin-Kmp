package com.mai.driveapp.android.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

/**
 * Main navigation component for the app after authentication
 */
@Composable
fun MainNavGraph(
    navController: NavHostController = rememberNavController(),
    onLogout: () -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = MainDestinations.HOME
    ) {
        composable(MainDestinations.HOME) {
            HomeScreen(onLogout = onLogout)
        }
        
        // Add other main app screens here
    }
}

/**
 * Placeholder home screen
 */
@Composable
fun HomeScreen(onLogout: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "به تاپی خوش آمدید",
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "شما با موفقیت وارد شدید!",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Button(onClick = onLogout) {
                Text("خروج")
            }
        }
    }
}

/**
 * Main app navigation destinations
 */
object MainDestinations {
    const val HOME = "home"
} 