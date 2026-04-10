package com.example.oriontek_technical_interview.feature.clients.domain.usecase

import com.example.oriontek_technical_interview.core.common.Result
import com.example.oriontek_technical_interview.feature.clients.domain.model.Client
import com.example.oriontek_technical_interview.feature.clients.domain.model.ClientId
import com.example.oriontek_technical_interview.feature.clients.domain.repository.ClientRepository
import javax.inject.Inject

/*
    Caso de uso para obtener un cliente específico por su ID.
    Maneja el caso donde el cliente no existe retornando un error descriptivo.
 */
class GetClientByIdUseCase @Inject constructor(
    private val repository: ClientRepository
) {
    suspend operator fun invoke(id: ClientId): Result<Client> {
        return Result.runCatching {
            repository.getClientById(id)
                ?: throw NoSuchElementException("No se encontró el cliente con ID: ${id.value}")
        }
    }
}
