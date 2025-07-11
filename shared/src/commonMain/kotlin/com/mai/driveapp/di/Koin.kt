package com.mai.driveapp.di

import com.mai.driveapp.auth.AuthRepository
import com.mai.driveapp.auth.AuthService
import com.mai.driveapp.auth.SessionManager
import com.mai.driveapp.auth.viewmodels.PhoneNumberViewModel
import com.mai.driveapp.auth.viewmodels.RegistrationViewModel
import com.mai.driveapp.auth.viewmodels.VerificationViewModel
import com.mai.driveapp.network.ApiClient
import com.mai.driveapp.network.DefaultTokenProvider
import org.koin.core.context.startKoin
import org.koin.core.qualifier.named
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

/**
 * Initialize Koin for dependency injection
 */
fun initKoin(appDeclaration: KoinAppDeclaration = {}) =
    startKoin {
        appDeclaration()
        modules(
            platformModule(),
            networkModule,
            authModule,
            viewModelsModule
        )
    }

/**
 * Platform-specific module to be implemented in androidMain and iosMain
 */
expect fun platformModule(): org.koin.core.module.Module

/**
 * Network related dependencies
 */
val networkModule = module {
    single {
        val apiUrl = get<String>(qualifier = named(API_URL_QUALIFIER))
        ApiClient(apiUrl, get<DefaultTokenProvider>())
    }
    
    single { DefaultTokenProvider() }
}

/**
 * Authentication related dependencies
 */
val authModule = module {
    single { AuthService(get()) }
    single { SessionManager(get()) }
    single { AuthRepository(get(), get()) }
}

/**
 * ViewModels for UI components
 */
val viewModelsModule = module {
    factory { PhoneNumberViewModel(get()) }
    factory { VerificationViewModel(get()) }
    factory { RegistrationViewModel(get()) }
}

/**
 * Qualifier for API base URL
 */
const val API_URL_QUALIFIER = "api_url" 