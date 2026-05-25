package com.auralyx

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.auralyx.service.AuralyxPlaybackService
import com.auralyx.ui.components.PermissionScreen
import com.auralyx.ui.navigation.AuralyxNavGraph
import com.auralyx.ui.theme.AuralyxTheme
import com.auralyx.utils.PermissionUtils
import com.auralyx.utils.PreferencesManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var prefs: PreferencesManager

    private val permLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { results ->
        // Re-compose will pick up hasPermission reactively
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Request permissions immediately; UI reacts reactively
        permLauncher.launch(PermissionUtils.getRequiredPermissions())

        // Start playback service early so MediaSession is available
        try { startService(Intent(this, AuralyxPlaybackService::class.java)) } catch (_: Exception) {}

        setContent {
            val isDark   by prefs.isDarkTheme.collectAsState(initial = true)
            val dynColor by prefs.dynamicColor.collectAsState(initial = false)

            AuralyxTheme(darkTheme = isDark, dynamicColor = dynColor) {
                Surface(Modifier.fillMaxSize()) {
                    val hasPermission = PermissionUtils.hasStoragePermission(this@MainActivity)
                    if (hasPermission) {
                        AuralyxNavGraph()
                    } else {
                        PermissionScreen(onGrant = { permLauncher.launch(PermissionUtils.getRequiredPermissions()) })
                    }
                }
            }
        }
    }
}
