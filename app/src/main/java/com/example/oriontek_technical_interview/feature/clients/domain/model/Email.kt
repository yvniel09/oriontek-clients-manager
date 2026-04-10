package com.example.oriontek_technical_interview.feature.clients.domain.model

/*
    Value object de Email. Normaliza el formato de mail.
 */
@JvmInline
value class Email(val value: String) {

    init {
        val normalized = value.trim().lowercase()

        require(normalized.isNotBlank()) {
            "El email no puede estar vacío"
        }

        val regex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")

        require(regex.matches(normalized)) {
            "Formato de email inválido"
        }

        require(normalized.substringAfter("@").contains(".")) {
            "El dominio del email es inválido"
        }
    }
}