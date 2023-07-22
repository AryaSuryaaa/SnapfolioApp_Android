package com.aryasurya.snapfolio2.data

import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class User(
    var username: String? = null,
    var description: String? = null,
    var email: String? = null,
    var telephone: String?= null,
    var photoUrl: String? = null) {

    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "username" to username,
            "description" to description,
        )
    }
}