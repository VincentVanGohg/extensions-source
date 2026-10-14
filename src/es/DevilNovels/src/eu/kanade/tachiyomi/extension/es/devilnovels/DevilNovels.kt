package eu.kanade.tachiyomi.novelextension.es.devilnovels

import eu.kanade.tachiyomi.multisrc.lightnovelwpnovel.LightNovelWPNovel
import eu.kanade.tachiyomi.network.POST
import eu.kanade.tachiyomi.source.model.SChapter
import keiyoushi.annotation.Source
import okhttp3.FormBody
import org.json.JSONObject
import org.jsoup.nodes.Document

@Source
class DevilNovels(
    override val lang: String,
    override val id: Long,
) : LightNovelWPNovel() {
override val name = "DevilNovels"
    override val baseUrl = "https://devilnovels.com"
    override val seriesPath = ""

    override fun parseChapterList(doc: Document): List<SChapter> {
        val chapters = mutableListOf<SChapter>()
        var page = 1
        var totalPages = 1

        do {
            val body = FormBody.Builder()
                .add("action", "dv_load_chapters")
                .add("cat_id", "133")
                .add("page", page.toString())
                .add("search", "")
                .build()

            val request = POST(
                "$baseUrl/wp-admin/admin-ajax.php",
                headers,
                body,
            )

            val jsonText = client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    throw Exception("Error HTTP ${response.code}")
                }
                response.body?.string()
                    ?: throw Exception("Respuesta vacía")
            }

            val root = JSONObject(jsonText)
            if (!root.optBoolean("success")) break

            val data = root.getJSONObject("data")
            totalPages = data.optInt("pages", 1)
            val items = data.getJSONArray("chapters")

            for (i in 0 until items.length()) {
                val item = items.getJSONObject(i)
                val title = item.optString("title").trim()
                val link = item.optString("link").trim()

                if (title.isBlank() || link.isBlank()) continue

                val chapterUrl = if (link.startsWith("http")) {
                    link.removePrefix(baseUrl)
                } else {
                    link
                }

                val number = Regex(
                    """(?i)(?:cap[ií]tulo|chapter|cap\.?)\s*(\d+(?:[.,]\d+)?)"""
                ).find(title)?.groupValues?.get(1)
                    ?.replace(',', '.')
                    ?.toFloatOrNull()
                    ?: -1f

                chapters.add(
                    SChapter.create().apply {
                        name = title
                        url = chapterUrl
                        chapter_number = number
                    }
                )
            }

            page++
        } while (page <= totalPages)

        return chapters
    }
}