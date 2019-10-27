package ru.netology.saturn33.kt1.diploma

import com.jayway.jsonpath.JsonPath
import io.ktor.application.Application
import io.ktor.config.MapApplicationConfig
import io.ktor.http.*
import io.ktor.http.content.PartData
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import io.ktor.server.testing.withTestApplication
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.io.streams.asInput
import org.junit.Test
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@KtorExperimentalAPI
class ApplicationTest {
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

    private val configureExp: Application.() -> Unit = {
        (environment.config as MapApplicationConfig).apply {
            put("homework.upload.dir", uploadPath)
            put("homework.jwt.secret", "qweqwe")
            put("homework.jwt.expire", "2")
        }
        module()
    }

    @Test
    fun testUnauthorized() {
        withTestApplication(configure) {
            with(handleRequest(HttpMethod.Get, "/api/v1/posts")) {
                assertEquals(HttpStatusCode.Unauthorized, response.status())
            }
        }
    }

    @Test
    fun testAuth() {
        withTestApplication(configure) {
            runBlocking {
                var token: String?
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
                with(handleRequest(HttpMethod.Get, "/api/v1/me") {
                    addHeader(HttpHeaders.Authorization, "Bearer $token")
                }) {
                    assertEquals(HttpStatusCode.OK, response.status())
                    val username = JsonPath.read<String>(response.content!!, "$.username")
                    assertEquals("vasya", username)
                }
            }
        }
    }

    @Test
    fun testWrongAuth() {
        withTestApplication(configure) {
            runBlocking {
                with(handleRequest(HttpMethod.Post, "/api/v1/authentication") {
                    addHeader(HttpHeaders.ContentType, jsonContentType.toString())
                    setBody(
                        """
                        {
                            "username": "vasya",
                            "password": "wRoNgPaSsWoRd"
                        }
                    """.trimIndent()
                    )
                }) {
                    assertEquals(HttpStatusCode.Unauthorized, response.status())
                    println(response.content)
                }
            }
        }
    }

    @Test
    fun testReg() {
        withTestApplication(configure) {
            runBlocking {
                var token: String?
                with(handleRequest(HttpMethod.Post, "/api/v1/registration") {
                    addHeader(HttpHeaders.ContentType, jsonContentType.toString())
                    setBody(
                        """
                        {
                            "username": "andrey",
                            "password": "password2"
                        }
                    """.trimIndent()
                    )
                }) {
                    assertEquals(HttpStatusCode.OK, response.status())
                    token = JsonPath.read<String>(response.content!!, "$.token")
                }
                with(handleRequest(HttpMethod.Get, "/api/v1/me") {
                    addHeader(HttpHeaders.Authorization, "Bearer $token")
                }) {
                    assertEquals(HttpStatusCode.OK, response.status())
                    val username = JsonPath.read<String>(response.content!!, "$.username")
                    assertEquals("andrey", username)
                }
            }
        }
    }

    @Test
    fun testRegDup() {
        withTestApplication(configure) {
            runBlocking {
                with(handleRequest(HttpMethod.Post, "/api/v1/registration") {
                    addHeader(HttpHeaders.ContentType, jsonContentType.toString())
                    setBody(
                        """
                        {
                            "username": "vasya",
                            "password": "password2"
                        }
                    """.trimIndent()
                    )
                }) {
                    assertEquals(HttpStatusCode.BadRequest, response.status())
                    val error = JsonPath.read<String>(response.content!!, "$.error")
                    assertEquals("Пользователь с таким логином уже зарегистрирован", error)
                }
            }
        }
    }

    @Test
    fun testDeleteSomeoneElsePost() {
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

                //delete vasya's post
                with(handleRequest(HttpMethod.Delete, "/api/v1/posts/1") {
                    addHeader(HttpHeaders.ContentType, jsonContentType.toString())
                    addHeader(HttpHeaders.Authorization, "Bearer $token")
                }) {
                    assertEquals(HttpStatusCode.Forbidden, response.status())
                    println(response.content)
                }
            }
        }
    }

    @Test
    fun testUpload() {
        withTestApplication(configure) {
            var token: String?
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
            with(handleRequest(HttpMethod.Post, "/api/v1/media") {
                addHeader(HttpHeaders.ContentType, multipartContentType)
                addHeader(HttpHeaders.Authorization, "Bearer $token")
                setBody(
                    multipartBoundary,
                    listOf(
                        PartData.FileItem({
                            Files.newInputStream(Paths.get("./src/test/resources/test.png")).asInput()
                        }, {}, headersOf(
                            HttpHeaders.ContentDisposition to listOf(
                                ContentDisposition.File.withParameter(
                                    ContentDisposition.Parameters.Name,
                                    "file"
                                ).toString(),
                                ContentDisposition.File.withParameter(
                                    ContentDisposition.Parameters.FileName,
                                    "photo.png"
                                ).toString()
                            ),
                            HttpHeaders.ContentType to listOf(ContentType.Image.PNG.toString())
                        )
                        )
                    )
                )
            }) {
                assertEquals(HttpStatusCode.OK, response.status())
                assertTrue(response.content!!.contains("\"id\""))
                println(response.content)
            }
        }
    }

    @Test
    fun testExpire() {
        withTestApplication(configureExp) {
            runBlocking {
                var token: String?
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

                delay(1000)
                with(handleRequest(HttpMethod.Get, "/api/v1/me") {
                    addHeader(HttpHeaders.Authorization, "Bearer $token")
                }) {
                    assertEquals(HttpStatusCode.OK, response.status())
                }

                delay(2000)
                with(handleRequest(HttpMethod.Get, "/api/v1/me") {
                    addHeader(HttpHeaders.Authorization, "Bearer $token")
                }) {
                    assertEquals(HttpStatusCode.Unauthorized, response.status())
                }
            }
        }
    }
}