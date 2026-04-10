package com.example.oriontek_technical_interview.feature.clients.domain.usecase

import com.example.oriontek_technical_interview.feature.clients.domain.MockClientRepository
import com.example.oriontek_technical_interview.feature.clients.domain.TestClientFactory
import com.example.oriontek_technical_interview.feature.clients.domain.model.ClientId
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class ObserveClientsUseCaseTest {

    private lateinit var repository: MockClientRepository
    private lateinit var useCase: ObserveClientsUseCase

    @Before
    fun setUp() {
        repository = MockClientRepository()
        useCase = ObserveClientsUseCase(repository)
    }

    @Test
    fun `observar emite lista vacia cuando no hay clientes`() = runTest {
        val result = useCase().first()
        assertTrue(result.isEmpty())
    }

    @Test
    fun `observar emite la lista actual de clientes`() = runTest {
        val client1 = TestClientFactory.createClient(id = ClientId("1"), name = "Cliente A")
        val client2 = TestClientFactory.createClient(id = ClientId("2"), name = "Cliente B")
        repository.setClients(listOf(client1, client2))

        val result = useCase().first()

        assertEquals(2, result.size)
        assertEquals("Cliente A", result[0].name)
        assertEquals("Cliente B", result[1].name)
    }
}
