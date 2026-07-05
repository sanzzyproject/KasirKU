package com.example.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.screens.PosScreen
import com.example.ui.screens.ReportsScreen
import com.example.ui.screens.HistoryScreen

enum class Screen(val route: String, val title: String, val icon: @Composable () -> Unit) {
    POS("pos", "Kasir", { Icon(Icons.Filled.ShoppingCart, contentDescription = "Kasir") }),
    REPORTS("reports", "Laporan", { Icon(Icons.Filled.TrendingUp, contentDescription = "Laporan") }),
    HISTORY("history", "Riwayat", { Icon(Icons.Filled.List, contentDescription = "Riwayat") })
}

@Composable
fun AppNavigation(viewModel: KasirViewModel, modifier: Modifier = Modifier, navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = Screen.POS.route, modifier = modifier) {
        composable(Screen.POS.route) {
            PosScreen(viewModel = viewModel)
        }
        composable(Screen.REPORTS.route) {
            ReportsScreen(viewModel = viewModel)
        }
        composable(Screen.HISTORY.route) {
            HistoryScreen(viewModel = viewModel)
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavController) {
    val items = listOf(Screen.POS, Screen.REPORTS, Screen.HISTORY)
    NavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route
        items.forEach { screen ->
            NavigationBarItem(
                icon = screen.icon,
                label = { Text(screen.title) },
                selected = currentRoute == screen.route,
                onClick = {
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}
