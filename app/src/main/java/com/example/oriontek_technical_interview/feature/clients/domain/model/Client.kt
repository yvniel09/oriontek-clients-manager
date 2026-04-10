package com.example.oriontek_technical_interview.feature.clients.domain.model

data class Client(
    val id: ClientId,
    val identificationType: Identification,
    val name: String,
    val addresses: List<Address>,
    val phoneNumber: PhoneNumber,
    val email: Email
){
    init {
        require(name.isNotBlank()){     //Reglas de negocio: No permite que el nombre sea nulo o en blanco
            "El nombre no puede estar vacío"
        }
        require(addresses.isNotEmpty()){ //Reglas de negocio: Valida que cada cliente tenga al menos 1 dirección.
            "El cliente debe tener al menos una dirección"
        }
    }
}
