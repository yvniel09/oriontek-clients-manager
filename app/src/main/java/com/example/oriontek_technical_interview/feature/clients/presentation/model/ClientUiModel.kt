package com.example.oriontek_technical_interview.feature.clients.presentation.model

import com.example.oriontek_technical_interview.feature.clients.domain.model.Client
import com.example.oriontek_technical_interview.feature.clients.domain.model.Identification

/*
    Modelo de UI para representar un cliente en la capa de presentación.
    Aísla la UI de los value objects del dominio, usando solo tipos primitivos.
 */
data class ClientUiModel(
    val id: String,
    val name: String,
    val identificationLabel: String,
    val phoneNumber: String,
    val email: String,
    val addresses: List<String>
) {
    companion object {
        fun fromDomain(client: Client): ClientUiModel {
            return ClientUiModel(
                id = client.id.value,
                name = client.name,
                identificationLabel = formatIdentification(client.identificationType),
                phoneNumber = client.phoneNumber.value,
                email = client.email.value,
                addresses = client.addresses.map { it.value }
            )
        }

        private fun formatIdentification(identification: Identification): String {
            return when (identification) {
                is Identification.Cedula -> "Cédula: ${identification.value}"
                is Identification.RNC -> "RNC: ${identification.value}"
                is Identification.ForeignId -> "ID Extranjero: ${identification.value}"
                is Identification.SocialWelfareNumber -> "NSS: ${identification.value}"
            }
        }
    }
}
