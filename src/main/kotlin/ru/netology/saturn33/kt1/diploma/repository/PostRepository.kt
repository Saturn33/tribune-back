package ru.netology.saturn33.kt1.diploma.repository

import ru.netology.saturn33.kt1.diploma.model.PostModel
import ru.netology.saturn33.kt1.diploma.model.Reaction
import ru.netology.saturn33.kt1.diploma.model.UserModel


interface PostRepository {
    suspend fun getLast(userId: Long, count: Int): List<PostModel>
    suspend fun getBefore(id: Long, userId: Long, count: Int): List<PostModel>
    suspend fun getById(id: Long, incrementViews: Boolean = false): PostModel?
    suspend fun save(item: PostModel): PostModel
    suspend fun promoteById(user: UserModel, id: Long): PostModel?
    suspend fun demoteById(user: UserModel, id: Long): PostModel?
    suspend fun getReactions(postId: Long): List<Reaction>
}
