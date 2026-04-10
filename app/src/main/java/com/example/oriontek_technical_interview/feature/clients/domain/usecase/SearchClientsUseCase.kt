package com.example.oriontek_technical_interview.feature.clients.domain.usecase

import com.example.oriontek_technical_interview.core.common.Result
import com.example.oriontek_technical_interview.feature.clients.domain.model.Client
import com.example.oriontek_technical_interview.feature.clients.domain.repository.ClientRepository
import javax.inject.Inject

/*
    Caso de uso para buscar clientes por nombre.
    Valida que el término de búsqueda no esté vacío y
    normaliza el input eliminando espacios innecesarios.
 */
class SearchClientsUseCase @Inject constructor(
    private val repository: ClientRepository
) {
    suspend operator fun invoke(query: String): Result<List<Client>> {
        val trimmed = query.trim()

        if (trimmed.isBlank()) {
            return Result.Error("El término de búsqueda no puede estar vacío")
        }

        return Result.runCatching {
            repository.searchClientsByName(trimmed)
        }
    }
}
