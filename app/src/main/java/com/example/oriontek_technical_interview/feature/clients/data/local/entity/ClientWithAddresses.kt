package com.example.oriontek_technical_interview.feature.clients.data.local.entity

/*
    Clase que agrupa un ClientEntity con sus AddressEntity asociados.
    Representa la relación completa 1:N entre cliente y direcciones
    en la capa de datos.
 */
data class ClientWithAddresses(
    val client: ClientEntity,
    val addresses: List<AddressEntity>
)
