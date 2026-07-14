package com.utp.recetaslid.model

data class FeedPost(
    val recetaId: Int,
    val userName: String,
    val userInitial: String,
    val recipeTitle: String,
    val imagen: String,
    val likes: Int,
    val isLiked: Boolean
)
