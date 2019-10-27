package ru.netology.saturn33.kt1.diploma.dto

import ru.netology.saturn33.kt1.diploma.model.AttachmentModel
import ru.netology.saturn33.kt1.diploma.model.PostModel
import ru.netology.saturn33.kt1.diploma.model.UserModel


data class PostResponseDto(
    val id: Long,
    val date: Long,
    val author: UserResponseDto,
    val text: String,
    val promotes: Int,
    val promotedByMe: Boolean,
    val demotes: Int,
    val demotedByMe: Boolean,
    val attachment: AttachmentModel
) {
    companion object {
        fun fromModel(currentUser: UserModel, user: UserModel, model: PostModel) = PostResponseDto(
            id = model.id,
            date = model.date,
            author = UserResponseDto.fromModel(user),
            promotes = model.promotes.size,
            promotedByMe = model.promotes.find { it.uid == currentUser.id } != null,
            demotes = model.demotes.size,
            demotedByMe = model.demotes.find { it.uid == currentUser.id } != null,
            text = model.text,
            attachment = model.attachment
        )
    }
}
