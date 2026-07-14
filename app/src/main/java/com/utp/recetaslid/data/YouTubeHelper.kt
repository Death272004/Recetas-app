package com.utp.recetaslid.data

import android.text.Html
import com.utp.recetaslid.model.VideoYouTube
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

object YouTubeHelper {

    fun buscar(query: String, maxResults: Int = 10): List<VideoYouTube> {
        val key = AppConfig.YOUTUBE_API_KEY
        if (key.isEmpty()) return emptyList()

        val encoded = URLEncoder.encode("receta $query", "UTF-8")
        val url = URL(
            "https://www.googleapis.com/youtube/v3/search" +
                "?part=snippet&q=$encoded&type=video&maxResults=$maxResults&key=$key"
        )

        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "GET"
        conn.connectTimeout = 5000
        conn.readTimeout = 5000

        val response = conn.inputStream.bufferedReader().readText()
        conn.disconnect()

        val json = JSONObject(response)
        val items = json.getJSONArray("items")
        val resultados = mutableListOf<VideoYouTube>()

        for (i in 0 until items.length()) {
            val item = items.getJSONObject(i)
            val id = item.getJSONObject("id").getString("videoId")
            val snippet = item.getJSONObject("snippet")
            val titulo = Html.fromHtml(
                snippet.getString("title"), Html.FROM_HTML_MODE_LEGACY
            ).toString()
            val canal = snippet.getString("channelTitle")
            val thumb = snippet.getJSONObject("thumbnails")
                .getJSONObject("medium").getString("url")
            resultados.add(VideoYouTube(id, titulo, canal, thumb))
        }

        return resultados
    }
}
