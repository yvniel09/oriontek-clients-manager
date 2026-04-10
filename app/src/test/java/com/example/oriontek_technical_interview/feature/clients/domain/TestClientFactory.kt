package com.example.oriontek_technical_interview.feature.clients.domain

import com.example.oriontek_technical_interview.feature.clients.domain.model.Address
import com.example.oriontek_technical_interview.feature.clients.domain.model.Client
import com.example.oriontek_technical_interview.feature.clients.domain.model.ClientId
import com.example.oriontek_technical_interview.feature.clients.domain.model.Email
import com.example.oriontek_technical_interview.feature.clients.domain.model.Identification
import com.example.oriontek_technical_interview.feature.clients.domain.model.PhoneNumber

/*
    Object helper con datos de prueba reutilizables
    para los tests del dominio.
 */
object TestClientFactory {

    val DEFAULT_ID = ClientId("test-id-001")
    val DEFAULT_NAME = "Juan Pérez"
    val DEFAULT_IDENTIFICATION = Identification.Cedula("00112345678")
    val DEFAULT_PHONE = PhoneNumber("+18091234567")
    val DEFAULT_EMAIL = Email("juan@example.com")
    val DEFAULT_ADDRESS = Address("Calle Principal #1, Santo Domingo")

    fun createClient(
        id: ClientId = DEFAULT_ID,
        name: String = DEFAULT_NAME,
        identification: Identification = DEFAULT_IDENTIFICATION,
        addresses: List<Address> = listOf(DEFAULT_ADDRESS),
        phone: PhoneNumber = DEFAULT_PHONE,
        email: Email = DEFAULT_EMAIL
    ): Client = Client(
        id = id,
        identificationType = identification,
        name = name,
        addresses = addresses,
        phoneNumber = phone,
        email = email
    )

    fun createClientWithMultipleAddresses(
        id: ClientId = DEFAULT_ID,
        addressValues: List<String> = listOf(
            "Calle Principal #1, Santo Domingo",
            "Av. 27 de Febrero #200, Santiago"
        )
    ): Client = createClient(
        id = id,
        addresses = addressValues.map { Address(it) }
    )
}
