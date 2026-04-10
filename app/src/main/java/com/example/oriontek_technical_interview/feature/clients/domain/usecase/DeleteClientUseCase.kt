package com.example.oriontek_technical_interview.feature.clients.domain.usecase

import com.example.oriontek_technical_interview.core.common.Result
import com.example.oriontek_technical_interview.feature.clients.domain.model.ClientId
import com.example.oriontek_technical_interview.feature.clients.domain.repository.ClientRepository
import javax.inject.Inject

/*
    Caso de uso para eliminar un cliente y todas sus direcciones asociadas.
    Verifica que el cliente exista antes de intentar eliminarlo.
 */
class DeleteClientUseCase @Inject constructor(
    private val repository: ClientRepository
) {
    suspend operator fun invoke(id: ClientId): Result<Unit> {
        // Verificar que el cliente existe antes de eliminar
        val existingClient = repository.getClientById(id)
            ?: return Result.Error("No se puede eliminar: el cliente con ID ${id.value} no existe")

        return Result.runCatching {
            repository.deleteClient(id)
        }
    }
}
