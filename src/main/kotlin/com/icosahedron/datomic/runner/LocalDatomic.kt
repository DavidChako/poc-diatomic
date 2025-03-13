package com.icosahedron.datomic.runner

import com.icosahedron.datomic.dataset.MovieDataset
import datomic.Peer

fun main() {
    val uri = "datomic:mem:/example-db"
    Peer.createDatabase(uri)

    val dataset = MovieDataset()
    val connection = Peer.connect(uri)
    connection.transact(dataset.schema()).get()
    connection.transact(dataset.data()).get()

    val query = dataset.sampleQuery()
    val results = Peer.q(query, connection.db())
    println("\nQuery results:\n${results.joinToString("\n")}")

    connection.release()
}