package com.natrajtechnology.giggo.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.natrajtechnology.giggo.navigation.BottomNavItem
import com.natrajtechnology.giggo.navigation.Screen
import com.natrajtechnology.giggo.navigation.bottomNavItems
import com.natrajtechnology.giggo.presentation.viewmodel.AuthViewModel
import com.natrajtechnology.giggo.ui.theme.Primary

@Composable
fun DashboardScreen(
    authViewModel: AuthViewModel,
    onLogout: () -> Unit
) {
    val navController = rememberNavController()
    
    Scaffold(
        bottomBar = {
            BottomNavigationBar(navController = navController)
        }
    ) { innerPadding ->
        DashboardNavHost(
            navController = navController,
            authViewModel = authViewModel,
            onLogout = onLogout,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        )
    }
}

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Box(
        modifier = Modifier.fillMaxWidth()
    ) {
        NavigationBar(
            containerColor = Color.White,
            contentColor = Primary
        ) {
            bottomNavItems.forEach { item ->
                val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true
                
                if (item.isCenter) {
                    // Create space for the center FAB
                    Spacer(modifier = Modifier.weight(1f))
                } else {
                    NavigationBarItem(
                        icon = {
                            BadgedBox(
                                badge = {
                                    if (item.badgeCount != null) {
                                        Badge {
                                            Text(item.badgeCount.toString())
                                        }
                                    } else if (item.hasNews) {
                                        Badge()
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = item.title
                                )
                            }
                        },
                        label = { Text(item.title) },
                        selected = selected,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Primary,
                            selectedTextColor = Primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        }
        
        // Center FAB
        val centerItem = bottomNavItems.find { it.isCenter }
        centerItem?.let { item ->
            val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true
            
            FloatingActionButton(
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = (-30).dp)
                    .size(64.dp),
                containerColor = if (selected) Primary.copy(alpha = 0.9f) else Primary,
                contentColor = Color.White,
                shape = CircleShape,
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 8.dp,
                    pressedElevation = 12.dp
                )
            ) {
                Icon(
                    imageVector = item.selectedIcon,
                    contentDescription = item.title,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

@Composable
fun DashboardNavHost(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                authViewModel = authViewModel,
                onLogout = onLogout,
                onNavigateToGigDetail = { gigId ->
                    println("Debug: DashboardScreen - Navigating to gig detail with ID: '$gigId'")
                    if (gigId.isNotBlank()) {
                        navController.navigate("gig_detail/$gigId")
                    } else {
                        println("Debug: DashboardScreen - ERROR: Attempted to navigate with empty gigId!")
                    }
                }
            )
        }
        
        composable(Screen.Search.route) {
            SearchScreen(
                onNavigateToGigDetail = { gigId ->
                    println("Debug: DashboardScreen - Navigating to gig detail with ID: '$gigId'")
                    if (gigId.isNotBlank()) {
                        navController.navigate("gig_detail/$gigId")
                    } else {
                        println("Debug: DashboardScreen - ERROR: Attempted to navigate with empty gigId!")
                    }
                }
            )
        }
        
        composable(Screen.AddGig.route) {
            AddGigScreen(
                authViewModel = authViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.Notifications.route) {
            NotificationsScreen()
        }
        
        composable(Screen.Settings.route) {
            SettingsScreen(
                authViewModel = authViewModel,
                onLogout = onLogout,
                onNavigateToProfile = {
                    navController.navigate(Screen.Profile.route)
                },
                onNavigateToChangePassword = {
                    navController.navigate(Screen.ChangePassword.route)
                },
                onNavigateToHelpSupport = {
                    navController.navigate(Screen.HelpSupport.route)
                },
                onNavigateToPrivacyPolicy = {
                    navController.navigate(Screen.PrivacyPolicy.route)
                },
                onNavigateToTermsOfService = {
                    navController.navigate(Screen.TermsOfService.route)
                }
            )
        }
        
        composable(Screen.Profile.route) {
            ProfileScreen(
                authViewModel = authViewModel,
                onLogout = onLogout,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable("gig_detail/{gigId}") { backStackEntry ->
            val gigId = backStackEntry.arguments?.getString("gigId") ?: ""
            println("Debug: DashboardScreen - Received gigId from navigation: '$gigId'")
            println("Debug: DashboardScreen - BackStackEntry arguments: ${backStackEntry.arguments}")
            
            if (gigId.isNotBlank()) {
                GigDetailScreen(
                    gigId = gigId,
                    onNavigateBack = {
                        println("Debug: DashboardScreen - Navigating back from gig detail")
                        navController.popBackStack()
                    }
                )
            } else {
                println("Debug: DashboardScreen - ERROR: Empty gigId received in navigation!")
                // Navigate back or show error
                navController.popBackStack()
            }
        }
        
        // Settings screens
        composable(Screen.ChangePassword.route) {
            ChangePasswordScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.HelpSupport.route) {
            HelpSupportScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.PrivacyPolicy.route) {
            PrivacyPolicyScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.TermsOfService.route) {
            TermsOfServiceScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
