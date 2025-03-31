package com.icosahedron.datomic.schema

import kotlin.reflect.KClass
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.primaryConstructor

class Schema(val entity: String, val fields: Map<String, Field>) {
    override fun toString() = "Schema { entity=$entity, fields=\n${fields.entries.joinToString("\n")}\n}"

    fun render(): List<*> = fields.map { (name, field) -> field.render(entity, name) }

    fun <T:Any> renderData(data: List<T>): List<*> = data.map { item ->
//    fun <T:Any> renderData(data: List<T>): List<Map<Any,Any>> = data.map { item ->
        fields.values.associate { field ->
            field.identifier to field.extractValue(item)
        }
    }

    fun renderPull() = "" +
            "[:find (pull ?e [*])" +
            " :where\n" +
            " [?e :db/ident ?ident]\n" +
            " [_ :db.install/attribute ?e]\n" +
            " [(.toString ?ident) ?val]\n" +
            " [(.startsWith ?val \":$entity\")]" +
            "]"

    fun renderQuery() = "" +
            "[:find ?eid, ?ident, ?type, ?card, ?doc\n" +
            " :where\n" +
            " [?eid :db/ident ?ident]\n" +
            " [?eid :db/valueType ?eType]\n" +
            " [?eType :db/ident ?type]\n" +
            " [?eid :db/cardinality ?eCard]\n" +
            " [?eCard :db/ident ?card]\n" +
            " [?eid :db/doc ?doc]\n" +
            " [_ :db.install/attribute ?eid]\n" +
            " [(.toString ?ident) ?val]\n" +
            " [(.startsWith ?val \":$entity\")]" +
            "]"

    fun renderDataPull(field: String) = "" +
            "[:find (pull ?eid [*])\n" +
            " :where [?eid :$entity/$field]\n" +
            "]"

    fun renderDataPull(field: String, value: String) = "" +
            "[:find (pull ?eid [*])\n" +
            " :where [?eid :$entity/$field \"$value\"]\n" +
            "]"


    fun builder() = Builder(entity, fields)

    class Builder(val entity: String, nominalFields: Map<String, Field>) {
        constructor(entity: String): this(entity, mapOf())

        private val fields = nominalFields.toMutableMap()

        fun addField(name: String, field: Field) = this.also { fields[name] = field }
//        fun removeField(name: String) = this.also { fields.remove(name) }

        fun addField(name: String, type: Field.Type, cardinality: Field.Cardinality, description: String) =
            addField(name, Field("$entity/$name", type, cardinality, description))

        fun build() = Schema(entity, fields)
    }

    companion object {
        @JvmStatic
        fun <T : Any> fromDataClass(javaClass: Class<T>) = fromDataClass(javaClass.kotlin)

        fun <T : Any> fromDataClass(dataClass: KClass<T>): Schema {
            if (!dataClass.isData) throw IllegalArgumentException("Not a data class: $dataClass")
            val properties = dataClass.declaredMemberProperties
            val entity = dataClass.simpleName!!
            val builder = Builder(entity)

            dataClass.primaryConstructor?.parameters?.forEach { parameter ->
                val property = properties.find { it.name == parameter.name } ?: return@forEach
                val name = parameter.name!!
                val type = Field.Type.infer(parameter.type)
                val cardinality = Field.Cardinality.ONE
                val description = "fixme"
                val field = Field(":$entity/$name", type, cardinality, description)
                builder.addField(name, field.withExtractor { property.getter.call(it) })
            }

            return builder.build()
        }
    }
}