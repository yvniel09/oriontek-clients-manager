package com.example.oriontek_technical_interview.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.example.oriontek_technical_interview.app.navigation.AppNavGraph
import com.example.oriontek_technical_interview.ui.theme.OriontektechnicalinterviewTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            OriontektechnicalinterviewTheme {
                val navController = rememberNavController()
                AppNavGraph(navController = navController)
            }
        }
    }
}