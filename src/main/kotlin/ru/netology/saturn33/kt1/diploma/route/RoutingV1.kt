package ru.netology.saturn33.kt1.diploma.route

import io.ktor.application.call
import io.ktor.auth.authenticate
import io.ktor.auth.authentication
import io.ktor.http.content.files
import io.ktor.http.content.static
import io.ktor.request.receive
import io.ktor.request.receiveMultipart
import io.ktor.response.respond
import io.ktor.routing.*
import ru.netology.saturn33.kt1.diploma.dto.*
import ru.netology.saturn33.kt1.diploma.exception.ParameterConversionException
import ru.netology.saturn33.kt1.diploma.model.UserModel
import ru.netology.saturn33.kt1.diploma.service.FileService
import ru.netology.saturn33.kt1.diploma.service.PostService
import ru.netology.saturn33.kt1.diploma.service.UserService

class RoutingV1(
    private val staticPath: String,
    private val postService: PostService,
    private val fileService: FileService,
    private val userService: UserService
) {
    fun setup(configuration: Routing) {
        with(configuration) {
            route("/api/v1") {
                static("/static") {
                    files(staticPath)
                }

                route("") {
                    post("/registration") {
                        val input = call.receive<RegistrationRequestDto>()
                        val response = userService.register(input)
                        call.respond(response)
                    }
                    post("/authentication") {
                        val input = call.receive<AuthenticationRequestDto>()
                        val response = userService.authenticate(input)
                        call.respond(response)
                    }
                }
                route("/token") {
                    delete("/{id}") {
                        val userId = call.parameters["id"]?.toLongOrNull() ?: throw ParameterConversionException("id", "Long")
                        val model = userService.getModelById(userId)
                        val response = if (model != null) userService.deleteToken(model) else throw ParameterConversionException("id", "Long")
                        call.respond(response)
                    }
                }

                authenticate {
                    route("/token") {
                        post {
                            val input = call.receive<PushTokenRequestDto>()
                            val me = call.authentication.principal<UserModel>()
                            val response = userService.saveToken(me!!, input)
                            call.respond(response)
                        }
                    }

                    route("/posts") {
                        //main post operations
                        get("/recent/{userId}/{count}") {
                            val userId = call.parameters["userId"]?.toLongOrNull() ?: throw ParameterConversionException("userid", "Long")
                            val count = call.parameters["count"]?.toIntOrNull() ?: throw ParameterConversionException("count", "Int")
                            val me = call.authentication.principal<UserModel>()

                            val response = postService.getLast(me!!, userId, count)
                            call.respond(response)
                        }
                        get("/before/{userId}/{id}/{count}") {
                            val userId = call.parameters["userId"]?.toLongOrNull() ?: throw ParameterConversionException("userid", "Long")
                            val id = call.parameters["id"]?.toLongOrNull() ?: throw ParameterConversionException("id", "Long")
                            val count = call.parameters["count"]?.toIntOrNull() ?: throw ParameterConversionException("count", "Int")
                            val me = call.authentication.principal<UserModel>()

                            val response = postService.getBefore(me!!, userId, id, count)
                            call.respond(response)
                        }
                        post {
                            val input = call.receive<PostRequestDto>()
                            val me = call.authentication.principal<UserModel>()
                            val response = postService.save(me!!, input)
                            call.respond(response)
                        }

                        //likes
                        post("/{id}/promote") {
                            val id = call.parameters["id"]?.toLongOrNull() ?: throw ParameterConversionException("id", "Long")
                            val me = call.authentication.principal<UserModel>()
                            val response = postService.promote(me!!, id)
                            call.respond(response)
                        }
                        post("/{id}/demote") {
                            val id = call.parameters["id"]?.toLongOrNull() ?: throw ParameterConversionException("id", "Long")
                            val me = call.authentication.principal<UserModel>()
                            val response = postService.demote(me!!, id)
                            call.respond(response)
                        }

                        //reactions
                        get("/reactions/{postId}") {
                            val postId = call.parameters["postId"]?.toLongOrNull() ?: throw ParameterConversionException("postId", "Long")
                            val response = postService.getReactions(postId)
                            call.respond(response)
                        }
                    }

                    route("/profile") {
                        //profile operations
                        get {
                            val me = call.authentication.principal<UserModel>()
                            val response = userService.getProfile(me!!)
                            call.respond(response)
                        }
                        post {
                            val me = call.authentication.principal<UserModel>()
                            val input = call.receive<ProfileRequestDto>()
                            val response = userService.saveProfile(me!!, input)
                            call.respond(response)
                        }
                    }

                    //загрузка media
                    route("/media") {
                        post {
                            val multipart = call.receiveMultipart()
                            val response = fileService.save(multipart)
                            call.respond(response)
                        }
                    }
                }
            }
        }
    }
}
