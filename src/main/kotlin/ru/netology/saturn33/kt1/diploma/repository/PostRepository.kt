package ru.netology.saturn33.kt1.diploma.repository

import ru.netology.saturn33.kt1.diploma.model.PostModel
import ru.netology.saturn33.kt1.diploma.model.UserModel


interface PostRepository {
    suspend fun getAll(): List<PostModel>
    suspend fun getLast(count: Int): List<PostModel>
    suspend fun getAfter(id: Long): List<PostModel>
    suspend fun getBefore(id: Long, count: Int): List<PostModel>
    suspend fun getById(id: Long, incrementViews: Boolean = false): PostModel?
    suspend fun save(item: PostModel): PostModel
    suspend fun removeById(id: Long): Boolean
    suspend fun likeById(user: UserModel, id: Long): PostModel?
    suspend fun dislikeById(user: UserModel, id: Long): PostModel?
    suspend fun repostById(user: Long, id: Long): Boolean
    suspend fun unrepostById(user: Long, id: Long): Boolean
//    suspend fun shareById(id: Long): PostModel?
}
