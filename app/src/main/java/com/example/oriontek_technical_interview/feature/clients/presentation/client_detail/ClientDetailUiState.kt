package com.example.oriontek_technical_interview.feature.clients.presentation.client_detail

import com.example.oriontek_technical_interview.feature.clients.presentation.model.ClientUiModel

/*
    Estado de UI para la pantalla de detalle de cliente.
    Muestra la información completa de un solo cliente.
 */
data class ClientDetailUiState(
    val client: ClientUiModel? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)
