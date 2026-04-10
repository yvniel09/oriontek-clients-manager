package com.example.oriontek_technical_interview.feature.clients.domain.usecase

import com.example.oriontek_technical_interview.core.common.Result
import com.example.oriontek_technical_interview.feature.clients.domain.MockClientRepository
import com.example.oriontek_technical_interview.feature.clients.domain.TestClientFactory
import com.example.oriontek_technical_interview.feature.clients.domain.model.ClientId
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class GetClientByIdUseCaseTest {

    private lateinit var repository: MockClientRepository
    private lateinit var useCase: GetClientByIdUseCase

    @Before
    fun setUp() {
        repository = MockClientRepository()
        useCase = GetClientByIdUseCase(repository)
    }

    @Test
    fun `obtener cliente existente retorna Success con datos correctos`() = runTest {
        val client = TestClientFactory.createClient()
        repository.setClients(listOf(client))

        val result = useCase(client.id)

        assertTrue(result.isSuccess)
        val found = (result as Result.Success).data
        assertEquals(client.name, found.name)
        assertEquals(client.id, found.id)
    }

    @Test
    fun `obtener cliente inexistente retorna Error`() = runTest {
        val result = useCase(ClientId("id-fantasma"))

        assertTrue(result.isError)
        val error = result as Result.Error
        assertTrue(error.message.contains("No se encontró"))
    }

    @Test
    fun `obtener por id retorna el cliente correcto entre varios`() = runTest {
        val target = TestClientFactory.createClient(id = ClientId("target"), name = "Buscado")
        val other = TestClientFactory.createClient(id = ClientId("other"), name = "Otro")
        repository.setClients(listOf(other, target))

        val result = useCase(ClientId("target"))

        val found = (result as Result.Success).data
        assertEquals("Buscado", found.name)
    }
}
