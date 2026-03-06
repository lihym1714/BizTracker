package com.biztracker.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.biztracker.R

@Composable
fun BTErrorState(
    message: String,
    modifier: Modifier = Modifier,
    onRetry: (() -> Unit)? = null,
) {
    BTEmptyState(
        modifier = modifier,
        title = stringResource(id = R.string.error_generic_title),
        message = message,
        icon = Icons.Rounded.ErrorOutline,
        action = {
            if (onRetry != null) {
                TextButton(onClick = onRetry) {
                    Text(text = stringResource(id = R.string.action_retry))
                }
            }
        },
    )
}
