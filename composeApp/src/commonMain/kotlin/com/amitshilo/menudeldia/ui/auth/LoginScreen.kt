package com.amitshilo.menudeldia.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.amitshilo.menudeldia.getPlatform
import com.amitshilo.menudeldia.ui.theme.MenuTheme
import menudeldia.composeapp.generated.resources.Res
import menudeldia.composeapp.generated.resources.login_apple
import menudeldia.composeapp.generated.resources.login_google
import menudeldia.composeapp.generated.resources.login_guest
import menudeldia.composeapp.generated.resources.login_subtitle
import menudeldia.composeapp.generated.resources.login_title
import org.jetbrains.compose.resources.stringResource

@Composable
fun LoginScreen(vm: LoginViewModel = viewModel { LoginViewModel() }) {
    val uiState by vm.uiState.collectAsState()
    val snackbarHost = remember { SnackbarHostState() }
    val isLoading = uiState is LoginUiState.Loading

    LaunchedEffect(uiState) {
        if (uiState is LoginUiState.Error) {
            snackbarHost.showSnackbar((uiState as LoginUiState.Error).message)
            vm.clearError()
        }
    }

    LoginContent(
        isLoading = isLoading,
        showAppleButton = getPlatform().name.contains("ios", ignoreCase = true),
        snackbarHost = snackbarHost,
        onGoogleSignIn = vm::signInWithGoogle,
        onAppleSignIn = vm::signInWithApple,
        onContinueAsGuest = vm::continueAsGuest,
    )
}

@Composable
private fun LoginContent(
    isLoading: Boolean,
    showAppleButton: Boolean,
    snackbarHost: SnackbarHostState,
    onGoogleSignIn: () -> Unit,
    onAppleSignIn: () -> Unit,
    onContinueAsGuest: () -> Unit,
) {
    Scaffold(snackbarHost = { SnackbarHost(snackbarHost) }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "🍽️",
                style = MaterialTheme.typography.displayLarge,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = stringResource(Res.string.login_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
            Text(
                text = stringResource(Res.string.login_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp),
            )
            Spacer(Modifier.height(48.dp))

            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(48.dp))
            } else {
                Button(
                    onClick = onGoogleSignIn,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(Res.string.login_google))
                }
                if (showAppleButton) {
                    Spacer(Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = onAppleSignIn,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(stringResource(Res.string.login_apple))
                    }
                }
                Spacer(Modifier.height(24.dp))
                TextButton(onClick = onContinueAsGuest) {
                    Text(stringResource(Res.string.login_guest))
                }
            }
        }
    }
}

// ── Previews ────────────────────────────────────────────────────────────────

@PreviewLightDark
@Composable
private fun PreviewLoginIdle() {
    MenuTheme {
        LoginContent(
            isLoading = false,
            showAppleButton = false,
            snackbarHost = SnackbarHostState(),
            onGoogleSignIn = {},
            onAppleSignIn = {},
            onContinueAsGuest = {},
        )
    }
}

@PreviewLightDark
@Composable
private fun PreviewLoginLoading() {
    MenuTheme {
        LoginContent(
            isLoading = true,
            showAppleButton = false,
            snackbarHost = SnackbarHostState(),
            onGoogleSignIn = {},
            onAppleSignIn = {},
            onContinueAsGuest = {},
        )
    }
}
