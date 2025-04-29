package com.pokeskies.skiescrates.config

import com.google.gson.JsonParser
import com.google.gson.stream.JsonReader
import com.pokeskies.skiescrates.SkiesCrates
import com.pokeskies.skiescrates.data.Crate
import com.pokeskies.skiescrates.data.Key
import com.pokeskies.skiescrates.data.animations.InventoryAnimation
import com.pokeskies.skiescrates.data.previews.Preview
import com.pokeskies.skiescrates.utils.Utils
import java.io.*
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.util.stream.Collectors

object ConfigManager {
    private var assetPackage = "assets/${SkiesCrates.MOD_ID}"

    lateinit var CONFIG: SkiesCratesConfig
    lateinit var CRATES: MutableMap<String, Crate>
    lateinit var KEYS: MutableMap<String, Key>
    lateinit var ANIMATIONS_INVENTORY: MutableMap<String, InventoryAnimation>
    lateinit var PREVIEW: MutableMap<String, Preview>

    fun load() {
        // Load defaulted configs if they do not exist
        copyDefaults()

        // Load all files
        CONFIG = loadFile("config.json", SkiesCratesConfig())
        loadCrates()
        loadKeys()
        loadInventoryAnimations()
        loadPreviews()
    }

    private fun copyDefaults() {
        val classLoader = SkiesCrates::class.java.classLoader

        SkiesCrates.INSTANCE.configDir.mkdirs()

        attemptDefaultFileCopy(classLoader, "config.json")
        attemptDefaultDirectoryCopy(classLoader, "crates")
        attemptDefaultDirectoryCopy(classLoader, "keys")
        attemptDefaultDirectoryCopy(classLoader, "animations/inventory")
        attemptDefaultDirectoryCopy(classLoader, "animations/physical")
        attemptDefaultDirectoryCopy(classLoader, "previews")
    }

    private fun loadCrates() {
        CRATES = mutableMapOf()

        val dir = SkiesCrates.INSTANCE.configDir.resolve("crates")
        if (dir.exists() && dir.isDirectory) {
            val files = Files.walk(dir.toPath())
                .filter { path: Path -> Files.isRegularFile(path) }
                .map { it.toFile() }
                .collect(Collectors.toList())
            if (files != null) {
                SkiesCrates.LOGGER.info("Found ${files.size} crate files: ${files.map { it.name }}")
                val enabledFiles = mutableListOf<String>()
                for (file in files) {
                    val fileName = file.name
                    if (file.isFile && fileName.contains(".json")) {
                        val id = fileName.substring(0, fileName.lastIndexOf(".json"))
                        val jsonReader = JsonReader(InputStreamReader(FileInputStream(file), Charsets.UTF_8))
                        try {
                            val config = SkiesCrates.INSTANCE.gsonPretty.fromJson(JsonParser.parseReader(jsonReader), Crate::class.java)
                            if (config.enabled) {
                                config.id = id
                                CRATES[id] = config
                                enabledFiles.add(fileName)
                            } else {
                                Utils.printError("Crate $fileName is disabled, skipping...")
                            }
                        } catch (ex: Exception) {
                            Utils.printError("Error while trying to parse the crate $fileName!")
                            ex.printStackTrace()
                        }
                    } else {
                        Utils.printError("File $fileName is either not a file or is not a .json file!")
                    }
                }
                Utils.printInfo("Successfully read and loaded the following enabled crate files: $enabledFiles")
            }
        } else {
            Utils.printError("The 'crate' directory either does not exist or is not a directory!")
        }
    }

    private fun loadKeys() {
        KEYS = mutableMapOf()

        val dir = SkiesCrates.INSTANCE.configDir.resolve("keys")
        if (dir.exists() && dir.isDirectory) {
            val files = Files.walk(dir.toPath())
                .filter { path: Path -> Files.isRegularFile(path) }
                .map { it.toFile() }
                .collect(Collectors.toList())
            if (files != null) {
                SkiesCrates.LOGGER.info("Found ${files.size} key files: ${files.map { it.name }}")
                val enabledFiles = mutableListOf<String>()
                for (file in files) {
                    val fileName = file.name
                    if (file.isFile && fileName.contains(".json")) {
                        val id = fileName.substring(0, fileName.lastIndexOf(".json"))
                        val jsonReader = JsonReader(InputStreamReader(FileInputStream(file), Charsets.UTF_8))
                        try {
                            val config = SkiesCrates.INSTANCE.gsonPretty.fromJson(JsonParser.parseReader(jsonReader), Key::class.java)
                            if (config.enabled) {
                                config.id = id
                                KEYS[id] = config
                                enabledFiles.add(fileName)
                            } else {
                                Utils.printError("Key $fileName is disabled, skipping...")
                            }
                        } catch (ex: Exception) {
                            Utils.printError("Error while trying to parse the key $fileName!")
                            ex.printStackTrace()
                        }
                    } else {
                        Utils.printError("File $fileName is either not a file or is not a .json file!")
                    }
                }
                Utils.printInfo("Successfully read and loaded the following enabled key files: $enabledFiles")
            }
        } else {
            Utils.printError("The 'keys' directory either does not exist or is not a directory!")
        }
    }

    private fun loadInventoryAnimations() {
        ANIMATIONS_INVENTORY = mutableMapOf()

        val dir = SkiesCrates.INSTANCE.configDir.resolve("animations/inventory")
        if (dir.exists() && dir.isDirectory) {
            val files = Files.walk(dir.toPath())
                .filter { path: Path -> Files.isRegularFile(path) }
                .map { it.toFile() }
                .collect(Collectors.toList())
            if (files != null) {
                SkiesCrates.LOGGER.info("Found ${files.size} key files: ${files.map { it.name }}")
                val enabledFiles = mutableListOf<String>()
                for (file in files) {
                    val fileName = file.name
                    if (file.isFile && fileName.contains(".json")) {
                        val id = fileName.substring(0, fileName.lastIndexOf(".json"))
                        val jsonReader = JsonReader(InputStreamReader(FileInputStream(file), Charsets.UTF_8))
                        try {
                            val config = SkiesCrates.INSTANCE.gsonPretty.fromJson(JsonParser.parseReader(jsonReader), InventoryAnimation::class.java)
                            ANIMATIONS_INVENTORY[id] = config
                            enabledFiles.add(fileName)
                        } catch (ex: Exception) {
                            Utils.printError("Error while trying to parse the inventory animation $fileName!")
                            ex.printStackTrace()
                        }
                    } else {
                        Utils.printError("File $fileName is either not a file or is not a .json file!")
                    }
                }
                Utils.printInfo("Successfully read and loaded the following enabled inventory animation files: $enabledFiles")
            }
        } else {
            Utils.printError("The 'animations/inventory' directory either does not exist or is not a directory!")
        }
    }

    private fun loadPreviews() {
        PREVIEW = mutableMapOf()

        val dir = SkiesCrates.INSTANCE.configDir.resolve("previews")
        if (dir.exists() && dir.isDirectory) {
            val files = Files.walk(dir.toPath())
                .filter { path: Path -> Files.isRegularFile(path) }
                .map { it.toFile() }
                .collect(Collectors.toList())
            if (files != null) {
                SkiesCrates.LOGGER.info("Found ${files.size} preview files: ${files.map { it.name }}")
                val enabledFiles = mutableListOf<String>()
                for (file in files) {
                    val fileName = file.name
                    if (file.isFile && fileName.contains(".json")) {
                        val id = fileName.substring(0, fileName.lastIndexOf(".json"))
                        val jsonReader = JsonReader(InputStreamReader(FileInputStream(file), Charsets.UTF_8))
                        try {
                            val config = SkiesCrates.INSTANCE.gsonPretty.fromJson(JsonParser.parseReader(jsonReader), Preview::class.java)
                            PREVIEW[id] = config
                            enabledFiles.add(fileName)
                        } catch (ex: Exception) {
                            Utils.printError("Error while trying to parse the preview $fileName!")
                            ex.printStackTrace()
                        }
                    } else {
                        Utils.printError("File $fileName is either not a file or is not a .json file!")
                    }
                }
                Utils.printInfo("Successfully read and loaded the following enabled preview files: $enabledFiles")
            }
        } else {
            Utils.printError("The 'previews' directory either does not exist or is not a directory!")
        }
    }

    fun <T : Any> loadFile(filename: String, default: T, create: Boolean = false): T {
        val file = File(SkiesCrates.INSTANCE.configDir, filename)
        var value: T = default
        try {
            Files.createDirectories(SkiesCrates.INSTANCE.configDir.toPath())
            if (file.exists()) {
                FileReader(file).use { reader ->
                    val jsonReader = JsonReader(reader)
                    value = SkiesCrates.INSTANCE.gsonPretty.fromJson(jsonReader, default::class.java)
                }
            } else if (create) {
                Files.createFile(file.toPath())
                FileWriter(file).use { fileWriter ->
                    fileWriter.write(SkiesCrates.INSTANCE.gsonPretty.toJson(default))
                    fileWriter.flush()
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return value
    }

    fun <T> saveFile(filename: String, `object`: T): Boolean {
        val dir = SkiesCrates.INSTANCE.configDir
        val file = File(dir, filename)
        try {
            FileWriter(file).use { fileWriter ->
                fileWriter.write(SkiesCrates.INSTANCE.gsonPretty.toJson(`object`))
                fileWriter.flush()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
        return true
    }

    private fun attemptDefaultFileCopy(classLoader: ClassLoader, fileName: String) {
        val file = SkiesCrates.INSTANCE.configDir.resolve(fileName)
        if (!file.exists()) {
            try {
                val stream = classLoader.getResourceAsStream("${assetPackage}/$fileName")
                    ?: throw NullPointerException("File not found $fileName")

                Files.copy(stream, file.toPath(), StandardCopyOption.REPLACE_EXISTING)
            } catch (e: Exception) {
                Utils.printError("Failed to copy the default file '$fileName': $e")
            }
        }
    }

    private fun attemptDefaultDirectoryCopy(classLoader: ClassLoader, directoryName: String) {
        val directory = SkiesCrates.INSTANCE.configDir.resolve(directoryName)
        if (!directory.exists()) {
            directory.mkdirs()
            try {
                val sourceUrl = classLoader.getResource("${assetPackage}/$directoryName")
                    ?: throw NullPointerException("Directory not found $directoryName")
                val sourcePath = Paths.get(sourceUrl.toURI())

                Files.walk(sourcePath).use { stream ->
                    stream.forEach { sourceFile ->
                        val destinationFile = directory.resolve(sourcePath.relativize(sourceFile).toString())
                        if (Files.isDirectory(sourceFile)) {
                            // Create subdirectories in the destination
                            destinationFile.mkdirs()
                        } else {
                            // Copy files to the destination
                            Files.copy(sourceFile, destinationFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
                        }
                    }
                }
            } catch (e: Exception) {
                Utils.printError("Failed to copy the default directory '$directoryName': " + e.message)
            }
        }
    }
}
