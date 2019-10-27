package ru.netology.saturn33.kt1.diploma.dto

import ru.netology.saturn33.kt1.diploma.model.AttachmentModel
import ru.netology.saturn33.kt1.diploma.model.UserBadge
import ru.netology.saturn33.kt1.diploma.model.UserModel

data class ProfileResponseDto(
    val username: String,
    val isReadOnly: Boolean,
    val badge: UserBadge?,
    val avatar: AttachmentModel?
) {
    companion object {
        fun fromModel(model: UserModel) = ProfileResponseDto(
            model.username,
            model.readOnly,
            model.badge,
            model.avatar
        )
    }
}
