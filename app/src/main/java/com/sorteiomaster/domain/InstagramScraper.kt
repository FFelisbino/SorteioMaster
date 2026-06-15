package com.sorteiomaster.domain

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.sorteiomaster.data.model.Comentario
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

/**
 * Scraper de comentários públicos do Instagram.
 * Usa apenas dados publicamente acessíveis via API gráfica do Instagram
 * (equivalente a abrir o post no browser sem login).
 *
 * Limitação: posts privados ou com comentários restritos não funcionam.
 */
class InstagramScraper {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .header("User-Agent", "Mozilla/5.0 (Linux; Android 13) AppleWebKit/537.36 Chrome/120.0 Mobile Safari/537.36")
                .header("Accept", "application/json, text/plain, */*")
                .header("Accept-Language", "pt-BR,pt;q=0.9,en;q=0.8")
                .header("X-IG-App-ID", "936619743392459") // ID público do app web do Instagram
                .build()
            chain.proceed(request)
        }
        .build()

    private val gson = Gson()

    sealed class Resultado {
        data class Sucesso(val comentarios: List<Comentario>) : Resultado()
        data class Erro(val mensagem: String) : Resultado()
    }

    suspend fun buscarComentarios(urlPost: String): Resultado = withContext(Dispatchers.IO) {
        try {
            val shortcode = extrairShortcode(urlPost)
                ?: return@withContext Resultado.Erro("URL inválida. Use o link de um post do Instagram.\nEx: https://www.instagram.com/p/XXXXX/")

            val comentarios = mutableListOf<Comentario>()
            var cursor: String? = null
            var tentativas = 0
            val maxPaginas = 20 // Limite de segurança: ~400 comentários

            do {
                val url = buildUrl(shortcode, cursor)
                val response = fetchJson(url)
                    ?: return@withContext Resultado.Erro("Não foi possível acessar os comentários.\n\nPossíveis causas:\n• Post privado\n• Comentários desativados\n• Instagram temporariamente bloqueou o acesso")

                val parsed = parsePage(response)
                comentarios.addAll(parsed.comentarios)
                cursor = parsed.nextCursor
                tentativas++

                // Pequeno delay para não sobrecarregar
                if (cursor != null) kotlinx.coroutines.delay(800)

            } while (cursor != null && tentativas < maxPaginas)

            if (comentarios.isEmpty()) {
                return@withContext Resultado.Erro("Nenhum comentário encontrado.\n\nVerifique se o post é público e tem comentários habilitados.")
            }

            Resultado.Sucesso(comentarios)

        } catch (e: Exception) {
            Resultado.Erro("Erro ao buscar comentários: ${e.message}")
        }
    }

    private fun extrairShortcode(url: String): String? {
        // Aceita formatos:
        // https://www.instagram.com/p/SHORTCODE/
        // https://instagram.com/p/SHORTCODE
        // https://www.instagram.com/reel/SHORTCODE/
        val pattern = Pattern.compile("instagram\\.com/(?:p|reel|tv)/([A-Za-z0-9_-]+)")
        val matcher = pattern.matcher(url)
        return if (matcher.find()) matcher.group(1) else null
    }

    private fun buildUrl(shortcode: String, cursor: String?): String {
        val variables = buildString {
            append("""{"shortcode":"$shortcode","first":50""")
            if (cursor != null) append(""","after":"$cursor"""")
            append("}")
        }
        val encoded = java.net.URLEncoder.encode(variables, "UTF-8")
        return "https://www.instagram.com/api/graphql?variables=$encoded&doc_id=17852405266163336"
    }

    private fun fetchJson(url: String): JsonObject? {
        return try {
            val request = Request.Builder().url(url).get().build()
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) return null
            val body = response.body?.string() ?: return null
            gson.fromJson(body, JsonObject::class.java)
        } catch (e: Exception) {
            null
        }
    }

    private data class PaginaResult(
        val comentarios: List<Comentario>,
        val nextCursor: String?
    )

    private fun parsePage(json: JsonObject): PaginaResult {
        val comentarios = mutableListOf<Comentario>()
        var nextCursor: String? = null

        try {
            // Navegar na estrutura do JSON do Instagram
            val media = json
                .getAsJsonObject("data")
                ?.getAsJsonObject("shortcode_media")
                ?: return PaginaResult(emptyList(), null)

            val edgeComments = media.getAsJsonObject("edge_media_to_parent_comment")
                ?: return PaginaResult(emptyList(), null)

            // Paginação
            val pageInfo = edgeComments.getAsJsonObject("page_info")
            if (pageInfo?.get("has_next_page")?.asBoolean == true) {
                nextCursor = pageInfo.get("end_cursor")?.asString
            }

            // Comentários
            val edges = edgeComments.getAsJsonArray("edges") ?: return PaginaResult(emptyList(), null)

            for (edge in edges) {
                val node = edge.asJsonObject.getAsJsonObject("node") ?: continue
                val username = node.getAsJsonObject("owner")?.get("username")?.asString ?: continue
                val texto = node.get("text")?.asString ?: continue
                val timestamp = node.get("created_at")?.asString ?: ""

                // Extrair menções (@usuario) do texto
                val mencoes = extrairMencoes(texto)

                comentarios.add(
                    Comentario(
                        username = username,
                        texto = texto,
                        mencoes = mencoes,
                        timestamp = timestamp
                    )
                )

                // Respostas aos comentários (opcional — 1 nível)
                val respostas = node.getAsJsonObject("edge_threaded_comments")
                    ?.getAsJsonArray("edges")
                respostas?.forEach { resp ->
                    val rNode = resp.asJsonObject.getAsJsonObject("node") ?: return@forEach
                    val rUser = rNode.getAsJsonObject("owner")?.get("username")?.asString ?: return@forEach
                    val rTexto = rNode.get("text")?.asString ?: return@forEach
                    val rTs = rNode.get("created_at")?.asString ?: ""
                    comentarios.add(
                        Comentario(
                            username = rUser,
                            texto = rTexto,
                            mencoes = extrairMencoes(rTexto),
                            timestamp = rTs
                        )
                    )
                }
            }
        } catch (e: Exception) {
            // Ignora erros de parsing parcial
        }

        return PaginaResult(comentarios, nextCursor)
    }

    private fun extrairMencoes(texto: String): List<String> {
        val pattern = Pattern.compile("@([A-Za-z0-9._]+)")
        val matcher = pattern.matcher(texto)
        val mencoes = mutableListOf<String>()
        while (matcher.find()) {
            mencoes.add(matcher.group(1).lowercase())
        }
        return mencoes.distinct()
    }
}
