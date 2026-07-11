package me.modmuss50.mpp.platforms.curseforge

import me.modmuss50.mpp.CurseForgePublishResult
import me.modmuss50.mpp.Platform
import me.modmuss50.mpp.PublishContext
import me.modmuss50.mpp.PublishResult
import me.modmuss50.mpp.PublishWorkAction
import me.modmuss50.mpp.PublishWorkParameters
import me.modmuss50.mpp.Retry
import me.modmuss50.mpp.Validators
import me.modmuss50.mpp.path
import me.modmuss50.mpp.platforms.curseforge.options.ICurseforgeOptions
import org.gradle.api.logging.Logger
import javax.inject.Inject
import kotlin.random.Random

abstract class Curseforge @Inject constructor(
    name: String,
) : Platform(name), ICurseforgeOptions {
    override fun validateInputs() {
        super.validateInputs()
        Validators.validateUnique("minecraftVersions", minecraftVersions)
        Validators.validateUnique("javaVersions", javaVersions)

        if (client.orNull != true && server.orNull != true) {
            throw IllegalArgumentException("At least one of client or server must be set to true")
        }
    }

    override fun publish(context: PublishContext) {
        context.submit(UploadWorkAction::class) {
            it.from(this)
        }
    }

    override fun dryRunPublishResult(): PublishResult =
        CurseForgePublishResult(
            projectId = projectId.get(),
            projectSlug = projectSlug.map { "dry-run" }.orNull,
            // Use a random file ID so that the URL is different each time, this is needed because Discord drops duplicate URLs
            fileId = Random.nextInt(0, 1000000),
            title = announcementTitle.getOrElse("Download from CurseForge"),
        )

    override fun printDryRunInfo(logger: Logger) {
        for (dependency in dependencies.get()) {
            logger.lifecycle("Dependency(slug: ${dependency.slug.get()}, type: ${dependency.type.get()})")
        }
    }

    interface IUploadParams :
        PublishWorkParameters,
        ICurseforgeOptions

    abstract class UploadWorkAction : PublishWorkAction<IUploadParams> {
        override fun publish(): PublishResult {
            with(parameters) {
                val api = CurseforgeApi(
                    accessToken = accessToken.get(),
                    baseUrl = apiEndpoint.get(),
                )

                val versions =
                    CurseforgeVersions(
                        Retry.run(maxRetries.get(), "Failed to get game version types") {
                            api.getVersionTypes()
                        },
                        Retry.run(maxRetries.get(), "Failed to get game versions") {
                            api.getGameVersions()
                        },
                    )

                val gameVersions = ArrayList<Int>()
                for (version in minecraftVersions.get()) {
                    gameVersions.add(versions.getMinecraftVersion(version))
                }

                for (modLoader in modLoaders.get()) {
                    gameVersions.add(versions.getModLoaderVersion(modLoader))
                }

                if (client.isPresent && client.get()) {
                    gameVersions.add(versions.getClientVersion())
                }

                if (server.isPresent && server.get()) {
                    gameVersions.add(versions.getServerVersion())
                }

                for (javaVersion in javaVersions.get()) {
                    gameVersions.add(versions.getJavaVersion(javaVersion))
                }

                val projectRelations =
                    dependencies.get().map {
                        CurseforgeApi.ProjectFileRelation(
                            slug = it.slug.get(),
                            type = CurseforgeApi.RelationType.valueOf(it.type.get()),
                        )
                    }

                val relations =
                    if (projectRelations.isNotEmpty()) {
                        CurseforgeApi.UploadFileRelations(
                            projects = projectRelations,
                        )
                    } else {
                        null
                    }

                val metadata =
                    CurseforgeApi.UploadFileMetadata(
                        changelog = changelog.get(),
                        changelogType = CurseforgeApi.ChangelogType.of(changelogType.get()),
                        displayName = displayName.get(),
                        gameVersions = gameVersions,
                        releaseType = CurseforgeApi.ReleaseType.valueOf(type.get()),
                        relations = relations,
                    )

                val response =
                    Retry.run(maxRetries.get(), "Failed to upload file") {
                        api.uploadFile(projectId.get(), file.path, metadata)
                    }

                val additionalFileOptions =
                    additionalFilesExt
                        .get()
                        .map { (key, value) ->
                            key.singleFile.toPath() to value
                        }.toMap()

                for (additionalFile in additionalFiles.files) {
                    val fileOptions = additionalFileOptions[additionalFile.toPath()]
                    val additionalMetadata =
                        metadata.copy(
                            parentFileID = response.id,
                            gameVersions = null,
                            displayName = fileOptions?.name?.orNull,
                        )

                    Retry.run(maxRetries.get(), "Failed to upload additional file") {
                        api.uploadFile(projectId.get(), additionalFile.toPath(), additionalMetadata)
                    }
                }

                return CurseForgePublishResult(
                    projectId = projectId.get(),
                    projectSlug = projectSlug.orNull,
                    fileId = response.id,
                    title = announcementTitle.getOrElse("Download from CurseForge"),
                )
            }
        }
    }
}
