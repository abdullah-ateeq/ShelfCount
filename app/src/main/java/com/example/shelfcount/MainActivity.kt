package com.example.shelfcount

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.shelfcount.ui.navigation.ShelfCountApp
import com.example.shelfcount.ui.theme.ShelfCountTheme
import com.example.shelfcount.viewmodel.ShelfCountViewModel
import com.example.shelfcount.viewmodel.ShelfCountViewModelFactory

class MainActivity : ComponentActivity() {

    private val viewModel: ShelfCountViewModel by viewModels {
        ShelfCountViewModelFactory(applicationContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val settings by viewModel.settings.collectAsState()

            ShelfCountTheme(
                themeMode = settings.themeMode,
                dynamicColor = settings.dynamicColor
            ) {
                ShelfCountApp(viewModel = viewModel)
            }
        }
    }
}