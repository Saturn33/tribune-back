package ru.netology.saturn33.kt1.diploma

const val REDUCER_LIMIT = 20

//сколько нужно набрать минусов в одной идее без наличия плюсов, чтобы получить статус read-only
const val READ_ONLY_CAP = 3L

//сколько реакций одного типа должен проявить пользователь, чтобы получить соответствующий бадж
const val BADGE_MIN = 10L

//пользователь должен входить в TOP-N по количеству реакций одного типа, чтобы получить соответствующий бадж
const val BADGE_TOP = 5L