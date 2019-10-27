package ru.netology.saturn33.kt1.diploma.repository

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import ru.netology.saturn33.kt1.diploma.model.PostModel
import ru.netology.saturn33.kt1.diploma.model.Reaction
import ru.netology.saturn33.kt1.diploma.model.ReactionType
import ru.netology.saturn33.kt1.diploma.model.UserModel
import java.lang.Integer.max
import java.util.*

class PostRepositoryInMemoryWithMutexImpl : PostRepository {
    private var nextId = 1L
    private val items = mutableListOf<PostModel>()
    private val mutex = Mutex()

    override suspend fun getLast(userId: Long, count: Int): List<PostModel> {
        mutex.withLock {
            val filteredList = items.filter { if (userId > 0) userId == it.author else true }
            val subList = filteredList.subList(max(filteredList.lastIndex - count + 1, 0), filteredList.lastIndex + 1) as MutableList<PostModel>
            subList.forEachIndexed { ind, post ->
                subList[ind] = post.copy(views = post.views + 1)
            }
            return subList.reversed()
        }
    }

    override suspend fun getBefore(id: Long, userId: Long, count: Int): List<PostModel> {
        mutex.withLock {
            val filteredList = items.filter { (if (userId > 0) userId == it.author else true) && (it.id < id)}
            val subList = filteredList.subList(max(filteredList.lastIndex - count + 1, 0), filteredList.lastIndex + 1) as MutableList<PostModel>
            subList.forEachIndexed { ind, post ->
                subList[ind] = post.copy(views = post.views + 1)
            }
            return subList.reversed()
        }
    }

    override suspend fun getById(id: Long, incrementViews: Boolean): PostModel? {
        mutex.withLock {
            return when (val index = items.indexOfFirst { it.id == id }) {
                -1 -> null
                else -> {
                    if (incrementViews) items[index] = items[index].copy(views = items[index].views + 1)
                    items[index]
                }
            }
        }
    }

    override suspend fun save(item: PostModel): PostModel {
        mutex.withLock {
            val copy = item.copy(id = nextId++)
            items.add(copy)
            return copy
        }
    }

    override suspend fun promoteById(user: UserModel, id: Long): PostModel? {
        mutex.withLock {
            return when (val index = items.indexOfFirst { it.id == id }) {
                -1 -> null
                else -> {
                    val item = items[index]
                    if (item.promotes.find { it.uid == user.id } == null && item.demotes.find { it.uid == user.id } == null) {
                        val copy = item.copy(
                            promotes = item.promotes.plus(
                                Reaction(
                                    user.id,
                                    Date().time,
                                    ReactionType.PROMOTE
                                )
                            )
                        )
                        items[index] = copy
                        copy
                    } else {
                        item
                    }
                }
            }
        }
    }

    override suspend fun demoteById(user: UserModel, id: Long): PostModel? {
        mutex.withLock {
            return when (val index = items.indexOfFirst { it.id == id }) {
                -1 -> null
                else -> {
                    val item = items[index]
                    if (item.promotes.find { it.uid == user.id } == null && item.demotes.find { it.uid == user.id } == null) {
                        val copy = item.copy(
                            demotes = item.demotes.plus(
                                Reaction(
                                    user.id,
                                    Date().time,
                                    ReactionType.DEMOTE
                                )
                            )
                        )
                        items[index] = copy
                        copy
                    } else {
                        item
                    }
                }
            }
        }
    }
    override suspend fun getReactions(postId: Long): List<Reaction> {
        mutex.withLock {
            return when (val index = items.indexOfFirst { it.id == postId }) {
                -1 -> listOf()
                else -> {
                    val item = items[index]
                    val reactions: MutableList<Reaction> = mutableListOf()
                    reactions.addAll(item.promotes)
                    reactions.addAll(item.demotes)
                    reactions.sortByDescending { it.date }
                    reactions
                }
            }
        }
    }

}