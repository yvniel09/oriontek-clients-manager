package com.example.oriontek_technical_interview.feature.clients.domain

import com.example.oriontek_technical_interview.feature.clients.domain.model.Address
import com.example.oriontek_technical_interview.feature.clients.domain.model.Client
import com.example.oriontek_technical_interview.feature.clients.domain.model.ClientId
import com.example.oriontek_technical_interview.feature.clients.domain.repository.ClientRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

/*
    Implementación falsa del repositorio para pruebas unitarias.
    Usa un MutableStateFlow internamente para simular
    el comportamiento reactivo sin necesidad de Room ni data layer.
 */
class MockClientRepository : ClientRepository {

    private val clients = MutableStateFlow<List<Client>>(emptyList())

    // Helpers para configurar estado en tests

    fun setClients(list: List<Client>) {
        clients.value = list
    }

    fun clear() {
        clients.value = emptyList()
    }

    // Implementación del contrato

    override fun observeClients(): Flow<List<Client>> = clients

    override suspend fun getClients(): List<Client> = clients.value

    override suspend fun getClientById(id: ClientId): Client? {
        return clients.value.find { it.id == id }
    }

    override suspend fun searchClientsByName(query: String): List<Client> {
        return clients.value.filter {
            it.name.contains(query, ignoreCase = true)
        }
    }

    override suspend fun createClient(client: Client) {
        clients.update { current -> current + client }
    }

    override suspend fun updateClient(client: Client) {
        clients.update { current ->
            current.map { if (it.id == client.id) client else it }
        }
    }

    override suspend fun deleteClient(id: ClientId) {
        clients.update { current -> current.filter { it.id != id } }
    }

    override suspend fun getAddressesByClientId(clientId: ClientId): List<Address> {
        return clients.value.find { it.id == clientId }?.addresses ?: emptyList()
    }

    override suspend fun addAddress(clientId: ClientId, address: Address) {
        clients.update { current ->
            current.map {
                if (it.id == clientId) it.copy(addresses = it.addresses + address) else it
            }
        }
    }

    override suspend fun addAddresses(clientId: ClientId, addresses: List<Address>) {
        clients.update { current ->
            current.map {
                if (it.id == clientId) it.copy(addresses = it.addresses + addresses) else it
            }
        }
    }

    override suspend fun updateAddress(clientId: ClientId, oldAddress: Address, newAddress: Address) {
        clients.update { current ->
            current.map { client ->
                if (client.id == clientId) {
                    client.copy(
                        addresses = client.addresses.map {
                            if (it.value.trim().lowercase() == oldAddress.value.trim().lowercase()) newAddress else it
                        }
                    )
                } else client
            }
        }
    }

    override suspend fun removeAddress(clientId: ClientId, address: Address) {
        clients.update { current ->
            current.map { client ->
                if (client.id == clientId) {
                    client.copy(
                        addresses = client.addresses.filter {
                            it.value.trim().lowercase() != address.value.trim().lowercase()
                        }
                    )
                } else client
            }
        }
    }

    override suspend fun replaceAddresses(clientId: ClientId, addresses: List<Address>) {
        clients.update { current ->
            current.map {
                if (it.id == clientId) it.copy(addresses = addresses) else it
            }
        }
    }
}
