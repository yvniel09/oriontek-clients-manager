package com.example.oriontek_technical_interview.feature.clients.presentation.client_form

/*
    Estado de UI para el formulario de creación/edición de cliente.
    Contiene todos los campos del formulario, errores por campo,
    y el modo actual (crear o editar).
 */
data class ClientFormUiState(
    val mode: FormMode = FormMode.CREATE,
    val clientId: String? = null,
    val name: String = "",
    val identificationType: IdentificationTypeUi = IdentificationTypeUi.CEDULA,
    val identificationValue: String = "",
    val phoneNumber: String = "",
    val email: String = "",
    val addresses: List<String> = listOf(""),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val nameError: String? = null,
    val identificationError: String? = null,
    val phoneError: String? = null,
    val emailError: String? = null,
    val addressError: String? = null,
    val formError: String? = null
) {
    val isEditMode: Boolean get() = mode == FormMode.EDIT

    val screenTitle: String get() = when (mode) {
        FormMode.CREATE -> "Nuevo Cliente"
        FormMode.EDIT -> "Editar Cliente"
    }

    val saveButtonText: String get() = when (mode) {
        FormMode.CREATE -> "Crear Cliente"
        FormMode.EDIT -> "Guardar Cambios"
    }
}

enum class FormMode {
    CREATE, EDIT
}

/*
    Representación de los tipos de identificación para la UI.
    Desacoplado de Identification del dominio.
 */
enum class IdentificationTypeUi(val label: String) {
    CEDULA("Cédula"),
    RNC("RNC"),
    FOREIGN_ID("ID Extranjero"),
    SOCIAL_WELFARE("NSS")
}
