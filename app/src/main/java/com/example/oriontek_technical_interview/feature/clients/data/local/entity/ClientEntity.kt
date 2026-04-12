package com.example.oriontek_technical_interview.feature.clients.data.local.entity

/*
    Entidad de datos que representa un cliente en la capa de persistencia.
    Es independiente del modelo de dominio para permitir diferentes
    estrategias de almacenamiento (Room, archivos, red, etc.)
 */
data class ClientEntity(
    val id: String,
    val name: String,
    val identificationType: String,
    val identificationValue: String,
    val phoneNumber: String,
    val email: String
)
