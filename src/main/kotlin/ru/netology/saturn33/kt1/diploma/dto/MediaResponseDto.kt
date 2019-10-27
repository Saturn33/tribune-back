package ru.netology.saturn33.kt1.diploma.dto

import ru.netology.saturn33.kt1.diploma.model.MediaModel
import ru.netology.saturn33.kt1.diploma.model.MediaType

data class MediaResponseDto(val id: String, val mediaType: MediaType) {
    companion object {
        fun fromModel(model: MediaModel) = MediaResponseDto(
            id = model.id,
            mediaType = model.mediaType
        )
    }
}
