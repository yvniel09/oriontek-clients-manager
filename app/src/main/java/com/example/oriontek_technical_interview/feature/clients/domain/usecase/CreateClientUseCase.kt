package com.example.oriontek_technical_interview.feature.clients.domain.usecase

import com.example.oriontek_technical_interview.core.common.Result
import com.example.oriontek_technical_interview.feature.clients.domain.model.Address
import com.example.oriontek_technical_interview.feature.clients.domain.model.Client
import com.example.oriontek_technical_interview.feature.clients.domain.model.ClientId
import com.example.oriontek_technical_interview.feature.clients.domain.model.Email
import com.example.oriontek_technical_interview.feature.clients.domain.model.Identification
import com.example.oriontek_technical_interview.feature.clients.domain.model.PhoneNumber
import com.example.oriontek_technical_interview.feature.clients.domain.repository.ClientRepository
import javax.inject.Inject

/*
    Caso de uso para crear un nuevo cliente.
    Valida las reglas de negocio antes de delegar al repositorio:
    - El nombre no puede estar vacío
    - Debe tener al menos una dirección
    - No se permiten direcciones duplicadas
    - Genera un ID único automáticamente

    La validación de formato de email, teléfono e identificación
    se delega a los value objects del modelo de dominio.
 */
class CreateClientUseCase @Inject constructor(
    private val repository: ClientRepository
) {
    suspend operator fun invoke(
        name: String,
        identificationType: Identification,
        addresses: List<Address>,
        phoneNumber: PhoneNumber,
        email: Email
    ): Result<Client> {
        // Validar nombre
        val trimmedName = name.trim()
        if (trimmedName.isBlank()) {
            return Result.Error("El nombre del cliente no puede estar vacío")
        }

        // Validar direcciones
        if (addresses.isEmpty()) {
            return Result.Error("El cliente debe tener al menos una dirección")
        }

        // Verificar direcciones duplicadas
        val uniqueAddresses = addresses.distinctBy { it.value.trim().lowercase() }
        if (uniqueAddresses.size != addresses.size) {
            return Result.Error("El cliente tiene direcciones duplicadas")
        }

        return Result.runCatching {
            val client = Client(
                id = ClientId.generate(),
                identificationType = identificationType,
                name = trimmedName,
                addresses = uniqueAddresses,
                phoneNumber = phoneNumber,
                email = email
            )
            repository.createClient(client)
            client
        }
    }
}
