package com.icosahedron.datomic.dataset

import datomic.Util

class MovieDataset : Dataset {
    override fun schema(): List<*> = Util.list(
        Util.map(
            ":db/ident", ":movie/title",
            ":db/valueType", ":db.type/string",
            ":db/cardinality", ":db.cardinality/one",
            ":db/doc", "The title of the movie",
        ),
        Util.map(
            ":db/ident", ":movie/release-year",
            ":db/valueType", ":db.type/long",
            ":db/cardinality", ":db.cardinality/one",
            ":db/doc", "The release year of the movie",
        )
    )

    override fun data(): List<*> = Util.list(
        Util.map(
            ":movie/title", "The Matrix",
            ":movie/release-year", 1999
        ),
        Util.map(
            ":movie/title", "Inception",
            ":movie/release-year", 2010
        )
    )

    override fun sampleQuery() = """
        [:find ?title ?year
         :where
         [?movie :movie/title ?title]
         [?movie :movie/release-year ?year]
        ]
    """.trimIndent()
}
