package com.example.oriontek_technical_interview.feature.clients.data.local.entity

/*
    Entidad de datos que representa una dirección asociada a un cliente.
    Se almacena con referencia al clientId para la relación 1:N.
 */
data class AddressEntity(
    val id: Long = 0,
    val clientId: String,
    val address: String
)
