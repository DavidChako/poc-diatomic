package com.icosahedron.datomic.dataset

import datomic.Util

data class Field(
    val type: Type,
    val cardinality: Cardinality,
    val description: String
) {
    fun toDatomic(entity: String, name:String): Map<*,*> = Util.map(
        ":db/ident", ":$entity/$name",
        ":db/valueType", ":db.type/${type.value}",
        ":db/cardinality", ":db.cardinality/${cardinality.value}",
        ":db/doc", description
    )

    enum class Type(val value: String) {
        STRING("string"),
        LONG("long")
    }

    enum class Cardinality(val value: String) {
        ONE("one"),
        MANY("many")
    }
}