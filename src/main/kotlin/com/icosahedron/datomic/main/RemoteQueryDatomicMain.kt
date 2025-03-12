package com.icosahedron.datomic.main

import com.icosahedron.datomic.ManipulateDatomic
import com.icosahedron.datomic.dataset.MovieDataset

fun main() {
    val uri = "datomic:dev://localhost:4334/movie-db"
    val connection = ManipulateDatomic.connect(uri)
    val dataset = MovieDataset()
    val query = dataset.titleAndYearQuery()
    val results = ManipulateDatomic.query(connection, query)
    println("\nQuery results:\n${results.joinToString("\n")}")
    connection.release()
}