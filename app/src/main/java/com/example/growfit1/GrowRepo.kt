package com.example.growfit1

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.tasks.await

object GrowRepo {
    private val db = FirebaseFirestore.getInstance()

    // ---------- JOURNAL ----------
    suspend fun addWorkout(uid: String, entry: WorkoutEntry) {
        db.collection("users").document(uid).collection("journal").add(entry).await()
    }

    suspend fun getJournal(uid: String): List<WorkoutEntry> {
        val snap = db.collection("users").document(uid)
            .collection("journal")
            .orderBy("ts", Query.Direction.DESCENDING)
            .get().await()

        return snap.documents.mapNotNull { d ->
            d.toObject<WorkoutEntry>()?.copy(id = d.id)
        }
    }

    suspend fun deleteWorkout(uid: String, id: String) {
        db.collection("users").document(uid)
            .collection("journal").document(id)
            .delete().await()
    }

    suspend fun updateWorkout(uid: String, id: String, fields: Map<String, Any?>) {
        db.collection("users").document(uid)
            .collection("journal").document(id)
            .set(fields, SetOptions.merge())
            .await()
    }

    // ---------- PLAN ----------
    private val DAY_ORDER = listOf("Sun","Mon","Tue","Wed","Thu","Fri","Sat")
    private fun dayIndex(d: String) = DAY_ORDER.indexOf(d).takeIf { it >= 0 } ?: Int.MAX_VALUE

    suspend fun getPlan(uid: String): List<PlanItem> {
        val snap = db.collection("users").document(uid)
            .collection("plan")
            .get().await()

        return snap.documents.mapNotNull { d ->
            d.toObject<PlanItem>()?.copy(id = d.id)
        }.sortedWith(compareBy<PlanItem>({ dayIndex(it.day) }, { it.exercise }))
    }

    suspend fun addPlanItem(uid: String, item: PlanItem) {
        db.collection("users").document(uid).collection("plan").add(item).await()
    }

    suspend fun updatePlanItem(uid: String, id: String, fields: Map<String, Any?>) {
        db.collection("users").document(uid)
            .collection("plan").document(id)
            .set(fields, SetOptions.merge())
            .await()
    }

    suspend fun deletePlanItem(uid: String, id: String) {
        db.collection("users").document(uid)
            .collection("plan").document(id)
            .delete().await()
    }
}
