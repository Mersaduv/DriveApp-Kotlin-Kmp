package com.mai.driveapp.android.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.mai.driveapp.android.ui.auth.PhoneNumberScreen
import com.mai.driveapp.android.ui.auth.RegistrationScreen
import com.mai.driveapp.android.ui.auth.VerificationScreen
import com.mai.driveapp.auth.SessionManager
import com.mai.driveapp.auth.viewmodels.VerificationUiState
import com.mai.driveapp.auth.viewmodels.VerificationViewModel
import kotlinx.coroutines.delay
import org.koin.androidx.compose.get
import org.koin.androidx.compose.koinViewModel

/**
 * Main navigation component for authentication flow
 */
@Composable
fun AuthNavGraph(
    navController: NavHostController = rememberNavController(),
    onAuthenticationComplete: () -> Unit,
    sessionManager: SessionManager = get()
) {
    NavHost(
        navController = navController,
        startDestination = AuthDestinations.PHONE_NUMBER
    ) {
        // Phone number screen
        composable(AuthDestinations.PHONE_NUMBER) {
            PhoneNumberScreen(
                onNavigateToVerification = { code ->
                    navController.navigate(AuthDestinations.verificationRoute(code)) {
                        popUpTo(AuthDestinations.PHONE_NUMBER) { inclusive = true }
                    }
                }
            )
        }
        
        // Verification screen
        composable(
            route = AuthDestinations.VERIFICATION_ROUTE_WITH_CODE,
            arguments = listOf(
                navArgument(AuthDestinations.VERIFICATION_CODE_ARG) {
                    type = NavType.StringType
                    nullable = true
                }
            )
        ) { backStackEntry ->
            val code = backStackEntry.arguments?.getString(AuthDestinations.VERIFICATION_CODE_ARG)
            val viewModel = koinViewModel<VerificationViewModel>()
            val uiState by viewModel.uiState.collectAsState()
            val shouldNavigate by viewModel.shouldNavigateToRegistration.collectAsState()
            
            // راه‌حل 1: بررسی وضعیت موفقیت برای هدایت به صفحه ثبت‌نام
            LaunchedEffect(uiState) {
                if (uiState is VerificationUiState.Success) {
                    delay(500)  // تاخیر کوتاه برای اطمینان از تکمیل فرآیندهای قبلی
                    navController.navigate(AuthDestinations.REGISTRATION) {
                        popUpTo(AuthDestinations.VERIFICATION_ROUTE_WITH_CODE) { inclusive = true }
                    }
                }
            }
            
            // راه‌حل 2: استفاده از پرچم برای هدایت به صفحه ثبت‌نام
            LaunchedEffect(shouldNavigate) {
                if (shouldNavigate) {
                    navController.navigate(AuthDestinations.REGISTRATION) {
                        popUpTo(AuthDestinations.VERIFICATION_ROUTE_WITH_CODE) { inclusive = true }
                    }
                    viewModel.onNavigationComplete()
                }
            }

            // راه‌حل 3: بررسی وضعیت کاربر برای تعیین مرحله بعدی
            LaunchedEffect(Unit) {
                // اگر کاربر به اشتباه به این صفحه آمده و قبلاً نیاز به تکمیل ثبت‌نام دارد، مستقیم هدایت کن
                val userId = sessionManager.getVerificationUserId()
                if (userId != null) {
                    navController.navigate(AuthDestinations.REGISTRATION) {
                        popUpTo(AuthDestinations.VERIFICATION_ROUTE_WITH_CODE) { inclusive = true }
                    }
                }
            }
            
            VerificationScreen(
                viewModel = viewModel,
                prefillCode = code,
                onNavigateToRegistration = {
                    // راه‌حل 4: دکمه هدایت مستقیم در UI
                    navController.navigate(AuthDestinations.REGISTRATION) {
                        popUpTo(AuthDestinations.VERIFICATION_ROUTE_WITH_CODE) { inclusive = true }
                    }
                },
                onBackToPhoneScreen = {
                    navController.navigate(AuthDestinations.PHONE_NUMBER) {
                        // Clear everything up to PHONE_NUMBER from the back stack
                        popUpTo(AuthDestinations.PHONE_NUMBER) { inclusive = true }
                    }
                }
            )
        }
        
        // Registration screen
        composable(AuthDestinations.REGISTRATION) {
            RegistrationScreen(
                onNavigateToHome = {
                    onAuthenticationComplete()
                },
                onBackPressed = {
                    // We want to keep the user in the registration flow once they're here
                    // So don't allow navigation back to verification
                }
            )
        }
    }
}

/**
 * Authentication navigation destinations
 */
object AuthDestinations {
    const val AUTH_ROUTE = "auth"
    const val PHONE_NUMBER = "auth/phone"
    const val VERIFICATION_ROUTE_WITH_CODE = "auth/verification?code={code}"
    const val VERIFICATION_CODE_ARG = "code"
    const val REGISTRATION = "auth/registration"
    
    fun verificationRoute(code: String): String {
        return "auth/verification?code=$code"
    }
} 