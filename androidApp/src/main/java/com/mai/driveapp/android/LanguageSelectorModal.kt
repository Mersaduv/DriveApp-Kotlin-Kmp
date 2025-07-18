package com.mai.driveapp.android

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mai.driveapp.Language

// Fixed dimensions to prevent recreations
private val topPadding = 16.dp
private val topCornerRadius = 16.dp
private val headerFontSize = 20.sp
private val closeFontSize = 24.sp
private val verticalPadding = 24.dp
private val cardVerticalPadding = 4.dp
private val rowPadding = Modifier.padding(vertical = 12.dp, horizontal = 16.dp)
private val buttonHeight = 56.dp
private val buttonFontSize = 18.sp
private val titleFontSize = 18.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageSelectorModal(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    currentLanguage: Language,
    onLanguageSelected: (Language) -> Unit
) {
    if (isVisible) {
        // Use temporary state for language selection
        var selectedLanguage by remember { mutableStateOf(currentLanguage) }
        
        // Retrieve static texts to avoid recompositions
        val saveText = LocalizedStrings.save
        val languageSelectionTitle = LocalizedStrings.languageSelection
        
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            shape = RoundedCornerShape(topStart = topCornerRadius, topEnd = topCornerRadius),
            containerColor = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Placeholder for layout balance
                    Spacer(Modifier.width(24.dp))
                    
                    // Title - stable reference
                    Text(
                        text = languageSelectionTitle,
                        fontSize = headerFontSize,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                    
                    // Close button - minimize allocations
                    IconButton(onClick = onDismiss) {
                        Text("✕", fontSize = closeFontSize)
                    }
                }
                
                Spacer(modifier = Modifier.height(verticalPadding))
                
                // Language options - use remember for stable references
                LanguageOption(
                    language = Language.ENGLISH,
                    isSelected = selectedLanguage == Language.ENGLISH,
                    onSelect = { selectedLanguage = Language.ENGLISH }
                )
                
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                
                LanguageOption(
                    language = Language.PERSIAN,
                    isSelected = selectedLanguage == Language.PERSIAN,
                    onSelect = { selectedLanguage = Language.PERSIAN }
                )
                
                Spacer(modifier = Modifier.height(verticalPadding))
                
                // Submit button
                Button(
                    onClick = { 
                        // Apply language change only when button is clicked
                        onLanguageSelected(selectedLanguage)
                        onDismiss() 
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(buttonHeight),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        text = saveText,
                        fontSize = buttonFontSize
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun LanguageOption(
    language: Language,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    // Make entire row clickable and stable
    val backgroundColor = if (isSelected) 
        MaterialTheme.colorScheme.surfaceVariant 
    else 
        MaterialTheme.colorScheme.surface
        
    Card(
        onClick = onSelect,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = cardVerticalPadding),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        )
    ) {
        Row(
            modifier = rowPadding
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = language.displayName,
                fontSize = titleFontSize
            )
            
            RadioButton(
                selected = isSelected,
                onClick = null, // The onClick is handled by the Card
                colors = RadioButtonDefaults.colors(
                    selectedColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    }
} 