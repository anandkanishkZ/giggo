package com.natrajtechnology.giggo.presentation.preview

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.natrajtechnology.giggo.presentation.screen.ForgotPasswordScreen
import com.natrajtechnology.giggo.presentation.screen.LoginScreen
import com.natrajtechnology.giggo.presentation.screen.SignUpScreen
import com.natrajtechnology.giggo.presentation.screen.SplashScreen
import com.natrajtechnology.giggo.presentation.viewmodel.AuthViewModel
import com.natrajtechnology.giggo.ui.theme.GigGOTheme

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SplashScreenPreview() {
    GigGOTheme {
        SplashScreen(
            onNavigateToLogin = {},
            onNavigateToHome = {},
            isAuthenticated = false
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun LoginScreenPreview() {
    GigGOTheme {
        LoginScreen(
            authViewModel = viewModel(),
            onNavigateToSignUp = {},
            onNavigateToForgotPassword = {},
            onNavigateToHome = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SignUpScreenPreview() {
    GigGOTheme {
        SignUpScreen(
            authViewModel = viewModel(),
            onNavigateToLogin = {},
            onNavigateToHome = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ForgotPasswordScreenPreview() {
    GigGOTheme {
        ForgotPasswordScreen(
            authViewModel = viewModel(),
            onNavigateBack = {}
        )
    }
}
