# MAi Drive App

A ride-hailing application with multi-language support (English and Persian) and RTL layout capability.

## Features

### Phone Authentication
- Custom OTP-based authentication
- Phone number validation for Afghan numbers (10 digits, starting with "07")
- OTP verification with time-limited resend option
- User profile creation with full name

### Multi-language Support
- Supports both English and Persian languages
- Dynamically switches between LTR and RTL layouts
- Language selection available in drawer menu and on first screen
- Persistent language preferences across app sessions

### Secure Token Management
- Encrypted token storage using EncryptedSharedPreferences
- Automatic token expiration after 7 days
- Auto-login for users with valid tokens

## Implementation Details

### Language Management
- Uses a central `LanguageManager` to track current language
- Composition-based language provider for Compose UI components
- Automatic RTL/LTR layout switching based on selected language
- Bottom sheet modal for language selection

### UI Components
- Material 3 design throughout the application
- Custom OTP input field with automatic focus and navigation
- Drawer menu for app navigation and language/logout options
- Proper error handling and validation messages in both languages

## How to Run
1. Make sure you have Android Studio installed with JDK 17+
2. Open the project in Android Studio
3. Run the app on an emulator or physical device 