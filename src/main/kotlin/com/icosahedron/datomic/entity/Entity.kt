package com.icosahedron.datomic.entity

interface Entity {
    fun render(entity: String): Map<*,*>
}