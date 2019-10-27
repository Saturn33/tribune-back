package ru.netology.saturn33.kt1.diploma.service

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import java.util.*

class JWTTokenService(secret: String, private val expire: Long = 0) {
    private val algo = Algorithm.HMAC256(secret)
    val verifier: JWTVerifier = JWT.require(algo).build()

    fun generate(id: Long): String {
        val jwt = JWT.create()
            .withClaim("id", id)
        if (expire > 0) {
            jwt.withExpiresAt(Date(System.currentTimeMillis() + 1000 * expire))
        }
        return jwt.sign(algo)
    }
}
