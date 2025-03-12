import datomic.Connection
import datomic.Peer
import datomic.Util

fun main() {
    val databaseUri = "datomic:mem://example-db"
    Peer.createDatabase(databaseUri)
    println("\nDatabase created: $databaseUri")

    val databaseConnection: Connection = Peer.connect(databaseUri)
    println("\nDatabase connection established: $databaseConnection")

    val schema = Util.list(
        Util.map(
            ":db/cardinality", ":db.cardinality/one",
            ":db/ident", ":movie/title",
            ":db/valueType", ":db.type/string",
            ":db/doc", "The title of the movie",
        ),
        Util.map(
            ":db/ident", ":movie/release-year",
            ":db/valueType", ":db.type/long",
            ":db/cardinality", ":db.cardinality/one",
            ":db/doc", "The release year of the movie",
        )
    )

    databaseConnection.transact(schema).get()
    println("\nSchema created:\n${schema.joinToString("\n")}")

    val data = Util.list(
        Util.map(
            ":movie/title", "The Matrix",
            ":movie/release-year", 1999
        ),
        Util.map(
            ":movie/title", "Inception",
            ":movie/release-year", 2010
        )
    )

    databaseConnection.transact(data).get()
    println("\nData added:\n${data.joinToString("\n")}")

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