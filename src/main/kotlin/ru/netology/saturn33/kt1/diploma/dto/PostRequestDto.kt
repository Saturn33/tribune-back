package ru.netology.saturn33.kt1.diploma.dto

import ru.netology.saturn33.kt1.diploma.model.AttachmentModel
import ru.netology.saturn33.kt1.diploma.model.PostType

data class PostRequestDto(
    val id: Long,
    val postType: PostType = PostType.POST,
    val content: String? = null,//for post, event, repost, youtube
    val attachment: AttachmentModel? = null
//    val media: MediaModel? = null,
//    val location: Location? = null,//for event
//    val video: String? = null//for youtube
)
