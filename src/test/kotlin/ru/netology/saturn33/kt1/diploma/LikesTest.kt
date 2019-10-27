package ru.netology.saturn33.kt1.diploma

import com.jayway.jsonpath.JsonPath
import io.ktor.application.Application
import io.ktor.config.MapApplicationConfig
import io.ktor.http.*
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import io.ktor.server.testing.withTestApplication
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.runBlocking
import org.junit.Test
import java.nio.file.Files
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@KtorExperimentalAPI
class LikesTest {
    private val jsonContentType = ContentType.Application.Json.withCharset(Charsets.UTF_8)
    private val multipartBoundary = "***blob***"
    private val multipartContentType =
        ContentType.MultiPart.FormData.withParameter("boundary", multipartBoundary).toString()
    private val uploadPath = Files.createTempDirectory("test").toString()
    private val configure: Application.() -> Unit = {
        (environment.config as MapApplicationConfig).apply {
            put("homework.upload.dir", uploadPath)
            put("homework.jwt.secret", "qweqwe")
            put("homework.jwt.expire", "0")
        }
        module()
    }

    @Test
    fun testLike() {
        withTestApplication(configure) {
            runBlocking {
                var token: String?

                //auth as vasya
                with(handleRequest(HttpMethod.Post, "/api/v1/authentication") {
                    addHeader(HttpHeaders.ContentType, jsonContentType.toString())
                    setBody(
                        """
                        {
                            "username": "vasya",
                            "password": "password"
                        }
                    """.trimIndent()
                    )
                }) {
                    assertEquals(HttpStatusCode.OK, response.status())
                    token = JsonPath.read<String>(response.content!!, "$.token")
                }

                //make post
                with(handleRequest(HttpMethod.Post, "/api/v1/posts") {
                    addHeader(HttpHeaders.ContentType, jsonContentType.toString())
                    addHeader(HttpHeaders.Authorization, "Bearer $token")
                    setBody(
                        """
                        {
                            "id": 0,
                            "postType": "POST",
                            "content": "test"
                        }
                    """.trimIndent()
                    )
                }) {
                    assertEquals(HttpStatusCode.OK, response.status())
                    assertTrue(response.content!!.contains("\"id\": 1"))
                }

                //like post
                with(handleRequest(HttpMethod.Post, "/api/v1/posts/1/like") {
                    addHeader(HttpHeaders.ContentType, jsonContentType.toString())
                    addHeader(HttpHeaders.Authorization, "Bearer $token")
                }) {
                    assertEquals(HttpStatusCode.OK, response.status())
                    val likes = JsonPath.read<Int>(response.content!!, "$.likes")
                    assertEquals(1, likes, "Likes count")
                }
            }
        }
    }

    @Test
    fun testLikeTwice() {
        withTestApplication(configure) {
            runBlocking {
                var token: String?

                //auth as vasya
                with(handleRequest(HttpMethod.Post, "/api/v1/authentication") {
                    addHeader(HttpHeaders.ContentType, jsonContentType.toString())
                    setBody(
                        """
                        {
                            "username": "vasya",
                            "password": "password"
                        }
                    """.trimIndent()
                    )
                }) {
                    assertEquals(HttpStatusCode.OK, response.status())
                    token = JsonPath.read<String>(response.content!!, "$.token")
                }

                //make post
                with(handleRequest(HttpMethod.Post, "/api/v1/posts") {
                    addHeader(HttpHeaders.ContentType, jsonContentType.toString())
                    addHeader(HttpHeaders.Authorization, "Bearer $token")
                    setBody(
                        """
                        {
                            "id": 0,
                            "postType": "POST",
                            "content": "test"
                        }
                    """.trimIndent()
                    )
                }) {
                    assertEquals(HttpStatusCode.OK, response.status())
                    assertTrue(response.content!!.contains("\"id\": 1"))
                }

                //like post
                with(handleRequest(HttpMethod.Post, "/api/v1/posts/1/like") {
                    addHeader(HttpHeaders.ContentType, jsonContentType.toString())
                    addHeader(HttpHeaders.Authorization, "Bearer $token")
                }) {
                    assertEquals(HttpStatusCode.OK, response.status())
                    val likes = JsonPath.read<Int>(response.content!!, "$.likes")
                    assertEquals(1, likes, "Likes count")
                }

                //like post second time
                with(handleRequest(HttpMethod.Post, "/api/v1/posts/1/like") {
                    addHeader(HttpHeaders.ContentType, jsonContentType.toString())
                    addHeader(HttpHeaders.Authorization, "Bearer $token")
                }) {
                    assertEquals(HttpStatusCode.OK, response.status())
                    val likes = JsonPath.read<Int>(response.content!!, "$.likes")
                    assertEquals(1, likes, "Likes count")
                }
            }
        }
    }

    @Test
    fun testLikeDislike() {
        withTestApplication(configure) {
            runBlocking {
                var token: String?

                //auth as vasya
                with(handleRequest(HttpMethod.Post, "/api/v1/authentication") {
                    addHeader(HttpHeaders.ContentType, jsonContentType.toString())
                    setBody(
                        """
                        {
                            "username": "vasya",
                            "password": "password"
                        }
                    """.trimIndent()
                    )
                }) {
                    assertEquals(HttpStatusCode.OK, response.status())
                    token = JsonPath.read<String>(response.content!!, "$.token")
                }

                //make post
                with(handleRequest(HttpMethod.Post, "/api/v1/posts") {
                    addHeader(HttpHeaders.ContentType, jsonContentType.toString())
                    addHeader(HttpHeaders.Authorization, "Bearer $token")
                    setBody(
                        """
                        {
                            "id": 0,
                            "postType": "POST",
                            "content": "test"
                        }
                    """.trimIndent()
                    )
                }) {
                    assertEquals(HttpStatusCode.OK, response.status())
                    assertTrue(response.content!!.contains("\"id\": 1"))
                }

                //like post
                with(handleRequest(HttpMethod.Post, "/api/v1/posts/1/like") {
                    addHeader(HttpHeaders.ContentType, jsonContentType.toString())
                    addHeader(HttpHeaders.Authorization, "Bearer $token")
                }) {
                    assertEquals(HttpStatusCode.OK, response.status())
                    val likes = JsonPath.read<Int>(response.content!!, "$.likes")
                    assertEquals(1, likes, "Likes count")
                }

                //dislike post
                with(handleRequest(HttpMethod.Delete, "/api/v1/posts/1/like") {
                    addHeader(HttpHeaders.ContentType, jsonContentType.toString())
                    addHeader(HttpHeaders.Authorization, "Bearer $token")
                }) {
                    assertEquals(HttpStatusCode.OK, response.status())
                    val likes = JsonPath.read<Int>(response.content!!, "$.likes")
                    assertEquals(0, likes, "Likes count")
                }
            }
        }
    }

    @Test
    fun testLikeBy2Users() {
        withTestApplication(configure) {
            runBlocking {
                var token: String?

                //auth as vasya
                with(handleRequest(HttpMethod.Post, "/api/v1/authentication") {
                    addHeader(HttpHeaders.ContentType, jsonContentType.toString())
                    setBody(
                        """
                        {
                            "username": "vasya",
                            "password": "password"
                        }
                    """.trimIndent()
                    )
                }) {
                    assertEquals(HttpStatusCode.OK, response.status())
                    token = JsonPath.read<String>(response.content!!, "$.token")
                }

                //make post
                with(handleRequest(HttpMethod.Post, "/api/v1/posts") {
                    addHeader(HttpHeaders.ContentType, jsonContentType.toString())
                    addHeader(HttpHeaders.Authorization, "Bearer $token")
                    setBody(
                        """
                        {
                            "id": 0,
                            "postType": "POST",
                            "content": "test"
                        }
                    """.trimIndent()
                    )
                }) {
                    assertEquals(HttpStatusCode.OK, response.status())
                    assertTrue(response.content!!.contains("\"id\": 1"))
                }

                //like post
                with(handleRequest(HttpMethod.Post, "/api/v1/posts/1/like") {
                    addHeader(HttpHeaders.ContentType, jsonContentType.toString())
                    addHeader(HttpHeaders.Authorization, "Bearer $token")
                }) {
                    assertEquals(HttpStatusCode.OK, response.status())
                    val likes = JsonPath.read<Int>(response.content!!, "$.likes")
                    assertEquals(1, likes, "Likes count")
                }

                //auth as petya
                with(handleRequest(HttpMethod.Post, "/api/v1/authentication") {
                    addHeader(HttpHeaders.ContentType, jsonContentType.toString())
                    setBody(
                        """
                        {
                            "username": "petya",
                            "password": "password"
                        }
                    """.trimIndent()
                    )
                }) {
                    assertEquals(HttpStatusCode.OK, response.status())
                    token = JsonPath.read<String>(response.content!!, "$.token")
                }

                //like post
                with(handleRequest(HttpMethod.Post, "/api/v1/posts/1/like") {
                    addHeader(HttpHeaders.ContentType, jsonContentType.toString())
                    addHeader(HttpHeaders.Authorization, "Bearer $token")
                }) {
                    assertEquals(HttpStatusCode.OK, response.status())
                    val likes = JsonPath.read<Int>(response.content!!, "$.likes")
                    assertEquals(2, likes, "Likes count")
                }
            }
        }
    }

    @Test
    fun testLikeDislikeByAnotherUser() {
        withTestApplication(configure) {
            runBlocking {
                var token: String?

                //auth as vasya
                with(handleRequest(HttpMethod.Post, "/api/v1/authentication") {
                    addHeader(HttpHeaders.ContentType, jsonContentType.toString())
                    setBody(
                        """
                        {
                            "username": "vasya",
                            "password": "password"
                        }
                    """.trimIndent()
                    )
                }) {
                    assertEquals(HttpStatusCode.OK, response.status())
                    token = JsonPath.read<String>(response.content!!, "$.token")
                }

                //make post
                with(handleRequest(HttpMethod.Post, "/api/v1/posts") {
                    addHeader(HttpHeaders.ContentType, jsonContentType.toString())
                    addHeader(HttpHeaders.Authorization, "Bearer $token")
                    setBody(
                        """
                        {
                            "id": 0,
                            "postType": "POST",
                            "content": "test"
                        }
                    """.trimIndent()
                    )
                }) {
                    assertEquals(HttpStatusCode.OK, response.status())
                    assertTrue(response.content!!.contains("\"id\": 1"))
                }

                //like post
                with(handleRequest(HttpMethod.Post, "/api/v1/posts/1/like") {
                    addHeader(HttpHeaders.ContentType, jsonContentType.toString())
                    addHeader(HttpHeaders.Authorization, "Bearer $token")
                }) {
                    assertEquals(HttpStatusCode.OK, response.status())
                    val likes = JsonPath.read<Int>(response.content!!, "$.likes")
                    assertEquals(1, likes, "Likes count")
                }

                //auth as petya
                with(handleRequest(HttpMethod.Post, "/api/v1/authentication") {
                    addHeader(HttpHeaders.ContentType, jsonContentType.toString())
                    setBody(
                        """
                        {
                            "username": "petya",
                            "password": "password"
                        }
                    """.trimIndent()
                    )
                }) {
                    assertEquals(HttpStatusCode.OK, response.status())
                    token = JsonPath.read<String>(response.content!!, "$.token")
                }

                //dislike post
                with(handleRequest(HttpMethod.Delete, "/api/v1/posts/1/like") {
                    addHeader(HttpHeaders.ContentType, jsonContentType.toString())
                    addHeader(HttpHeaders.Authorization, "Bearer $token")
                }) {
                    assertEquals(HttpStatusCode.OK, response.status())
                    val likes = JsonPath.read<Int>(response.content!!, "$.likes")
                    assertEquals(1, likes, "Likes count")
                }
            }
        }
    }
}