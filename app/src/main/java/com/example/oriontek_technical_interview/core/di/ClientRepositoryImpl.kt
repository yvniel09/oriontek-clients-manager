package com.example.oriontek_technical_interview.core.di

import com.example.oriontek_technical_interview.feature.clients.data.local.ClientLocalDataSource
import com.example.oriontek_technical_interview.feature.clients.data.local.entity.AddressEntity
import com.example.oriontek_technical_interview.feature.clients.data.local.entity.ClientEntity
import com.example.oriontek_technical_interview.feature.clients.domain.model.Address
import com.example.oriontek_technical_interview.feature.clients.domain.model.Client
import com.example.oriontek_technical_interview.feature.clients.domain.model.ClientId
import com.example.oriontek_technical_interview.feature.clients.domain.model.Email
import com.example.oriontek_technical_interview.feature.clients.domain.model.Identification
import com.example.oriontek_technical_interview.feature.clients.domain.model.PhoneNumber
import com.example.oriontek_technical_interview.feature.clients.domain.repository.ClientRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/*
    Adaptador que conecta ClientLocalDataSource (capa de datos)
    con ClientRepository (capa de dominio). Realiza el mapeo
    entre entidades de datos y modelos de dominio.
 */
class ClientRepositoryImpl(
    private val localDataSource: ClientLocalDataSource
) : ClientRepository {

    // ── Operaciones de Cliente ──────────────────────────────────

    override fun observeClients(): Flow<List<Client>> {
        return localDataSource.observeAllClientsWithAddresses().map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun getClients(): List<Client> {
        return localDataSource.getAllClientsWithAddresses().map { it.toDomain() }
    }

    override suspend fun getClientById(id: ClientId): Client? {
        return localDataSource.getClientWithAddressesById(id.value)?.toDomain()
    }

    override suspend fun searchClientsByName(query: String): List<Client> {
        return localDataSource.searchClientsByName(query).map { it.toDomain() }
    }

    override suspend fun createClient(client: Client) {
        localDataSource.insertClientWithAddresses(
            client = client.toEntity(),
            addresses = client.addresses.map { it.toEntity(client.id.value) }
        )
    }

    override suspend fun updateClient(client: Client) {
        localDataSource.updateClientWithAddresses(
            client = client.toEntity(),
            addresses = client.addresses.map { it.toEntity(client.id.value) }
        )
    }

    override suspend fun deleteClient(id: ClientId) {
        localDataSource.deleteClientWithAddresses(id.value)
    }

    // ── Operaciones de Direcciones ──────────────────────────────

    override suspend fun getAddressesByClientId(clientId: ClientId): List<Address> {
        return localDataSource.getAddressesByClientId(clientId.value).map {
            Address(it.address)
        }
    }

    override suspend fun addAddress(clientId: ClientId, address: Address) {
        localDataSource.insertAddress(address.toEntity(clientId.value))
    }

    override suspend fun addAddresses(clientId: ClientId, addresses: List<Address>) {
        localDataSource.insertAddresses(addresses.map { it.toEntity(clientId.value) })
    }

    override suspend fun updateAddress(clientId: ClientId, oldAddress: Address, newAddress: Address) {
        val existing = localDataSource.getAddressesByClientId(clientId.value)
        val target = existing.find {
            it.address.trim().lowercase() == oldAddress.value.trim().lowercase()
        } ?: return
        localDataSource.updateAddress(target.copy(address = newAddress.value))
    }

    override suspend fun removeAddress(clientId: ClientId, address: Address) {
        val existing = localDataSource.getAddressesByClientId(clientId.value)
        val target = existing.find {
            it.address.trim().lowercase() == address.value.trim().lowercase()
        } ?: return
        localDataSource.deleteAddress(target.id)
    }

    override suspend fun replaceAddresses(clientId: ClientId, addresses: List<Address>) {
        localDataSource.replaceAddresses(
            clientId.value,
            addresses.map { it.toEntity(clientId.value) }
        )
    }
}

// ── Funciones de mapeo ──────────────────────────────────────

private fun com.example.oriontek_technical_interview.feature.clients.data.local.entity.ClientWithAddresses.toDomain(): Client {
    return Client(
        id = ClientId(client.id),
        identificationType = parseIdentification(client.identificationType, client.identificationValue),
        name = client.name,
        addresses = addresses.map { Address(it.address) },
        phoneNumber = PhoneNumber(client.phoneNumber),
        email = Email(client.email)
    )
}

private fun parseIdentification(type: String, value: String): Identification {
    return when (type) {
        "Cedula" -> Identification.Cedula(value)
        "RNC" -> Identification.RNC(value)
        "ForeignId" -> Identification.ForeignId(value)
        "SocialWelfareNumber" -> Identification.SocialWelfareNumber(value)
        else -> Identification.Cedula(value)
    }
}

private fun Client.toEntity(): ClientEntity {
    val (idType, idValue) = when (identificationType) {
        is Identification.Cedula -> "Cedula" to identificationType.value
        is Identification.RNC -> "RNC" to identificationType.value
        is Identification.ForeignId -> "ForeignId" to identificationType.value
        is Identification.SocialWelfareNumber -> "SocialWelfareNumber" to identificationType.value
    }
    return ClientEntity(
        id = id.value,
        name = name,
        identificationType = idType,
        identificationValue = idValue,
        phoneNumber = phoneNumber.value,
        email = email.value
    )
}

private fun Address.toEntity(clientId: String): AddressEntity {
    return AddressEntity(id = 0, clientId = clientId, address = value)
}
