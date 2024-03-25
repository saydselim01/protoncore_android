/*
 * Copyright (c) 2022 Proton Technologies AG
 * This file is part of Proton AG and ProtonCore.
 *
 * ProtonCore is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ProtonCore is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ProtonCore.  If not, see <https://www.gnu.org/licenses/>.
 */
package configuration

import com.android.build.api.dsl.ApplicationBuildType
import com.android.build.api.dsl.DefaultConfig
import com.android.build.gradle.BaseExtension
import configuration.extensions.environmentConfiguration
import configuration.extensions.mergeWith
import configuration.extensions.printEnvironmentConfigDetails
import configuration.extensions.sourceClassContent
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.configurationcache.extensions.capitalized
import java.util.Locale

typealias ConfigMatrix = List<Pair<String, EnvironmentConfig>>

class ProtonEnvironmentConfigurationPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        target.logger.info("Applying Proton environment configurations for ${target.name}")

        target.afterEvaluate {
            handleConfigurations(target)
        }
    }

    /**
     * Handles the configurations for the given project.
     *
     * Goes through all the product flavors and build types of the project,
     * merges their configurations, and then creates the necessary directories and files.
     *
     * @param project The project for which the configurations are being handled.
     */
    private fun handleConfigurations(project: Project) {
        project.extensions.getByType(BaseExtension::class.java).apply {
            val flavors = flavorDimensionList.mapNotNull { dimension ->
                productFlavors.filter { it.dimension == dimension }
                    .takeIf { it.isNotEmpty() }
                    ?.map { it.name.capitalized() to it.environmentConfiguration }
            }

            if (flavors.isEmpty()) {
                buildTypes.forEach {
                    project.handleBuildTypeOnly(defaultConfig, it)
                }
                return@apply
            }

            flavors.permutations.forEach { flavor ->
                buildTypes.forEach { buildType ->
                    project.handleFlavorAndBuildType(
                        defaultConfig,
                        flavor.joinedDecapitalized(),
                        buildType,
                        mergeConfigurations(*flavor.joinedConfig(buildType))
                    )
                }
            }
        }
    }

    private fun Project.handleFlavorAndBuildType(
        defaultConfig: DefaultConfig,
        flavorName: String,
        buildType: ApplicationBuildType,
        environmentConfig: EnvironmentConfig
    ) {
        val mergedConfig = mergeConfigurations(defaultConfig.environmentConfiguration, environmentConfig)
        val variantName = "${flavorName}${buildType.name.capitalized()}"
        val variantLocation = "${flavorName}/${buildType.name}"

        createJavaFileForVariant(
            variantName = variantName,
            variantLocation = variantLocation,
            config = mergedConfig
        )
    }


    private fun Project.handleBuildTypeOnly(
        defaultConfig: DefaultConfig,
        buildType: ApplicationBuildType
    ) {
        val mergedConfig = mergeConfigurations(
            defaultConfig.environmentConfiguration,
            buildType.environmentConfiguration
        )
        createJavaFileForVariant(
            variantName = buildType.name,
            variantLocation = buildType.name,
            config = mergedConfig
        )
    }

    private fun Project.createJavaFileForVariant(
        variantName: String,
        variantLocation: String,
        config: EnvironmentConfig
    ) {
        project.logger.debug("environment configuration :$variantName")
        project.printEnvironmentConfigDetails(config)
        project.createJavaFileInDir(
            variantName = variantName,
            variantLocation = variantLocation,
            sourceClassContent = config.sourceClassContent()
        )
    }

    /**
     * Creates a [javaClassName].java file in [generatedSourceLocation]/[variantLocation]/[packagePath]
     * adds flavor directories to source sets
     *
     * @param variantLocation Location relative to [generatedSourceLocation].
     * @param variantName Name of the variant.
     * @param sourceClassContent The content to be written to the source class file.
     * @param generatedSourceLocation Generated files dir
     * @param javaClassName Java class name to be generated
     * @param packagePath Package directory
     */
    @Suppress("LongParameterList")
    private fun Project.createJavaFileInDir(
        variantLocation: String,
        variantName: String,
        sourceClassContent: String,
        generatedSourceLocation: String = ENV_CONFIG_LOCATION,
        javaClassName: String = DEFAULTS_CLASS_NAME,
        packagePath: String = PACKAGE_NAME.replace(".", "/"),
    ) {
        val location = "$generatedSourceLocation/$variantLocation"
        val variantDirectory = project.layout.buildDirectory.asFile.get().resolve(location)
        val configDirectory =
            project.layout.buildDirectory.asFile.get().resolve("$location/$packagePath")

        configDirectory.mkdirs()

        val environmentConfigFile = configDirectory
            .resolve("$javaClassName.java").apply {
                writeText(sourceClassContent)
            }

        project.logger.info("generated ${environmentConfigFile.path}")

        project.extensions.getByType(BaseExtension::class.java).sourceSets
            .findByName(variantName)
            ?.java?.apply {
                // avoid duplicate content roots
                if (!srcDirs.contains(variantDirectory)) {
                    srcDir(variantDirectory)
                }
            }
    }

    private fun mergeConfigurations(vararg configs: EnvironmentConfig): EnvironmentConfig =
        configs.reduce { first, other -> first.mergeWith(other) }

    companion object {
        const val ENV_CONFIG_LOCATION: String = "generated/source/envConfig"
        const val PACKAGE_NAME: String = "me.proton.core.configuration"
        const val DEFAULTS_CLASS_NAME: String = "EnvironmentConfigurationDefaults"
    }
}

val <T> List<List<T>>.permutations: List<List<T>>
    get() =
        takeIf { isNotEmpty() }
            ?.first()
            ?.flatMap { item ->
                drop(1).permutations.map { listOf(item) + it }
            } ?: listOf(listOf())

fun ConfigMatrix.joinedDecapitalized() = joinToString("") { it.first }
    .replaceFirstChar { it.lowercase(Locale.getDefault()) }

fun ConfigMatrix.joinedConfig(buildType: ApplicationBuildType): Array<out EnvironmentConfig> =
    (map { it.second } + listOf(buildType.environmentConfiguration)).toTypedArray()