package com.subtrack.app.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.subtrack.app.ui.screens.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: SubTrackViewModel) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val bottomItems = listOf(
        BottomItem("dashboard", "Главная", Icons.Default.Home),
        BottomItem("list", "Подписки", Icons.Default.List),
        BottomItem("cancelled", "Отменённые", Icons.Default.Star)
    )

    Scaffold(
        bottomBar = {
            if (currentRoute in bottomItems.map { it.route }) {
                NavigationBar {
                    bottomItems.forEach { item ->
                        NavigationBarItem(
                            selected = currentRoute == item.route,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo("dashboard") { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) }
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            if (currentRoute == "list" || currentRoute == "dashboard") {
                FloatingActionButton(onClick = { navController.navigate("add") }) {
                    Icon(Icons.Default.Add, contentDescription = "Добавить подписку")
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = "dashboard",
            modifier = Modifier.padding(padding)
        ) {
            composable("dashboard") {
                DashboardScreen(viewModel) { navController.navigate("list") }
            }
            composable("list") {
                SubscriptionListScreen(
                    viewModel = viewModel,
                    onAddClick = { navController.navigate("add") },
                    onEditClick = { id -> navController.navigate("edit/$id") },
                    onCancelClick = { id -> navController.navigate("cancel/$id") }
                )
            }
            composable("cancelled") {
                CancelledScreen(viewModel)
            }
            composable("add") {
                AddEditSubscriptionScreen(viewModel, null) { navController.popBackStack() }
            }
            composable("edit/{id}") { entry ->
                val id = entry.arguments?.getString("id")?.toLongOrNull()
                AddEditSubscriptionScreen(viewModel, id) { navController.popBackStack() }
            }
            composable("cancel/{id}") { entry ->
                val id = entry.arguments?.getString("id")?.toLongOrNull()
                CancelScreen(viewModel, id) { navController.popBackStack() }
            }
            composable("achievements") {
                AchievementsScreen(viewModel) { navController.popBackStack() }
            }
        }
    }
}

data class BottomItem(val route: String, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)
