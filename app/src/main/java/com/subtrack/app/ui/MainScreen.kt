package com.subtrack.app.ui

import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.subtrack.app.ui.screens.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: SubTrackViewModel) {
    val context = LocalContext.current
    var showOnboarding by remember { mutableStateOf(!OnboardingPrefs.hasSeenOnboarding(context)) }

    if (showOnboarding) {
        OnboardingScreen {
            OnboardingPrefs.setOnboardingSeen(context)
            showOnboarding = false
        }
        return
    }

    val navController = rememberNavController()
   val tabs = listOf("Главная", "Подписки", "Отмены", "Экономия")
    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "SubTrack",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                actions = {
                    IconButton(onClick = { navController.navigate("settings") }) {
                        Icon(Icons.Default.Settings, contentDescription = "Настройки")
                    }
                }
            )
        },
        floatingActionButton = {
            if (pagerState.currentPage == 0 || pagerState.currentPage == 1) {
                FloatingActionButton(
                    onClick = { navController.navigate("add") },
                    shape = MaterialTheme.shapes.large
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Добавить подписку")
                }
            }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            PrimaryTabRow(
                selectedTabIndex = pagerState.currentPage,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(
                                    index,
                                    animationSpec = tween(350)
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        text = {
                            Text(
                                title,
                                textAlign = TextAlign.Center,
                                fontWeight = if (pagerState.currentPage == index)
                                    FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                when (page) {
                    0 -> DashboardScreen(viewModel) {
                        scope.launch { pagerState.animateScrollToPage(1) }
                    }
                    1 -> SubscriptionListScreen(
                        viewModel = viewModel,
                        onEditClick = { id -> navController.navigate("edit/$id") },
                        onCancelClick = { id -> navController.navigate("cancel/$id") }
                    )
                    2 -> CancelledScreen(viewModel)
                    3 -> SavingsScreen(viewModel)
                }
            }
        }
    }

    NavHost(navController = navController, startDestination = "root") {
        composable("root") { }
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
        composable("settings") {
            SettingsScreen(viewModel) { navController.popBackStack() }
        }
        composable("achievements") {
            AchievementsScreen(viewModel) { navController.popBackStack() }
        }
    }
}
