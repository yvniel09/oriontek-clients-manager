package com.example.oriontek_technical_interview.feature.clients.presentation.client_form

import androidx.lifecycle.SavedStateHandle
import com.example.oriontek_technical_interview.core.common.Result
import com.example.oriontek_technical_interview.feature.clients.domain.model.Address
import com.example.oriontek_technical_interview.feature.clients.domain.model.Client
import com.example.oriontek_technical_interview.feature.clients.domain.model.ClientId
import com.example.oriontek_technical_interview.feature.clients.domain.model.Email
import com.example.oriontek_technical_interview.feature.clients.domain.model.Identification
import com.example.oriontek_technical_interview.feature.clients.domain.model.PhoneNumber
import com.example.oriontek_technical_interview.feature.clients.domain.usecase.CreateClientUseCase
import com.example.oriontek_technical_interview.feature.clients.domain.usecase.GetClientByIdUseCase
import com.example.oriontek_technical_interview.feature.clients.domain.usecase.UpdateClientUseCase
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
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
class ClientFormViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var createClientUseCase: CreateClientUseCase
    private lateinit var updateClientUseCase: UpdateClientUseCase
    private lateinit var getClientByIdUseCase: GetClientByIdUseCase

    private val testClient = Client(
        id = ClientId("test-001"),
        name = "Juan Pérez",
        identificationType = Identification.Cedula("00112345678"),
        addresses = listOf(Address("Calle Principal #1")),
        phoneNumber = PhoneNumber("+18091234567"),
        email = Email("juan@example.com")
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        createClientUseCase = mockk()
        updateClientUseCase = mockk()
        getClientByIdUseCase = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(clientId: String? = null): ClientFormViewModel {
        val map = if (clientId != null) mapOf("clientId" to clientId) else emptyMap()
        return ClientFormViewModel(createClientUseCase, updateClientUseCase, getClientByIdUseCase, SavedStateHandle(map))
    }

    // ── Modo creación ───────────────────────────────────────────

    @Test
    fun `sin clientId inicia en modo CREATE`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(FormMode.CREATE, viewModel.uiState.value.mode)
        assertFalse(viewModel.uiState.value.isEditMode)
    }

    @Test
    fun `modo CREATE tiene titulo correcto`() = runTest {
        val viewModel = createViewModel()
        assertEquals("Nuevo Cliente", viewModel.uiState.value.screenTitle)
    }

    // ── Modo edición ────────────────────────────────────────────

    @Test
    fun `con clientId inicia en modo EDIT y carga datos`() = runTest {
        coEvery { getClientByIdUseCase(ClientId("test-001")) } returns Result.Success(testClient)

        val viewModel = createViewModel("test-001")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(FormMode.EDIT, state.mode)
        assertTrue(state.isEditMode)
        assertEquals("Juan Pérez", state.name)
        assertEquals("00112345678", state.identificationValue)
        assertEquals(IdentificationTypeUi.CEDULA, state.identificationType)
        assertEquals("+18091234567", state.phoneNumber)
        assertEquals("juan@example.com", state.email)
        assertEquals(listOf("Calle Principal #1"), state.addresses)
    }

    @Test
    fun `modo EDIT tiene titulo correcto`() = runTest {
        coEvery { getClientByIdUseCase(ClientId("test-001")) } returns Result.Success(testClient)

        val viewModel = createViewModel("test-001")
        advanceUntilIdle()

        assertEquals("Editar Cliente", viewModel.uiState.value.screenTitle)
    }

    @Test
    fun `error al cargar cliente en modo edicion muestra formError`() = runTest {
        coEvery { getClientByIdUseCase(ClientId("test-001")) } returns Result.Error("No encontrado")

        val viewModel = createViewModel("test-001")
        advanceUntilIdle()

        assertEquals("No encontrado", viewModel.uiState.value.formError)
    }

    // ── Actualización de campos ─────────────────────────────────

    @Test
    fun `cambiar nombre actualiza estado y limpia error`() = runTest {
        val viewModel = createViewModel()

        viewModel.onNameChanged("Nuevo Nombre")

        assertEquals("Nuevo Nombre", viewModel.uiState.value.name)
        assertNull(viewModel.uiState.value.nameError)
    }

    @Test
    fun `cambiar tipo de identificacion limpia valor y error`() = runTest {
        val viewModel = createViewModel()
        viewModel.onIdentificationValueChanged("00112345678")

        viewModel.onIdentificationTypeChanged(IdentificationTypeUi.RNC)

        assertEquals(IdentificationTypeUi.RNC, viewModel.uiState.value.identificationType)
        assertEquals("", viewModel.uiState.value.identificationValue)
        assertNull(viewModel.uiState.value.identificationError)
    }

    @Test
    fun `cambiar telefono actualiza estado`() = runTest {
        val viewModel = createViewModel()
        viewModel.onPhoneChanged("+18091234567")
        assertEquals("+18091234567", viewModel.uiState.value.phoneNumber)
    }

    @Test
    fun `cambiar email actualiza estado`() = runTest {
        val viewModel = createViewModel()
        viewModel.onEmailChanged("test@mail.com")
        assertEquals("test@mail.com", viewModel.uiState.value.email)
    }

    // ── Gestión de direcciones ───────────────────────────────────

    @Test
    fun `agregar direccion agrega campo vacio a la lista`() = runTest {
        val viewModel = createViewModel()
        val initialCount = viewModel.uiState.value.addresses.size

        viewModel.onAddressAdded()

        assertEquals(initialCount + 1, viewModel.uiState.value.addresses.size)
    }

    @Test
    fun `editar direccion modifica el valor en el indice correcto`() = runTest {
        val viewModel = createViewModel()
        viewModel.onAddressChanged(0, "Calle Modificada")

        assertEquals("Calle Modificada", viewModel.uiState.value.addresses[0])
    }

    @Test
    fun `eliminar direccion la remueve de la lista`() = runTest {
        val viewModel = createViewModel()
        viewModel.onAddressAdded() // ahora hay 2
        viewModel.onAddressChanged(1, "Segunda dirección")

        viewModel.onAddressRemoved(0)

        assertEquals(1, viewModel.uiState.value.addresses.size)
        assertEquals("Segunda dirección", viewModel.uiState.value.addresses[0])
    }

    @Test
    fun `no permite eliminar la ultima direccion`() = runTest {
        val viewModel = createViewModel()

        viewModel.onAddressRemoved(0)

        assertEquals(1, viewModel.uiState.value.addresses.size)
        assertNotNull(viewModel.uiState.value.addressError)
    }

    // ── Validación del formulario ────────────────────────────────

    @Test
    fun `guardar con nombre vacio muestra error de nombre`() = runTest {
        val viewModel = createViewModel()
        viewModel.onSave()
        advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.nameError)
    }

    @Test
    fun `guardar con identificacion vacia muestra error`() = runTest {
        val viewModel = createViewModel()
        viewModel.onNameChanged("Juan")
        viewModel.onSave()
        advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.identificationError)
    }

    @Test
    fun `guardar con cedula de longitud incorrecta muestra error`() = runTest {
        val viewModel = createViewModel()
        viewModel.onNameChanged("Juan")
        viewModel.onIdentificationValueChanged("12345") // necesita 11
        viewModel.onSave()
        advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.identificationError)
        assertTrue(viewModel.uiState.value.identificationError!!.contains("11"))
    }

    @Test
    fun `guardar con telefono vacio muestra error`() = runTest {
        val viewModel = createViewModel()
        viewModel.onNameChanged("Juan")
        viewModel.onIdentificationValueChanged("00112345678")
        viewModel.onSave()
        advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.phoneError)
    }

    @Test
    fun `guardar con email vacio muestra error`() = runTest {
        val viewModel = createViewModel()
        viewModel.onNameChanged("Juan")
        viewModel.onIdentificationValueChanged("00112345678")
        viewModel.onPhoneChanged("+18091234567")
        viewModel.onSave()
        advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.emailError)
    }

    @Test
    fun `guardar sin direcciones validas muestra error`() = runTest {
        val viewModel = createViewModel()
        viewModel.onNameChanged("Juan")
        viewModel.onIdentificationValueChanged("00112345678")
        viewModel.onPhoneChanged("+18091234567")
        viewModel.onEmailChanged("test@mail.com")
        // La dirección por defecto es "" (vacía)
        viewModel.onSave()
        advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.addressError)
    }

    // ── Creación exitosa ────────────────────────────────────────

    @Test
    fun `crear cliente exitoso emite NavigateBack`() = runTest {
        coEvery {
            createClientUseCase(
                name = "Juan Pérez",
                identificationType = Identification.Cedula("00112345678"),
                addresses = listOf(Address("Calle Principal #1")),
                phoneNumber = PhoneNumber("+18091234567"),
                email = Email("juan@example.com")
            )
        } returns Result.Success(testClient)

        val viewModel = createViewModel()
        fillValidForm(viewModel)

        val effectDeferred = async { viewModel.effect.first() }
        viewModel.onSave()
        advanceUntilIdle()

        val effect = effectDeferred.await()
        assertTrue(effect is ClientFormEffect.NavigateBack)
        assertFalse(viewModel.uiState.value.isSaving)
    }

    @Test
    fun `crear cliente fallido muestra formError`() = runTest {
        coEvery {
            createClientUseCase(
                name = "Juan Pérez",
                identificationType = Identification.Cedula("00112345678"),
                addresses = listOf(Address("Calle Principal #1")),
                phoneNumber = PhoneNumber("+18091234567"),
                email = Email("juan@example.com")
            )
        } returns Result.Error("El cliente tiene direcciones duplicadas")

        val viewModel = createViewModel()
        fillValidForm(viewModel)
        viewModel.onSave()
        advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.formError)
        assertFalse(viewModel.uiState.value.isSaving)
    }

    // ── Validación de formatos de identificación ────────────────

    @Test
    fun `RNC con longitud incorrecta muestra error`() = runTest {
        val viewModel = createViewModel()
        viewModel.onNameChanged("Test")
        viewModel.onIdentificationTypeChanged(IdentificationTypeUi.RNC)
        viewModel.onIdentificationValueChanged("12345") // necesita 9
        viewModel.onSave()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.identificationError!!.contains("9"))
    }

    @Test
    fun `identificacion con letras en tipo numerico muestra error`() = runTest {
        val viewModel = createViewModel()
        viewModel.onNameChanged("Test")
        viewModel.onIdentificationValueChanged("abc12345678")
        viewModel.onSave()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.identificationError!!.contains("dígitos"))
    }

    // ── Helper ──────────────────────────────────────────────────

    private fun fillValidForm(viewModel: ClientFormViewModel) {
        viewModel.onNameChanged("Juan Pérez")
        viewModel.onIdentificationValueChanged("00112345678")
        viewModel.onPhoneChanged("+18091234567")
        viewModel.onEmailChanged("juan@example.com")
        viewModel.onAddressChanged(0, "Calle Principal #1")
    }
}
