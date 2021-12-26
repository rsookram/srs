package io.github.rsookram.srs

import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.core.view.WindowCompat
import com.google.accompanist.insets.ProvideWindowInsets
import com.google.accompanist.systemuicontroller.SystemUiController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import dagger.hilt.android.AndroidEntryPoint
import io.github.rsookram.srs.ui.theme.SrsTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            SrsTheme {
                val systemUiController = rememberSystemUiController()
                val isLight = MaterialTheme.colors.isLight

                SideEffect { systemUiController.applyTheme(isLight) }

                ProvideWindowInsets { Surface(color = MaterialTheme.colors.background) { Main() } }
            }
        }
    }

    @Suppress("DEPRECATION") // Workaround for https://issuetracker.google.com/issues/180881870
    private fun SystemUiController.applyTheme(isLight: Boolean) {
        val systemBar = Color(resources.getColor(R.color.system_bar, theme))

        setStatusBarColor(systemBar)
        setNavigationBarColor(systemBar)

        if (!isLight) {
            window.decorView.systemUiVisibility =
                window.decorView.systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
        }
    }
}
