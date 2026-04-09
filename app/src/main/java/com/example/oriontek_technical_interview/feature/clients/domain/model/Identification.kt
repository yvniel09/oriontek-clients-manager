package com.example.oriontek_technical_interview.feature.clients.domain.model

/*
    Esta clase se utiliza para abstraer los tipos de
    identificación dentro de la entidad Client. Esto para evitar
    tipos de datos primitivos y reglamentar el tipo de entrada a la entidad
 */
sealed class Identification {
    abstract val value: String
    /*
        Verifica si el dato proporcionado es un dígito.

        @param value Cadena de texto de entrada a la función
        @return Validación de la entrada de texto para las clases de datos
     */
    protected fun validateDigits(value: String) {
        require(value.all { it.isDigit() }) {
            "La identificación solo debe contener dígitos"
        }
    }

    data class RNC(override val value: String): Identification() {
        init {
            require(value.isNotBlank()) { "El RNC no puede estar vacío" }
            validateDigits(value)
            require(value.length == 9) {
                "El RNC debe contener 9 dígitos"
            }
        }
    }
    data class Cedula(override val value: String): Identification() {
        init {
            require(value.isNotBlank()) { "La cédula no puede estar vacía" }
            validateDigits(value)
            require(value.length == 11) {
                "La cédula debe contener 11 dígitos"
            }
        }
    }
    data class ForeignId(override val value: String): Identification() {
        init {
            require(value.isNotBlank()) { "La identificación extranjera no puede estar vacía" }
            require(value.length in 5..20) {
                "Identificación extranjera inválida"
            }
        }
    }
    data class SocialWelfareNumber(override val value: String): Identification() {
        init {
            require(value.isNotBlank()) { "El NSS no puede estar vacío" }
            validateDigits(value)
            require(value.length in 8..12) {
                "Número de seguridad social inválido"
            }
        }
    }
}