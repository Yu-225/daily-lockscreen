package com.yulapps.dailylockscreen

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yulapps.dailylockscreen.ui.MainScreen
import com.yulapps.dailylockscreen.ui.MainViewModel
import com.yulapps.dailylockscreen.ui.theme.DailyLockscreenTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DailyLockscreenTheme {
                val viewModel: MainViewModel = viewModel()
                MainScreen(viewModel = viewModel)
            }
        }
    }
}
