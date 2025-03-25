package com.icosahedron.datomic.dataset

interface Dataset {
    fun schema(): Schema
    fun data(): List<Any?>
    fun sampleQuery(): String


}
