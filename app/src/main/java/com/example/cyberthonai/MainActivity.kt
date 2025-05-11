package com.example.cyberthonai

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.cyberthonai.ui.theme.CyberthonAiTheme
import com.google.android.ads.mediationtestsuite.activities.HomeActivity
import java.util.concurrent.Executor

class MainActivity : AppCompatActivity() {
    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize biometric components
        executor = ContextCompat.getMainExecutor(this)
        biometricPrompt = BiometricPrompt(this, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                Toast.makeText(this@MainActivity, "Authentication successful!", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this@MainActivity, HomeActivity::class.java))
                finish()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                Toast.makeText(this@MainActivity, "Error: $errString", Toast.LENGTH_SHORT).show()
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                Toast.makeText(this@MainActivity, "Authentication failed", Toast.LENGTH_SHORT).show()
            }
        })

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Fingerprint Login")
            .setSubtitle("Use your fingerprint to login")
            .setNegativeButtonText("Cancel")
            .build()

        setContent {
            CyberthonAiTheme {
                AuthScreen(
                    onAuthenticate = { biometricPrompt.authenticate(promptInfo) },
                    canAuthenticate = checkBiometricAvailability()
                )
            }
        }
    }

    private fun checkBiometricAvailability(): Boolean {
        val biometricManager = BiometricManager.from(this)
        return when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS -> true
            else -> false
        }
    }
}

@Composable
fun AuthScreen(onAuthenticate: () -> Unit, canAuthenticate: Boolean) {
    val context = LocalContext.current
    var showAddFingerForm by remember { mutableStateOf(false) }

    // Automatically trigger authentication if biometric is available
    LaunchedEffect(canAuthenticate) {
        if (canAuthenticate) {
            onAuthenticate()
        }
    }

    Scaffold(modifier = Modifier.fillMaxSize()) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(24.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center
        ) {
            if (!canAuthenticate) {
                Text(
                    text = "Fingerprint authentication is not available or not enrolled.",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            } else {
                Button(
                    onClick = { onAuthenticate() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Authenticate to Login")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { showAddFingerForm = !showAddFingerForm },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Add/Delete Fingerprint")
            }

            if (showAddFingerForm) {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Note: Fingerprint enrollment is managed by system settings",
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        context.startActivity(Intent(Settings.ACTION_SECURITY_SETTINGS))
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Open Security Settings")
                }
            }
        }
    }
}