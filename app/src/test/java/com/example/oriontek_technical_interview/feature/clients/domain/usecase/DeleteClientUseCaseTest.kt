package com.example.oriontek_technical_interview.feature.clients.domain.usecase

import com.example.oriontek_technical_interview.core.common.Result
import com.example.oriontek_technical_interview.feature.clients.domain.MockClientRepository
import com.example.oriontek_technical_interview.feature.clients.domain.TestClientFactory
import com.example.oriontek_technical_interview.feature.clients.domain.model.ClientId
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class DeleteClientUseCaseTest {

    private lateinit var repository: MockClientRepository
    private lateinit var useCase: DeleteClientUseCase

    @Before
    fun setUp() {
        repository = MockClientRepository()
        useCase = DeleteClientUseCase(repository)
    }

    @Test
    fun `eliminar cliente existente retorna Success`() = runTest {
        val client = TestClientFactory.createClient()
        repository.setClients(listOf(client))

        val result = useCase(client.id)

        assertTrue(result.isSuccess)
        assertTrue(repository.getClients().isEmpty())
    }

    @Test
    fun `eliminar cliente inexistente retorna Error`() = runTest {
        val result = useCase(ClientId("no-existe"))

        assertTrue(result.isError)
        val error = result as Result.Error
        assertTrue(error.message.contains("no existe"))
    }

    @Test
    fun `eliminar cliente no afecta otros clientes`() = runTest {
        val client1 = TestClientFactory.createClient(id = ClientId("id-1"), name = "Cliente 1")
        val client2 = TestClientFactory.createClient(id = ClientId("id-2"), name = "Cliente 2")
        repository.setClients(listOf(client1, client2))

        useCase(client1.id)

        val remaining = repository.getClients()
        assertEquals(1, remaining.size)
        assertEquals("Cliente 2", remaining[0].name)
    }
}
