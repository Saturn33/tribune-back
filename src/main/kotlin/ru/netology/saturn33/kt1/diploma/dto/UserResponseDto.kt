package ru.netology.saturn33.kt1.diploma.dto

import ru.netology.saturn33.kt1.diploma.model.AttachmentModel
import ru.netology.saturn33.kt1.diploma.model.UserBadge
import ru.netology.saturn33.kt1.diploma.model.UserModel


data class UserResponseDto(
    val id: Long,
    val username: String,
    val badge: UserBadge?,
    val avatar: AttachmentModel?
) {
    companion object {
        fun fromModel(model: UserModel) = UserResponseDto(
            id = model.id,
            username = model.username,
            badge = model.badge,
            avatar = model.avatar
        )
    }
}
