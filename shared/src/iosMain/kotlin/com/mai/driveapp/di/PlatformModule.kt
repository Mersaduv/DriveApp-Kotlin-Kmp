package com.mai.driveapp.di

import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module

/**
 * Platform-specific module implementation for iOS
 */
actual fun platformModule(): Module = module {
    // iOS-specific API URL (can be changed in app's build configuration)
    single(qualifier = named(API_URL_QUALIFIER)) {
        "localhost:5399" // Same URL as Android for now
    }
} 