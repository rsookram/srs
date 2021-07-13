package io.github.rsookram.srs.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.material.DropdownMenu
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState

@Composable
fun OverflowMenu(expanded: MutableState<Boolean>, content: @Composable () -> Unit) {
    Box {
        IconButton(onClick = { expanded.value = true }) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "More options",
            )
        }

        DropdownMenu(expanded.value, onDismissRequest = { expanded.value = false }) {
            content()
        }
    }
}
