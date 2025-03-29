package com.icosahedron.datomic.entity

import datomic.Util
import kotlin.reflect.KClass
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.primaryConstructor

interface Entity {
    fun render(entity: String): Map<*,*> {
        return renderer(entity, this::class).render(this)
    }

    private class Renderer(val entity: String, val dataClass: KClass<out Entity>) {
        init {
            require(dataClass.isData) { "Not a data class: $dataClass" }
        }

        private val keyValues = generateKeyValueAdders()

        fun render(item: Entity): Map<*,*> {
            val keyValueList = mutableListOf<Any>()

            keyValues.forEach { (key, value) ->
                keyValueList.add(key)
                keyValueList.add(value(item))
            }

            return Util.map(*keyValueList.toTypedArray())
        }

        private fun generateKeyValueAdders(): Map<String, (item: Any?) -> Any> {
            if (!dataClass.isData) throw IllegalArgumentException("Not a data class object: $this")

            val fields = dataClass.primaryConstructor?.parameters!!.mapNotNull { it.name }

            val adders = mutableMapOf<String, (item: Any?) -> Any>()

            dataClass.declaredMemberProperties
                .filter { fields.contains(it.name) }
                .forEach { property ->
                    adders.put(":$entity/${property.name}", { item: Any? -> property.getter.call(item)!! })
                }

            return adders
        }
    }

    companion object {
        private val renderers = mutableMapOf<String, Renderer>()
        private fun renderer(entity: String, dataClass: KClass<out Entity>) = renderers.computeIfAbsent(entity) {  Renderer(entity, dataClass) }
    }
}