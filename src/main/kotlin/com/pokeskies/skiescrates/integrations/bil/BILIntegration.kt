package com.pokeskies.skiescrates.integrations.bil

import com.pokeskies.skiescrates.SkiesCrates
import com.pokeskies.skiescrates.SkiesCrates.Companion.asyncScope
import com.pokeskies.skiescrates.config.block.ModelOptions
import com.pokeskies.skiescrates.data.CrateInstance
import com.pokeskies.skiescrates.data.CrateOpenData
import com.pokeskies.skiescrates.integrations.IntegratedMod
import com.pokeskies.skiescrates.managers.CratesManager.openCrate
import com.pokeskies.skiescrates.managers.CratesManager.previewCrate
import com.pokeskies.skiescrates.utils.Utils
import de.tomalbrc.bil.core.model.Model
import de.tomalbrc.bil.file.loader.AjBlueprintLoader
import de.tomalbrc.bil.file.loader.AjModelLoader
import de.tomalbrc.bil.file.loader.BbModelLoader
import eu.pb4.polymer.virtualentity.api.attachment.ChunkAttachment
import eu.pb4.polymer.virtualentity.api.elements.InteractionElement
import eu.pb4.polymer.virtualentity.api.elements.VirtualElement.InteractionHandler
import kotlinx.coroutines.launch
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionHand
import net.minecraft.world.phys.Vec3
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

    fun createCrateData(instance: CrateInstance, modelOptions: ModelOptions): BILCrateData? {
        val model = getModel(modelOptions.id) ?: run {
            Utils.printError("The crate '${instance.crate.id}' is using a model '$${modelOptions.id}' which could not be found!")
            return null
        }

        val holder = CrateModelHolder(
            instance.level,
            instance.pos.bottomCenter,
            model,
            modelOptions.rotation,
            modelOptions.offset
        )
        holder.scale = modelOptions.scale
        modelOptions.animations.idle?.let { holder.animator.playAnimation(it) }
        val element = InteractionElement()
        element.setSize(modelOptions.hitbox.width, modelOptions.hitbox.height)
        element.offset = modelOptions.hitbox.offset
        element.setHandler(object : InteractionHandler {
            override fun interactAt(player: ServerPlayer, hand: InteractionHand, pos: Vec3) {
                asyncScope.launch {
                    openCrate(player, instance.crate, CrateOpenData(instance.dimPos, null), false)
                }
            }

            override fun attack(player: ServerPlayer) {
                previewCrate(player, instance.crate)
            }
        })
        holder.addElement(element)

        val attachment = ChunkAttachment.ofTicking(holder, instance.level,instance. pos)

        return BILCrateData(holder, attachment)
    }
}