// MainActivity.kt
package com.example.new_assignment_1

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Text
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.new_assignment_1.ui.theme.New_Assignment_1Theme
import androidx.compose.foundation.layout.fillMaxSize

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            New_Assignment_1Theme {
                Surface(
                    modifier = Modifier.fillMaxSize()

                ) {
                    JourneyApp()
                }
            }
        }
    }
}
