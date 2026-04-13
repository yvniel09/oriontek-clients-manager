package com.example.oriontek_technical_interview.feature.clients.presentation.client_form

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.oriontek_technical_interview.core.common.Result
import com.example.oriontek_technical_interview.feature.clients.domain.model.Address
import com.example.oriontek_technical_interview.feature.clients.domain.model.ClientId
import com.example.oriontek_technical_interview.feature.clients.domain.model.Email
import com.example.oriontek_technical_interview.feature.clients.domain.model.Identification
import com.example.oriontek_technical_interview.feature.clients.domain.model.PhoneNumber
import com.example.oriontek_technical_interview.feature.clients.domain.usecase.CreateClientUseCase
import com.example.oriontek_technical_interview.feature.clients.domain.usecase.GetClientByIdUseCase
import com.example.oriontek_technical_interview.feature.clients.domain.usecase.UpdateClientUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/*
    Efectos de un solo disparo que la UI debe consumir y no reprocesar.
 */
sealed class ClientFormEffect {
    data object NavigateBack : ClientFormEffect()
    data class ShowSnackbar(val message: String) : ClientFormEffect()
}

@HiltViewModel
class ClientFormViewModel @Inject constructor(
    private val createClientUseCase: CreateClientUseCase,
    private val updateClientUseCase: UpdateClientUseCase,
    private val getClientByIdUseCase: GetClientByIdUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(ClientFormUiState())
    val uiState: StateFlow<ClientFormUiState> = _uiState.asStateFlow()

    private val _effect = Channel<ClientFormEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    private val clientId: String? = savedStateHandle["clientId"]

    init {
        if (clientId != null) {
            _uiState.update { it.copy(mode = FormMode.EDIT, clientId = clientId) }
            loadClient(clientId)
        }
    }

    private fun loadClient(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            when (val result = getClientByIdUseCase(ClientId(id))) {
                is Result.Success -> {
                    val client = result.data
                    val idType = when (client.identificationType) {
                        is Identification.Cedula -> IdentificationTypeUi.CEDULA
                        is Identification.RNC -> IdentificationTypeUi.RNC
                        is Identification.ForeignId -> IdentificationTypeUi.FOREIGN_ID
                        is Identification.SocialWelfareNumber -> IdentificationTypeUi.SOCIAL_WELFARE
                    }
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            name = client.name,
                            identificationType = idType,
                            identificationValue = client.identificationType.value,
                            phoneNumber = client.phoneNumber.value,
                            email = client.email.value,
                            addresses = client.addresses.map { addr -> addr.value }
                        )
                    }
                }
                is Result.Error -> {
                    _uiState.update { it.copy(isLoading = false, formError = result.message) }
                }
            }
        }
    }

    // ── Event handlers ──────────────────────────────────────────

    fun onNameChanged(value: String) {
        _uiState.update { it.copy(name = value, nameError = null) }
    }

    fun onIdentificationTypeChanged(type: IdentificationTypeUi) {
        _uiState.update {
            it.copy(
                identificationType = type,
                identificationValue = "",
                identificationError = null
            )
        }
    }

    fun onIdentificationValueChanged(value: String) {
        _uiState.update { it.copy(identificationValue = value, identificationError = null) }
    }

    fun onPhoneChanged(value: String) {
        _uiState.update { it.copy(phoneNumber = value, phoneError = null) }
    }

    fun onEmailChanged(value: String) {
        _uiState.update { it.copy(email = value, emailError = null) }
    }

    fun onAddressChanged(index: Int, value: String) {
        _uiState.update { state ->
            val updated = state.addresses.toMutableList()
            if (index in updated.indices) {
                updated[index] = value
            }
            state.copy(addresses = updated, addressError = null)
        }
    }

    fun onAddressAdded() {
        _uiState.update { state ->
            state.copy(addresses = state.addresses + "", addressError = null)
        }
    }

    fun onAddressRemoved(index: Int) {
        _uiState.update { state ->
            if (state.addresses.size <= 1) {
                state.copy(addressError = "El cliente debe tener al menos una dirección")
            } else {
                val updated = state.addresses.toMutableList()
                updated.removeAt(index)
                state.copy(addresses = updated, addressError = null)
            }
        }
    }

    fun onSave() {
        val state = _uiState.value
        if (!validateForm(state)) return

        _uiState.update { it.copy(isSaving = true, formError = null) }

        viewModelScope.launch {
            val result = if (state.isEditMode) {
                performUpdate(state)
            } else {
                performCreate(state)
            }

            when (result) {
                is Result.Success -> {
                    _uiState.update { it.copy(isSaving = false) }
                    _effect.send(ClientFormEffect.NavigateBack)
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(isSaving = false, formError = result.message)
                    }
                }
            }
        }
    }

    // ── Validación ──────────────────────────────────────────────

    private fun validateForm(state: ClientFormUiState): Boolean {
        var isValid = true

        if (state.name.isBlank()) {
            _uiState.update { it.copy(nameError = "El nombre es obligatorio") }
            isValid = false
        }

        if (state.identificationValue.isBlank()) {
            _uiState.update { it.copy(identificationError = "La identificación es obligatoria") }
            isValid = false
        } else {
            val idError = validateIdentificationFormat(state.identificationType, state.identificationValue)
            if (idError != null) {
                _uiState.update { it.copy(identificationError = idError) }
                isValid = false
            }
        }

        if (state.phoneNumber.isBlank()) {
            _uiState.update { it.copy(phoneError = "El teléfono es obligatorio") }
            isValid = false
        }

        if (state.email.isBlank()) {
            _uiState.update { it.copy(emailError = "El email es obligatorio") }
            isValid = false
        }

        val nonBlankAddresses = state.addresses.filter { it.isNotBlank() }
        if (nonBlankAddresses.isEmpty()) {
            _uiState.update { it.copy(addressError = "Debe tener al menos una dirección") }
            isValid = false
        }

        return isValid
    }

    private fun validateIdentificationFormat(type: IdentificationTypeUi, value: String): String? {
        return when (type) {
            IdentificationTypeUi.CEDULA -> {
                if (!value.all { it.isDigit() }) "Solo dígitos permitidos"
                else if (value.length != 11) "La cédula debe tener 11 dígitos"
                else null
            }
            IdentificationTypeUi.RNC -> {
                if (!value.all { it.isDigit() }) "Solo dígitos permitidos"
                else if (value.length != 9) "El RNC debe tener 9 dígitos"
                else null
            }
            IdentificationTypeUi.FOREIGN_ID -> {
                if (value.length !in 5..20) "Debe tener entre 5 y 20 caracteres"
                else null
            }
            IdentificationTypeUi.SOCIAL_WELFARE -> {
                if (!value.all { it.isDigit() }) "Solo dígitos permitidos"
                else if (value.length !in 8..12) "Debe tener entre 8 y 12 dígitos"
                else null
            }
        }
    }

    // ── Operaciones de persistencia ─────────────────────────────

    private suspend fun performCreate(state: ClientFormUiState): Result<*> {
        return try {
            val identification = buildIdentification(state.identificationType, state.identificationValue)
            val phone = PhoneNumber(state.phoneNumber)
            val email = Email(state.email)
            val addresses = state.addresses.filter { it.isNotBlank() }.map { Address(it) }

            createClientUseCase(
                name = state.name,
                identificationType = identification,
                addresses = addresses,
                phoneNumber = phone,
                email = email
            )
        } catch (e: IllegalArgumentException) {
            Result.Error(e.message ?: "Error de validación")
        }
    }

    private suspend fun performUpdate(state: ClientFormUiState): Result<*> {
        val id = state.clientId ?: return Result.Error("ID de cliente no disponible")

        return try {
            val clientResult = getClientByIdUseCase(ClientId(id))
            val client = (clientResult as? Result.Success)?.data
                ?: return Result.Error("No se encontró el cliente")

            val identification = buildIdentification(state.identificationType, state.identificationValue)
            val phone = PhoneNumber(state.phoneNumber)
            val email = Email(state.email)
            val addresses = state.addresses.filter { it.isNotBlank() }.map { Address(it) }

            updateClientUseCase(
                client = client,
                newName = state.name,
                newIdentificationType = identification,
                newAddresses = addresses,
                newPhoneNumber = phone,
                newEmail = email
            )
        } catch (e: IllegalArgumentException) {
            Result.Error(e.message ?: "Error de validación")
        }
    }

    private fun buildIdentification(type: IdentificationTypeUi, value: String): Identification {
        return when (type) {
            IdentificationTypeUi.CEDULA -> Identification.Cedula(value)
            IdentificationTypeUi.RNC -> Identification.RNC(value)
            IdentificationTypeUi.FOREIGN_ID -> Identification.ForeignId(value)
            IdentificationTypeUi.SOCIAL_WELFARE -> Identification.SocialWelfareNumber(value)
        }
    }
}
