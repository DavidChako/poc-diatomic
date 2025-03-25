package com.icosahedron.datomic.dataset

data class Schema constructor(val entity: String, val fields: Map<String,Field>) {
    fun toDatomic(): List<*> = fields.map { (name, field) -> field.toDatomic(entity, name) }

    fun builder() = Builder(entity, fields)

    class Builder(val entity: String, nominalFields: Map<String,Field>) {
        private val fields = nominalFields.toMutableMap()

        constructor(entity: String): this(entity, mapOf())

        fun putField(name: String, field: Field): Builder {
            fields.put(name, field)
            return this
        }

        fun removeField(name: String): Builder {
            fields.remove(name)
            return this
        }

        fun build() = Schema(entity, fields)
    }
}
