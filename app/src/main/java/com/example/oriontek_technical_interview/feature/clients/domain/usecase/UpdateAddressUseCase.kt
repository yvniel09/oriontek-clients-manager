package com.example.oriontek_technical_interview.feature.clients.domain.usecase

import com.example.oriontek_technical_interview.core.common.Result
import com.example.oriontek_technical_interview.feature.clients.domain.model.Address
import com.example.oriontek_technical_interview.feature.clients.domain.model.ClientId
import com.example.oriontek_technical_interview.feature.clients.domain.repository.ClientRepository
import javax.inject.Inject

/*
    Caso de uso para actualizar una dirección específica de un cliente.
    Valida que:
    - El cliente exista
    - La dirección original exista en el cliente
    - La nueva dirección no duplique otra dirección existente
 */
class UpdateAddressUseCase @Inject constructor(
    private val repository: ClientRepository
) {
    suspend operator fun invoke(
        clientId: ClientId,
        oldAddress: Address,
        newAddress: Address
    ): Result<Unit> {
        // Verificar que el cliente existe
        val client = repository.getClientById(clientId)
            ?: return Result.Error("No se encontró el cliente con ID: ${clientId.value}")

        // Verificar que la dirección original exista
        val oldExists = client.addresses.any {
            it.value.trim().lowercase() == oldAddress.value.trim().lowercase()
        }
        if (!oldExists) {
            return Result.Error("La dirección '${oldAddress.value}' no pertenece a este cliente")
        }

        // Verificar que la nueva dirección no duplique otra existente (excluyendo la original)
        val otherAddresses = client.addresses.filter {
            it.value.trim().lowercase() != oldAddress.value.trim().lowercase()
        }
        val isDuplicate = otherAddresses.any {
            it.value.trim().lowercase() == newAddress.value.trim().lowercase()
        }
        if (isDuplicate) {
            return Result.Error("La dirección '${newAddress.value}' ya existe para este cliente")
        }

        return Result.runCatching {
            repository.updateAddress(clientId, oldAddress, newAddress)
        }
    }
}
