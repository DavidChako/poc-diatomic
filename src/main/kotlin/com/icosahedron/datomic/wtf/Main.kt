package com.icosahedron.datomic.wtf

fun main() {
    val dataClass = Book::class
    val schema = Schema.fromDataClass(dataClass)
    println("Schema: $schema")

//    val again = Schema.fromDataClass(dataClass)
//    println("Again: $again")
}
