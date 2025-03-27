package com.icosahedron.datomic

import com.icosahedron.datomic.entity.Movie
import com.icosahedron.datomic.schema.Schema
import datomic.Peer
import kotlin.system.exitProcess

/* Prerequisites:
A. Obtain datomic transactor executable if necessary:
    1. % mkdir ~/datomic
    2. % cd ~/datomic
    3. % curl https://datomic-pro-downloads.s3.amazonaws.com/1.0.7277/datomic-pro-1.0.7277.zip -O
    4. % unzip datomic-pro-1.0.7277.zip
B. Run transactor if not already running:
    1. % cd ~/datomic/datomic-pro-1.0.7277
    2. % ./bin/transactor config/samples/dev-transactor-template.properties
 */

fun main() {
    val uri = "datomic:dev://localhost:4334/example-db"

    val connection = Peer.connect(uri)
    println("Established connection to uri $uri: $connection")

    val schema = Schema.fromDataClass("movie", Movie::class)
    println("Generated $schema")

    val datomicSchema = schema.render()
    connection.transact(datomicSchema).get()
    println("Applied schema:\n${datomicSchema.joinToString("\n")}")

    val data = listOf(
        Movie("The Matrix", 1999),
        Movie("Inception", 2010)
    )
    println("Generated data:\n${data.joinToString("\n")}")

    val datomicData = schema.renderData(data)
    connection.transact(datomicData).get()
    println("Included data:\n${datomicData.joinToString("\n")}")

    val query = "" +
            "[:find ?title ?year\n" +
            " :where\n" +
            " [?movie :movie/title ?title]\n" +
            " [?movie :movie/releaseYear ?year]\n" +
            "]"
    println("Executing query:\n$query")

    val results = Peer.q(query, connection!!.db())
    println("Query results:\n${results.joinToString("\n")}")

    exitProcess(0)
}