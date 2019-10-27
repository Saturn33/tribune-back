package ru.netology.saturn33.kt1.diploma.service

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.springframework.security.crypto.password.PasswordEncoder
import ru.netology.saturn33.kt1.diploma.dto.*
import ru.netology.saturn33.kt1.diploma.exception.BadRequestException
import ru.netology.saturn33.kt1.diploma.exception.InvalidPasswordException
import ru.netology.saturn33.kt1.diploma.exception.NotFoundException
import ru.netology.saturn33.kt1.diploma.exception.PasswordChangeException
import ru.netology.saturn33.kt1.diploma.model.PushToken
import ru.netology.saturn33.kt1.diploma.model.UserModel
import ru.netology.saturn33.kt1.diploma.repository.UserRepository

class UserService(
    private val repo: UserRepository,
    private val tokenService: JWTTokenService,
    private val passwordEncoder: PasswordEncoder
) {
    private val mutex = Mutex()

    suspend fun getModelById(id: Long): UserModel? {
        return repo.getById(id)
    }

    suspend fun getById(id: Long): UserResponseDto {
        val model = repo.getById(id) ?: throw NotFoundException()
        return UserResponseDto.fromModel(model)
    }

    suspend fun register(input: RegistrationRequestDto): AuthenticationResponseDto {
        mutex.withLock {
            if (repo.getByUsername(input.username) != null) throw BadRequestException("Пользователь с таким логином уже зарегистрирован")
            val model = repo.save(UserModel(
                username = input.username,
                password = passwordEncoder.encode(input.password)
            ))
            val token = tokenService.generate(model.id)
            return AuthenticationResponseDto(token, model.readOnly)
        }
    }

    suspend fun authenticate(input: AuthenticationRequestDto): AuthenticationResponseDto {
        val model = repo.getByUsername(input.username) ?: throw NotFoundException("User not found")
        if (!passwordEncoder.matches(input.password, model.password)) {
            throw InvalidPasswordException("Wrong password!")
        }

        val token = tokenService.generate(model.id)
        return AuthenticationResponseDto(token, model.readOnly)
    }

    suspend fun save(username: String, password: String, readOnly: Boolean) {
        repo.save(UserModel(username = username, password = passwordEncoder.encode(password), readOnly = readOnly))
        return
    }

    suspend fun saveToken(user: UserModel, input: PushTokenRequestDto) {
        mutex.withLock {
            val copy = user.copy(token = PushToken(input.token))
            repo.save(copy)
        }
    }

    suspend fun deleteToken(user: UserModel) {
        mutex.withLock {
            val copy = user.copy(token = null)
            repo.save(copy)
        }
    }
}
