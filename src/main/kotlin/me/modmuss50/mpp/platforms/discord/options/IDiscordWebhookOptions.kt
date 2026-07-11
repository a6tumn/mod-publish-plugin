package me.modmuss50.mpp.platforms.discord.options

import org.gradle.api.Action
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.Optional

interface IDiscordWebhookOptions {
    @get:Input
    val webhookUrl: Property<String>

    @get:Input
    @get:Optional
    val dryRunWebhookUrl: Property<String>

    @get:Input
    val username: Property<String>

    @get:Input
    @get:Optional
    val avatarUrl: Property<String>

    @get:Input
    val content: Property<String>

    @get:Nested
    val style: Property<IMessageStyleOptions>

    fun from(other: IDiscordWebhookOptions) {
        webhookUrl.convention(other.webhookUrl)
        dryRunWebhookUrl.convention(other.dryRunWebhookUrl)
        username.convention(other.username)
        avatarUrl.convention(other.avatarUrl)
        content.convention(other.content)
        style.convention(other.style)
    }

    fun style(style: Action<IMessageStyleOptions>) {
        style.execute(this.style.get())
    }
}
