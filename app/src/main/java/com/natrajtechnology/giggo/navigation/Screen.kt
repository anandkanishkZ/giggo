package com.natrajtechnology.giggo.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object SignUp : Screen("signup")
    object ForgotPassword : Screen("forgot_password")
    object EmailVerification : Screen("email_verification")
    
    // Dashboard screens
    object Dashboard : Screen("dashboard")
    object Home : Screen("home")
    object Profile : Screen("profile")
    object Settings : Screen("settings")
    object Search : Screen("search")
    object Notifications : Screen("notifications")
    object AddGig : Screen("add_gig")
    object GigDetail : Screen("gig_detail/{gigId}")
    
    // Settings screens
    object ChangePassword : Screen("change_password")
    object HelpSupport : Screen("help_support")
    object PrivacyPolicy : Screen("privacy_policy")
    object TermsOfService : Screen("terms_of_service")
}

object NavigationRoutes {
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val SIGNUP = "signup"
    const val FORGOT_PASSWORD = "forgot_password"
    const val HOME = "home"
}
