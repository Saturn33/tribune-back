package ru.netology.saturn33.kt1.diploma.dto

import ru.netology.saturn33.kt1.diploma.model.Reaction
import ru.netology.saturn33.kt1.diploma.model.ReactionType
import ru.netology.saturn33.kt1.diploma.model.UserModel

data class ReactionResponseDto(
    val date: Long,
    val user: UserResponseDto,
    val type: ReactionType
)
{
    companion object {
        fun fromModel(user: UserModel, model: Reaction) = ReactionResponseDto(
            date = model.date,
            user = UserResponseDto.fromModel(user),
            type = model.reactionType
        )
    }
}