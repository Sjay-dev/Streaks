package com.example.streaks.View.Components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

data class PaletteColor(val name: String, val color: Color)

val StreakPalette = listOf(
    PaletteColor("Blue", Color(0xFF17689A)),
    PaletteColor("Green", Color(0xFF2E7D32)),
    PaletteColor("Red", Color(0xFFC62828)),
    PaletteColor("Yellow", Color(0xFFFFE082)),
    PaletteColor("White", Color.White),
    PaletteColor("Purple", Color(0xFF7B1FA2)),
    PaletteColor("Black", Color.Black)
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ColorPalette(
    selectedColor: Color,
    onColorSelected: (Color) -> Unit,
    modifier: Modifier = Modifier,
    contentDescriptionSuffix: String = "goal color"
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        StreakPalette.forEach { choice ->
            val selected = choice.color.value == selectedColor.value
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .semantics {
                        contentDescription = "${choice.name} $contentDescriptionSuffix"
                        this.selected = selected
                        role = Role.RadioButton
                    }
                    .clickable { onColorSelected(choice.color) },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(choice.color, CircleShape)
                        .border(
                            width = if (selected) 4.dp else 2.dp,
                            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (selected) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            tint = if (choice.color.luminance() > .5f) Color.Black else Color.White
                        )
                    }
                }
            }
        }
    }
}
