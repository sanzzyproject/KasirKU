package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.rememberNavController
import com.example.data.AppDatabase
import com.example.data.KasirRepository
import com.example.ui.AppNavigation
import com.example.ui.BottomNavigationBar
import com.example.ui.KasirViewModel
import com.example.ui.KasirViewModelFactory
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    
    val database = AppDatabase.getDatabase(this)
    val repository = KasirRepository(database.kasirDao())
    val factory = KasirViewModelFactory(repository)
    val viewModel = ViewModelProvider(this, factory)[KasirViewModel::class.java]

    setContent {
      MyApplicationTheme {
        val navController = rememberNavController()
        Scaffold(
          modifier = Modifier.fillMaxSize(),
          containerColor = androidx.compose.material3.MaterialTheme.colorScheme.background,
          bottomBar = { BottomNavigationBar(navController) }
        ) { innerPadding ->
          AppNavigation(
            viewModel = viewModel,
            modifier = Modifier.padding(innerPadding),
            navController = navController
          )
        }
      }
    }
  }
}
