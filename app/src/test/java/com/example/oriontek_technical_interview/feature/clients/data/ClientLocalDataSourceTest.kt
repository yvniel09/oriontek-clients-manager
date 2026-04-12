package com.example.oriontek_technical_interview.feature.clients.data

import com.example.oriontek_technical_interview.feature.clients.data.local.ClientLocalDataSource
import com.example.oriontek_technical_interview.feature.clients.data.local.entity.AddressEntity
import com.example.oriontek_technical_interview.feature.clients.data.repository.InMemoryClientRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/*
    Tests del contrato de ClientLocalDataSource.
    Se testea la interfaz, no la implementación concreta.
    InMemoryClientRepository se usa como implementación de referencia;
    si se migra a Room, estos mismos tests deben pasar con la nueva implementación.
 */
class ClientLocalDataSourceTest {

    // Se declara como la interfaz, no como InMemoryClientRepository
    private lateinit var dataSource: ClientLocalDataSource

    @Before
    fun setUp() {
        dataSource = InMemoryClientRepository()
    }

    // ── Insertar y consultar clientes ───────────────────────────

    @Test
    fun `insertClient almacena el cliente correctamente`() = runTest {
        val client = TestEntityFactory.createClientEntity()
        dataSource.insertClient(client)

        val result = dataSource.getClientWithAddressesById(client.id)

        assertNotNull(result)
        assertEquals(client.id, result!!.client.id)
        assertEquals(client.name, result.client.name)
    }

    @Test
    fun `getAllClientsWithAddresses retorna lista vacia inicialmente`() = runTest {
        val result = dataSource.getAllClientsWithAddresses()
        assertTrue(result.isEmpty())
    }

    @Test
    fun `getAllClientsWithAddresses retorna todos los clientes insertados`() = runTest {
        val client1 = TestEntityFactory.createClientEntity(id = "c-001", name = "Cliente Uno")
        val client2 = TestEntityFactory.createClientEntity(id = "c-002", name = "Cliente Dos")
        dataSource.insertClient(client1)
        dataSource.insertClient(client2)

        val result = dataSource.getAllClientsWithAddresses()

        assertEquals(2, result.size)
    }

    @Test
    fun `getClientWithAddressesById retorna null si no existe`() = runTest {
        val result = dataSource.getClientWithAddressesById("id-inexistente")
        assertNull(result)
    }

    @Test
    fun `insertClient con mismo ID reemplaza el anterior`() = runTest {
        val original = TestEntityFactory.createClientEntity(name = "Original")
        val updated = TestEntityFactory.createClientEntity(name = "Actualizado")
        dataSource.insertClient(original)
        dataSource.insertClient(updated)

        val result = dataSource.getClientWithAddressesById(original.id)

        assertEquals("Actualizado", result!!.client.name)
        assertEquals(1, dataSource.getClientCount())
    }

    // ── Actualizar clientes ─────────────────────────────────────

    @Test
    fun `updateClient modifica los datos del cliente`() = runTest {
        val client = TestEntityFactory.createClientEntity()
        dataSource.insertClient(client)

        val updated = client.copy(name = "Nombre Actualizado", email = "nuevo@email.com")
        dataSource.updateClient(updated)

        val result = dataSource.getClientWithAddressesById(client.id)

        assertEquals("Nombre Actualizado", result!!.client.name)
        assertEquals("nuevo@email.com", result.client.email)
    }

    @Test
    fun `updateClient no hace nada si el cliente no existe`() = runTest {
        val ghost = TestEntityFactory.createClientEntity(id = "fantasma")
        dataSource.updateClient(ghost)

        assertEquals(0, dataSource.getClientCount())
    }

    // ── Eliminar clientes ───────────────────────────────────────

    @Test
    fun `deleteClient elimina el cliente`() = runTest {
        val client = TestEntityFactory.createClientEntity()
        dataSource.insertClient(client)
        dataSource.deleteClient(client.id)

        assertNull(dataSource.getClientWithAddressesById(client.id))
        assertEquals(0, dataSource.getClientCount())
    }

    @Test
    fun `deleteClient elimina direcciones asociadas en cascada`() = runTest {
        val client = TestEntityFactory.createClientEntity()
        dataSource.insertClientWithAddresses(
            client,
            TestEntityFactory.createMultipleAddressEntities()
        )

        dataSource.deleteClient(client.id)

        val addresses = dataSource.getAddressesByClientId(client.id)
        assertTrue(addresses.isEmpty())
    }

    @Test
    fun `deleteClient de un ID inexistente no afecta los datos`() = runTest {
        val client = TestEntityFactory.createClientEntity()
        dataSource.insertClient(client)

        dataSource.deleteClient("id-fantasma")

        assertEquals(1, dataSource.getClientCount())
    }

    // ── Búsqueda ────────────────────────────────────────────────

    @Test
    fun `searchClientsByName encuentra coincidencias parciales`() = runTest {
        dataSource.insertClient(TestEntityFactory.createClientEntity(id = "c-1", name = "Juan Pérez"))
        dataSource.insertClient(TestEntityFactory.createClientEntity(id = "c-2", name = "María López"))
        dataSource.insertClient(TestEntityFactory.createClientEntity(id = "c-3", name = "Juan Carlos"))

        val result = dataSource.searchClientsByName("Juan")

        assertEquals(2, result.size)
    }

    @Test
    fun `searchClientsByName es case-insensitive`() = runTest {
        dataSource.insertClient(TestEntityFactory.createClientEntity(name = "Pedro Martínez"))

        val result = dataSource.searchClientsByName("pedro")

        assertEquals(1, result.size)
    }

    @Test
    fun `searchClientsByName retorna lista vacia sin coincidencias`() = runTest {
        dataSource.insertClient(TestEntityFactory.createClientEntity(name = "Juan Pérez"))

        val result = dataSource.searchClientsByName("Inexistente")

        assertTrue(result.isEmpty())
    }

    // ── Utilidades de cliente ────────────────────────────────────

    @Test
    fun `clientExists retorna true si el cliente existe`() = runTest {
        val client = TestEntityFactory.createClientEntity()
        dataSource.insertClient(client)

        assertTrue(dataSource.clientExists(client.id))
    }

    @Test
    fun `clientExists retorna false si no existe`() = runTest {
        assertFalse(dataSource.clientExists("no-existe"))
    }

    @Test
    fun `getClientCount refleja la cantidad correcta`() = runTest {
        assertEquals(0, dataSource.getClientCount())

        dataSource.insertClient(TestEntityFactory.createClientEntity(id = "c-1"))
        assertEquals(1, dataSource.getClientCount())

        dataSource.insertClient(TestEntityFactory.createClientEntity(id = "c-2"))
        assertEquals(2, dataSource.getClientCount())

        dataSource.deleteClient("c-1")
        assertEquals(1, dataSource.getClientCount())
    }

    // ── Insertar direcciones ────────────────────────────────────

    @Test
    fun `insertAddress genera un ID unico y retorna el ID`() = runTest {
        val client = TestEntityFactory.createClientEntity()
        dataSource.insertClient(client)

        val id = dataSource.insertAddress(TestEntityFactory.createAddressEntity())

        assertTrue(id > 0)
    }

    @Test
    fun `insertAddress genera IDs secuenciales`() = runTest {
        val client = TestEntityFactory.createClientEntity()
        dataSource.insertClient(client)

        val id1 = dataSource.insertAddress(TestEntityFactory.createAddressEntity(address = "Calle A"))
        val id2 = dataSource.insertAddress(TestEntityFactory.createAddressEntity(address = "Calle B"))

        assertTrue(id2 > id1)
    }

    @Test
    fun `insertAddresses genera IDs para todas las direcciones`() = runTest {
        val client = TestEntityFactory.createClientEntity()
        dataSource.insertClient(client)

        val ids = dataSource.insertAddresses(
            TestEntityFactory.createMultipleAddressEntities()
        )

        assertEquals(2, ids.size)
        assertTrue(ids.all { it > 0 })
        assertNotEquals(ids[0], ids[1])
    }

    @Test
    fun `insertAddress ignora el ID del entity recibido y genera uno nuevo`() = runTest {
        val client = TestEntityFactory.createClientEntity()
        dataSource.insertClient(client)

        val entityWithId = TestEntityFactory.createAddressEntity(id = 999)
        val generatedId = dataSource.insertAddress(entityWithId)

        assertNotEquals(999L, generatedId)
        val stored = dataSource.getAddressesByClientId(client.id)
        assertEquals(generatedId, stored.first().id)
    }

    // ── Consultar direcciones ───────────────────────────────────

    @Test
    fun `getAddressesByClientId retorna solo las del cliente indicado`() = runTest {
        val client1 = TestEntityFactory.createClientEntity(id = "c-1")
        val client2 = TestEntityFactory.createClientEntity(id = "c-2")
        dataSource.insertClient(client1)
        dataSource.insertClient(client2)

        dataSource.insertAddress(TestEntityFactory.createAddressEntity(clientId = "c-1", address = "Dir A"))
        dataSource.insertAddress(TestEntityFactory.createAddressEntity(clientId = "c-1", address = "Dir B"))
        dataSource.insertAddress(TestEntityFactory.createAddressEntity(clientId = "c-2", address = "Dir C"))

        val addressesC1 = dataSource.getAddressesByClientId("c-1")
        val addressesC2 = dataSource.getAddressesByClientId("c-2")

        assertEquals(2, addressesC1.size)
        assertEquals(1, addressesC2.size)
    }

    @Test
    fun `getAddressesByClientId retorna lista vacia si no tiene direcciones`() = runTest {
        val client = TestEntityFactory.createClientEntity()
        dataSource.insertClient(client)

        val result = dataSource.getAddressesByClientId(client.id)

        assertTrue(result.isEmpty())
    }

    @Test
    fun `getAddressCount retorna la cantidad correcta`() = runTest {
        val client = TestEntityFactory.createClientEntity()
        dataSource.insertClient(client)

        assertEquals(0, dataSource.getAddressCount(client.id))

        dataSource.insertAddress(TestEntityFactory.createAddressEntity(address = "Dir 1"))
        assertEquals(1, dataSource.getAddressCount(client.id))

        dataSource.insertAddress(TestEntityFactory.createAddressEntity(address = "Dir 2"))
        assertEquals(2, dataSource.getAddressCount(client.id))
    }

    // ── Actualizar direcciones ──────────────────────────────────

    @Test
    fun `updateAddress modifica la direccion existente`() = runTest {
        val client = TestEntityFactory.createClientEntity()
        dataSource.insertClient(client)
        val id = dataSource.insertAddress(TestEntityFactory.createAddressEntity(address = "Original"))

        dataSource.updateAddress(AddressEntity(id = id, clientId = client.id, address = "Modificada"))

        val addresses = dataSource.getAddressesByClientId(client.id)
        assertEquals(1, addresses.size)
        assertEquals("Modificada", addresses.first().address)
    }

    @Test
    fun `updateAddress no hace nada si el ID no existe`() = runTest {
        val client = TestEntityFactory.createClientEntity()
        dataSource.insertClient(client)
        dataSource.insertAddress(TestEntityFactory.createAddressEntity(address = "Original"))

        dataSource.updateAddress(AddressEntity(id = 9999, clientId = client.id, address = "Fantasma"))

        val addresses = dataSource.getAddressesByClientId(client.id)
        assertEquals(1, addresses.size)
        assertEquals("Original", addresses.first().address)
    }

    // ── Eliminar direcciones ────────────────────────────────────

    @Test
    fun `deleteAddress elimina solo la direccion indicada`() = runTest {
        val client = TestEntityFactory.createClientEntity()
        dataSource.insertClient(client)
        val id1 = dataSource.insertAddress(TestEntityFactory.createAddressEntity(address = "Dir A"))
        dataSource.insertAddress(TestEntityFactory.createAddressEntity(address = "Dir B"))

        dataSource.deleteAddress(id1)

        val remaining = dataSource.getAddressesByClientId(client.id)
        assertEquals(1, remaining.size)
        assertEquals("Dir B", remaining.first().address)
    }

    @Test
    fun `deleteAddressesByClientId elimina todas las del cliente`() = runTest {
        val client = TestEntityFactory.createClientEntity()
        dataSource.insertClient(client)
        dataSource.insertAddresses(TestEntityFactory.createMultipleAddressEntities())

        dataSource.deleteAddressesByClientId(client.id)

        assertEquals(0, dataSource.getAddressCount(client.id))
    }

    @Test
    fun `deleteAddressesByClientId no afecta direcciones de otros clientes`() = runTest {
        dataSource.insertClient(TestEntityFactory.createClientEntity(id = "c-1"))
        dataSource.insertClient(TestEntityFactory.createClientEntity(id = "c-2"))
        dataSource.insertAddress(TestEntityFactory.createAddressEntity(clientId = "c-1", address = "Dir 1"))
        dataSource.insertAddress(TestEntityFactory.createAddressEntity(clientId = "c-2", address = "Dir 2"))

        dataSource.deleteAddressesByClientId("c-1")

        assertEquals(0, dataSource.getAddressCount("c-1"))
        assertEquals(1, dataSource.getAddressCount("c-2"))
    }

    // ── Reemplazar direcciones ──────────────────────────────────

    @Test
    fun `replaceAddresses elimina las anteriores e inserta las nuevas`() = runTest {
        val client = TestEntityFactory.createClientEntity()
        dataSource.insertClient(client)
        dataSource.insertAddresses(TestEntityFactory.createMultipleAddressEntities())

        val newAddresses = listOf(
            TestEntityFactory.createAddressEntity(address = "Nueva Dir 1"),
            TestEntityFactory.createAddressEntity(address = "Nueva Dir 2"),
            TestEntityFactory.createAddressEntity(address = "Nueva Dir 3")
        )
        dataSource.replaceAddresses(client.id, newAddresses)

        val result = dataSource.getAddressesByClientId(client.id)
        assertEquals(3, result.size)
        assertTrue(result.any { it.address == "Nueva Dir 1" })
        assertTrue(result.any { it.address == "Nueva Dir 2" })
        assertTrue(result.any { it.address == "Nueva Dir 3" })
    }

    @Test
    fun `replaceAddresses genera nuevos IDs para las direcciones reemplazadas`() = runTest {
        val client = TestEntityFactory.createClientEntity()
        dataSource.insertClient(client)
        val oldIds = dataSource.insertAddresses(TestEntityFactory.createMultipleAddressEntities())

        dataSource.replaceAddresses(client.id, listOf(
            TestEntityFactory.createAddressEntity(address = "Reemplazo")
        ))

        val result = dataSource.getAddressesByClientId(client.id)
        assertTrue(result.none { it.id in oldIds })
    }

    // ── Operaciones transaccionales ─────────────────────────────

    @Test
    fun `insertClientWithAddresses inserta cliente y direcciones atomicamente`() = runTest {
        val client = TestEntityFactory.createClientEntity()
        val addresses = TestEntityFactory.createMultipleAddressEntities()

        dataSource.insertClientWithAddresses(client, addresses)

        val result = dataSource.getClientWithAddressesById(client.id)
        assertNotNull(result)
        assertEquals(client.name, result!!.client.name)
        assertEquals(2, result.addresses.size)
        assertTrue(result.addresses.all { it.id > 0 })
    }

    @Test
    fun `insertClientWithAddresses asigna el clientId correcto a cada direccion`() = runTest {
        val client = TestEntityFactory.createClientEntity(id = "cli-test")
        val addresses = TestEntityFactory.createMultipleAddressEntities(clientId = "cualquier-cosa")

        dataSource.insertClientWithAddresses(client, addresses)

        val result = dataSource.getAddressesByClientId("cli-test")
        assertEquals(2, result.size)
        assertTrue(result.all { it.clientId == "cli-test" })
    }

    @Test
    fun `updateClientWithAddresses actualiza datos y reemplaza direcciones`() = runTest {
        val client = TestEntityFactory.createClientEntity()
        dataSource.insertClientWithAddresses(client, TestEntityFactory.createMultipleAddressEntities())

        val updatedClient = client.copy(name = "Nombre Nuevo")
        val newAddresses = listOf(
            TestEntityFactory.createAddressEntity(address = "Dirección Única Nueva")
        )
        dataSource.updateClientWithAddresses(updatedClient, newAddresses)

        val result = dataSource.getClientWithAddressesById(client.id)
        assertEquals("Nombre Nuevo", result!!.client.name)
        assertEquals(1, result.addresses.size)
        assertEquals("Dirección Única Nueva", result.addresses.first().address)
    }

    @Test
    fun `updateClientWithAddresses no hace nada si el cliente no existe`() = runTest {
        val ghost = TestEntityFactory.createClientEntity(id = "fantasma")
        dataSource.updateClientWithAddresses(ghost, listOf(
            TestEntityFactory.createAddressEntity(address = "Dir fantasma")
        ))

        assertNull(dataSource.getClientWithAddressesById("fantasma"))
        assertEquals(0, dataSource.getClientCount())
    }

    @Test
    fun `deleteClientWithAddresses elimina cliente y direcciones`() = runTest {
        val client = TestEntityFactory.createClientEntity()
        dataSource.insertClientWithAddresses(client, TestEntityFactory.createMultipleAddressEntities())

        dataSource.deleteClientWithAddresses(client.id)

        assertNull(dataSource.getClientWithAddressesById(client.id))
        assertTrue(dataSource.getAddressesByClientId(client.id).isEmpty())
    }

    // ── Observación reactiva ────────────────────────────────────

    @Test
    fun `observeAllClientsWithAddresses emite lista vacia inicialmente`() = runTest {
        val result = dataSource.observeAllClientsWithAddresses().first()
        assertTrue(result.isEmpty())
    }

    @Test
    fun `observeAllClientsWithAddresses emite actualizaciones tras insercion`() = runTest {
        val client = TestEntityFactory.createClientEntity()
        dataSource.insertClientWithAddresses(client, TestEntityFactory.createMultipleAddressEntities())

        val result = dataSource.observeAllClientsWithAddresses().first()

        assertEquals(1, result.size)
        assertEquals(client.name, result.first().client.name)
        assertEquals(2, result.first().addresses.size)
    }

    @Test
    fun `observeAllClientsWithAddresses refleja eliminaciones`() = runTest {
        val client = TestEntityFactory.createClientEntity()
        dataSource.insertClient(client)

        dataSource.deleteClient(client.id)

        val result = dataSource.observeAllClientsWithAddresses().first()
        assertTrue(result.isEmpty())
    }

    // ── Consulta incluye direcciones correctamente ──────────────

    @Test
    fun `getClientWithAddressesById incluye las direcciones del cliente`() = runTest {
        val client = TestEntityFactory.createClientEntity()
        dataSource.insertClient(client)
        dataSource.insertAddress(TestEntityFactory.createAddressEntity(address = "Dir 1"))
        dataSource.insertAddress(TestEntityFactory.createAddressEntity(address = "Dir 2"))

        val result = dataSource.getClientWithAddressesById(client.id)

        assertEquals(2, result!!.addresses.size)
    }

    @Test
    fun `searchClientsByName incluye las direcciones de cada resultado`() = runTest {
        val client = TestEntityFactory.createClientEntity(name = "Buscable")
        dataSource.insertClientWithAddresses(
            client,
            TestEntityFactory.createMultipleAddressEntities()
        )

        val results = dataSource.searchClientsByName("Buscable")

        assertEquals(1, results.size)
        assertEquals(2, results.first().addresses.size)
    }
}
