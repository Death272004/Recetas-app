package com.utp.recetaslid.model

data class VideoYouTube(
    val videoId: String,
    val titulo: String,
    val canal: String,
    val thumbnail: String
) {
    fun getUrl(): String = "https://www.youtube.com/watch?v=$videoId"
}
