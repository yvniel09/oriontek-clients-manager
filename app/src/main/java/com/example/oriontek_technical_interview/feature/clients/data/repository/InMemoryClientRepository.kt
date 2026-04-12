package com.example.oriontek_technical_interview.feature.clients.data.repository

import com.example.oriontek_technical_interview.feature.clients.data.local.ClientLocalDataSource
import com.example.oriontek_technical_interview.feature.clients.data.local.entity.AddressEntity
import com.example.oriontek_technical_interview.feature.clients.data.local.entity.ClientEntity
import com.example.oriontek_technical_interview.feature.clients.data.local.entity.ClientWithAddresses
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Inject
import javax.inject.Singleton

/*
    Implementación en memoria de ClientLocalDataSource.
    Utiliza estructuras concurrentes para garantizar seguridad
    entre coroutines y un contador atómico para la generación
    de IDs de AddressEntity (simulando el auto-increment de una DB).

    Esta implementación es volátil: los datos se pierden al
    cerrar la aplicación. Ideal para desarrollo, pruebas y prototipos.
 */
@Singleton
class InMemoryClientRepository @Inject constructor() : ClientLocalDataSource {

    // Almacén principal de clientes indexado por clientId
    private val clients = mutableMapOf<String, ClientEntity>()

    // Almacén de direcciones indexado por addressId
    private val addresses = mutableMapOf<Long, AddressEntity>()

    // Generador de IDs secuenciales para AddressEntity (simula auto-increment)
    private val addressIdGenerator = AtomicLong(1)

    // Mutex para operaciones thread-safe con coroutines
    private val mutex = Mutex()

    // Flow reactivo que emite la lista completa ante cada modificación
    private val dataFlow = MutableStateFlow<List<ClientWithAddresses>>(emptyList())

    // ── Utilidad interna ────────────────────────────────────────

    /*
        Genera el siguiente ID disponible para una dirección.
     */
    private fun nextAddressId(): Long = addressIdGenerator.getAndIncrement()

    /*
        Reconstruye la lista de ClientWithAddresses y emite al Flow.
        Debe llamarse dentro del mutex tras cualquier modificación.
     */
    private fun emitSnapshot() {
        val snapshot = clients.values.map { client ->
            ClientWithAddresses(
                client = client,
                addresses = addresses.values
                    .filter { it.clientId == client.id }
                    .sortedBy { it.id }
            )
        }
        dataFlow.value = snapshot
    }

    // ── Operaciones de Cliente ──────────────────────────────────

    override fun observeAllClientsWithAddresses(): Flow<List<ClientWithAddresses>> {
        return dataFlow
    }

    override suspend fun getAllClientsWithAddresses(): List<ClientWithAddresses> {
        return mutex.withLock {
            clients.values.map { client ->
                ClientWithAddresses(
                    client = client,
                    addresses = addresses.values
                        .filter { it.clientId == client.id }
                        .sortedBy { it.id }
                )
            }
        }
    }

    override suspend fun getClientWithAddressesById(clientId: String): ClientWithAddresses? {
        return mutex.withLock {
            val client = clients[clientId] ?: return@withLock null
            ClientWithAddresses(
                client = client,
                addresses = addresses.values
                    .filter { it.clientId == clientId }
                    .sortedBy { it.id }
            )
        }
    }

    override suspend fun searchClientsByName(query: String): List<ClientWithAddresses> {
        val lowerQuery = query.lowercase()
        return mutex.withLock {
            clients.values
                .filter { it.name.lowercase().contains(lowerQuery) }
                .map { client ->
                    ClientWithAddresses(
                        client = client,
                        addresses = addresses.values
                            .filter { it.clientId == client.id }
                            .sortedBy { it.id }
                    )
                }
        }
    }

    override suspend fun insertClient(client: ClientEntity) {
        mutex.withLock {
            clients[client.id] = client
            emitSnapshot()
        }
    }

    override suspend fun updateClient(client: ClientEntity) {
        mutex.withLock {
            if (clients.containsKey(client.id)) {
                clients[client.id] = client
                emitSnapshot()
            }
        }
    }

    override suspend fun deleteClient(clientId: String) {
        mutex.withLock {
            clients.remove(clientId)
            // Eliminar direcciones asociadas en cascada
            val toRemove = addresses.values.filter { it.clientId == clientId }.map { it.id }
            toRemove.forEach { addresses.remove(it) }
            emitSnapshot()
        }
    }

    override suspend fun clientExists(clientId: String): Boolean {
        return mutex.withLock {
            clients.containsKey(clientId)
        }
    }

    override suspend fun getClientCount(): Int {
        return mutex.withLock {
            clients.size
        }
    }

    // ── Operaciones de Direcciones ──────────────────────────────

    override suspend fun getAddressesByClientId(clientId: String): List<AddressEntity> {
        return mutex.withLock {
            addresses.values
                .filter { it.clientId == clientId }
                .sortedBy { it.id }
        }
    }

    override suspend fun insertAddress(address: AddressEntity): Long {
        return mutex.withLock {
            val id = nextAddressId()
            val entity = address.copy(id = id)
            addresses[id] = entity
            emitSnapshot()
            id
        }
    }

    override suspend fun insertAddresses(addresses: List<AddressEntity>): List<Long> {
        return mutex.withLock {
            val ids = addresses.map { address ->
                val id = nextAddressId()
                val entity = address.copy(id = id)
                this.addresses[id] = entity
                id
            }
            emitSnapshot()
            ids
        }
    }

    override suspend fun updateAddress(address: AddressEntity) {
        mutex.withLock {
            if (addresses.containsKey(address.id)) {
                addresses[address.id] = address
                emitSnapshot()
            }
        }
    }

    override suspend fun deleteAddress(addressId: Long) {
        mutex.withLock {
            addresses.remove(addressId)
            emitSnapshot()
        }
    }

    override suspend fun deleteAddressesByClientId(clientId: String) {
        mutex.withLock {
            val toRemove = addresses.values.filter { it.clientId == clientId }.map { it.id }
            toRemove.forEach { addresses.remove(it) }
            emitSnapshot()
        }
    }

    override suspend fun replaceAddresses(clientId: String, addresses: List<AddressEntity>) {
        mutex.withLock {
            // Eliminar las existentes
            val toRemove = this.addresses.values.filter { it.clientId == clientId }.map { it.id }
            toRemove.forEach { this.addresses.remove(it) }

            // Insertar las nuevas con IDs generados
            addresses.forEach { address ->
                val id = nextAddressId()
                this.addresses[id] = address.copy(id = id, clientId = clientId)
            }
            emitSnapshot()
        }
    }

    override suspend fun getAddressCount(clientId: String): Int {
        return mutex.withLock {
            addresses.values.count { it.clientId == clientId }
        }
    }

    // ── Operaciones transaccionales ─────────────────────────────

    override suspend fun insertClientWithAddresses(
        client: ClientEntity,
        addresses: List<AddressEntity>
    ) {
        mutex.withLock {
            // Insertar cliente
            clients[client.id] = client

            // Insertar direcciones con IDs generados
            addresses.forEach { address ->
                val id = nextAddressId()
                this.addresses[id] = address.copy(id = id, clientId = client.id)
            }
            emitSnapshot()
        }
    }

    override suspend fun updateClientWithAddresses(
        client: ClientEntity,
        addresses: List<AddressEntity>
    ) {
        mutex.withLock {
            if (!clients.containsKey(client.id)) return@withLock

            // Actualizar cliente
            clients[client.id] = client

            // Reemplazar direcciones
            val toRemove = this.addresses.values.filter { it.clientId == client.id }.map { it.id }
            toRemove.forEach { this.addresses.remove(it) }

            addresses.forEach { address ->
                val id = nextAddressId()
                this.addresses[id] = address.copy(id = id, clientId = client.id)
            }
            emitSnapshot()
        }
    }

    override suspend fun deleteClientWithAddresses(clientId: String) {
        mutex.withLock {
            clients.remove(clientId)
            val toRemove = addresses.values.filter { it.clientId == clientId }.map { it.id }
            toRemove.forEach { addresses.remove(it) }
            emitSnapshot()
        }
    }
}