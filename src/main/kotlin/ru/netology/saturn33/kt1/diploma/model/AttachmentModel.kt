package ru.netology.saturn33.kt1.diploma.model

enum class AttachmentType {
    IMAGE,
    AUDIO,
    VIDEO
}

data class AttachmentModel(
    val id: String,
    val mediaType: AttachmentType
)