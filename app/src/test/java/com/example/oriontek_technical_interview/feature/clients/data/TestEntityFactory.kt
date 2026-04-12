package com.example.oriontek_technical_interview.feature.clients.data

import com.example.oriontek_technical_interview.feature.clients.data.local.entity.AddressEntity
import com.example.oriontek_technical_interview.feature.clients.data.local.entity.ClientEntity

/*
    Object helper con datos de prueba reutilizables
    para los tests de la capa de datos.
 */
object TestEntityFactory {

    const val DEFAULT_CLIENT_ID = "client-001"
    const val DEFAULT_NAME = "Juan Pérez"
    const val DEFAULT_IDENTIFICATION_TYPE = "Cedula"
    const val DEFAULT_IDENTIFICATION_VALUE = "00112345678"
    const val DEFAULT_PHONE = "+18091234567"
    const val DEFAULT_EMAIL = "juan@example.com"
    const val DEFAULT_ADDRESS = "Calle Principal #1, Santo Domingo"

    fun createClientEntity(
        id: String = DEFAULT_CLIENT_ID,
        name: String = DEFAULT_NAME,
        identificationType: String = DEFAULT_IDENTIFICATION_TYPE,
        identificationValue: String = DEFAULT_IDENTIFICATION_VALUE,
        phoneNumber: String = DEFAULT_PHONE,
        email: String = DEFAULT_EMAIL
    ): ClientEntity = ClientEntity(
        id = id,
        name = name,
        identificationType = identificationType,
        identificationValue = identificationValue,
        phoneNumber = phoneNumber,
        email = email
    )

    fun createAddressEntity(
        id: Long = 0,
        clientId: String = DEFAULT_CLIENT_ID,
        address: String = DEFAULT_ADDRESS
    ): AddressEntity = AddressEntity(
        id = id,
        clientId = clientId,
        address = address
    )

    fun createMultipleAddressEntities(
        clientId: String = DEFAULT_CLIENT_ID,
        addresses: List<String> = listOf(
            "Calle Principal #1, Santo Domingo",
            "Av. 27 de Febrero #200, Santiago"
        )
    ): List<AddressEntity> = addresses.map {
        AddressEntity(id = 0, clientId = clientId, address = it)
    }
}
