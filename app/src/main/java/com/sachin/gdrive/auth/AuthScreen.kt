package com.sachin.gdrive.auth

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.sachin.gdrive.common.getActivity
import com.sachin.gdrive.common.log.logD
import com.sachin.gdrive.common.showToast
import com.sachin.gdrive.ui.Destination
import com.sachin.gdrive.ui.widget.RoundButton
import com.sachin.gdrive.ui.widget.TextBox
import org.koin.androidx.compose.getViewModel

@Composable
fun AuthScreen(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val viewModel: AuthViewModel = getViewModel()
    val context = LocalContext.current
    val notLoggedIn = remember { mutableStateOf(true) }

    val gsoContract = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        viewModel.onSignIn(result.data)
    }
    SetupBackPress()
    SetupObservers(navController, viewModel) {
        notLoggedIn.value = true
    }

    CreateLoginScreen(modifier) {
        if (notLoggedIn.value) {
            viewModel.getSignInIntent()?.let {
                gsoContract.launch(it)
            } ?: context.showToast("Please try again later")
        }
    }
}

@Composable
fun CreateLoginScreen(
    modifier: Modifier = Modifier,
    onLoginClick: () -> Unit
) {
    Column(modifier = modifier.fillMaxSize()) {
        Spacer(
            modifier = Modifier
                .height(250.dp)
                .fillMaxWidth()
        )
        TextBox(
            fontSize = 25.sp,
            text = "Welcome Back!",
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(
            modifier = Modifier
                .height(30.dp)
                .fillMaxWidth()
        )
        RoundButton(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            content = {
                TextBox(text = "Login") {
                    onLoginClick()
                }
            }, onClick = { onLoginClick() })
    }
}

@Composable
private fun SetupObservers(
    navController: NavHostController,
    viewModel: AuthViewModel,
    notLoggedIn: () -> Unit
) {
    val context = LocalContext.current
    viewModel.authState.observeAsState().value?.let { state ->
        logD { "current auth state: $state" }
        when (state) {
            is AuthState.SignInSuccess -> {
                context.showToast("Login success!")
                navController.navigate(Destination.DASHBOARD_SCREEN.name)
            }

            is AuthState.AlreadyLoggedIn -> {
                navController.navigate(Destination.DASHBOARD_SCREEN.name)
            }

            is AuthState.SignInFailed -> {
                context.showToast(state.error)
            }

            is AuthState.NotLoggedIn -> {
                notLoggedIn()
            }
        }

    }
}

@Composable
private fun SetupBackPress() {
    val ctx = LocalContext.current
    BackHandler {
        ctx.getActivity()?.finish()
    }
}

@Preview(showSystemUi = true)
@Composable
private fun Preview() {
    CreateLoginScreen {
    }
}
