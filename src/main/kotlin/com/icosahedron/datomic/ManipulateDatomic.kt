package com.icosahedron.datomic

import com.icosahedron.datomic.dataset.Dataset
import datomic.Peer
import datomic.Connection

object ManipulateDatomic {
    fun create(uri: String) {
        Peer.createDatabase(uri)
        println("\nDatabase created: $uri")
    }

    fun connect(uri: String): Connection {
        val connection = Peer.connect(uri)
        println("\nConnected to database at $uri: $connection")
        return connection
    }

    fun addSchema(connection: Connection, dataset: Dataset) {
        val schema = dataset.schema()
        connection.transact(schema).get()
        println("\nSchema created:\n${schema.joinToString("\n")}")
    }

    fun addData(connection: Connection, dataset: Dataset) {
        val data = dataset.data()
        connection.transact(data).get()
        println("\nData added:\n${data.joinToString("\n")}")
    }

    fun query(connection: Connection, query: String): Collection<List<Any>> {
        val results = Peer.q(query, connection.db())
        println("\nExecuted query:\n$query")
        return results
    }
}