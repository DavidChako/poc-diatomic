package com.icosahedron.datomic.runner.remote

import com.icosahedron.datomic.dataset.Dataset
import com.icosahedron.datomic.dataset.MovieDataset
import datomic.Connection
import datomic.Peer

// assumes running transactor at uri host path
fun main() {
    val uri = "datomic:dev://localhost:4334/movie-db"
    val dataset = MovieDataset()

    fun initializeDatabase(uri: String, dataset: Dataset): Connection {
        val connection = Peer.connect(uri)
        connection.transact(dataset.schema()).get()
        connection.transact(dataset.data()).get()
        return connection
    }

    val connection = if (Peer.createDatabase(uri)) {
        initializeDatabase(uri, dataset)
    } else {
        Peer.connect(uri)
    }

    val query = dataset.sampleQuery()
    val results = Peer.q(query, connection.db())
    println("\nQuery results:\n${results.joinToString("\n")}")

    connection.release()
}
