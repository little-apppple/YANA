package com.example.stillalive

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.exclude
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.stillalive.service.MonitorService
import com.example.stillalive.ui.history.HistoryScreen
import com.example.stillalive.ui.home.HomeScreen
import com.example.stillalive.ui.settings.SettingsScreen
import com.example.stillalive.ui.theme.StillAliveTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            StillAliveTheme {
                // Surface removed as Scaffold provides the background
                val navController = rememberNavController()
                val app = application as StillAliveApp
                val isAutoMode by app.settingsRepository.isAutoMode.collectAsState(initial = false)

                // 权限请求
                val permissionsToRequest = mutableListOf(
                    Manifest.permission.SEND_SMS
                )
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
                }

                val launcher = rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestMultiplePermissions()
                ) { permissions ->
                    // Handle permissions granted/rejected
                }

                LaunchedEffect(Unit) {
                    launcher.launch(permissionsToRequest.toTypedArray())

                    // Request ignore battery optimizations for keep-alive
                    val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
                    if (!powerManager.isIgnoringBatteryOptimizations(packageName)) {
                        val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                            data = Uri.parse("package:$packageName")
                        }
                        startActivity(intent)
                    }
                }

                // 监听自动模式开关，控制服务
                LaunchedEffect(isAutoMode) {
                    val intent = Intent(this@MainActivity, MonitorService::class.java)
                    if (isAutoMode) {
                        startForegroundService(intent)
                    } else {
                        stopService(intent)
                    }
                }

                Scaffold(
                    contentWindowInsets = ScaffoldDefaults.contentWindowInsets.exclude(WindowInsets.statusBars),
                    bottomBar = {
                        NavigationBar {
                            val navBackStackEntry by navController.currentBackStackEntryAsState()
                            val currentDestination = navBackStackEntry?.destination?.route

                            NavigationBarItem(
                                icon = { Icon(Icons.Filled.Home, contentDescription = "首页") },
                                label = { Text("首页") },
                                selected = currentDestination == "home",
                                onClick = {
                                    navController.navigate("home") {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            )
                            NavigationBarItem(
                                icon = { Icon(Icons.Filled.DateRange, contentDescription = "历史") },
                                label = { Text("历史") },
                                selected = currentDestination == "history",
                                onClick = {
                                    navController.navigate("history") {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            )
                            NavigationBarItem(
                                icon = { Icon(Icons.Filled.Settings, contentDescription = "设置") },
                                label = { Text("设置") },
                                selected = currentDestination == "settings",
                                onClick = {
                                    navController.navigate("settings") {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            )
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "home",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("home") { HomeScreen(navController) }
                        composable("settings") { SettingsScreen(navController) }
                        composable("history") { HistoryScreen(navController) }
                    }
                }
            }
        }
    }
}
