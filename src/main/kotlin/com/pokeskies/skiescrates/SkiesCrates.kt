package com.pokeskies.skiescrates

import com.github.benmanes.caffeine.cache.AsyncLoadingCache
import com.github.benmanes.caffeine.cache.Caffeine
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.pokeskies.skiescrates.commands.BaseCommand
import com.pokeskies.skiescrates.commands.KeysCommand
import com.pokeskies.skiescrates.config.ConfigManager
import com.pokeskies.skiescrates.config.SoundOption
import com.pokeskies.skiescrates.config.lang.Lang
import com.pokeskies.skiescrates.data.CrateOpenData
import com.pokeskies.skiescrates.data.DimensionalBlockPos
import com.pokeskies.skiescrates.data.KeyCacheKey
import com.pokeskies.skiescrates.data.actions.Action
import com.pokeskies.skiescrates.data.actions.ActionType
import com.pokeskies.skiescrates.data.rewards.Reward
import com.pokeskies.skiescrates.data.rewards.RewardType
import com.pokeskies.skiescrates.economy.EconomyType
import com.pokeskies.skiescrates.economy.IEconomyService
import com.pokeskies.skiescrates.gui.InventoryType
import com.pokeskies.skiescrates.managers.CratesManager
import com.pokeskies.skiescrates.managers.CratesManager.tick
import com.pokeskies.skiescrates.managers.HologramsManager
import com.pokeskies.skiescrates.placeholders.PlaceholderManager
import com.pokeskies.skiescrates.storage.IStorage
import com.pokeskies.skiescrates.storage.StorageType
import com.pokeskies.skiescrates.utils.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.fabricmc.fabric.api.event.player.AttackBlockCallback
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents
import net.fabricmc.fabric.api.event.player.UseBlockCallback
import net.fabricmc.fabric.api.event.player.UseItemCallback
import net.fabricmc.loader.api.FabricLoader
import net.kyori.adventure.platform.fabric.FabricServerAudiences
import net.kyori.adventure.text.minimessage.MiniMessage
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtOps
import net.minecraft.nbt.Tag
import net.minecraft.resources.RegistryOps
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvent
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.item.Item
import net.minecraft.world.phys.BlockHitResult
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import xyz.nucleoid.stimuli.Stimuli
import xyz.nucleoid.stimuli.event.player.PlayerSwingHandEvent
import java.io.File
import java.io.IOException
import java.util.*
import java.util.concurrent.Executor
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
    var storage: IStorage? = null

    lateinit var adventure: FabricServerAudiences
    lateinit var server: MinecraftServer
    lateinit var nbtOpts: RegistryOps<Tag>

    private var economyServices: Map<EconomyType, IEconomyService> = emptyMap()

    private val cacheExecutor: Executor = Executors.newFixedThreadPool(2) { r ->
        val thread = Thread(r, "SkiesCrates-Keys-Cache")
        thread.isDaemon = true
        thread
    }

    private val playerKeyCache: AsyncLoadingCache<KeyCacheKey, Int> = Caffeine.newBuilder()
        .expireAfterWrite(5, TimeUnit.SECONDS)
        .executor(cacheExecutor)
        .buildAsync { key ->
            val storage = storage ?: return@buildAsync 0
            try {
                val playerData = runBlocking {
                    storage.getUser(key.playerUuid)
                }
                playerData.keys[key.keyId] ?: 0
            } catch (e: Exception) {
                0
            }
        }

    var gson: Gson = GsonBuilder().disableHtmlEscaping()
        .registerTypeAdapter(Reward::class.java, RewardType.RewardTypeAdaptor())
        .registerTypeAdapter(Action::class.java, ActionType.ActionTypeAdaptor())
        .registerTypeAdapter(StorageType::class.java, StorageType.StorageTypeAdaptor())
        .registerTypeHierarchyAdapter(Item::class.java, Utils.RegistrySerializer(BuiltInRegistries.ITEM))
        .registerTypeHierarchyAdapter(SoundEvent::class.java, Utils.RegistrySerializer(BuiltInRegistries.SOUND_EVENT))
        .registerTypeHierarchyAdapter(CompoundTag::class.java, Utils.CodecSerializer(CompoundTag.CODEC))
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
            this.storage = null
        }
        Lang.init()

        this.economyServices = IEconomyService.getLoadedEconomyServices()

        registerEvents()
    }

    private fun registerEvents() {
        ServerLifecycleEvents.SERVER_STARTING.register(ServerLifecycleEvents.ServerStarting { server: MinecraftServer ->
            this.adventure = FabricServerAudiences.of(server)
            this.server = server
            this.nbtOpts = server.registryAccess().createSerializationContext(NbtOps.INSTANCE)
        })
        ServerLifecycleEvents.SERVER_STARTED.register(ServerLifecycleEvents.ServerStarted { server: MinecraftServer ->
            CratesManager.init()
            PlaceholderManager.init()
            if (FabricLoader.getInstance().isModLoaded("holodisplays")) HologramsManager.load()
        })
        CommandRegistrationCallback.EVENT.register { dispatcher, _, _ ->
            BaseCommand().register(dispatcher)
            KeysCommand().register(dispatcher)
        }
        ServerLifecycleEvents.SERVER_STOPPED.register(ServerLifecycleEvents.ServerStopped { server: MinecraftServer ->
            if (FabricLoader.getInstance().isModLoaded("holodisplays")) HologramsManager.unload()
            this.storage?.close()
        })

        ServerTickEvents.END_SERVER_TICK.register(ServerTickEvents.EndTick { server ->
            tick()
        })

        // Preventing block breaking
        PlayerBlockBreakEvents.BEFORE.register(PlayerBlockBreakEvents.Before { level, player, blockPos, blockState, blockEntity ->
            val dimensionalPos = DimensionalBlockPos(
                level.dimension().location().toString(),
                blockPos.x,
                blockPos.y,
                blockPos.z
            )
            CratesManager.getCrateBlock(dimensionalPos)?.let { crate ->
                return@Before false
            }
            return@Before true
        })
        // Initially attempting to break a block
        AttackBlockCallback.EVENT.register(AttackBlockCallback { player, level, hand, blockPos, direction ->
            if (player !is ServerPlayer) return@AttackBlockCallback InteractionResult.PASS

            val dimensionalPos = DimensionalBlockPos(
                level.dimension().location().toString(),
                blockPos.x,
                blockPos.y,
                blockPos.z
            )
            CratesManager.getCrateBlock(dimensionalPos)?.let { crate ->
                CratesManager.previewCrate(player, crate)
                return@AttackBlockCallback InteractionResult.FAIL
            }
            return@AttackBlockCallback InteractionResult.PASS
        })
        // Called when right clicking a block, whether you use an item or not
        UseBlockCallback.EVENT.register(UseBlockCallback { player, level, hand, blockHitResult ->
            if (player !is ServerPlayer) return@UseBlockCallback InteractionResult.PASS
            if (hand != InteractionHand.MAIN_HAND) return@UseBlockCallback InteractionResult.PASS

            // Detect for a crate in hand to prevent the placement and attempt to open the crate
            val item = player.getItemInHand(hand)
            if (!item.isEmpty) {
                val crate = CratesManager.getCrateOrNull(item)
                if (crate != null) {
                    asyncScope.launch {
                        CratesManager.openCrate(player, crate, CrateOpenData(null, item), false)
                    }
                    return@UseBlockCallback InteractionResult.FAIL
                }
            }

            // Detect for a crate block
            val blockPos = DimensionalBlockPos(
                level.dimension().location().toString(),
                blockHitResult.blockPos.x,
                blockHitResult.blockPos.y,
                blockHitResult.blockPos.z
            )
            CratesManager.getCrateBlock(blockPos)?.let { crate ->
                asyncScope.launch {
                    CratesManager.openCrate(player, crate, CrateOpenData(blockPos, null), false)
                }
                return@UseBlockCallback InteractionResult.FAIL
            }

            return@UseBlockCallback InteractionResult.PASS
        })
        // Called when Right Clicking with an item/block in hand.
        // We need to detect for a crate in hand here, not key as that is handled by UseBlockCallback
        UseItemCallback.EVENT.register(UseItemCallback { player, level, hand ->
            if (player !is ServerPlayer) return@UseItemCallback InteractionResultHolder.pass(player.getItemInHand(hand))

            val item = player.getItemInHand(hand)
            if (hand != InteractionHand.MAIN_HAND) return@UseItemCallback InteractionResultHolder.pass(item)

            val crate = CratesManager.getCrateOrNull(item) ?: return@UseItemCallback InteractionResultHolder.pass(item)
            asyncScope.launch {
                CratesManager.openCrate(player, crate, CrateOpenData(null, item), false)
            }

            return@UseItemCallback InteractionResultHolder.fail(item)
        })
        // Called when swinging with your hand. This can happen in both a left-click and a right-click on a block
        Stimuli.global().listen(PlayerSwingHandEvent.EVENT, PlayerSwingHandEvent { player, hand ->
            if (hand != InteractionHand.MAIN_HAND) return@PlayerSwingHandEvent

            // This is a hacky fix to prevent right-clicking on blocks from opening preview menus
            val blockResult = player.pick(5.0, 0.0F, false)
            if (blockResult != null &&
                blockResult is BlockHitResult &&
                !player.serverLevel().getBlockState(blockResult.blockPos).isAir) return@PlayerSwingHandEvent

            val item = player.getItemInHand(hand)
            if (item.isEmpty) return@PlayerSwingHandEvent

            val crate = CratesManager.getCrateOrNull(item) ?: return@PlayerSwingHandEvent
            CratesManager.previewCrate(player, crate)
        })
    }

    fun reload() {
        if (FabricLoader.getInstance().isModLoaded("holodisplays")) HologramsManager.unload()
        this.storage?.close()

        ConfigManager.load()
        try {
            this.storage = IStorage.load(ConfigManager.CONFIG.storage)
        } catch (e: IOException) {
            Utils.printError(e.message)
            this.storage = null
        }
        Lang.init()

        this.economyServices = IEconomyService.getLoadedEconomyServices()

        CratesManager.init()
        if (FabricLoader.getInstance().isModLoaded("holodisplays")) HologramsManager.load()
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
        return playerKeyCache.get(KeyCacheKey(uuid, keyId)).get()
    }
}
