package com.icosahedron.datomic.pro

import datomic.Connection
import datomic.Peer

fun main() {
    val databaseUri = "datomic:dev://localhost:4334/hello"

    val databaseConnection: Connection = Peer.connect(databaseUri)
    println("\nConnected to database at $databaseUri\n" +
            "connection details: $databaseConnection")

    databaseConnection.release()
}