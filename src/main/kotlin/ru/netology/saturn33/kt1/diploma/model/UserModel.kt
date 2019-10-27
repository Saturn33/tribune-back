package ru.netology.saturn33.kt1.diploma.model

import io.ktor.auth.Principal

data class UserModel(
    val id: Long = 0,
    val username: String,
    val password: String,
    val badge: UserBadge? = null,
    val avatar: AttachmentModel? = null,
    val token: PushToken? = null,
    val readOnly: Boolean = false
) : Principal
