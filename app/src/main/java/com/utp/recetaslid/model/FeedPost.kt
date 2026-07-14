package com.utp.recetaslid.model

data class FeedPost(
    val recetaId: Int,
    val autorId: Int,
    val userName: String,
    val userInitial: String,
    val recipeTitle: String,
    val imagen: String,
    val likes: Int,
    val isLiked: Boolean,
    val comentarios: Int
)
