package com.subtrack.app.ui

import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.subtrack.app.ui.components.AdBanner
import com.subtrack.app.ui.screens.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: SubTrackViewModel,
    onThemeChanged: () -> Unit = {},
    openCancelId: Long? = null,
    openTab: Int? = null
) {
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
    val tabs = listOf("Аналитика", "Подписки", "Достижения")
    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val scope = rememberCoroutineScope()
    val isPremium = viewModel.isPremium()

    LaunchedEffect(openTab) {
        if (openTab != null && openTab in tabs.indices) {
            pagerState.animateScrollToPage(openTab)
        }
    }

    LaunchedEffect(openCancelId) {
        if (openCancelId != null && openCancelId >= 0) {
            navController.navigate("cancel/$openCancelId")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (isPremium) {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = null,
                                tint = Color(0xFFFFD700)
                            )
                            Spacer(Modifier.width(8.dp))
                        }
                        Text(
                            "SubTrack",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate("settings") }) {
                        Icon(Icons.Default.Settings, contentDescription = "Настройки")
                    }
                }
            )
        },
        bottomBar = {
            if (!isPremium) {
                AdBanner(modifier = Modifier.padding(8.dp))
            }
        },
        floatingActionButton = {
            if (pagerState.currentPage == 1) {
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
                    0 -> DashboardScreen(
                        viewModel = viewModel,
                        onShowList = {
                            scope.launch { pagerState.animateScrollToPage(1) }
                        },
                        onAddClick = { navController.navigate("add") },
                        onBuyPremium = { navController.navigate("premium") }
                    )
                    1 -> SubscriptionListScreen(
                        viewModel = viewModel,
                        onEditClick = { id -> navController.navigate("edit/$id") },
                        onCancelClick = { id -> navController.navigate("cancel/$id") },
                        onAddInCategory = { category ->
                            navController.navigate("add_in_category/$category")
                        },
                        onBuyPremium = { navController.navigate("premium") }
                    )
                    2 -> AchievementsScreen(
                        viewModel = viewModel,
                        onBack = {},
                        onBuyPremium = { navController.navigate("premium") }
                    )
                }
            }
        }
    }

    NavHost(navController = navController, startDestination = "root") {
        composable("root") { }
        composable("add") {
            AddEditSubscriptionScreen(viewModel, null, null) { navController.popBackStack() }
        }
        composable("add_in_category/{category}") { entry ->
            val category = entry.arguments?.getString("category")
            AddEditSubscriptionScreen(viewModel, null, category) { navController.popBackStack() }
        }
        composable("edit/{id}") { entry ->
            val id = entry.arguments?.getString("id")?.toLongOrNull()
            AddEditSubscriptionScreen(viewModel, id, null) {
                navController.popBackStack("root", inclusive = false)
            }
        }
        composable("cancel/{id}") { entry ->
            val id = entry.arguments?.getString("id")?.toLongOrNull()
            CancelScreen(
                viewModel = viewModel,
                id = id,
                onDone = { navController.popBackStack() },
                onEditClick = { subId ->
                    navController.navigate("edit/$subId")
                }
            )
        }
        composable("settings") {
            SettingsScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onOpenPremium = { navController.navigate("premium") },
                onThemeChanged = onThemeChanged
            )
        }
        composable("premium") {
            PremiumScreen(
                onBack = { navController.popBackStack() },
                onRestoreClick = { navController.navigate("restore_premium") },
                onBuyClick = { navController.navigate("purchase") }
            )
        }
        composable("restore_premium") {
            RestorePremiumScreen(viewModel) { navController.popBackStack() }
        }
        composable("purchase") {
            PurchaseScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onSuccess = {
                    navController.popBackStack("root", inclusive = false)
                }
            )
        }
    }
}
