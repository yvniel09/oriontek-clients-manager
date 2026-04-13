package com.example.oriontek_technical_interview.feature.clients.presentation.client_list

import com.example.oriontek_technical_interview.feature.clients.presentation.model.ClientUiModel

/*
    Estado de UI para la pantalla de lista de clientes.
    Cada propiedad representa un aspecto del estado visible en la pantalla.
 */
data class ClientListUiState(
    val clients: List<ClientUiModel> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val deleteConfirmation: ClientUiModel? = null
)
