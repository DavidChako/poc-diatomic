package com.icosahedron.datomic.wtf

import clojure.lang.Keyword
import kotlin.collections.map
import kotlin.collections.toMutableMap
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor
import org.slf4j.LoggerFactory

data class Schema(val entity: String, val fields: Map<String,Field>) {
//    fun toDatomic(): List<*> = fields.map { (name, field) ->
//        field.toDatomic(entity, name)
//    }

    fun toDatomic(): List<*> {
        LOG.info("Rendering schema ...")

        val result = fields.map { (name, field) ->
            LOG.info("Rendering field {}={}", name, field)
            val datomicField = field.toDatomic(entity, name)
            LOG.info("Rendered datomic field {}", datomicField.entries.joinToString("\n"))
            datomicField
        }

        LOG.info("Done rendering schema ...")
        return result
    }

    fun keyword(name: String) = fields[name]?.keyword ?: ""


    //fun idQuery(id: String) = "[:find (pull ?eid [*]) :where [?eid :book/id \"$id\"]]"

    fun builder() = Builder(entity, fields)

    class Builder(val entity: String, nominalFields: Map<String,Field>) {
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
        private val LOG = LoggerFactory.getLogger(Schema.javaClass)

//        private inline fun <reified T : Any> isDataClass() = T::class.isData

        fun <T:Any> fromDataClass(dataClass: KClass<T>): Schema {
            if (!dataClass.isData) throw IllegalArgumentException("Not a data class: $dataClass")

            LOG.info("Generating schema for {}", dataClass)
            val builder = Builder(dataClass.simpleName!!)
            dataClass.primaryConstructor?.parameters?.forEach { field ->
                builder.addField(field.name!!, Field.inferType(field.type.toString()), Field.Cardinality.ONE, "fixme")
            }

            val schema = builder.build()
            LOG.info("Done generating schema: {}", schema)

            LOG.info("Datomic schema of {}:\n{}", dataClass, schema.toDatomic().joinToString("\n"))
            return schema
        }
    }
}


