package com.example.oriontek_technical_interview.feature.clients.domain.usecase

import com.example.oriontek_technical_interview.core.common.Result
import com.example.oriontek_technical_interview.feature.clients.domain.model.Address
import com.example.oriontek_technical_interview.feature.clients.domain.model.Client
import com.example.oriontek_technical_interview.feature.clients.domain.model.Email
import com.example.oriontek_technical_interview.feature.clients.domain.model.Identification
import com.example.oriontek_technical_interview.feature.clients.domain.model.PhoneNumber
import com.example.oriontek_technical_interview.feature.clients.domain.repository.ClientRepository
import javax.inject.Inject

/*
    Caso de uso para actualizar los datos de un cliente existente.
    Verifica que el cliente exista antes de actualizar y
    valida las mismas reglas de negocio que al crear.
 */
class UpdateClientUseCase @Inject constructor(
    private val repository: ClientRepository
) {
    suspend operator fun invoke(
        client: Client,
        newName: String? = null,
        newIdentificationType: Identification? = null,
        newAddresses: List<Address>? = null,
        newPhoneNumber: PhoneNumber? = null,
        newEmail: Email? = null
    ): Result<Client> {
        // Verificar que el cliente existe
        val existingClient = repository.getClientById(client.id)
            ?: return Result.Error("No se encontró el cliente con ID: ${client.id.value}")

        // Validar nombre si se proporciona uno nuevo
        val updatedName = newName?.trim() ?: existingClient.name
        if (updatedName.isBlank()) {
            return Result.Error("El nombre del cliente no puede estar vacío")
        }

        // Validar direcciones si se proporcionan nuevas
        val updatedAddresses = newAddresses ?: existingClient.addresses
        if (updatedAddresses.isEmpty()) {
            return Result.Error("El cliente debe tener al menos una dirección")
        }

        // Verificar direcciones duplicadas
        val uniqueAddresses = updatedAddresses.distinctBy { it.value.trim().lowercase() }
        if (uniqueAddresses.size != updatedAddresses.size) {
            return Result.Error("El cliente tiene direcciones duplicadas")
        }

        return Result.runCatching {
            val updatedClient = existingClient.copy(
                name = updatedName,
                identificationType = newIdentificationType ?: existingClient.identificationType,
                addresses = uniqueAddresses,
                phoneNumber = newPhoneNumber ?: existingClient.phoneNumber,
                email = newEmail ?: existingClient.email
            )
            repository.updateClient(updatedClient)
            updatedClient
        }
    }
}
