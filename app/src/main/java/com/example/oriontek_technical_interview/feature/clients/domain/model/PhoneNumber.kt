package com.example.oriontek_technical_interview.feature.clients.domain.model

/*
    Value object de PhoneNumber. Valida el formato
    del número de teléfono y valida dependiendo del caso.
    Utiliza regex para el formato de la cadena de texto.
 */
@JvmInline
value class PhoneNumber(val value: String) {

    init {
        val regex = Regex("^\\+?[0-9]{10,15}$")

        require(value.isNotBlank()) {
            "El número telefónico no puede estar vacío"
        }

        require(regex.matches(value)) {
            "Número telefónico inválido"
        }
    }
}