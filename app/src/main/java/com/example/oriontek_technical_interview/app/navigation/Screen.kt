package com.example.oriontek_technical_interview.app.navigation

/*
    Rutas de navegación de la aplicación.
    Cada objeto representa un destino en el NavGraph.
 */
sealed class Screen(val route: String) {
    data object ClientList : Screen("client_list")
    data object ClientDetail : Screen("client_detail/{clientId}") {
        fun createRoute(clientId: String) = "client_detail/$clientId"
    }
    data object ClientForm : Screen("client_form?clientId={clientId}") {
        fun createRoute(clientId: String? = null): String {
            return if (clientId != null) "client_form?clientId=$clientId"
            else "client_form"
        }
    }
}
