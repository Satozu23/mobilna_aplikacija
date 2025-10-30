package com.example.komsilukconnect.home
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.example.komsilukconnect.post.ui.PostListScreen
import com.example.komsilukconnect.ranking.ui.RankingScreen
import androidx.compose.material.icons.filled.Person
import com.example.komsilukconnect.post.ui.MyPostsScreen
import androidx.compose.material.icons.filled.People
import com.example.komsilukconnect.auth.ui.UserListScreen
import com.example.komsilukconnect.auth.navigation.Routes

sealed class BottomBarScreen(val route: String, val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    object MapScreen : BottomBarScreen("map", "Mapa", Icons.Filled.Map)
    object ListScreen : BottomBarScreen("list", "Lista", Icons.AutoMirrored.Filled.List)
    object Ranking : BottomBarScreen("ranking", "Rang", Icons.Default.Star)

    object MyPosts : BottomBarScreen("my_posts", "Objave", Icons.Default.Person)
    object Users : BottomBarScreen("users", "Korisnici", Icons.Default.People)
}
@Composable
fun MainScreen(mainNavController: androidx.navigation.NavController) {
    val bottomNavController = rememberNavController()

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                val screens = listOf(BottomBarScreen.MapScreen, BottomBarScreen.ListScreen, BottomBarScreen.Ranking,BottomBarScreen.MyPosts,BottomBarScreen.Users)

                screens.forEach { screen ->
                    NavigationBarItem(
                        label = { Text(screen.title) },

                        icon = { Icon(screen.icon, contentDescription = screen.title) },

                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            bottomNavController.navigate(screen.route) {
                                popUpTo(bottomNavController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = bottomNavController,
            startDestination = BottomBarScreen.MapScreen.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(BottomBarScreen.MapScreen.route) {
                HomeScreen(
                    onMarkerClick = { postId ->
                        mainNavController.navigate(com.example.komsilukconnect.auth.navigation.Routes.postDetails(postId))
                    }
                )
            }
            composable(BottomBarScreen.ListScreen.route) {
                PostListScreen(
                    onPostClick = { postId ->
                        mainNavController.navigate(com.example.komsilukconnect.auth.navigation.Routes.postDetails(postId))
                    }
                )
            }
            composable(BottomBarScreen.Ranking.route) {
                RankingScreen()
            }
            composable(BottomBarScreen.MyPosts.route) {
                MyPostsScreen(
                    onLogout = {
                        mainNavController.navigate(Routes.LOGIN_SCREEN) {
                            popUpTo(Routes.MAIN_SCREEN) { inclusive = true }
                        }
                    }
                )
            }
            composable(BottomBarScreen.Users.route) {
                UserListScreen(
                    onUserClick = { userId ->
                        mainNavController.navigate(Routes.userProfile(userId))
                    }
                )
            }
        }
    }
}