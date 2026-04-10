package com.example.oriontek_technical_interview.feature.clients.domain.usecase

import com.example.oriontek_technical_interview.core.common.Result
import com.example.oriontek_technical_interview.feature.clients.domain.MockClientRepository
import com.example.oriontek_technical_interview.feature.clients.domain.TestClientFactory
import com.example.oriontek_technical_interview.feature.clients.domain.model.ClientId
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class SearchClientsUseCaseTest {

    private lateinit var repository: MockClientRepository
    private lateinit var useCase: SearchClientsUseCase

    @Before
    fun setUp() {
        repository = MockClientRepository()
        useCase = SearchClientsUseCase(repository)
    }

    @Test
    fun `buscar con query valido retorna clientes que coinciden`() = runTest {
        repository.setClients(listOf(
            TestClientFactory.createClient(id = ClientId("1"), name = "Juan Pérez"),
            TestClientFactory.createClient(id = ClientId("2"), name = "María García"),
            TestClientFactory.createClient(id = ClientId("3"), name = "Juan Carlos")
        ))

        val result = useCase("Juan")

        assertTrue(result.isSuccess)
        val clients = (result as Result.Success).data
        assertEquals(2, clients.size)
    }

    @Test
    fun `buscar con query vacio retorna Error`() = runTest {
        val result = useCase("   ")

        assertTrue(result.isError)
        val error = result as Result.Error
        assertTrue(error.message.contains("búsqueda"))
    }

    @Test
    fun `buscar sin coincidencias retorna lista vacia`() = runTest {
        repository.setClients(listOf(
            TestClientFactory.createClient(name = "Pedro")
        ))

        val result = useCase("xyz")

        assertTrue(result.isSuccess)
        val clients = (result as Result.Success).data
        assertTrue(clients.isEmpty())
    }

    @Test
    fun `buscar trimea el query antes de ejecutar`() = runTest {
        repository.setClients(listOf(
            TestClientFactory.createClient(name = "Ana López")
        ))

        val result = useCase("  Ana  ")

        assertTrue(result.isSuccess)
        assertEquals(1, (result as Result.Success).data.size)
    }
}
