package com.example.oriontek_technical_interview.app.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.oriontek_technical_interview.feature.clients.presentation.client_detail.ClientDetailScreen
import com.example.oriontek_technical_interview.feature.clients.presentation.client_detail.ClientDetailViewModel
import com.example.oriontek_technical_interview.feature.clients.presentation.client_form.ClientFormScreen
import com.example.oriontek_technical_interview.feature.clients.presentation.client_form.ClientFormViewModel
import com.example.oriontek_technical_interview.feature.clients.presentation.client_list.ClientListScreen
import com.example.oriontek_technical_interview.feature.clients.presentation.client_list.ClientListViewModel

@Composable
fun AppNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.ClientList.route
    ) {
        // ── Lista de clientes ───────────────────────────────────
        composable(route = Screen.ClientList.route) {
            val viewModel: ClientListViewModel = hiltViewModel()
            ClientListScreen(
                viewModel = viewModel,
                onNavigateToCreate = {
                    navController.navigate(Screen.ClientForm.createRoute())
                },
                onNavigateToDetail = { clientId ->
                    navController.navigate(Screen.ClientDetail.createRoute(clientId))
                }
            )
        }

        // ── Detalle de cliente ──────────────────────────────────
        composable(
            route = Screen.ClientDetail.route,
            arguments = listOf(
                navArgument("clientId") { type = NavType.StringType }
            )
        ) {
            val viewModel: ClientDetailViewModel = hiltViewModel()
            ClientDetailScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEdit = { clientId ->
                    navController.navigate(Screen.ClientForm.createRoute(clientId))
                }
            )
        }

        // ── Formulario de cliente (crear / editar) ──────────────
        composable(
            route = Screen.ClientForm.route,
            arguments = listOf(
                navArgument("clientId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) {
            val viewModel: ClientFormViewModel = hiltViewModel()
            ClientFormScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
