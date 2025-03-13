package com.icosahedron.datomic.app

import com.icosahedron.datomic.ManipulateDatomic
import com.icosahedron.datomic.dataset.MovieDataset

fun main() {
    val uri = "datomic:dev://localhost:4334/movie-db"
    ManipulateDatomic.create(uri)

    val connection = ManipulateDatomic.connect(uri)
    val dataset = MovieDataset()
    ManipulateDatomic.addSchema(connection, dataset)
    ManipulateDatomic.addData(connection, dataset)

    connection.release()
}