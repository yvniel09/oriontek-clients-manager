package com.example.oriontek_technical_interview.feature.clients.domain.usecase

import com.example.oriontek_technical_interview.core.common.Result
import com.example.oriontek_technical_interview.feature.clients.domain.model.Address
import com.example.oriontek_technical_interview.feature.clients.domain.model.ClientId
import com.example.oriontek_technical_interview.feature.clients.domain.repository.ClientRepository
import javax.inject.Inject

/*
    Caso de uso para agregar una o múltiples direcciones a un cliente existente.
    Valida que:
    - El cliente exista
    - Las nuevas direcciones no estén duplicadas entre sí
    - Las nuevas direcciones no dupliquen direcciones existentes del cliente
 */
class AddAddressUseCase @Inject constructor(
    private val repository: ClientRepository
) {
    /*
        Agrega una sola dirección al cliente.
     */
    suspend operator fun invoke(clientId: ClientId, address: Address): Result<Unit> {
        return invoke(clientId, listOf(address))
    }

    /*
        Agrega múltiples direcciones al cliente.
     */
    suspend operator fun invoke(clientId: ClientId, addresses: List<Address>): Result<Unit> {
        // Verificar que el cliente existe
        val client = repository.getClientById(clientId)
            ?: return Result.Error("No se encontró el cliente con ID: ${clientId.value}")

        // Verificar duplicados entre las nuevas direcciones
        val uniqueNew = addresses.distinctBy { it.value.trim().lowercase() }
        if (uniqueNew.size != addresses.size) {
            return Result.Error("Las direcciones proporcionadas contienen duplicados")
        }

        // Verificar que no se dupliquen con las existentes
        val existingNormalized = client.addresses.map { it.value.trim().lowercase() }.toSet()
        val duplicates = uniqueNew.filter { it.value.trim().lowercase() in existingNormalized }
        if (duplicates.isNotEmpty()) {
            return Result.Error(
                "Las siguientes direcciones ya existen para este cliente: ${
                    duplicates.joinToString { it.value }
                }"
            )
        }

        return Result.runCatching {
            repository.addAddresses(clientId, uniqueNew)
        }
    }
}
