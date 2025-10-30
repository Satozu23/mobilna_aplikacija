package com.example.komsilukconnect

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.komsilukconnect.auth.navigation.AppNavigation
import com.example.komsilukconnect.ui.theme.KomsilukConnectTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KomsilukConnectTheme {
                AppNavigation()
            }
        }
    }
}
