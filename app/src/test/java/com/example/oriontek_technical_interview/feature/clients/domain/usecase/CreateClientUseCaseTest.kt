package com.example.oriontek_technical_interview.feature.clients.domain.usecase

import com.example.oriontek_technical_interview.core.common.Result
import com.example.oriontek_technical_interview.feature.clients.domain.MockClientRepository
import com.example.oriontek_technical_interview.feature.clients.domain.model.Address
import com.example.oriontek_technical_interview.feature.clients.domain.model.Email
import com.example.oriontek_technical_interview.feature.clients.domain.model.Identification
import com.example.oriontek_technical_interview.feature.clients.domain.model.PhoneNumber
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class CreateClientUseCaseTest {

    private lateinit var repository: MockClientRepository
    private lateinit var useCase: CreateClientUseCase

    @Before
    fun setUp() {
        repository = MockClientRepository()
        useCase = CreateClientUseCase(repository)
    }

    @Test
    fun `crear cliente con datos validos retorna Success`() = runTest {
        val result = useCase(
            name = "María García",
            identificationType = Identification.Cedula("00112345678"),
            addresses = listOf(Address("Calle 1, Santo Domingo")),
            phoneNumber = PhoneNumber("+18091234567"),
            email = Email("maria@example.com")
        )

        assertTrue(result.isSuccess)
        val client = (result as Result.Success).data
        assertEquals("María García", client.name)
        assertEquals(1, client.addresses.size)
    }

    @Test
    fun `crear cliente con nombre vacio retorna Error`() = runTest {
        val result = useCase(
            name = "   ",
            identificationType = Identification.Cedula("00112345678"),
            addresses = listOf(Address("Calle 1")),
            phoneNumber = PhoneNumber("+18091234567"),
            email = Email("test@example.com")
        )

        assertTrue(result.isError)
        val error = result as Result.Error
        assertTrue(error.message.contains("nombre"))
    }

    @Test
    fun `crear cliente sin direcciones retorna Error`() = runTest {
        val result = useCase(
            name = "Pedro",
            identificationType = Identification.Cedula("00112345678"),
            addresses = emptyList(),
            phoneNumber = PhoneNumber("+18091234567"),
            email = Email("pedro@example.com")
        )

        assertTrue(result.isError)
        val error = result as Result.Error
        assertTrue(error.message.contains("dirección"))
    }

    @Test
    fun `crear cliente con direcciones duplicadas retorna Error`() = runTest {
        val result = useCase(
            name = "Ana",
            identificationType = Identification.Cedula("00112345678"),
            addresses = listOf(
                Address("Calle Principal #1"),
                Address("calle principal #1")  // duplicada (case-insensitive)
            ),
            phoneNumber = PhoneNumber("+18091234567"),
            email = Email("ana@example.com")
        )

        assertTrue(result.isError)
        val error = result as Result.Error
        assertTrue(error.message.contains("duplicadas"))
    }

    @Test
    fun `crear cliente lo agrega al repositorio`() = runTest {
        useCase(
            name = "Luis",
            identificationType = Identification.RNC("123456789"),
            addresses = listOf(Address("Av. 27 de Febrero")),
            phoneNumber = PhoneNumber("+18091234567"),
            email = Email("luis@empresa.com")
        )

        val clients = repository.getClients()
        assertEquals(1, clients.size)
        assertEquals("Luis", clients[0].name)
    }

    @Test
    fun `crear cliente genera un ID unico`() = runTest {
        val result1 = useCase(
            name = "Cliente 1",
            identificationType = Identification.Cedula("00112345678"),
            addresses = listOf(Address("Dirección 1")),
            phoneNumber = PhoneNumber("+18091234567"),
            email = Email("c1@test.com")
        )
        val result2 = useCase(
            name = "Cliente 2",
            identificationType = Identification.Cedula("00198765432"),
            addresses = listOf(Address("Dirección 2")),
            phoneNumber = PhoneNumber("+18099876543"),
            email = Email("c2@test.com")
        )

        val id1 = (result1 as Result.Success).data.id
        val id2 = (result2 as Result.Success).data.id
        assertNotEquals(id1, id2)
    }

    @Test
    fun `crear cliente trimea el nombre`() = runTest {
        val result = useCase(
            name = "  Carlos Mejía  ",
            identificationType = Identification.Cedula("00112345678"),
            addresses = listOf(Address("Calle Foo")),
            phoneNumber = PhoneNumber("+18091234567"),
            email = Email("carlos@test.com")
        )

        val client = (result as Result.Success).data
        assertEquals("Carlos Mejía", client.name)
    }
}
