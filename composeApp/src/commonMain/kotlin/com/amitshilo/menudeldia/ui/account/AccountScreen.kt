package com.amitshilo.menudeldia.ui.account

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.amitshilo.menudeldia.domain.auth.model.AuthState
import com.amitshilo.menudeldia.navigation.Screen
import menudeldia.composeapp.generated.resources.Res
import menudeldia.composeapp.generated.resources.account_sign_out
import menudeldia.composeapp.generated.resources.account_title
import menudeldia.composeapp.generated.resources.arrow_back
import menudeldia.composeapp.generated.resources.back
import menudeldia.composeapp.generated.resources.login_google
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountScreen(
    navController: NavController,
    vm: AccountViewModel = viewModel { AccountViewModel() },
) {
    val authState by vm.authState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.account_title)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            painter = painterResource(Res.drawable.arrow_back),
                            contentDescription = stringResource(Res.string.back),
                        )
                    }
                },
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            when (val state = authState) {
                is AuthState.Authenticated -> {
                    val user = state.session.user
                    if (user.avatarUrl != null) {
                        AsyncImage(
                            model = user.avatarUrl,
                            contentDescription = null,
                            modifier = Modifier.size(80.dp).clip(CircleShape),
                        )
                        Spacer(Modifier.height(16.dp))
                    }
                    user.displayName?.let { name ->
                        Text(
                            text = name,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    user.email?.let { email ->
                        Text(
                            text = email,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Spacer(Modifier.height(32.dp))
                    Button(
                        onClick = vm::signOut,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer,
                        ),
                    ) {
                        Text(stringResource(Res.string.account_sign_out))
                    }
                }

                AuthState.Guest -> {
                    Text(
                        text = "You're browsing as a guest.",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Spacer(Modifier.height(24.dp))
                    TextButton(onClick = {
                        navController.navigate(Screen.Login.route)
                    }) {
                        Text(stringResource(Res.string.login_google))
                    }
                }

                else -> {}
            }
        }
    }
}
