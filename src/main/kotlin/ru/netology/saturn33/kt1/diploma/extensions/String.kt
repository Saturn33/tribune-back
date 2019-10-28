package ru.netology.saturn33.kt1.diploma.extensions

fun String.reducer(limit: Int = 20) : String {
    val s = this.trim()
    return if (s.length > limit) s.substring(0, limit) + "..." else s
}
