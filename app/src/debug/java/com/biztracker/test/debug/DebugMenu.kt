package com.biztracker.test.debug

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun DebugMenu(
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var status by remember { mutableStateOf("Ready") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "Debug Menu",
            style = MaterialTheme.typography.titleLarge,
        )

        Button(
            onClick = {
                scope.launch {
                    status = "Seeding..."
                    runCatching {
                        SeedDatabaseWorker.runSeed(context)
                    }.onSuccess {
                        status = "Seed complete"
                    }.onFailure {
                        status = "Seed failed"
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(text = "Seed Data")
        }

        Button(
            onClick = {
                scope.launch {
                    status = "Clearing..."
                    runCatching {
                        SeedDatabaseWorker.clearDatabase(context)
                    }.onSuccess {
                        status = "Database cleared"
                    }.onFailure {
                        status = "Clear failed"
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(text = "Clear Database")
        }

        Text(
            text = status,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}
