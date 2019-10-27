package ru.netology.saturn33.kt1.diploma.service

import ru.netology.saturn33.kt1.diploma.model.PostModel


class ValidatorService {
    fun checkSource(srcId: Long?, model: PostModel?): Pair<Boolean, PostModel?> {
        if (srcId == null) return false to null
        return (model != null) to model
    }
}
