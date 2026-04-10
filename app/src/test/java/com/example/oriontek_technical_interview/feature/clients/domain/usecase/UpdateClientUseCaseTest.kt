package com.example.oriontek_technical_interview.feature.clients.domain.usecase

import com.example.oriontek_technical_interview.core.common.Result
import com.example.oriontek_technical_interview.feature.clients.domain.MockClientRepository
import com.example.oriontek_technical_interview.feature.clients.domain.TestClientFactory
import com.example.oriontek_technical_interview.feature.clients.domain.model.Address
import com.example.oriontek_technical_interview.feature.clients.domain.model.ClientId
import com.example.oriontek_technical_interview.feature.clients.domain.model.Email
import com.example.oriontek_technical_interview.feature.clients.domain.model.Identification
import com.example.oriontek_technical_interview.feature.clients.domain.model.PhoneNumber
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class UpdateClientUseCaseTest {

    private lateinit var repository: MockClientRepository
    private lateinit var useCase: UpdateClientUseCase

    @Before
    fun setUp() {
        repository = MockClientRepository()
        useCase = UpdateClientUseCase(repository)
    }

    @Test
    fun `actualizar nombre de cliente existente retorna Success`() = runTest {
        val client = TestClientFactory.createClient()
        repository.setClients(listOf(client))

        val result = useCase(client, newName = "Nombre Actualizado")

        assertTrue(result.isSuccess)
        val updated = (result as Result.Success).data
        assertEquals("Nombre Actualizado", updated.name)
    }

    @Test
    fun `actualizar cliente inexistente retorna Error`() = runTest {
        val fakeClient = TestClientFactory.createClient(id = ClientId("no-existe"))

        val result = useCase(fakeClient, newName = "Nuevo")

        assertTrue(result.isError)
        assertTrue((result as Result.Error).message.contains("No se encontró"))
    }

    @Test
    fun `actualizar con nombre vacio retorna Error`() = runTest {
        val client = TestClientFactory.createClient()
        repository.setClients(listOf(client))

        val result = useCase(client, newName = "   ")

        assertTrue(result.isError)
        assertTrue((result as Result.Error).message.contains("nombre"))
    }

    @Test
    fun `actualizar con direcciones vacias retorna Error`() = runTest {
        val client = TestClientFactory.createClient()
        repository.setClients(listOf(client))

        val result = useCase(client, newAddresses = emptyList())

        assertTrue(result.isError)
        assertTrue((result as Result.Error).message.contains("dirección"))
    }

    @Test
    fun `actualizar con direcciones duplicadas retorna Error`() = runTest {
        val client = TestClientFactory.createClient()
        repository.setClients(listOf(client))

        val result = useCase(
            client,
            newAddresses = listOf(Address("Calle A"), Address("calle a"))
        )

        assertTrue(result.isError)
        assertTrue((result as Result.Error).message.contains("duplicadas"))
    }

    @Test
    fun `actualizar multiples campos a la vez retorna Success`() = runTest {
        val client = TestClientFactory.createClient()
        repository.setClients(listOf(client))

        val result = useCase(
            client,
            newName = "Nuevo Nombre",
            newIdentificationType = Identification.RNC("123456789"),
            newPhoneNumber = PhoneNumber("+18099999999"),
            newEmail = Email("nuevo@test.com")
        )

        assertTrue(result.isSuccess)
        val updated = (result as Result.Success).data
        assertEquals("Nuevo Nombre", updated.name)
        assertTrue(updated.identificationType is Identification.RNC)
        assertEquals("+18099999999", updated.phoneNumber.value)
        assertEquals("nuevo@test.com", updated.email.value)
    }

    @Test
    fun `actualizar sin cambios retorna Success con datos originales`() = runTest {
        val client = TestClientFactory.createClient()
        repository.setClients(listOf(client))

        val result = useCase(client) // Sin parámetros opcionales

        assertTrue(result.isSuccess)
        val updated = (result as Result.Success).data
        assertEquals(client.name, updated.name)
        assertEquals(client.addresses, updated.addresses)
    }

    @Test
    fun `actualizar persiste cambios en el repositorio`() = runTest {
        val client = TestClientFactory.createClient()
        repository.setClients(listOf(client))

        useCase(client, newName = "Persistido")

        val fromRepo = repository.getClientById(client.id)!!
        assertEquals("Persistido", fromRepo.name)
    }
}
