package org.mjdev.desktop.components.controlcenter.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/** Scrollable, padded root container used by every control center settings page. */
@Suppress("FunctionName")
@Composable
fun SettingsPageColumn(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(SettingsDefaults.pagePadding)
                .padding(bottom = SettingsDefaults.pageBottomPadding),
    ) {
        content()
    }
}
