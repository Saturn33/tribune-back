package ru.netology.saturn33.kt1.diploma.dto

import ru.netology.saturn33.kt1.diploma.model.AttachmentModel

data class PostRequestDto(
    val text: String,
    val attachment: AttachmentModel
)
