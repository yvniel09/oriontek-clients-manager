package com.example.oriontek_technical_interview.feature.clients.presentation.client_detail

import androidx.lifecycle.SavedStateHandle
import com.example.oriontek_technical_interview.core.common.Result
import com.example.oriontek_technical_interview.feature.clients.domain.model.Address
import com.example.oriontek_technical_interview.feature.clients.domain.model.Client
import com.example.oriontek_technical_interview.feature.clients.domain.model.ClientId
import com.example.oriontek_technical_interview.feature.clients.domain.model.Email
import com.example.oriontek_technical_interview.feature.clients.domain.model.Identification
import com.example.oriontek_technical_interview.feature.clients.domain.model.PhoneNumber
import com.example.oriontek_technical_interview.feature.clients.domain.usecase.GetClientByIdUseCase
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ClientDetailViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var getClientByIdUseCase: GetClientByIdUseCase

    private val testClient = Client(
        id = ClientId("test-001"),
        name = "Juan Pérez",
        identificationType = Identification.Cedula("00112345678"),
        addresses = listOf(Address("Calle Principal #1"), Address("Av. Independencia")),
        phoneNumber = PhoneNumber("+18091234567"),
        email = Email("juan@example.com")
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        getClientByIdUseCase = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(clientId: String = "test-001"): ClientDetailViewModel {
        val savedStateHandle = SavedStateHandle(mapOf("clientId" to clientId))
        return ClientDetailViewModel(getClientByIdUseCase, savedStateHandle)
    }

    // ── Carga exitosa ───────────────────────────────────────────

    @Test
    fun `carga exitosa muestra datos del cliente`() = runTest {
        coEvery { getClientByIdUseCase(ClientId("test-001")) } returns Result.Success(testClient)

        val viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNull(state.error)
        assertNotNull(state.client)
        assertEquals("Juan Pérez", state.client?.name)
        assertEquals(2, state.client?.addresses?.size)
    }

    @Test
    fun `carga exitosa mapea identificacion correctamente`() = runTest {
        coEvery { getClientByIdUseCase(ClientId("test-001")) } returns Result.Success(testClient)

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.client?.identificationLabel?.startsWith("Cédula") == true)
    }

    // ── Carga con error ─────────────────────────────────────────

    @Test
    fun `cliente no encontrado muestra error`() = runTest {
        coEvery { getClientByIdUseCase(ClientId("no-existe")) } returns
            Result.Error("No se encontró el cliente con ID: no-existe")

        val viewModel = createViewModel("no-existe")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNull(state.client)
        assertNotNull(state.error)
        assertTrue(state.error!!.contains("No se encontró"))
    }

    // ── Reintentar ──────────────────────────────────────────────

    @Test
    fun `reintentar carga el cliente nuevamente`() = runTest {
        coEvery { getClientByIdUseCase(ClientId("test-001")) } returns
            Result.Error("Error de red") andThen Result.Success(testClient)

        val viewModel = createViewModel()
        advanceUntilIdle()

        // Primera carga falla
        assertNotNull(viewModel.uiState.value.error)

        // Reintento exitoso
        viewModel.loadClient()
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.error)
        assertNotNull(viewModel.uiState.value.client)
    }

    @Test
    fun `estado de loading se activa durante la carga`() = runTest {
        coEvery { getClientByIdUseCase(ClientId("test-001")) } returns Result.Success(testClient)

        val viewModel = createViewModel()

        // Antes de que se complete la coroutine, debería estar loading
        assertTrue(viewModel.uiState.value.isLoading)

        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.isLoading)
    }
}
