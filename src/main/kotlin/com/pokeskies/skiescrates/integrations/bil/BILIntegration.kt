package com.pokeskies.skiescrates.integrations.bil

import com.pokeskies.skiescrates.SkiesCrates
import com.pokeskies.skiescrates.integrations.IntegratedMod
import com.pokeskies.skiescrates.utils.Utils
import de.tomalbrc.bil.core.model.Model
import de.tomalbrc.bil.file.loader.AjBlueprintLoader
import de.tomalbrc.bil.file.loader.AjModelLoader
import de.tomalbrc.bil.file.loader.BbModelLoader
import org.apache.commons.io.FilenameUtils
import java.nio.file.Files
import java.nio.file.Path
import java.util.stream.Collectors
import kotlin.io.path.name


class BILIntegration: IntegratedMod {
    private val models: MutableMap<String, Model> = mutableMapOf()

    override fun onInit() {
        Utils.printInfo("The mod Blockbench Import Library was found, enabling integrations...")
        loadModels()
    }

    fun loadModels() {
        val path = SkiesCrates.INSTANCE.configDir.resolve("models")
        if (path.exists() && path.isDirectory) {
            val files = Files.walk(path.toPath())
                .filter { path: Path -> Files.isRegularFile(path) }
                .map { it.toFile() }
                .collect(Collectors.toList())

            for (file in files) {
                processModelFile(file.toPath())
            }
        }

        Utils.printInfo("Loaded ${models.size} models for use in crates!")
    }

    private fun processModelFile(filePath: Path) {
        val name = filePath.name
        val ext = FilenameUtils.getExtension(name).lowercase()
        println("Processing model file $name with extension $ext located at $filePath")
        val model = when (ext) {
            "bbmodel" -> BbModelLoader.load(filePath.toString())
            "ajmodel" -> AjModelLoader.load(filePath.toString())
            "ajblueprint" -> AjBlueprintLoader.load(filePath.toString())
            else -> {
                Utils.printError("Unknown model file type found: $name")
                return
            }
        }

        models[FilenameUtils.getBaseName(name)] = model
    }

    fun getModel(name: String): Model? {
        return models[name]
    }
}