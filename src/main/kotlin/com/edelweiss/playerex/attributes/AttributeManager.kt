package com.edelweiss.playerex.attributes

import com.edelweiss.playerex.PlayerEXDirectorsCut
import com.edelweiss.playerex.attributes.io.EntityAttributeData
import com.edelweiss.playerex.attributes.io.EntityTypeData
import com.edelweiss.playerex.attributes.json.*
import com.edelweiss.playerex.attributes.mutable.MutableEntityAttribute
import com.edelweiss.playerex.attributes.mutable.PEXMutableRegistry
import com.edelweiss.playerex.attributes.tags.AttributeTags
import com.edelweiss.playerex.attributes.utils.NbtIO
import com.edelweiss.playerex.attributes.utils.merge
import com.mojang.logging.LogUtils
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import net.fabricmc.fabric.api.resource.SimpleResourceReloadListener
import net.minecraft.entity.EntityType
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.attribute.AttributeContainer
import net.minecraft.entity.attribute.EntityAttribute
import net.minecraft.entity.mob.HostileEntity
import net.minecraft.entity.mob.MobEntity
import net.minecraft.entity.mob.PathAwareEntity
import net.minecraft.entity.passive.AnimalEntity
import net.minecraft.entity.passive.PassiveEntity
import net.minecraft.nbt.NbtCompound
import net.minecraft.registry.Registries
import net.minecraft.resource.Resource
import net.minecraft.resource.ResourceManager
import net.minecraft.util.Identifier
import net.minecraft.util.profiler.Profiler
import java.io.IOException
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executor


class AttributeManager(
    private var entityAttributeData: Map<Identifier, EntityAttributeData> = mapOf(),
    private var entityTypeData: Map<Identifier, EntityTypeData> = mapOf(),
    private val handler: AttributeContainerHandler = AttributeContainerHandler(),
    private var updateFlag: Int = 0
) : SimpleResourceReloadListener<AttributeManager.Record>, NbtIO {
    @OptIn(ExperimentalSerializationApi::class)
    companion object {
        const private val DIRECTORY = "attributes"
        const private val PATH_SUFFIX = ".json"

        private val LOGGER = LogUtils.getLogger()
        private val ID = Identifier(PlayerEXDirectorsCut.MODID, DIRECTORY)
        private val ENTITY_TYPE_INSTANCES = mutableMapOf<Identifier, Tuple<Int>>()

        private fun formatFunctions(functionsIn: Map<String, AttributeFunctionJSON>): Map<Identifier, AttributeFunctionJSON> {
            val functions = mutableMapOf<Identifier, AttributeFunctionJSON>()
            functionsIn.keys.forEach { key -> functions[Identifier(key)] = functionsIn[key] ?: return@forEach }
            return functions
        }

        /** Attempts to obtain an attribute based on its identifier, and if not present, will create one and return it. */
        private fun getOrCreate(identifier: Identifier, attributesIn: EntityAttribute): EntityAttribute {
            return Registries.ATTRIBUTE[identifier] ?: PEXMutableRegistry.register(Registries.ATTRIBUTE, identifier, attributesIn)
        }

        private fun loadOverrides(manager: ResourceManager, entityAttributeData: MutableMap<Identifier, EntityAttributeData>) {
            val cache = mutableMapOf<Identifier, AttributeOverrideJSON>()
            val location = DIRECTORY + "/overrides"

            manager.findResources(location) { id -> id.path.endsWith(PATH_SUFFIX)}.entries.forEach { entry ->
                val resource = entry.key
                val path = entry.key.path
                val identifier = Identifier(resource.namespace, path.substring(location.length + 1, path.length - PATH_SUFFIX.length))

                try {
                    val json = Json.decodeFromStream<AttributeOverrideJSON>(entry.value.inputStream)
                    cache.put(identifier, json) ?: return@forEach
                    throw IllegalStateException("Duplicate data-file ignored: ${identifier}")
                }
                catch (exception: Exception) {
                    handleResourceException(exception, identifier, resource)
                }
            }
            cache.forEach { (k, v) -> entityAttributeData[k] = EntityAttributeData(v) }
        }

        private fun loadFunctions(manager: ResourceManager, entityAttributeData: MutableMap<Identifier, EntityAttributeData>) {
            val cache = mutableMapOf<Identifier, FunctionsJSON>()

            manager.findResources(DIRECTORY) { id -> id.path.endsWith("functions.json")}.entries.forEach { entry ->
                val resource = entry.key
                val path = resource.path
                val identifier = Identifier(resource.namespace, path.substring(DIRECTORY.length + 1, path.length - PATH_SUFFIX.length))

                try {
                    val json = Json.decodeFromStream<FunctionsJSON>(entry.value.inputStream)
                    cache.put(identifier, json) ?: return@forEach
                    throw IllegalStateException("Duplicate data-file ignored: ${identifier}")
                } catch (exception: Exception) {
                    handleResourceException(exception, identifier, resource)
                }
            }

            val functions = mutableMapOf<String, MutableMap<String, AttributeFunctionJSON>>()
            cache.values.forEach { json -> json.values merge functions }

            functions.keys.forEach { key ->
                val identifier = Identifier(key)
                val data = entityAttributeData.getOrDefault(identifier, EntityAttributeData())
                data.putFunctions(formatFunctions(functions[key] ?: return@forEach))
                entityAttributeData[identifier] = data
            }
        }

        private fun loadProperties(manager: ResourceManager, entityAttributeData: MutableMap<Identifier, EntityAttributeData>) {
            val cache = mutableMapOf<Identifier, PropertiesJSON>()

            manager.findResources(DIRECTORY) { id -> id.path.endsWith("properties.json")}.entries.forEach { entry ->
                val resource = entry.key
                val path = resource.path
                val identifier = Identifier(resource.namespace, path.substring(DIRECTORY.length + 1, path.length - PATH_SUFFIX.length))

                try {
                    val json = Json.decodeFromStream<PropertiesJSON>(entry.value.inputStream)
                    cache.put(identifier, json) ?: return@forEach
                    throw IllegalStateException("Duplicate data-file ignored: ${identifier}")
                } catch (exception: Exception) {
                    handleResourceException(exception, identifier, resource)
                }
            }

            val properties = mutableMapOf<String, MutableMap<String, String>>()
            cache.values.forEach { json -> json.values merge properties }

            properties.keys.forEach { key ->
                val identifier = Identifier(key)
                val data = entityAttributeData.getOrDefault(identifier, EntityAttributeData())
                data.putProperties(properties[key] ?: return@forEach)
                entityAttributeData[identifier] = data
            }
        }

        private fun loadEntityTypes(manager: ResourceManager, entityTypeData: MutableMap<Identifier, EntityTypeData>) {
            val cache = mutableMapOf<Identifier, EntityTypesJSON>()

            manager.findResources(DIRECTORY) { id -> id.path.endsWith("entity_types.json")}.entries.forEach { entry ->
                val resource = entry.key
                val path = resource.path
                val identifier = Identifier(resource.namespace, path.substring(DIRECTORY.length + 1, path.length - PATH_SUFFIX.length))

                try {
                    val json = Json.decodeFromStream<EntityTypesJSON>(entry.value.inputStream)
                    cache.put(identifier, json) ?: return@forEach
                    throw IllegalStateException("Duplicate data-file ignored: ${identifier}")
                } catch (exception: Exception) {
                    handleResourceException(exception, identifier, resource)
                }
            }

            val entityTypes = mutableMapOf<String, MutableMap<String, Double>>()
            cache.values.forEach { json -> json.values merge entityTypes }

            entityTypes.keys.forEach { key ->
                val identifier = Identifier(key)
                val data = EntityTypeData((entityTypes[key] ?: return@forEach).entries.associate {
                    entry -> Identifier(entry.key) to entry.value }.toMutableMap()
                )
                entityTypeData[identifier] = data
            }
        }

        private inline fun <reified T> deserializeEntryToCache(startingPath: String, entry: MutableMap.MutableEntry<Identifier, Resource>, cache: MutableMap<Identifier, T>): MutableMap<Identifier, T> {
            val resource = entry.key
            val path = resource.path
            val identifier = Identifier(resource.namespace, path.substring(startingPath.length + 1, path.length - PATH_SUFFIX.length))

            try {
                val json = Json.decodeFromStream<T>(entry.value.inputStream)
                cache.put(identifier, json) ?: return cache
                throw IllegalStateException("Duplicate data-file ignored: ${identifier}")
            } catch (exception: Exception) {
                handleResourceException(exception, identifier, resource)
            }

            return cache
        }

        private fun handleResourceException(exception: Exception, identifier: Identifier, resource: Identifier) {
            when (exception) {
                is IOException,
                is IllegalArgumentException -> LOGGER.error("failed to parse file ${identifier} from ${resource}!\n${exception}")
                else -> LOGGER.error(exception.toString())
            }
        }

        init {
            ENTITY_TYPE_INSTANCES[Identifier(PlayerEXDirectorsCut.MODID, AttributesAPI.ENTITY_INSTANCE_LIVING_ENTITY)] =    Tuple(LivingEntity::class.java, 0)
            ENTITY_TYPE_INSTANCES[Identifier(PlayerEXDirectorsCut.MODID, AttributesAPI.ENTITY_INSTANCE_MOB_ENTITY)] =       Tuple(MobEntity::class.java, 1)
            ENTITY_TYPE_INSTANCES[Identifier(PlayerEXDirectorsCut.MODID,AttributesAPI.ENTITY_INSTANCE_PATH_AWARE_ENTITY)] = Tuple(PathAwareEntity::class.java, 2)
            ENTITY_TYPE_INSTANCES[Identifier(PlayerEXDirectorsCut.MODID, AttributesAPI.ENTITY_INSTANCE_HOSTILE_ENTITY)] =   Tuple(HostileEntity::class.java, 3)
            ENTITY_TYPE_INSTANCES[Identifier(PlayerEXDirectorsCut.MODID, AttributesAPI.ENTITY_INSTANCE_PASSIVE_ENTITY)] =   Tuple(PassiveEntity::class.java, 4)
            ENTITY_TYPE_INSTANCES[Identifier(PlayerEXDirectorsCut.MODID, AttributesAPI.ENTITY_INSTANCE_ANIMAL_ENTITY)] =    Tuple(AnimalEntity::class.java, 5)
        }
    }


    @JvmRecord()
    data class Record(val attributeData: Map<Identifier, EntityAttributeData>, val typeData: Map<Identifier, EntityTypeData>)

    @JvmRecord()
    data class Tuple<T>(val entity: Class<out LivingEntity>, val value: T)

    override fun readFromNbt(tag: NbtCompound) {
        val entityAttributeNbt = NbtCompound()
        val entityTypeNbt = NbtCompound()

        this.entityAttributeData.forEach { (k, v) ->
            val entry = NbtCompound()
            v.writeToNbt(entry)
            entityAttributeNbt.put(k.toString(), entry)
        }

        this.entityTypeData.forEach { (k, v) ->
            val entry = NbtCompound()
            v.writeToNbt(entry)
            entityTypeNbt.put(k.toString(), entry)
        }

        tag.put(AttributeTags.ATTRIBUTE, entityAttributeNbt)
        tag.put(AttributeTags.ENTITY_TYPES, entityTypeNbt)
        tag.putInt(AttributeTags.UPDATE_FLAG, this.updateFlag)
    }

    override fun writeToNbt(tag: NbtCompound) {
        if (tag.contains(AttributeTags.ATTRIBUTE)) {
            val compound = tag.getCompound(AttributeTags.ATTRIBUTE)
            this.entityAttributeData = compound.keys.associateTo(mutableMapOf()) { key ->
                val entry = compound.getCompound(key)
                val data = EntityAttributeData()
                data.readFromNbt(entry)
                Identifier(key) to data
            }
        }
        if (tag.contains(AttributeTags.ENTITY_TYPES)) {
            val compound = tag.getCompound(AttributeTags.ENTITY_TYPES)
            this.entityTypeData = compound.keys.associateTo(mutableMapOf()) { key ->
                val entry = compound.getCompound(key)
                val data = EntityTypeData()
                data.readFromNbt(entry)
                Identifier(key) to data
            }
        }
        this.updateFlag = tag.getInt(AttributeTags.UPDATE_FLAG)
    }

    /** Shorthand to create and return a compound based on the information from this manager. **/
    fun toNBT(): NbtCompound { val compound = NbtCompound(); this.writeToNbt(compound); return compound }

    fun nextUpdateFlag() { this.updateFlag++ }

    fun getUpdateFlag() = this.updateFlag

    fun getContainer(entityType: EntityType<out LivingEntity>, livingEntity: LivingEntity): AttributeContainer = this.handler.getContainer(entityType, livingEntity)

    fun apply() {
        PEXMutableRegistry.unregister(Registries.ATTRIBUTE)

        Registries.ATTRIBUTE.ids.forEach { id -> (Registries.ATTRIBUTE[id] as? MutableEntityAttribute ?: return@forEach).clear() }

        this.entityAttributeData.keys.forEach { id ->
            this.entityAttributeData[id]?.override(id, AttributeManager::getOrCreate)

            // todo: ~ From here, the original source used to have two decoupled iterations... Unsure if that was needed, but will leave a marker here.
            // ~ In the case something goes unexpectedly wrong, investigate, and move out everything below this scope into its own iteration ~ Jecka
            this.entityAttributeData[id]?.copy(Registries.ATTRIBUTE[id] ?: return@forEach)
        }

        this.handler.buildContainers(this.entityTypeData, ENTITY_TYPE_INSTANCES)

// todo:        AttributesReloadedEvent.EVENT
    }

    override fun load(manager: ResourceManager, profiler: Profiler, executor: Executor): CompletableFuture<Record> {
        return CompletableFuture.supplyAsync({ ->
            val attributeData = mutableMapOf<Identifier, EntityAttributeData>()
            loadOverrides(manager, attributeData)
            loadFunctions(manager, attributeData)
            loadProperties(manager, attributeData)

            val typeData = mutableMapOf<Identifier, EntityTypeData>()
            loadEntityTypes(manager, typeData)

            return@supplyAsync Record(attributeData, typeData)
        }, executor)
    }

    override fun apply(
        data: Record,
        manager: ResourceManager?,
        profiler: Profiler?,
        executor: Executor?
    ): CompletableFuture<Void> {
        return CompletableFuture.runAsync({ ->
            val attributeData = mutableMapOf<Identifier, EntityAttributeData>()
            data.attributeData.forEach(attributeData::put)
            this.entityAttributeData = attributeData

            val typeData = mutableMapOf<Identifier, EntityTypeData>()
            data.typeData.forEach(typeData::put)
            this.entityTypeData = typeData

            this.apply()
        }, executor)
    }

    override fun getFabricId(): Identifier = ID
}