package ru.netology.saturn33.kt1.diploma.model

import java.util.*

data class PostModel(
    val id: Long,
    val date: Long,
    val author: Long,
    val text: String,
    val promotes: List<Reaction> = listOf(),
    val demotes: List<Reaction> = listOf(),
    val views: Int = 0,
    val attachment: AttachmentModel
)
