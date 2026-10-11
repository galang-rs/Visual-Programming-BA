package com.ecosort.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.ecosort.app.ui.navigation.EcoSortNavGraph
import com.ecosort.app.ui.theme.EcoSortTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EcoSortTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    EcoSortNavGraph(context = this)
                }
            }
        }
    }
}
