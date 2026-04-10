package com.example.oriontek_technical_interview.feature.clients.domain.usecase

import com.example.oriontek_technical_interview.feature.clients.domain.model.Client
import com.example.oriontek_technical_interview.feature.clients.domain.repository.ClientRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/*
    Caso de uso para observar la lista de clientes de forma reactiva.
    Retorna un Flow que emite actualizaciones cada vez que la data cambia.
 */
class ObserveClientsUseCase @Inject constructor(
    private val repository: ClientRepository
) {
    operator fun invoke(): Flow<List<Client>> {
        return repository.observeClients()
    }
}
