package com.example.autoanswerapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
//import androidx.compose.runtime.LaunchedEffect
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.setValue
//import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.tooling.preview.Preview


class MainActivity : ComponentActivity() {

    private val requiredPermissions = arrayOf(
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.ANSWER_PHONE_CALLS
    )

    private lateinit var requestPermissionLauncher: androidx.activity.result.ActivityResultLauncher<Array<String>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            if (permissions.all { it.value }) {
                startService(Intent(this, CallDetectorService::class.java))
            }
        }

        setContent {
            AutoAnswerApp(onServiceStateChanged = { isEnabled ->
                if (isEnabled) {
                    checkAndRequestPermissions()
                } else {
                    stopService(Intent(this, CallDetectorService::class.java))
                }
            }
            )
        }
    }

    private fun checkAndRequestPermissions() {
        if (requiredPermissions.all {
                ContextCompat.checkSelfPermission(
                    this,
                    it
                ) == PackageManager.PERMISSION_GRANTED
            }) {
            Log.d("MainActivity", "All permissions granted, starting service")
            startService(Intent(this, CallDetectorService::class.java))
        } else {
            Log.d("MainActivity", "Requesting permissions")
            requestPermissionLauncher.launch(requiredPermissions)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AutoAnswerApp(onServiceStateChanged: (Boolean) -> Unit) {
    val context = LocalContext.current
    var isAutoAnswerEnabled by remember { mutableStateOf(false) }
    var answerDelay by remember { mutableStateOf(5f) }
    var showSettingsDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isAutoAnswerEnabled = PreferenceManager.isAutoAnswerEnabled(context)
        answerDelay = PreferenceManager.getAnswerDelay(context).toFloat()
    }

    if (showSettingsDialog) {
        AlertDialog(
            onDismissRequest = { showSettingsDialog = false },
            title = { Text("Permissions Required") },
            text = { Text("This app requires phone permissions to function. Please grant them in the app settings.") },
            confirmButton = {
                TextButton(onClick = {
                    showSettingsDialog = false
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    intent.data = Uri.fromParts("package", context.packageName, null)
                    context.startActivity(intent)
                }) {
                    Text("Go to Settings")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSettingsDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Auto Answer App") }
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFD8BFFA))
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Auto Answer",
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Switch(
                checked = isAutoAnswerEnabled,
                onCheckedChange = {
                    isAutoAnswerEnabled = it
                    PreferenceManager.setAutoAnswerEnabled(context, it)
                    Log.d("AutoAnswerApp", "Auto-answer enabled: $it")
                    onServiceStateChanged(it)
                }
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Auto Answer: ${if (isAutoAnswerEnabled) "Enabled" else "Disabled"}",
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Answer Delay: ${answerDelay.toInt()} seconds",
                style = MaterialTheme.typography.bodyLarge
            )
            Slider(
                value = answerDelay,
                onValueChange = {
                    answerDelay = it
                    PreferenceManager.setAnswerDelay(context, it.toInt())
                },
                valueRange = 1f..30f,
                steps = 29
            )
        }
    }
}

    @Preview
    @Composable
    fun AutoAnswerAppPreview() {
        AutoAnswerApp(onServiceStateChanged = {})
    }

