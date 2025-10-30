package com.example.komsilukconnect.auth.navigation
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.komsilukconnect.auth.ui.LoginScreen
import com.example.komsilukconnect.auth.ui.RegisterScreen
import com.example.komsilukconnect.post.ui.PostDetailsScreen
import com.example.komsilukconnect.home.MainScreen
import com.example.komsilukconnect.auth.ui.UserProfileScreen
object Routes {
    const val LOGIN_SCREEN = "login"
    const val REGISTER_SCREEN = "register"
    const val MAIN_SCREEN = "main"
    const val POST_DETAILS_SCREEN = "post_details/{postId}"
    const val USER_PROFILE_SCREEN = "user_profile/{userId}"

    fun postDetails(postId: String) = "post_details/$postId"
    fun userProfile(userId: String) = "user_profile/$userId"

}
@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Routes.LOGIN_SCREEN) {
        composable(Routes.LOGIN_SCREEN) {
            LoginScreen(
                onNavigateToRegister = { navController.navigate(Routes.REGISTER_SCREEN) },
                onLoginSuccess = {
                    navController.navigate(Routes.MAIN_SCREEN) {
                        popUpTo(Routes.LOGIN_SCREEN) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.REGISTER_SCREEN) {
            RegisterScreen(
                onNavigateToLogin = { navController.navigate(Routes.LOGIN_SCREEN) },
                onRegisterSuccess = {
                    navController.navigate(Routes.MAIN_SCREEN) {
                        popUpTo(Routes.REGISTER_SCREEN) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.MAIN_SCREEN) {
            MainScreen(mainNavController = navController)
        }

        composable(
            route = Routes.POST_DETAILS_SCREEN,
            arguments = listOf(navArgument("postId") { type = NavType.StringType })
        ) {
            PostDetailsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.USER_PROFILE_SCREEN,
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) {
            UserProfileScreen(onNavigateBack = { navController.popBackStack() })
        }
    }
}
