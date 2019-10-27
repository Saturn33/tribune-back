package ru.netology.saturn33.kt1.diploma.dto

import ru.netology.saturn33.kt1.diploma.model.AttachmentModel
import ru.netology.saturn33.kt1.diploma.model.PostModel
import ru.netology.saturn33.kt1.diploma.model.PostType
import ru.netology.saturn33.kt1.diploma.model.UserModel


data class PostResponseDto(
    val id: Long,
    val created: Long,
    val author: UserResponseDto,
    val source: PostResponseDto? = null,//for repost
    val content: String? = null,//for post, event, repost, youtube
    val likes: Int,
    val likedByMe: Boolean,
    val reposts: Int,
    val repostedByMe: Boolean,
//    val views: Int,
//    val shares: Int,
    val postType: PostType = PostType.POST,
    val attachment: AttachmentModel? = null
//    val media: MediaModel? = null,
//    val location: Location? = null,//for event
//    val video: String? = null//for youtube
) {
    companion object {
        fun fromModel(currentUser: UserModel, user: UserModel, model: PostModel) = PostResponseDto(
            id = model.id,
            created = model.created.time,
            author = UserResponseDto.fromModel(user),
            likes = model.likes.size,
            likedByMe = model.likes.contains(currentUser.id),
            reposts = model.reposts.size,
            repostedByMe = model.reposts.contains(currentUser.id),
            postType = model.postType,
            content = model.content,
            source = model.source,
            attachment = model.attachment
        )
    }
}
