package com.example.oriontek_technical_interview.feature.clients.presentation.client_list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.oriontek_technical_interview.core.common.Result
import com.example.oriontek_technical_interview.feature.clients.domain.model.ClientId
import com.example.oriontek_technical_interview.feature.clients.domain.usecase.DeleteClientUseCase
import com.example.oriontek_technical_interview.feature.clients.domain.usecase.ObserveClientsUseCase
import com.example.oriontek_technical_interview.feature.clients.domain.usecase.SearchClientsUseCase
import com.example.oriontek_technical_interview.feature.clients.presentation.model.ClientUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ClientListViewModel @Inject constructor(
    private val observeClientsUseCase: ObserveClientsUseCase,
    private val searchClientsUseCase: SearchClientsUseCase,
    private val deleteClientUseCase: DeleteClientUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ClientListUiState())
    val uiState: StateFlow<ClientListUiState> = _uiState.asStateFlow()

    // Lista completa en memoria para filtrar localmente
    private var allClients: List<ClientUiModel> = emptyList()

    init {
        observeClients()
    }

    private fun observeClients() {
        observeClientsUseCase()
            .onStart {
                _uiState.update { it.copy(isLoading = true) }
            }
            .onEach { clients ->
                allClients = clients.map { ClientUiModel.fromDomain(it) }
                applySearchFilter()
                _uiState.update { it.copy(isLoading = false, error = null) }
            }
            .catch { throwable ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = throwable.message ?: "Error desconocido al cargar clientes"
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        if (query.isBlank()) {
            _uiState.update { it.copy(clients = allClients) }
        } else {
            viewModelScope.launch {
                when (val result = searchClientsUseCase(query)) {
                    is Result.Success -> {
                        val filtered = result.data.map { ClientUiModel.fromDomain(it) }
                        _uiState.update { it.copy(clients = filtered) }
                    }
                    is Result.Error -> {
                        // Si la búsqueda falla (query vacío después de trim), mostramos todos
                        _uiState.update { it.copy(clients = allClients) }
                    }
                }
            }
        }
    }

    fun onDeleteRequested(client: ClientUiModel) {
        _uiState.update { it.copy(deleteConfirmation = client) }
    }

    fun onDeleteDismissed() {
        _uiState.update { it.copy(deleteConfirmation = null) }
    }

    fun onDeleteConfirmed() {
        val client = _uiState.value.deleteConfirmation ?: return
        _uiState.update { it.copy(deleteConfirmation = null) }

        viewModelScope.launch {
            when (val result = deleteClientUseCase(ClientId(client.id))) {
                is Result.Success -> {
                    // La lista se actualiza automáticamente via el Flow de observeClients
                }
                is Result.Error -> {
                    _uiState.update { it.copy(error = result.message) }
                }
            }
        }
    }

    fun onErrorDismissed() {
        _uiState.update { it.copy(error = null) }
    }

    private fun applySearchFilter() {
        val query = _uiState.value.searchQuery
        if (query.isBlank()) {
            _uiState.update { it.copy(clients = allClients) }
        } else {
            val filtered = allClients.filter {
                it.name.contains(query, ignoreCase = true)
            }
            _uiState.update { it.copy(clients = filtered) }
        }
    }
}
