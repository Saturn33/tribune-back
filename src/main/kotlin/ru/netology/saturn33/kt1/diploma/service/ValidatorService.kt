package ru.netology.saturn33.kt1.diploma.service

import ru.netology.saturn33.kt1.diploma.model.Location
import ru.netology.saturn33.kt1.diploma.model.PostModel


class ValidatorService {
    fun checkYoutube(link: String?): Boolean {
        if (link == null) return false
        return Regex("^(?:https?://)?(?:www\\.)?(?:youtube\\.com/)([a-zA-Z_\\d-]+)$").find(link) != null
    }

    fun checkLocation(loc: Location?): Boolean = loc is Location

    fun checkSource(srcId: Long?, model: PostModel?): Pair<Boolean, PostModel?> {
        if (srcId == null) return false to null
        return (model != null) to model
    }
}
