package com.pokeskies.skiescrates.config

import com.eduardomcb.discord.webhook.WebhookManager
import com.eduardomcb.discord.webhook.models.*
import com.google.gson.annotations.SerializedName
import com.pokeskies.skiescrates.SkiesCrates

class WebhookSettings(
    @SerializedName("duplicate_key")
    val duplicateKey: WebhookOptions = WebhookOptions(),
    @SerializedName("crate_opened")
    val crateOpened: WebhookOptions = WebhookOptions(),
) {
    class WebhookOptions(
        @SerializedName("webhook_url", alternate = ["url"])
        val webhookURL: String = "",
        val username: String = "",
        @SerializedName("avatar_url")
        val avatarURL: String = "",
        val content: String = "",
        val embeds: List<EmbedOptions> = emptyList(),
    ) {
        fun send(placeholders: Map<String, String>) {
            if (webhookURL.isEmpty()) return

            SkiesCrates.INSTANCE.asyncExecutor.execute {
                val manager = WebhookManager().setChannelUrl(applyPlaceholders(webhookURL, placeholders))

                buildMessage(placeholders)?.let { manager.setMessage(it) }
                buildEmbed(placeholders).let { manager.setEmbeds(it.toTypedArray()) }

                manager.exec()
            }
        }

        private fun buildMessage(placeholders: Map<String, String>): Message? {
            if (username.isEmpty() && avatarURL.isEmpty() && content.isEmpty()) return null

            return Message().also { message ->
                parse(username, placeholders)?.let { message.setUsername(it) }
                parse(avatarURL, placeholders)?.let { message.setAvatarUrl(it) }
                parse(content, placeholders)?.let { message.content = it }
            }
        }

        private fun buildEmbed(placeholders: Map<String, String>): List<Embed> {
            if (embeds.isEmpty()) return emptyList()

            return embeds.map { embedConfig ->
                Embed().also { discordEmbed ->
                    parse(embedConfig.title, placeholders)?.let { discordEmbed.setTitle(it) }
                    discordEmbed.setColor(embedConfig.color)
                    parse(embedConfig.description, placeholders)?.let { discordEmbed.setDescription(it) }
                    parse(embedConfig.timestamp, placeholders)?.let { discordEmbed.setTimestamp(it) }
                    parse(embedConfig.url, placeholders)?.let { discordEmbed.setUrl(it) }
                    embedConfig.author?.toAuthor(placeholders)?.let { discordEmbed.setAuthor(it) }
                    embedConfig.image?.toImage(placeholders)?.let { discordEmbed.setImage(it) }
                    embedConfig.thumbnail?.toImage(placeholders)?.let { discordEmbed.setThumbnail(it) }
                    embedConfig.footer?.toFooter(placeholders)?.let { discordEmbed.setFooter(it) }
                    embedConfig.fields.mapNotNull { it.toField(placeholders) }
                        .takeIf { it.isNotEmpty() }
                        ?.let { discordEmbed.setFields(it.toTypedArray()) }
                }
            }
        }

        override fun toString(): String {
            return "WebhookOptions(url='$webhookURL', username='$username', avatarUrl='$avatarURL', content='$content', embeds=$embeds)"
        }

        class EmbedOptions(
            val title: String = "",
            val color: Int = 0x000000,
            val description: String = "",
            val timestamp: String = "",
            val url: String = "",
            val author: AuthorOptions? = null,
            val image: ImageOptions? = null,
            val thumbnail: ImageOptions? = null,
            val footer: FooterOptions? = null,
            val fields: List<FieldOptions> = emptyList(),
        ) {
            override fun toString(): String {
                return "EmbedOptions(title='$title', color=$color, description='$description', timestamp='$timestamp', url='$url', author=$author, image=$image, thumbnail=$thumbnail, footer=$footer, fields=$fields)"
            }
        }

        class AuthorOptions(
            val name: String = "",
            val url: String = "",
            @SerializedName("icon_url")
            val iconUrl: String = "",
        ) {
            fun toAuthor(placeholders: Map<String, String>): Author? {
                val renderedName = parse(name, placeholders) ?: return null
                return Author(renderedName, parse(url, placeholders) ?: "", parse(iconUrl, placeholders) ?: "")
            }

            override fun toString(): String {
                return "AuthorOptions(name='$name', url='$url', iconUrl='$iconUrl')"
            }
        }

        class FooterOptions(
            val text: String = "",
            @SerializedName("icon_url")
            val iconUrl: String = "",
        ) {
            fun toFooter(placeholders: Map<String, String>): Footer? {
                val renderedText = parse(text, placeholders) ?: return null
                return Footer(renderedText, parse(iconUrl, placeholders) ?: "")
            }

            override fun toString(): String {
                return "FooterOptions(text='$text', iconUrl='$iconUrl')"
            }
        }

        class ImageOptions(
            val url: String = "",
        ) {
            fun toImage(placeholders: Map<String, String>): Image? {
                return parse(url, placeholders)?.let { Image(it) }
            }

            override fun toString(): String {
                return "ImageOptions(url='$url')"
            }
        }

        class FieldOptions(
            val name: String = "",
            val value: String = "",
            val inline: Boolean = false,
        ) {
            fun toField(placeholders: Map<String, String>): Field? {
                val renderedName = parse(name, placeholders) ?: return null
                val renderedValue = parse(value, placeholders) ?: return null
                return Field(renderedName, renderedValue.take(1024), inline)
            }

            override fun toString(): String {
                return "FieldOptions(name='$name', value='$value', inline=$inline)"
            }
        }

        companion object {
            fun parse(value: String, placeholders: Map<String, String>): String? {
                if (value.isEmpty()) return null
                return applyPlaceholders(value, placeholders)
            }

            fun applyPlaceholders(value: String, placeholders: Map<String, String>): String {
                var parsed = value
                placeholders.forEach { (placeholder, replacement) ->
                    parsed = parsed.replace(placeholder, replacement)
                }
                return parsed
            }
        }
    }

    override fun toString(): String {
        return "WebhookSettings(duplicateKey='$duplicateKey', crateOpen='$crateOpened')"
    }
}
