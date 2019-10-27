package ru.netology.saturn33.kt1.diploma.service

import ru.netology.saturn33.kt1.diploma.dto.PostRequestDto
import ru.netology.saturn33.kt1.diploma.dto.PostResponseDto
import ru.netology.saturn33.kt1.diploma.exception.ForbiddenException
import ru.netology.saturn33.kt1.diploma.exception.NotFoundException
import ru.netology.saturn33.kt1.diploma.exception.ParameterConversionException
import ru.netology.saturn33.kt1.diploma.model.PostModel
import ru.netology.saturn33.kt1.diploma.model.PostType
import ru.netology.saturn33.kt1.diploma.model.UserModel
import ru.netology.saturn33.kt1.diploma.repository.PostRepository

class PostService(
    private val repo: PostRepository,
    private val userService: UserService,
    private val validatorService: ValidatorService,
    private val fcmService: FCMService
) {
    suspend fun getAll(currentUser: UserModel): List<PostResponseDto> {
        return repo.getAll().map { PostResponseDto.fromModel(currentUser, userService.getModelById(it.author)!!, it) }
    }

    suspend fun getLast(currentUser: UserModel, count: Int): List<PostResponseDto> {
        return repo.getLast(count).map { PostResponseDto.fromModel(currentUser, userService.getModelById(it.author)!!, it) }
    }

    suspend fun getAfter(currentUser: UserModel, postId: Long): List<PostResponseDto> {
        return repo.getAfter(postId).map { PostResponseDto.fromModel(currentUser, userService.getModelById(it.author)!!, it) }
    }

    suspend fun getBefore(currentUser: UserModel, postId: Long, count: Int): List<PostResponseDto> {
        return repo.getBefore(postId, count).map { PostResponseDto.fromModel(currentUser, userService.getModelById(it.author)!!, it) }
    }

    suspend fun getModelById(id: Long): PostModel? {
        return repo.getById(id)
    }

    suspend fun getById(currentUser: UserModel, id: Long, incrementView: Boolean = false): PostResponseDto {
        val model = repo.getById(id, incrementView) ?: throw NotFoundException()
        return PostResponseDto.fromModel(currentUser, userService.getModelById(model.author)!!, model)
    }

    suspend fun save(user: UserModel, input: PostRequestDto): PostResponseDto {
        repo.getById(input.id)?.let {
            if (it.author != user.id) throw ForbiddenException("Wrong author")
        }

        val model = when (input.postType) {
            PostType.POST -> PostModel(
                id = input.id,
                author = user.id,
                postType = input.postType,
                content = input.content,
                attachment = input.attachment,
//                media = input.media,
                views = 0
            )
/*
            PostType.EVENT -> if (validatorService.checkLocation(input.location)) PostModel(
                id = input.id,
                author = user.id,
                postType = input.postType,
                content = input.content,
                location = input.location,
                media = input.media,
                views = 0
            ) else throw ParameterConversionException("location", "Location")
            PostType.YOUTUBE -> if (validatorService.checkYoutube(input.video)) PostModel(
                id = input.id,
                author = user.id,
                postType = input.postType,
                content = input.content,
                video = input.video,
                views = 0
            ) else throw ParameterConversionException("video", "YouTube URL")
*/
            else -> throw ParameterConversionException("postType", "PostType")
        }
        return PostResponseDto.fromModel(user, user, repo.save(model))
    }

    suspend fun delete(user: UserModel, id: Long) {
        val post = repo.getById(id)
        if (post == null) {
            throw NotFoundException()
        } else {
            if (post.author != user.id) throw ForbiddenException("Wrong author")
            if (post.source != null)
            {
                repo.unrepostById(post.author, post.source.id)
            }
        }
        repo.removeById(id)
    }

    suspend fun like(user: UserModel, id: Long): PostResponseDto {
        val model = repo.likeById(user, id) ?: throw NotFoundException()
        val likeText = if (model.content?.length ?: 0 > 20) model.content?.substring(0, 20) + "..." else model.content
        if (user.id == model.author)
            sendSimplePush(model.author, "Автолайк", "Сам себя не полайкаешь - никто не полайкает!")
        else
            sendSimplePush(model.author, "Your post liked", "${user.username} лайкнул ваше сообщение '${likeText}'")
        return PostResponseDto.fromModel(user, userService.getModelById(model.author)!!, model)
    }

    suspend fun dislike(user: UserModel, id: Long): PostResponseDto {
        val model = repo.dislikeById(user, id) ?: throw NotFoundException()
        return PostResponseDto.fromModel(user, userService.getModelById(model.author)!!, model)
    }

    suspend fun repost(user: UserModel, postId: Long, input: PostRequestDto): PostResponseDto {
        val validResult = validatorService.checkSource(postId, getModelById(postId))
        if (validResult.first) {
            val model = PostModel(
                id = input.id,
                author = user.id,
                postType = PostType.REPOST,
                content = input.content,
//                media = input.media,
                source = if (validResult.second != null) PostResponseDto.fromModel(
                    user,
                    userService.getModelById((validResult.second)!!.author)!!,
                    validResult.second as PostModel
                ) else null,
                views = 0
            )
            val savedModel = repo.save(model)
            repo.repostById(user.id, postId)
            return PostResponseDto.fromModel(user, user, savedModel)
        } else throw ParameterConversionException("source", "PostModel")
    }

    suspend fun sendSimplePush(userId: Long, title: String, text: String) {
        val model = userService.getModelById(userId)
        if (model?.token != null) {
            fcmService.send(userId, model.token.token, title, text)
        }
    }
/*
    suspend fun share(id: Long): PostResponseDto {
        val model = repo.shareById(id) ?: throw NotFoundException("Source post not found")
        return PostResponseDto.fromModel(userService.getModelById(model.author)!!, model)
    }
*/
}
