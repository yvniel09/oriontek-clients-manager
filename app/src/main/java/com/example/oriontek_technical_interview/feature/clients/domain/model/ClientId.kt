package com.example.oriontek_technical_interview.feature.clients.domain.model
/*
    Value object de ClientId. Genera ids de tipo
    UUID v4 para mantener unicidad entre clientes.

    Nota: V4 no genera ids en orden de tiempo, pero es suficiente para un ejemplo como este
    porque importa la integridad de los datos sobre el orden.
 */
@JvmInline
value class ClientId(val value: String) {

    companion object {
        fun generate(): ClientId {
            return ClientId(java.util.UUID.randomUUID().toString())
        }
    }
}