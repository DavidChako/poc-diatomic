package com.icosahedron.datomic.schema

import com.icosahedron.datomic.entity.Entity
import kotlin.collections.component1
import kotlin.collections.component2

object Render {
    @JvmStatic
    fun schema(entity: String, fields: Map<String, Field>): List<*> = fields.map { (name, field) -> field.toDatomic(entity, name) }

    @JvmStatic
//    fun <T: Entity> data(entity: String, data: List<T>): List<*> = data.map { item(entity, it) }
    fun <T: Entity> data(entity: String, data: List<T>): List<*> = data.map { it.render(entity) }

    @JvmStatic
    fun schemaPull(entity: String) = "" +
            "[:find (pull ?e [*])" +
            " :where\n" +
            " [?e :db/ident ?ident]\n" +
            " [_ :db.install/attribute ?e]\n" +
            " [(.toString ?ident) ?val]\n" +
            " [(.startsWith ?val \":$entity\")]" +
            "]"

    @JvmStatic
    fun schemaQuery(entity: String) = "" +
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

    @JvmStatic
    fun dataPull(entity: String, field: String, value: String) = "" +
            "[:find (pull ?eid [*])\n" +
            " :where [?eid :$entity/$field \"$value\"]\n" +
            "]"

    @JvmStatic
    fun dataPull(entity: String, field: String) = "" +
            "[:find (pull ?eid [*])\n" +
            " :where [?eid :$entity/$field]\n" +
            "]"
}
