package com.example.oriontek_technical_interview.core.common

/*
    Wrapper sellado para representar el resultado de una operación.
    Encapsula éxito o error de forma segura, evitando excepciones
    no controladas en las capas superiores.
 */
sealed class Result<out T> {

    data class Success<out T>(val data: T) : Result<T>()

    data class Error(val message: String, val cause: Throwable? = null) : Result<Nothing>()

    /*
        Indica si el resultado es un éxito.
     */
    val isSuccess: Boolean get() = this is Success

    /*
        Indica si el resultado es un error.
     */
    val isError: Boolean get() = this is Error

    /*
        Obtiene el valor del resultado o null si es error.
     */
    fun getOrNull(): T? = when (this) {
        is Success -> data
        is Error -> null
    }

    /*
        Obtiene el valor del resultado o lanza la excepción si es error.
     */
    fun getOrThrow(): T = when (this) {
        is Success -> data
        is Error -> throw cause ?: IllegalStateException(message)
    }

    /*
        Transforma el valor del resultado si es éxito.
     */
    fun <R> map(transform: (T) -> R): Result<R> = when (this) {
        is Success -> Success(transform(data))
        is Error -> this
    }

    companion object {
        /*
            Ejecuta un bloque de código y envuelve el resultado en Success o Error.
         */
        inline fun <T> runCatching(block: () -> T): Result<T> {
            return try {
                Success(block())
            } catch (e: Exception) {
                Error(e.message ?: "Error desconocido", e)
            }
        }
    }
}
