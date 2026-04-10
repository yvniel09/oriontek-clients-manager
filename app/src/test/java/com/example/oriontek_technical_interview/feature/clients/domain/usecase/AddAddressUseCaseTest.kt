package com.example.oriontek_technical_interview.feature.clients.domain.usecase

import com.example.oriontek_technical_interview.core.common.Result
import com.example.oriontek_technical_interview.feature.clients.domain.MockClientRepository
import com.example.oriontek_technical_interview.feature.clients.domain.TestClientFactory
import com.example.oriontek_technical_interview.feature.clients.domain.model.Address
import com.example.oriontek_technical_interview.feature.clients.domain.model.ClientId
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class AddAddressUseCaseTest {

    private lateinit var repository: MockClientRepository
    private lateinit var useCase: AddAddressUseCase

    @Before
    fun setUp() {
        repository = MockClientRepository()
        useCase = AddAddressUseCase(repository)
    }

    @Test
    fun `agregar direccion a cliente existente retorna Success`() = runTest {
        val client = TestClientFactory.createClient()
        repository.setClients(listOf(client))

        val newAddress = Address("Av. Independencia #500")
        val result = useCase(client.id, newAddress)

        assertTrue(result.isSuccess)
        val updated = repository.getClientById(client.id)!!
        assertEquals(2, updated.addresses.size)
    }

    @Test
    fun `agregar direccion a cliente inexistente retorna Error`() = runTest {
        val result = useCase(ClientId("no-existe"), Address("Calle Foo"))

        assertTrue(result.isError)
        assertTrue((result as Result.Error).message.contains("No se encontró"))
    }

    @Test
    fun `agregar direccion duplicada retorna Error`() = runTest {
        val client = TestClientFactory.createClient(
            addresses = listOf(Address("Calle Principal #1"))
        )
        repository.setClients(listOf(client))

        val result = useCase(client.id, Address("calle principal #1"))

        assertTrue(result.isError)
        assertTrue((result as Result.Error).message.contains("ya existen"))
    }

    @Test
    fun `agregar multiples direcciones validas retorna Success`() = runTest {
        val client = TestClientFactory.createClient()
        repository.setClients(listOf(client))

        val newAddresses = listOf(
            Address("Dirección Nueva 1"),
            Address("Dirección Nueva 2")
        )
        val result = useCase(client.id, newAddresses)

        assertTrue(result.isSuccess)
        val updated = repository.getClientById(client.id)!!
        assertEquals(3, updated.addresses.size) // 1 original + 2 nuevas
    }

    @Test
    fun `agregar direcciones con duplicados entre si retorna Error`() = runTest {
        val client = TestClientFactory.createClient()
        repository.setClients(listOf(client))

        val result = useCase(client.id, listOf(
            Address("Misma Calle"),
            Address("misma calle")
        ))

        assertTrue(result.isError)
        assertTrue((result as Result.Error).message.contains("duplicados"))
    }
}
