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

class UpdateAddressUseCaseTest {

    private lateinit var repository: MockClientRepository
    private lateinit var useCase: UpdateAddressUseCase

    @Before
    fun setUp() {
        repository = MockClientRepository()
        useCase = UpdateAddressUseCase(repository)
    }

    @Test
    fun `actualizar direccion valida retorna Success`() = runTest {
        val client = TestClientFactory.createClient(
            addresses = listOf(Address("Dirección Original"))
        )
        repository.setClients(listOf(client))

        val result = useCase(client.id, Address("Dirección Original"), Address("Dirección Nueva"))

        assertTrue(result.isSuccess)
        val updated = repository.getClientById(client.id)!!
        assertEquals("Dirección Nueva", updated.addresses[0].value)
    }

    @Test
    fun `actualizar direccion de cliente inexistente retorna Error`() = runTest {
        val result = useCase(
            ClientId("no-existe"),
            Address("Vieja"),
            Address("Nueva")
        )

        assertTrue(result.isError)
        assertTrue((result as Result.Error).message.contains("No se encontró"))
    }

    @Test
    fun `actualizar direccion que no existe en el cliente retorna Error`() = runTest {
        val client = TestClientFactory.createClient(
            addresses = listOf(Address("Dirección Real"))
        )
        repository.setClients(listOf(client))

        val result = useCase(client.id, Address("No Existe"), Address("Nueva"))

        assertTrue(result.isError)
        assertTrue((result as Result.Error).message.contains("no pertenece"))
    }

    @Test
    fun `actualizar a direccion que ya existe retorna Error`() = runTest {
        val client = TestClientFactory.createClient(
            addresses = listOf(
                Address("Dirección A"),
                Address("Dirección B")
            )
        )
        repository.setClients(listOf(client))

        // Intentar cambiar "Dirección A" a "Dirección B" (duplicaría)
        val result = useCase(client.id, Address("Dirección A"), Address("Dirección B"))

        assertTrue(result.isError)
        assertTrue((result as Result.Error).message.contains("ya existe"))
    }

    @Test
    fun `actualizar direccion a si misma con diferente case funciona`() = runTest {
        val client = TestClientFactory.createClient(
            addresses = listOf(Address("calle principal"))
        )
        repository.setClients(listOf(client))

        // Cambiar el formato/case de la misma dirección — no debería ser "duplicada"
        val result = useCase(client.id, Address("calle principal"), Address("Calle Principal"))

        assertTrue(result.isSuccess)
    }
}
