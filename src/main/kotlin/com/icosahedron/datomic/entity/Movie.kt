package com.icosahedron.datomic.entity

import datomic.Util

data class Movie(val title: String, val releaseYear: Int) : Entity {
    override fun render(entity: String): Map<*,*> = Util.map(
        ":$entity/title", title,
        ":$entity/releaseYear", releaseYear
    )
}
//    override fun schema() = Schema.Builder("movie")
//        .putField("title", Field(Field.Type.STRING, Field.Cardinality.ONE, "The title of the movie"))
//        .putField("release-year", Field(Field.Type.LONG, Field.Cardinality.ONE, "The release year of the movie"))
//        .build()
//
//    override fun data(): List<*> = Util.list(
//        Util.map(
//            ":movie/title", "The Matrix",
//            ":movie/release-year", 1999
//        ),
//        Util.map(
//            ":movie/title", "Inception",
//            ":movie/release-year", 2010
//        )
//    )
//
//    override fun sampleQuery() = """
//        [:find ?title ?year
//         :where
//         [?movie :movie/title ?title]
//         [?movie :movie/release-year ?year]
//        ]
//    """.trimIndent()
//
//}