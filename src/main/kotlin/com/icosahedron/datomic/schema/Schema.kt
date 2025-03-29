package com.icosahedron.datomic.schema

import clojure.lang.Keyword
import com.icosahedron.datomic.entity.Entity
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor

class Schema constructor(val entity: String, val fields: Map<String, Field>) {
    override fun toString() = "Schema { entity=$entity, fields=\n${fields.entries.joinToString("\n")}\n}"

    fun render(): List<*> = Render.schema(entity, fields)
    fun renderPull() = Render.schemaPull(entity)
    fun renderQuery() = Render.schemaQuery(entity)

    fun <T: Entity> renderData(data: List<T>) = Render.data(entity, data)
    fun renderDataPull(field: String, value: String) = Render.dataPull(entity, field, value)
    fun renderDataPull(field: String) = Render.dataPull(entity, field)

//    fun keyword(field: String) = fields[field]?.keyword ?: throw IllegalArgumentException("Field not found: $field")
//fun keyword(name: String) = fields[name]?.keyword ?: ""


    fun builder() = Builder(entity, fields)

    class Builder(private val entity: String, nominalFields: Map<String, Field>) {
        constructor(entity: String): this(entity, mapOf())

        private val fields = nominalFields.toMutableMap()

        fun addField(name: String, type: Field.Type, cardinality: Field.Cardinality, description: String): Builder {
            val keyword = Keyword.intern("$entity/$name")
            val field = Field(keyword, type, cardinality, description)
            fields.put(name, field)
            return this
        }

        fun removeField(name: String): Builder {
            fields.remove(name)
            return this
        }

        fun build() = Schema(entity, fields)
    }

    companion object {
        @JvmStatic
        fun <T : Any> fromDataClass(entity: String, javaClass: Class<T>) = fromDataClass(entity, javaClass.kotlin)

        @JvmStatic
        fun <T : Any> fromDataClass(javaClass: Class<T>) = fromDataClass(javaClass.simpleName, javaClass.kotlin)

        fun <T : Any> fromDataClass(dataClass: KClass<T>) = fromDataClass(dataClass.simpleName!!, dataClass)

        fun <T : Any> fromDataClass(entity: String, dataClass: KClass<T>): Schema {
            if (!dataClass.isData) throw IllegalArgumentException("Not a data class: $dataClass")

            val builder = Builder(entity)

            dataClass.primaryConstructor?.parameters?.forEach { field ->
                builder.addField(field.name!!, Field.inferType(field.type.toString()), Field.Cardinality.ONE, "fixme")
            }

            return builder.build()
        }
    }
}