package com.icosahedron.datomic.main

import com.icosahedron.datomic.ManipulateDatomic
import com.icosahedron.datomic.dataset.MovieDataset

fun main() {
    val uri = "datomic:mem:/example-db"
    ManipulateDatomic.create(uri)

    val connection = ManipulateDatomic.connect(uri)
    val dataset = MovieDataset()
    ManipulateDatomic.addSchema(connection, dataset)
    ManipulateDatomic.addData(connection, dataset)

    val query = dataset.titleAndYearQuery()
    val results = ManipulateDatomic.query(connection, query)
    println("\nQuery results:\n${results.joinToString("\n")}")

    connection.release()
}