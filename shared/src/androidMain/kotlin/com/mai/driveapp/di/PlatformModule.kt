package com.mai.driveapp.di

import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module

/**
 * Platform-specific module implementation for Android
 */
actual fun platformModule(): Module = module {
    // Android-specific API URL configuration
    single(qualifier = named(API_URL_QUALIFIER)) {
        // For Android Emulator: Use 10.0.2.2 to access host machine's localhost
        "http://10.0.2.2:5399"
        
        // For real device: Use your development machine's actual IP address on your network
        // Uncomment and use this instead when testing on a real device:
        // "http://192.168.x.x:5399"  // Replace with your actual IP address
        
        // NOTE: Make sure backend is running and accessible from this IP/port
    }
} 