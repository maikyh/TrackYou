package com.example.trackingyou

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

object FirebaseService {
    private val firestore = FirebaseFirestore.getInstance()

    fun addUserFirestore(user: User, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        firestore.collection("users").document(user.id).set(user)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }

    fun deleteUser(userId: String, onComplete: (Boolean) -> Unit) {
        firestore.collection("users").document(userId).delete()
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    fun updateUserFirestore(user: User, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        firestore.collection("users").document(user.id).set(user)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }

    fun fetchUsersFirestoreRealtime(onSuccess: (List<User>) -> Unit, onFailure: (Exception) -> Unit) {
        firestore.collection("users")
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    onFailure(e)
                    return@addSnapshotListener
                }
                val users = snapshots?.documents?.mapNotNull { it.toObject(User::class.java) } ?: emptyList()
                onSuccess(users)
            }
    }

    fun addRecordToUser(
        userId: String,
        record: Record,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        firestore.collection("users").document(userId)
            .update("registros", FieldValue.arrayUnion(record))
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onFailure(e) }
    }
}
