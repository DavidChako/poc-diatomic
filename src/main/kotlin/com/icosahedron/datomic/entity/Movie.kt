package com.icosahedron.datomic.entity

data class Movie(val title: String, val releaseYear: Int) : Entity {
//    constructor(data: Map<*,*>): this(
//        data[SCHEMA.keyword("title")] as String,
//        data[SCHEMA.keyword("releaseYear")] as Int,
//    )

//    override fun render(entity: String): Map<*,*> = Util.map(
//        ":$entity/title", title,
//        ":$entity/releaseYear", releaseYear
//    )
}
