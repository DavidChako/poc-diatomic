package com.icosahedron.datomic.pro

import datomic.Connection
import datomic.Peer
import datomic.Util

fun main() {
    val databaseUri = "datomic:dev://localhost:4334/hello"

    val databaseConnection: Connection = Peer.connect(databaseUri)
    println("\nConnected to database at $databaseUri\n" +
            "connection details: $databaseConnection")


    val query = """
        [:find ?title ?year
         :where
         [?movie :movie/title ?title]
         [?movie :movie/release-year ?year]
        ]
    """.trimIndent()

    val results = Peer.q(query, databaseConnection.db())
    println("\nExecuted query:\n$query")

    println("\nQuery results:\n${results.joinToString("\n")}")

    databaseConnection.release()
}