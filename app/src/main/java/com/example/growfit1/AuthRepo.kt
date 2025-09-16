package com.example.growfit1

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await

object AuthRepo {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    suspend fun signInEmail(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password).await()
    }

    suspend fun signUpEmail(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password).await()
    }

    suspend fun signIn(email: String, password: String) {
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password).await()
    }

    suspend fun signUp(email: String, password: String) {
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password).await()
    }


    fun signOut() {
        com.google.firebase.auth.FirebaseAuth.getInstance().signOut()
    }


    fun uid(): String? = auth.currentUser?.uid

}
