package ru.netology.saturn33.kt1.diploma.service

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.Message
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.commons.codec.binary.Hex
import org.springframework.security.crypto.encrypt.Encryptors
import java.io.ByteArrayInputStream
import java.nio.file.Files
import java.nio.file.Paths


class FCMService(dbUrl: String, password: String, salt: String, path: String) {
    init {
        val decryptor = Encryptors.stronger(password, Hex.encodeHexString(salt.toByteArray(Charsets.UTF_8)))
        val decrypted = decryptor.decrypt(Files.readAllBytes(Paths.get(path)))

        val options = FirebaseOptions.Builder()
            .setCredentials(GoogleCredentials.fromStream(ByteArrayInputStream(decrypted)))
            .setDatabaseUrl(dbUrl)
            .build()

        FirebaseApp.initializeApp(options)
    }

    suspend fun send(recipientId: Long, recipientToken: String, postId: Long, title: String, text: String) = withContext(Dispatchers.IO) {
        try {
            val message = Message.builder()
                .putData("recipientId", recipientId.toString())
                .putData("title", title)
                .putData("text", text)
                .putData("postId", postId.toString())
                .setToken(recipientToken)
                .build()

            val messageId = FirebaseMessaging.getInstance().send(message)
            println(messageId)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}