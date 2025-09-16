package com.example.growfit1

import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.tasks.await
import java.util.UUID

object StorageRepo {
    private val storage = Firebase.storage

    suspend fun uploadJournalPhoto(uid: String, bytes: ByteArray): String {
        val ref = storage.reference.child("journal/$uid/${UUID.randomUUID()}.jpg")
        ref.putBytes(bytes).await()
        return ref.downloadUrl.await().toString()
    }
}
