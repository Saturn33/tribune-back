package ru.netology.saturn33.kt1.diploma.service

import ru.netology.saturn33.kt1.diploma.BADGE_MIN
import ru.netology.saturn33.kt1.diploma.BADGE_TIMES
import ru.netology.saturn33.kt1.diploma.BADGE_TOP
import ru.netology.saturn33.kt1.diploma.REDUCER_LIMIT
import ru.netology.saturn33.kt1.diploma.dto.PostRequestDto
import ru.netology.saturn33.kt1.diploma.dto.PostResponseDto
import ru.netology.saturn33.kt1.diploma.dto.ReactionResponseDto
import ru.netology.saturn33.kt1.diploma.exception.ForbiddenException
import ru.netology.saturn33.kt1.diploma.exception.NotFoundException
import ru.netology.saturn33.kt1.diploma.extensions.reducer
import ru.netology.saturn33.kt1.diploma.model.PostModel
import ru.netology.saturn33.kt1.diploma.model.UserBadge
import ru.netology.saturn33.kt1.diploma.model.UserModel
import ru.netology.saturn33.kt1.diploma.repository.PostRepository
import java.util.*

class PostService(
    private val repo: PostRepository,
    internal val userService: UserService,
    private val fcmService: FCMService
) {

    suspend fun getRO(userId: Long): Boolean {
        return repo.getRO(userId)
    }

    suspend fun getLast(currentUser: UserModel, userId: Long, count: Int): List<PostResponseDto> {
        return repo.getLast(userId, count).map { PostResponseDto.fromModel(currentUser, userService.getModelById(it.author)!!, it) }
    }

    suspend fun getBefore(currentUser: UserModel, userId: Long, postId: Long, count: Int): List<PostResponseDto> {
        return repo.getBefore(postId, userId, count).map { PostResponseDto.fromModel(currentUser, userService.getModelById(it.author)!!, it) }
    }

    suspend fun getModelById(id: Long): PostModel? {
        return repo.getById(id)
    }

    suspend fun getById(currentUser: UserModel, id: Long, incrementView: Boolean = false): PostResponseDto {
        val model = repo.getById(id, incrementView) ?: throw NotFoundException()
        return PostResponseDto.fromModel(currentUser, userService.getModelById(model.author)!!, model)
    }

    suspend fun save(user: UserModel, input: PostRequestDto): PostResponseDto {
        if (user.readOnly)
            throw ForbiddenException("You are in read-only mode")
        val model = PostModel(
            id = 0L,
            date = Date().time,
            author = user.id,
            text = input.text,
            link = input.link,
            attachment = input.attachment
        )
        return PostResponseDto.fromModel(user, user, repo.save(model))
    }

    suspend fun promote(user: UserModel, id: Long): PostResponseDto {
        val model = repo.promoteById(user, id) ?: throw NotFoundException()
        val postText = model.text.reducer(REDUCER_LIMIT)
        sendSimplePush(id, model.author, "Your post promoted", "${user.username} поддержал вашу идею '${postText}'")
        recalcReadOnlyforUser(model.author)
        userService.increasePromotes(user.id)
        recalcBadgeforUser()
        return PostResponseDto.fromModel(user, userService.getModelById(model.author)!!, model)
    }

    suspend fun demote(user: UserModel, id: Long): PostResponseDto {
        val model = repo.demoteById(user, id) ?: throw NotFoundException()
        val postText = model.text.reducer(REDUCER_LIMIT)
        sendSimplePush(id, model.author, "Your post demoted", "${user.username} против вашей идеи '${postText}'")
        recalcReadOnlyforUser(model.author)
        userService.increaseDemotes(user.id)
        recalcBadgeforUser()
        return PostResponseDto.fromModel(user, userService.getModelById(model.author)!!, model)
    }

    private suspend fun recalcBadgeforUser() {
        val allUsers = userService.getAllUsers()
        val promoteUsers = allUsers.sortedByDescending { it.promotes }
        val demoteUsers = allUsers.sortedByDescending { it.demotes }

        allUsers.forEach { userService.setBadge(it.id, null) }
        promoteUsers.take(BADGE_TOP).forEach {
            if (it.promotes > BADGE_MIN && it.promotes > it.demotes * BADGE_TIMES)
                userService.setBadge(it.id, UserBadge.PROMOTER)
        }
        demoteUsers.take(BADGE_TOP).forEach {
            if (it.demotes > BADGE_MIN && it.demotes > it.promotes * BADGE_TIMES)
                userService.setBadge(it.id, UserBadge.HATER)
        }
    }

    private suspend fun recalcReadOnlyforUser(userId: Long) {
        val ro = getRO(userId)
        userService.setRO(userId, ro)
    }

    suspend fun getReactions(postId: Long): List<ReactionResponseDto> {
        return repo.getReactions(postId).map { ReactionResponseDto.fromModel(userService.getModelById(it.uid)!!, it) }
    }

    suspend fun sendSimplePush(postId: Long, userId: Long, title: String, text: String) {
        val model = userService.getModelById(userId)
        if (model?.token != null) {
            fcmService.send(userId, model.token.token, postId, title, text)
        }
    }

}
