package io.github.rsookram.srs

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.core.view.WindowCompat
import dagger.hilt.android.AndroidEntryPoint
import io.github.rsookram.srs.ui.theme.SrsTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            SrsTheme {
                Surface(color = MaterialTheme.colors.background) {
                }
            }
        }
    }
}
