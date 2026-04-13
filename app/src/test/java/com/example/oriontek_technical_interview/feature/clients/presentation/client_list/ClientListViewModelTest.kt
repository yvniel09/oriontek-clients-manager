package com.example.oriontek_technical_interview.feature.clients.presentation.client_list

import com.example.oriontek_technical_interview.core.common.Result
import com.example.oriontek_technical_interview.feature.clients.domain.model.Address
import com.example.oriontek_technical_interview.feature.clients.domain.model.Client
import com.example.oriontek_technical_interview.feature.clients.domain.model.ClientId
import com.example.oriontek_technical_interview.feature.clients.domain.model.Email
import com.example.oriontek_technical_interview.feature.clients.domain.model.Identification
import com.example.oriontek_technical_interview.feature.clients.domain.model.PhoneNumber
import com.example.oriontek_technical_interview.feature.clients.domain.usecase.DeleteClientUseCase
import com.example.oriontek_technical_interview.feature.clients.domain.usecase.ObserveClientsUseCase
import com.example.oriontek_technical_interview.feature.clients.domain.usecase.SearchClientsUseCase
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
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
class ClientListViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var observeClientsUseCase: ObserveClientsUseCase
    private lateinit var searchClientsUseCase: SearchClientsUseCase
    private lateinit var deleteClientUseCase: DeleteClientUseCase
    private lateinit var viewModel: ClientListViewModel

    private val testClient = Client(
        id = ClientId("test-001"),
        name = "Juan Pérez",
        identificationType = Identification.Cedula("00112345678"),
        addresses = listOf(Address("Calle Principal #1")),
        phoneNumber = PhoneNumber("+18091234567"),
        email = Email("juan@example.com")
    )

    private val testClient2 = Client(
        id = ClientId("test-002"),
        name = "María López",
        identificationType = Identification.RNC("123456789"),
        addresses = listOf(Address("Av. 27 de Febrero")),
        phoneNumber = PhoneNumber("+18099876543"),
        email = Email("maria@example.com")
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        observeClientsUseCase = mockk()
        searchClientsUseCase = mockk()
        deleteClientUseCase = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(clients: List<Client> = emptyList()) {
        every { observeClientsUseCase() } returns flowOf(clients)
        viewModel = ClientListViewModel(observeClientsUseCase, searchClientsUseCase, deleteClientUseCase)
    }

    // ── Estado inicial ──────────────────────────────────────────

    @Test
    fun `estado inicial sin clientes muestra lista vacia`() = runTest {
        createViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.clients.isEmpty())
        assertFalse(state.isLoading)
        assertNull(state.error)
    }

    @Test
    fun `estado inicial con clientes muestra la lista`() = runTest {
        createViewModel(listOf(testClient, testClient2))
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(2, state.clients.size)
        assertEquals("Juan Pérez", state.clients[0].name)
        assertEquals("María López", state.clients[1].name)
    }

    // ── Búsqueda ────────────────────────────────────────────────

    @Test
    fun `busqueda con query vacio muestra todos los clientes`() = runTest {
        createViewModel(listOf(testClient, testClient2))
        advanceUntilIdle()

        viewModel.onSearchQueryChanged("")
        advanceUntilIdle()

        assertEquals(2, viewModel.uiState.value.clients.size)
    }

    @Test
    fun `busqueda filtra clientes por nombre`() = runTest {
        createViewModel(listOf(testClient, testClient2))
        advanceUntilIdle()

        coEvery { searchClientsUseCase("Juan") } returns Result.Success(listOf(testClient))
        viewModel.onSearchQueryChanged("Juan")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Juan", state.searchQuery)
        assertEquals(1, state.clients.size)
        assertEquals("Juan Pérez", state.clients[0].name)
    }

    @Test
    fun `busqueda fallida muestra todos los clientes`() = runTest {
        createViewModel(listOf(testClient))
        advanceUntilIdle()

        coEvery { searchClientsUseCase("  ") } returns Result.Error("Vacío")
        viewModel.onSearchQueryChanged("  ")
        advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.clients.size)
    }

    // ── Eliminación ─────────────────────────────────────────────

    @Test
    fun `solicitar eliminacion abre dialogo de confirmacion`() = runTest {
        createViewModel(listOf(testClient))
        advanceUntilIdle()

        val clientUi = viewModel.uiState.value.clients.first()
        viewModel.onDeleteRequested(clientUi)

        assertNotNull(viewModel.uiState.value.deleteConfirmation)
        assertEquals(clientUi.id, viewModel.uiState.value.deleteConfirmation?.id)
    }

    @Test
    fun `descartar eliminacion cierra dialogo`() = runTest {
        createViewModel(listOf(testClient))
        advanceUntilIdle()

        viewModel.onDeleteRequested(viewModel.uiState.value.clients.first())
        viewModel.onDeleteDismissed()

        assertNull(viewModel.uiState.value.deleteConfirmation)
    }

    @Test
    fun `confirmar eliminacion exitosa cierra dialogo sin error`() = runTest {
        createViewModel(listOf(testClient))
        advanceUntilIdle()

        coEvery { deleteClientUseCase(ClientId("test-001")) } returns Result.Success(Unit)
        viewModel.onDeleteRequested(viewModel.uiState.value.clients.first())
        viewModel.onDeleteConfirmed()
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.deleteConfirmation)
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun `confirmar eliminacion fallida muestra error`() = runTest {
        createViewModel(listOf(testClient))
        advanceUntilIdle()

        coEvery { deleteClientUseCase(ClientId("test-001")) } returns Result.Error("No se pudo eliminar")
        viewModel.onDeleteRequested(viewModel.uiState.value.clients.first())
        viewModel.onDeleteConfirmed()
        advanceUntilIdle()

        assertEquals("No se pudo eliminar", viewModel.uiState.value.error)
    }

    @Test
    fun `descartar error limpia el estado de error`() = runTest {
        createViewModel(listOf(testClient))
        advanceUntilIdle()

        coEvery { deleteClientUseCase(ClientId("test-001")) } returns Result.Error("Error")
        viewModel.onDeleteRequested(viewModel.uiState.value.clients.first())
        viewModel.onDeleteConfirmed()
        advanceUntilIdle()

        viewModel.onErrorDismissed()
        assertNull(viewModel.uiState.value.error)
    }
}
