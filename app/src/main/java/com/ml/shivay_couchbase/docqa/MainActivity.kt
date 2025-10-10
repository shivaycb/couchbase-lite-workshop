package com.ml.shivay_couchbase.docqa

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ml.shivay_couchbase.docqa.ui.screens.ChatScreen
import com.ml.couchbase.docqa.ui.screens.DocsScreen
import com.ml.shivay_couchbase.docqa.ui.screens.edit_credentials.EditCredentialsScreen
import com.ml.shivay_couchbase.docqa.ui.screens.local_models.LocalModelsScreen
import com.ml.shivay_couchbase.docqa.ui.viewmodels.ChatViewModel
import com.ml.couchbase.docqa.ui.viewmodels.DocsViewModel
import com.ml.shivay_couchbase.docqa.ui.screens.local_models.LocalModelsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.serialization.Serializable

@Serializable
object ChatRoute

@Serializable
object EditAPIKeyRoute

@Serializable
object DocsRoute

@Serializable
object LocalModelsRoute

sealed class ChatNavEvent {
    data object None : ChatNavEvent()
    data object ToDocsScreen : ChatNavEvent()
    data object ToEditAPIKeyScreen : ChatNavEvent()
    data object ToLocalModelsScreen : ChatNavEvent()
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val navHostController = rememberNavController()
            NavHost(
                navController = navHostController,
                startDestination = ChatRoute,
                enterTransition = { fadeIn() },
                exitTransition = { fadeOut() },
            ) {
                composable<DocsRoute> {
                    DocsScreen(onBackClick = { navHostController.navigateUp() })
                }
                composable<EditAPIKeyRoute> { 
                    EditCredentialsScreen(onBackClick = { navHostController.navigateUp() }) 
                }
                composable<LocalModelsRoute> { backStackEntry ->
                    val viewModel: LocalModelsViewModel = hiltViewModel(backStackEntry)
                    val uiState by viewModel.uiState.collectAsState()
                    LocalModelsScreen(
                        uiState = uiState,
                        onEvent = viewModel::onEvent,
                        onBackClick = { navHostController.navigateUp() },
                    )
                }
                composable<ChatRoute> { backStackEntry ->
                    val viewModel: ChatViewModel = hiltViewModel(backStackEntry)
                    val chatScreenUIState by viewModel.chatScreenUIState.collectAsState()
                    val navEvent by viewModel.navEventChannel.collectAsState(ChatNavEvent.None)
                    LaunchedEffect(navEvent) {
                        when (navEvent) {
                            is ChatNavEvent.ToDocsScreen -> {
                                navHostController.navigate(DocsRoute)
                            }

                            is ChatNavEvent.ToEditAPIKeyScreen -> {
                                navHostController.navigate(EditAPIKeyRoute)
                            }

                            is ChatNavEvent.ToLocalModelsScreen -> {
                                navHostController.navigate(LocalModelsRoute)
                            }

                            is ChatNavEvent.None -> {}
                        }
                    }
                    ChatScreen(
                        screenUiState = chatScreenUIState,
                        onScreenEvent = { viewModel.onChatScreenEvent(it) },
                    )
                }
            }
        }
    }
}
