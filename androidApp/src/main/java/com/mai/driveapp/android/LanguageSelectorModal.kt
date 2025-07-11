package com.mai.driveapp.android

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mai.driveapp.Language

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageSelectorModal(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    currentLanguage: Language,
    onLanguageSelected: (Language) -> Unit
) {
    if (isVisible) {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
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
                    // Close icon - hidden for now
                    Spacer(Modifier.width(24.dp))
                    
                    // Title
                    Text(
                        text = LocalizedStrings.languageSelection,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                    
                    // Close button
                    IconButton(onClick = onDismiss) {
                        Text("✕", fontSize = 24.sp)
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Language options
                LanguageOption(
                    language = Language.ENGLISH,
                    isSelected = currentLanguage == Language.ENGLISH,
                    onSelect = { onLanguageSelected(Language.ENGLISH) }
                )
                
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                
                LanguageOption(
                    language = Language.PERSIAN,
                    isSelected = currentLanguage == Language.PERSIAN,
                    onSelect = { onLanguageSelected(Language.PERSIAN) }
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Submit button
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        text = LocalizedStrings.save,
                        fontSize = 18.sp
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
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = language.displayName,
            fontSize = 18.sp
        )
        
        RadioButton(
            selected = isSelected,
            onClick = onSelect,
            colors = RadioButtonDefaults.colors(
                selectedColor = MaterialTheme.colorScheme.primary
            )
        )
    }
} 