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

class RemoveAddressUseCaseTest {

    private lateinit var repository: MockClientRepository
    private lateinit var useCase: RemoveAddressUseCase

    @Before
    fun setUp() {
        repository = MockClientRepository()
        useCase = RemoveAddressUseCase(repository)
    }

    @Test
    fun `eliminar direccion de cliente con multiples direcciones retorna Success`() = runTest {
        val client = TestClientFactory.createClientWithMultipleAddresses()
        repository.setClients(listOf(client))

        val addressToRemove = client.addresses[0]
        val result = useCase(client.id, addressToRemove)

        assertTrue(result.isSuccess)
        val updated = repository.getClientById(client.id)!!
        assertEquals(1, updated.addresses.size)
    }

    @Test
    fun `eliminar unica direccion retorna Error`() = runTest {
        val client = TestClientFactory.createClient() // 1 sola dirección
        repository.setClients(listOf(client))

        val result = useCase(client.id, client.addresses[0])

        assertTrue(result.isError)
        assertTrue((result as Result.Error).message.contains("al menos una"))
    }

    @Test
    fun `eliminar direccion de cliente inexistente retorna Error`() = runTest {
        val result = useCase(ClientId("no-existe"), Address("Calle Foo"))

        assertTrue(result.isError)
        assertTrue((result as Result.Error).message.contains("No se encontró"))
    }

    @Test
    fun `eliminar direccion que no pertenece al cliente retorna Error`() = runTest {
        val client = TestClientFactory.createClientWithMultipleAddresses()
        repository.setClients(listOf(client))

        val result = useCase(client.id, Address("Dirección Fantasma"))

        assertTrue(result.isError)
        assertTrue((result as Result.Error).message.contains("no pertenece"))
    }
}
