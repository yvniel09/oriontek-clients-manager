package com.example.oriontek_technical_interview.feature.clients.domain.model

/*
   Value object de Address. Se utiliza para validar el formato
   de la dirección y asegurar que no esté vacía.
*/
@JvmInline
value class Address(val value: String) {

    init {
        require(value.isNotBlank()) { "La dirección no puede estar vacía" }
    }
}
