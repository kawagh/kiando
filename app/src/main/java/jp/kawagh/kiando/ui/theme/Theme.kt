package jp.kawagh.kiando.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

/*
    for Mateiral 3
*/
private val DarkColorScheme = darkColorScheme(
    primary = Purple200,
    secondary = Teal200
)

private val LightColorScheme = lightColorScheme(
    primary = BoardColorUnfocused,
    secondary = Teal200,
//    surfaceVariant = CardColor // Card and TextField  background
)

@Composable
fun KiandoM3Theme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val colorScheme = if (darkTheme) {
        DarkColorScheme
    } else {
        LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
