package ru.netology.saturn33.kt1.diploma

const val REDUCER_LIMIT = 20

//сколько нужно набрать минусов в одной идее без наличия плюсов, чтобы получить статус read-only
const val READ_ONLY_CAP = 2L

//сколько реакций одного типа должен проявить пользователь, чтобы получить соответствующий бадж
const val BADGE_MIN = 5L

//пользователь должен входить в TOP-N по количеству реакций одного типа, чтобы получить соответствующий бадж
const val BADGE_TOP = 3

//во сколько раз количество реакций одного типа должно быть больше, чем количество реакций противоположного типа, чтобы получить бадж
const val BADGE_TIMES = 2