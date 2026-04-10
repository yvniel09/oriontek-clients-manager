package com.example.oriontek_technical_interview.feature.clients.domain.repository

import com.example.oriontek_technical_interview.feature.clients.domain.model.Address
import com.example.oriontek_technical_interview.feature.clients.domain.model.Client
import com.example.oriontek_technical_interview.feature.clients.domain.model.ClientId
import kotlinx.coroutines.flow.Flow

/*
    Interfaz del repositorio de clientes. Define las operaciones
    disponibles para la gestión de clientes y sus direcciones.

    Utiliza Flow para observación reactiva y suspend para
    operaciones asíncronas con coroutines.
 */
interface ClientRepository {

    // ── Operaciones de Cliente ──────────────────────────────────

    /*
        Observa la lista completa de clientes de forma reactiva.
        Emite una nueva lista cada vez que hay un cambio en los datos.
     */
    fun observeClients(): Flow<List<Client>>

    /*
        Obtiene todos los clientes de forma puntual (sin observación reactiva).
     */
    suspend fun getClients(): List<Client>

    /*
        Obtiene un cliente por su identificador único.

        @param id Identificador del cliente
        @return El cliente encontrado o null si no existe
     */
    suspend fun getClientById(id: ClientId): Client?

    /*
        Busca clientes cuyo nombre contenga el texto proporcionado.

        @param query Texto de búsqueda
        @return Lista de clientes que coinciden con la búsqueda
     */
    suspend fun searchClientsByName(query: String): List<Client>

    /*
        Crea un nuevo cliente con sus direcciones.

        @param client El cliente a crear
     */
    suspend fun createClient(client: Client)

    /*
        Actualiza los datos de un cliente existente.

        @param client El cliente con los datos actualizados
     */
    suspend fun updateClient(client: Client)

    /*
        Elimina un cliente y todas sus direcciones asociadas.

        @param id Identificador del cliente a eliminar
     */
    suspend fun deleteClient(id: ClientId)

    // ── Operaciones de Direcciones ──────────────────────────────

    /*
        Obtiene todas las direcciones asociadas a un cliente.

        @param clientId Identificador del cliente
        @return Lista de direcciones del cliente
     */
    suspend fun getAddressesByClientId(clientId: ClientId): List<Address>

    /*
        Agrega una nueva dirección a un cliente existente.

        @param clientId Identificador del cliente
        @param address La dirección a agregar
     */
    suspend fun addAddress(clientId: ClientId, address: Address)

    /*
        Agrega múltiples direcciones a un cliente existente.

        @param clientId Identificador del cliente
        @param addresses Lista de direcciones a agregar
     */
    suspend fun addAddresses(clientId: ClientId, addresses: List<Address>)

    /*
        Actualiza una dirección específica de un cliente.

        @param clientId Identificador del cliente
        @param oldAddress La dirección actual a reemplazar
        @param newAddress La nueva dirección
     */
    suspend fun updateAddress(clientId: ClientId, oldAddress: Address, newAddress: Address)

    /*
        Elimina una dirección específica de un cliente.
        El cliente debe mantener al menos una dirección.

        @param clientId Identificador del cliente
        @param address La dirección a eliminar
        @throws IllegalStateException si el cliente solo tiene una dirección
     */
    suspend fun removeAddress(clientId: ClientId, address: Address)

    /*
        Reemplaza todas las direcciones de un cliente con una nueva lista.
        La lista debe contener al menos una dirección.

        @param clientId Identificador del cliente
        @param addresses Nueva lista de direcciones
     */
    suspend fun replaceAddresses(clientId: ClientId, addresses: List<Address>)
}