package hu.fatbrains.data.model

import io.ktor.auth.*


data class UserSession(
    val userId:String?,
    val sessionId:String,
): Principal
