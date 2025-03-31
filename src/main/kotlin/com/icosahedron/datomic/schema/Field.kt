package com.icosahedron.datomic.schema

import clojure.lang.Keyword
import datomic.Util
import kotlin.reflect.KType
import kotlin.reflect.typeOf

data class Field (
    val identifier: String,
    val type: Type,
    val cardinality: Cardinality,
    val description: String
) {
    private val keyword = Keyword.intern(identifier.trimStart(':'))

    private var extractor: Extractor? = null

    fun withExtractor(extractor: Extractor) = this.also { this.extractor = extractor }

    fun render(entity: String, name: String): Map<*,*> = Util.map(
        ":db/ident", ":$entity/$name",
        ":db/valueType", ":db.type/${type.valueType}",
        ":db/cardinality", ":db.cardinality/${cardinality.cardinality}",
        ":db/doc", description
    )

    fun extractValue(item: Any) = extractor?.extract(item) ?: type.defaultValue()

    fun interface Extractor {
        fun extract(item: Any): Any?
    }

    enum class Type(val valueType: String) {
        STRING("string"),
        LONG("long");

        // TODO: replace use of defaultValue with retractions
        fun defaultValue() = when (this) {
            STRING -> ""
            LONG -> -1L
        }

        companion object {
            fun infer(type: KType) = when (type) {
                typeOf<String>() -> STRING
                typeOf<Int>() -> LONG
                else -> throw IllegalArgumentException("Unsupported type $type")
            }
        }
    }

    enum class Cardinality(val cardinality: String) {
        ONE("one"),
        MANY("many");
    }
}