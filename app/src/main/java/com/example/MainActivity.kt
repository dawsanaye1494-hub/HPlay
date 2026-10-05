package com.example

import android.content.Intent
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.ui.HPlayMainScreen
import com.example.ui.theme.HPlayTheme
import com.example.viewmodel.HPlayViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: HPlayViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        com.example.logger.FileLogManager.init(this)
        enableEdgeToEdge()
        applyImmersiveMode()
        handleIntent(intent)

        setContent {
            val isNightMode by viewModel.isNightMode.collectAsState()

            HPlayTheme(forceNightMode = isNightMode) {
                HPlayMainScreen(
                    viewModel = viewModel,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent == null) return
        if (UsbManager.ACTION_USB_DEVICE_ATTACHED == intent.action) {
            val device: UsbDevice? = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)
            device?.let { viewModel.engine.handleUsbAttach(it) }
        }
    }

    override fun onResume() {
        super.onResume()
        applyImmersiveMode()
        // Refresh connection settings on resume and ensure automatic reconnection for persistent streaming
        if (viewModel.session.value.state == com.example.model.SessionState.DISCONNECTED ||
            viewModel.session.value.state == com.example.model.SessionState.FAILED) {
            viewModel.reconnect()
        }
    }

    private fun applyImmersiveMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.let { controller ->
                controller.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                controller.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN
            )
        }
    }

    // Intercept hardware steering wheel and multimedia keys
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (event != null && viewModel.dispatchHardwareKey(event)) {
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        if (event != null && viewModel.dispatchHardwareKey(event)) {
            return true
        }
        return super.onKeyUp(keyCode, event)
    }
}
