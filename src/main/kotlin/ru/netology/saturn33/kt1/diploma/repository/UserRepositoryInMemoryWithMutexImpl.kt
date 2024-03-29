package ru.netology.saturn33.kt1.diploma.repository

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import ru.netology.saturn33.kt1.diploma.model.UserModel

class UserRepositoryInMemoryWithMutexImpl : UserRepository {
    private var nextId = 1L
    private val items = mutableListOf<UserModel>()
    private val mutex = Mutex()

    override suspend fun getAll(): List<UserModel> {
        mutex.withLock {
            return items.toList()
        }
    }

    override suspend fun getById(id: Long): UserModel? {
        mutex.withLock {
            return items.find { id == it.id }
        }
    }

    override suspend fun getByIds(ids: Collection<Long>): List<UserModel> {
        mutex.withLock {
            return items.filter { ids.contains(it.id) }
        }
    }

    override suspend fun getByUsername(username: String): UserModel? {
        mutex.withLock {
            return items.firstOrNull { (it.username).toLowerCase() == username.toLowerCase() }
        }
    }

    override suspend fun save(item: UserModel): UserModel {
        mutex.withLock {
            return when (val index = items.indexOfFirst { it.id == item.id }) {
                -1 -> {
                    val copy = item.copy(id = nextId++)
                    items.add(copy)
                    copy
                }
                else -> {
                    val copy = items[index].copy(
                        username = item.username,
                        password = item.password,
                        token = item.token,
                        avatar = item.avatar,
                        readOnly = item.readOnly,
                        badge = item.badge,
                        promotes = item.promotes,
                        demotes = item.demotes
                    )
                    items[index] = copy
                    copy
                }
            }
        }
    }
}
