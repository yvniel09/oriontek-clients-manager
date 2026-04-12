package com.example.oriontek_technical_interview.feature.clients.data.local

import com.example.oriontek_technical_interview.feature.clients.data.local.entity.AddressEntity
import com.example.oriontek_technical_interview.feature.clients.data.local.entity.ClientEntity
import com.example.oriontek_technical_interview.feature.clients.data.local.entity.ClientWithAddresses
import kotlinx.coroutines.flow.Flow

/*
    Interfaz desacoplada y agnóstica que define las operaciones de bajo nivel
    con la fuente de datos local. Permite implementar diferentes estrategias
    de persistencia (Room, archivos, memoria, etc.) sin afectar el resto
    de la arquitectura.

    Opera exclusivamente con entidades de datos (ClientEntity, AddressEntity),
    dejando el mapeo a modelos de dominio a la capa de repositorio.
 */
interface ClientLocalDataSource {

    // ── Operaciones de Cliente ──────────────────────────────────

    /*
        Observa todos los clientes con sus direcciones de forma reactiva.
        Emite una nueva lista ante cualquier cambio en clientes o direcciones.

        @return Flow con la lista de clientes y sus direcciones asociadas
     */
    fun observeAllClientsWithAddresses(): Flow<List<ClientWithAddresses>>

    /*
        Obtiene todos los clientes con sus direcciones de forma puntual.

        @return Lista de clientes con sus direcciones
     */
    suspend fun getAllClientsWithAddresses(): List<ClientWithAddresses>

    /*
        Obtiene un cliente con sus direcciones por su identificador.

        @param clientId Identificador del cliente
        @return El cliente con direcciones o null si no existe
     */
    suspend fun getClientWithAddressesById(clientId: String): ClientWithAddresses?

    /*
        Busca clientes cuyo nombre contenga el texto proporcionado.
        La búsqueda no distingue entre mayúsculas y minúsculas.

        @param query Texto de búsqueda
        @return Lista de clientes con sus direcciones que coinciden
     */
    suspend fun searchClientsByName(query: String): List<ClientWithAddresses>

    /*
        Inserta un nuevo cliente en la fuente de datos.
        Si ya existe un cliente con el mismo ID, lo reemplaza.

        @param client Entidad del cliente a insertar
     */
    suspend fun insertClient(client: ClientEntity)

    /*
        Actualiza los datos de un cliente existente.

        @param client Entidad del cliente con los datos actualizados
     */
    suspend fun updateClient(client: ClientEntity)

    /*
        Elimina un cliente por su identificador.
        Las direcciones asociadas deben eliminarse en cascada
        o ser manejadas por la implementación.

        @param clientId Identificador del cliente a eliminar
     */
    suspend fun deleteClient(clientId: String)

    /*
        Verifica si existe un cliente con el identificador dado.

        @param clientId Identificador del cliente
        @return true si el cliente existe, false en caso contrario
     */
    suspend fun clientExists(clientId: String): Boolean

    /*
        Obtiene la cantidad total de clientes almacenados.

        @return Número total de clientes
     */
    suspend fun getClientCount(): Int

    // ── Operaciones de Direcciones ──────────────────────────────

    /*
        Obtiene todas las direcciones asociadas a un cliente.

        @param clientId Identificador del cliente
        @return Lista de entidades de dirección
     */
    suspend fun getAddressesByClientId(clientId: String): List<AddressEntity>

    /*
        Inserta una nueva dirección asociada a un cliente.

        @param address Entidad de dirección a insertar
        @return ID generado de la dirección insertada
     */
    suspend fun insertAddress(address: AddressEntity): Long

    /*
        Inserta múltiples direcciones asociadas a un cliente.

        @param addresses Lista de entidades de dirección a insertar
        @return Lista de IDs generados
     */
    suspend fun insertAddresses(addresses: List<AddressEntity>): List<Long>

    /*
        Actualiza una dirección existente.

        @param address Entidad de dirección con los datos actualizados
     */
    suspend fun updateAddress(address: AddressEntity)

    /*
        Elimina una dirección por su ID.

        @param addressId Identificador de la dirección a eliminar
     */
    suspend fun deleteAddress(addressId: Long)

    /*
        Elimina todas las direcciones asociadas a un cliente.

        @param clientId Identificador del cliente
     */
    suspend fun deleteAddressesByClientId(clientId: String)

    /*
        Reemplaza todas las direcciones de un cliente con una nueva lista.
        Elimina las existentes e inserta las nuevas en una operación atómica.

        @param clientId Identificador del cliente
        @param addresses Nueva lista de direcciones
     */
    suspend fun replaceAddresses(clientId: String, addresses: List<AddressEntity>)

    /*
        Obtiene la cantidad de direcciones de un cliente.

        @param clientId Identificador del cliente
        @return Número de direcciones del cliente
     */
    suspend fun getAddressCount(clientId: String): Int

    // ── Operaciones transaccionales ─────────────────────────────

    /*
        Inserta un cliente junto con todas sus direcciones en una
        operación atómica. Si alguna parte falla, se revierte todo.

        @param client Entidad del cliente
        @param addresses Lista de direcciones del cliente
     */
    suspend fun insertClientWithAddresses(client: ClientEntity, addresses: List<AddressEntity>)

    /*
        Actualiza un cliente y reemplaza todas sus direcciones
        en una operación atómica.

        @param client Entidad del cliente actualizada
        @param addresses Nueva lista de direcciones
     */
    suspend fun updateClientWithAddresses(client: ClientEntity, addresses: List<AddressEntity>)

    /*
        Elimina un cliente y todas sus direcciones en una operación atómica.

        @param clientId Identificador del cliente
     */
    suspend fun deleteClientWithAddresses(clientId: String)
}
