package com.icosahedron.datomic.dataset

interface Dataset {
    fun schema(): List<*>
    fun data(): List<*>
}
