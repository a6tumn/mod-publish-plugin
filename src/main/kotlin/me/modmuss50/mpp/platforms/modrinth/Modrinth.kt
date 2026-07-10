package me.modmuss50.mpp.platforms.modrinth

import me.modmuss50.mpp.ModrinthPublishResult
import me.modmuss50.mpp.Platform
import me.modmuss50.mpp.PublishContext
import me.modmuss50.mpp.PublishResult
import me.modmuss50.mpp.PublishWorkAction
import me.modmuss50.mpp.PublishWorkParameters
import me.modmuss50.mpp.Retry
import me.modmuss50.mpp.Validators
import me.modmuss50.mpp.path
import me.modmuss50.mpp.platforms.modrinth.dependencies.IModrinthDependency
import me.modmuss50.mpp.platforms.modrinth.options.IModrinthOptions
import org.gradle.api.logging.Logger
import java.nio.file.Path
import javax.inject.Inject
import kotlin.random.Random

abstract class Modrinth @Inject constructor(
    name: String,
) : Platform(name), IModrinthOptions {
    override fun validateInputs() {
        super.validateInputs()
        Validators.validateUnique("minecraftVersions", minecraftVersions)
    }

    override fun publish(context: PublishContext) {
        context.submit(UploadWorkAction::class) {
            it.from(this)
        }
    }

    override fun dryRunPublishResult(): PublishResult =
        ModrinthPublishResult(
            // Use a random file ID so that the URL is different each time, this is needed because discord drops duplicate URLs
            id = "${Random.nextInt(0, 1000000)}",
            projectId = "dry-run",
            title = announcementTitle.getOrElse("Download from Modrinth"),
        )

    override fun printDryRunInfo(logger: Logger) {
        for (dependency in dependencies.get()) {
            val idOrSlug = dependency.id.orNull ?: dependency.slug.get()
            logger.lifecycle("Dependency(id/slug: $idOrSlug, version: ${dependency.version.orNull})")
        }
    }

    interface IUploadParams :
        PublishWorkParameters,
        IModrinthOptions

    abstract class UploadWorkAction : PublishWorkAction<IUploadParams> {
        override fun publish(): PublishResult {
            with(parameters) {
                val api = ModrinthApi(
                    accessToken = accessToken.get(),
                    baseUrl = apiEndpoint.get(),
                )

                val primaryFileKey = "primaryFile"
                val files = HashMap<String, Path>()
                files[primaryFileKey] = file.path

                additionalFiles.files.forEachIndexed { index, additionalFile ->
                    files["file_$index"] = additionalFile.toPath()
                }

                val dependencies = dependencies.get().map { toApiDependency(it, api) }

                val metadata =
                    ModrinthApi.CreateVersion(
                        name = displayName.get(),
                        versionNumber = version.get(),
                        changelog = changelog.orNull,
                        dependencies = dependencies,
                        gameVersions = minecraftVersions.get(),
                        versionType = ModrinthApi.VersionType.valueOf(type.get()),
                        environment = environment.orNull,
                        loaders = modLoaders.get().map { it.lowercase() },
                        featured = featured.get(),
                        projectId = projectId.get().modrinthId,
                        fileParts = files.keys.toList(),
                        primaryFile = primaryFileKey,
                    )

                val response =
                    Retry.run(maxRetries.get(), "Failed to create version") {
                        api.createVersion(metadata, files)
                    }

                if (projectDescription.isPresent) {
                    Retry.run(maxRetries.get(), "Failed to update project description") {
                        api.modifyProject(projectId.get().modrinthId, ModrinthApi.ModifyProject(body = projectDescription.get()))
                    }
                }

                return ModrinthPublishResult(
                    id = response.id,
                    projectId = response.projectId,
                    title = announcementTitle.getOrElse("Download from Modrinth"),
                )
            }
        }

        private fun toApiDependency(
            dependency: IModrinthDependency,
            api: ModrinthApi,
        ): ModrinthApi.Dependency {
            with(dependency) {
                var projectId: String? = null
                var versionId: String? = null

                // Use the project ID if we have it
                if (id.isPresent) {
                    projectId = id.get().modrinthId
                }

                // Lookup the project ID from the slug
                if (slug.isPresent) {
                    // Don't allow a slug and id to both be specified
                    if (projectId != null) {
                        throw IllegalStateException("Modrinth dependency cannot specify both projectId and projectSlug")
                    }

                    projectId =
                        Retry
                            .run(parameters.maxRetries.get(), "Failed to lookup project id from slug: ${slug.get()}") {
                                api.checkProject(slug.get())
                            }.id
                }

                // Ensure we have an id
                if (projectId == null) {
                    throw IllegalStateException("Modrinth dependency has no configured projectId or projectSlug value")
                }

                if (version.isPresent) {
                    val response =
                        Retry.run(parameters.maxRetries.get(), "Failed to list versions from slug/id: ${version.get()}") {
                            api.listVersions(projectId)
                        }

                    val versions =
                        response.filter {
                            it.id == version.get() || it.versionNumber == version.get()
                        }

                    versionId =
                        when (versions.size) {
                            0 -> throw IllegalStateException(
                                "Modrinth dependency has a version configured but no matches found for version: ${version.get()}",
                            )

                            1 -> versions.first().id

                            else -> throw IllegalStateException(
                                "Modrinth dependency has a version configured but multiple matches found for version: ${version.get()}",
                            )
                        }
                }

                return ModrinthApi.Dependency(
                    projectId = projectId,
                    versionId = versionId,
                    dependencyType = ModrinthApi.DependencyType.valueOf(type.get()),
                )
            }
        }
    }
}

private val ID_REGEX = Regex("[0-9a-zA-Z]{8}")

// Returns a validated ModrinthID
private val String.modrinthId: String
    get() {
        if (!this.matches(ID_REGEX)) {
            throw IllegalArgumentException("$this is not a valid Modrinth ID")
        }

        return this
    }
