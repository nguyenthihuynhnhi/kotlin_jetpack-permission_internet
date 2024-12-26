package com.example.permission_internet

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            InternetPermissionApp()
        }
    }

    private fun isInternetEnabled(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } else {
            val activeNetworkInfo = connectivityManager.activeNetworkInfo
            activeNetworkInfo != null && activeNetworkInfo.isConnected
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun InternetPermissionApp() {
        // State để theo dõi trạng thái
        val permissionGranted = remember { mutableStateOf(false) }
        val internetEnabled = remember { mutableStateOf(isInternetEnabled()) }

        // Launcher để yêu cầu quyền INTERNET
        val permissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            permissionGranted.value = isGranted
        }

        Scaffold(
            topBar = { TopAppBar(title = { Text("Internet Permission App") }) },
            content = { contentPadding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(contentPadding)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    if (internetEnabled.value) {
                        Button(onClick = {
                            // Yêu cầu quyền Internet
                            permissionLauncher.launch(android.Manifest.permission.INTERNET)
                        }) {
                            Text("Request Internet Permission")
                        }
                    } else {
                        Button(onClick = {
                            // Đưa người dùng đến giao diện cài đặt
                            val intent = Intent(Settings.ACTION_WIFI_SETTINGS)
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            startActivity(intent)
                        }) {
                            Text("Open System Settings to Enable Internet")
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = when {
                            !internetEnabled.value -> "Internet is not enabled. Please enable it in settings."
                            permissionGranted.value -> "Internet permission granted!"
                            else -> "Internet permission not granted."
                        }
                    )
                }
            }
        )
    }
}
