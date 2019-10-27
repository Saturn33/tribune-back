package ru.netology.saturn33.kt1.diploma.model

data class MediaModel(
    val id: String,
    val mediaType: MediaType
)

enum class MediaType {
    IMAGE
}
