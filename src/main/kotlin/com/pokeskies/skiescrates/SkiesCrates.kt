package com.pokeskies.skiescrates

import com.github.benmanes.caffeine.cache.AsyncLoadingCache
import com.github.benmanes.caffeine.cache.Caffeine
import com.google.common.util.concurrent.ThreadFactoryBuilder
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.pokeskies.skiescrates.commands.BaseCommand
import com.pokeskies.skiescrates.commands.KeysCommand
import com.pokeskies.skiescrates.config.ConfigManager
import com.pokeskies.skiescrates.config.SoundOption
import com.pokeskies.skiescrates.config.lang.Lang
import com.pokeskies.skiescrates.data.KeyCacheKey
import com.pokeskies.skiescrates.data.actions.Action
import com.pokeskies.skiescrates.data.actions.ActionType
import com.pokeskies.skiescrates.data.particles.effects.ParticleEffect
import com.pokeskies.skiescrates.data.rewards.Reward
import com.pokeskies.skiescrates.data.rewards.RewardType
import com.pokeskies.skiescrates.economy.EconomyType
import com.pokeskies.skiescrates.economy.IEconomyService
import com.pokeskies.skiescrates.gui.InventoryType
import com.pokeskies.skiescrates.integrations.ModIntegration
import com.pokeskies.skiescrates.managers.CratesManager
import com.pokeskies.skiescrates.managers.CratesManager.tick
import com.pokeskies.skiescrates.managers.HologramsManager
import com.pokeskies.skiescrates.placeholders.PlaceholderManager
import com.pokeskies.skiescrates.storage.IStorage
import com.pokeskies.skiescrates.storage.StorageType
import com.pokeskies.skiescrates.utils.Utils
import kotlinx.coroutines.*
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.fabricmc.loader.api.FabricLoader
import net.kyori.adventure.platform.fabric.FabricServerAudiences
import net.kyori.adventure.text.minimessage.MiniMessage
import net.minecraft.core.particles.ParticleOptions
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtOps
import net.minecraft.nbt.Tag
import net.minecraft.resources.RegistryOps
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import net.minecraft.sounds.SoundEvent
import net.minecraft.world.item.Item
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.io.File
import java.io.IOException
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class SkiesCrates : ModInitializer {
    companion object {
        lateinit var INSTANCE: SkiesCrates

        const val MOD_ID = "skiescrates"
        const val MOD_NAME = "SkiesCrates"

        val LOGGER: Logger = LogManager.getLogger(MOD_ID)
        val MINI_MESSAGE: MiniMessage = MiniMessage.miniMessage()

        val asyncScope = CoroutineScope(Dispatchers.IO)

        @JvmStatic
        fun asResource(path: String): ResourceLocation {
            return ResourceLocation.fromNamespaceAndPath(MOD_ID, path)
        }
    }

    lateinit var configDir: File
    lateinit var storage: IStorage

    lateinit var adventure: FabricServerAudiences
    lateinit var server: MinecraftServer
    lateinit var nbtOpts: RegistryOps<Tag>

    private var economyServices: Map<EconomyType, IEconomyService> = emptyMap()

    val asyncExecutor: ExecutorService = Executors.newFixedThreadPool(8, ThreadFactoryBuilder()
        .setNameFormat("SkiesCrates-Async-%d")
        .setDaemon(true)
        .build())

    val playerKeyCache: AsyncLoadingCache<KeyCacheKey, Int> = Caffeine.newBuilder()
        .expireAfterWrite(30, TimeUnit.SECONDS)
        .refreshAfterWrite(10, TimeUnit.SECONDS)
        .executor(asyncExecutor)
        .buildAsync { key, executor ->
            if (server.playerList.getPlayer(key.playerUuid) == null) {
                return@buildAsync CompletableFuture.completedFuture(0)
            }

            try {
                CompletableFuture.supplyAsync({
                    try {
                        val userData = storage.getUser(key.playerUuid)
                        userData.keys[key.keyId] ?: 0
                    } catch (e: Exception) {
                        LOGGER.error("Error fetching key cache for ${key.playerUuid}: ${e.message}")
                        0
                    }
                }, asyncExecutor)
            } catch (e: Exception) {
                LOGGER.error("Failed to start async user data fetch: ${e.message}")
                CompletableFuture.completedFuture(0)
            }
        }

    @OptIn(DelicateCoroutinesApi::class)
    private val particleThreadPool = newFixedThreadPoolContext(1, "SkiesCratesParticleThread")
    private val particleScope = CoroutineScope(particleThreadPool + SupervisorJob())
    fun runOnParticleThread(block: suspend () -> Unit) {
        particleScope.launch {
            block()
            delay(1)
        }
    }

    var gson: Gson = GsonBuilder().disableHtmlEscaping()
        .registerTypeAdapter(Reward::class.java, RewardType.RewardTypeAdaptor())
        .registerTypeAdapter(Action::class.java, ActionType.ActionTypeAdaptor())
        .registerTypeAdapter(StorageType::class.java, StorageType.StorageTypeAdaptor())
        .registerTypeAdapter(ParticleEffect::class.java, ParticleEffect.ParticleEffectAdapter())
        .registerTypeHierarchyAdapter(Item::class.java, Utils.RegistrySerializer(BuiltInRegistries.ITEM))
        .registerTypeHierarchyAdapter(SoundEvent::class.java, Utils.RegistrySerializer(BuiltInRegistries.SOUND_EVENT))
        .registerTypeHierarchyAdapter(CompoundTag::class.java, Utils.CodecSerializer(CompoundTag.CODEC))
        .registerTypeHierarchyAdapter(ParticleOptions::class.java, Utils.CodecSerializer(ParticleTypes.CODEC))
        .registerTypeAdapter(InventoryType::class.java, InventoryType.Deserializer())
        .registerTypeAdapter(SoundOption::class.java, SoundOption.Adaptor())
        .create()

    var gsonPretty: Gson = gson.newBuilder().setPrettyPrinting().create()

    override fun onInitialize() {
        INSTANCE = this

        this.configDir = File(FabricLoader.getInstance().configDirectory, MOD_ID)
        ConfigManager.load()
        try {
            this.storage = IStorage.load(ConfigManager.CONFIG.storage)
        } catch (e: IOException) {
            Utils.printError(e.message)
        }
        Lang.init()

        this.economyServices = IEconomyService.getLoadedEconomyServices()

        ModIntegration.onInit()

        registerEvents()
    }

    private fun registerEvents() {
        ServerLifecycleEvents.SERVER_STARTING.register(ServerLifecycleEvents.ServerStarting { server: MinecraftServer ->
            this.adventure = FabricServerAudiences.of(server)
            this.server = server
            this.nbtOpts = server.registryAccess().createSerializationContext(NbtOps.INSTANCE)
            ModIntegration.onServerStarting()
        })
        ServerLifecycleEvents.SERVER_STARTED.register(ServerLifecycleEvents.ServerStarted { server: MinecraftServer ->
            CratesManager.init()
            PlaceholderManager.init()
            ModIntegration.onServerStarted()
        })
        CommandRegistrationCallback.EVENT.register { dispatcher, _, _ ->
            BaseCommand().register(dispatcher)
            KeysCommand().register(dispatcher)
        }
        ServerLifecycleEvents.SERVER_STOPPING.register(ServerLifecycleEvents.ServerStopping { server: MinecraftServer ->
            this.storage.close()
            ModIntegration.onServerShutdown()
        })
        ServerTickEvents.END_SERVER_TICK.register(ServerTickEvents.EndTick { server ->
            tick()

            if (server.tickCount % 6000 == 0) {
                cleanCache()
            }
        })
    }

    fun reload() {
        if (FabricLoader.getInstance().isModLoaded("holodisplays")) HologramsManager.unload()
        this.storage.close()

        ConfigManager.load()
        try {
            this.storage = IStorage.load(ConfigManager.CONFIG.storage)
        } catch (e: IOException) {
            Utils.printError(e.message)
        }
        Lang.init()

        this.economyServices = IEconomyService.getLoadedEconomyServices()

        CratesManager.init()
        if (FabricLoader.getInstance().isModLoaded("holodisplays")) HologramsManager.load()
    }

    private fun cleanCache() {
        playerKeyCache.synchronous().asMap().keys.forEach { key ->
            if (server.playerList.getPlayer(key.playerUuid) == null) {
                playerKeyCache.synchronous().invalidate(key)
                Utils.printDebug("cleanCache - Removed offline player ${key.playerUuid} from key cache")
            }
        }
    }

    fun getLoadedEconomyServices(): Map<EconomyType, IEconomyService> {
        return this.economyServices
    }

    fun getEconomyService(economyType: EconomyType?): IEconomyService? {
        return economyType?.let { this.economyServices[it] }
    }

    fun getEconomyServiceOrDefault(economyType: EconomyType?): IEconomyService? {
        return economyType?.let { this.economyServices[it] } ?: this.economyServices.values.firstOrNull()
    }

    fun getCachedKeys(uuid: UUID, keyId: String): Int {
        return playerKeyCache.get(KeyCacheKey(uuid, keyId)).getNow(0) ?: 0
    }
}
