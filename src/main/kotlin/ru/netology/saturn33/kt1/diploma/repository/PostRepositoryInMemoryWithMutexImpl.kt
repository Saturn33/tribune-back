package ru.netology.saturn33.kt1.diploma.repository

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import ru.netology.saturn33.kt1.diploma.model.PostModel
import ru.netology.saturn33.kt1.diploma.model.UserModel
import java.lang.Integer.max

class PostRepositoryInMemoryWithMutexImpl : PostRepository {
    private var nextId = 1L
    private val items = mutableListOf<PostModel>()
    private val mutex = Mutex()

    override suspend fun getAll(): List<PostModel> {
        mutex.withLock {
            items.forEachIndexed { ind, post ->
                items[ind] = post.copy(views = post.views + 1)
            }
            return items.reversed()
        }
    }

    override suspend fun getLast(count: Int): List<PostModel> {
        mutex.withLock {
            val subList = items.subList(max(items.lastIndex - count + 1, 0), items.lastIndex + 1)
            subList.forEachIndexed { ind, post ->
                subList[ind] = post.copy(views = post.views + 1)
            }
            return subList.reversed()
        }
    }

    override suspend fun getAfter(id: Long): List<PostModel> {
        mutex.withLock {
            val filteredList = items.filter { it.id > id } as MutableList<PostModel>
            filteredList.forEachIndexed { ind, post ->
                filteredList[ind] = post.copy(views = post.views + 1)
            }
            return filteredList.reversed()
        }
    }

    override suspend fun getBefore(id: Long, count: Int): List<PostModel> {
        mutex.withLock {
            val filteredList = items.filter { it.id < id }
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
            return when (val index = items.indexOfFirst { it.id == item.id }) {
                -1 -> {
                    val copy = item.copy(id = nextId++)
                    items.add(copy)
                    copy
                }
                else -> {
                    //не затирать поля, не зависящие от контента (время добавления, лайки, просмотры, автора)
                    val copy = item.copy(
                        created = items[index].created,
                        likes = items[index].likes,
                        views = items[index].views,
                        author = items[index].author
                    )
                    items[index] = copy
                    copy
                }
            }
        }
    }

    override suspend fun removeById(id: Long): Boolean {
        mutex.withLock {
            return items.removeIf { it.id == id }
        }
    }

    override suspend fun likeById(user: UserModel, id: Long): PostModel? {
        mutex.withLock {
            return when (val index = items.indexOfFirst { it.id == id }) {
                -1 -> null
                else -> {
                    val item = items[index]
                    if (item.likes.contains(user.id)) {
                        item
                    } else {
                        val copy = item.copy(likes = item.likes.plus(user.id))
                        items[index] = copy
                        copy
                    }
                }
            }
        }
    }

    override suspend fun dislikeById(user: UserModel, id: Long): PostModel? {
        mutex.withLock {
            return when (val index = items.indexOfFirst { it.id == id }) {
                -1 -> null
                else -> {
                    val item = items[index]
                    if (item.likes.contains(user.id)) {
                        val copy = item.copy(likes = item.likes.minus(user.id))
                        items[index] = copy
                        copy
                    } else {
                        item
                    }
                }
            }
        }
    }

    override suspend fun repostById(user: Long, id: Long): Boolean {
        mutex.withLock {
            return when (val index = items.indexOfFirst { it.id == id }) {
                -1 -> false
                else -> {
                    val item = items[index]
                    val copy = item.copy(reposts = item.reposts.plus(user))
                    items[index] = copy
                    true
                }
            }
        }
    }

    override suspend fun unrepostById(user: Long, id: Long): Boolean {
        mutex.withLock {
            return when (val index = items.indexOfFirst { it.id == id }) {
                -1 -> false
                else -> {
                    val item = items[index]
                    val copy = item.copy(reposts = item.reposts.minus(user))
                    items[index] = copy
                    true
                }
            }
        }
    }

/*
    override suspend fun shareById(id: Long): PostModel? {
        mutex.withLock {
            return when (val index = items.indexOfFirst { it.id == id }) {
                -1 -> null
                else -> {
                    val item = items[index]
                    val copy = item.copy(shares = item.shares + 1)
                    items[index] = copy
                    copy
                }
            }
        }
    }
*/
}