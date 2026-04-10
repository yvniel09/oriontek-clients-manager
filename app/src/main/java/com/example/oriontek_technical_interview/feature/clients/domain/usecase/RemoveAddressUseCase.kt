package com.example.oriontek_technical_interview.feature.clients.domain.usecase

import com.example.oriontek_technical_interview.core.common.Result
import com.example.oriontek_technical_interview.feature.clients.domain.model.Address
import com.example.oriontek_technical_interview.feature.clients.domain.model.ClientId
import com.example.oriontek_technical_interview.feature.clients.domain.repository.ClientRepository
import javax.inject.Inject

/*
    Caso de uso para eliminar una dirección de un cliente.
    Valida que:
    - El cliente exista
    - La dirección exista en el cliente
    - El cliente mantenga al menos una dirección después de la eliminación
 */
class RemoveAddressUseCase @Inject constructor(
    private val repository: ClientRepository
) {
    suspend operator fun invoke(clientId: ClientId, address: Address): Result<Unit> {
        // Verificar que el cliente existe
        val client = repository.getClientById(clientId)
            ?: return Result.Error("No se encontró el cliente con ID: ${clientId.value}")

        // Verificar que la dirección exista en el cliente
        val addressExists = client.addresses.any {
            it.value.trim().lowercase() == address.value.trim().lowercase()
        }
        if (!addressExists) {
            return Result.Error("La dirección '${address.value}' no pertenece a este cliente")
        }

        // Verificar que no quede sin direcciones
        if (client.addresses.size <= 1) {
            return Result.Error(
                "No se puede eliminar la dirección: el cliente debe tener al menos una dirección"
            )
        }

        return Result.runCatching {
            repository.removeAddress(clientId, address)
        }
    }
}
