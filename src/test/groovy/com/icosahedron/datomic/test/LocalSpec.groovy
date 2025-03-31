package com.icosahedron.datomic.test

import com.icosahedron.datomic.DatomicConfiguration
import com.icosahedron.datomic.entity.Movie
import com.icosahedron.datomic.schema.Field
import com.icosahedron.datomic.schema.Schema
import datomic.Connection
import datomic.Peer
import datomic.Util
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import spock.lang.Ignore
import spock.lang.Specification

@ContextConfiguration(classes = DatomicConfiguration)
@TestPropertySource("classpath:application-test.properties")
class LocalSpec extends Specification {
    static LOG = LoggerFactory.getLogger(LocalSpec)

    @Autowired Connection connection

    def "create in-memory db, setup schema, add data, query"() {
        given:
        def schema = Schema.fromDataClass(Movie)
        def entity = schema.entity
        LOG.info('Schema:\n{}', schema)

        def data = [
                new Movie("The Matrix", 1999),
                new Movie("Inception", 2010)
        ]

        when:
        def datomicSchema = schema.render()
        connection.transact(datomicSchema).get()
        then:
        LOG.info('Schema created:\n{}', datomicSchema.join('\n'))

        when:
        def schemaPull = schema.renderPull()
        then:
        executeQuery('schema pull', schemaPull, connection.db())

        when:
        def schemaQuery = schema.renderQuery()
        then:
        executeQuery('schema query', schemaQuery, connection.db())

        when:
        def datomicData = schema.renderData(data)
        connection.transact(datomicData).get()
        then:
        LOG.info('Data added:\n{}', datomicData.join('\n'))

        when:
        def query = "" +
                "[:find ?title ?year\n" +
                " :where\n" +
                " [?movie :$entity/title ?title]\n" +
                " [?movie :$entity/releaseYear ?year]\n" +
                "]"
        then:
        executeQuery('titleAndReleaseYear', query, connection.db())

        when:
        query = schema.renderDataPull('title')
        then:
        executeQuery('pull all', query, connection.db())

        when:
        def field = 'title'
        def value = 'Inception'
        query = schema.renderDataPull(field, value)
        then:
        executeQuery("pull by field: $field = $value", query, connection.db())
    }

    @Ignore
    def "play with schema"() {
        given:
        def entity = 'book'
        def initialSchema = new Schema.Builder(entity)
                .addField("id", Field.Type.STRING, Field.Cardinality.ONE, "The id of the $entity")
                .addField("title", Field.Type.STRING, Field.Cardinality.ONE, "The title of the $entity")
                .addField("pageCount", Field.Type.LONG, Field.Cardinality.ONE, "The page count of the $entity")
                .addField("authorId", Field.Type.STRING, Field.Cardinality.ONE, "The id of the author of the $entity")
                .build()

        when:
        def initialDatomicSchema = initialSchema.render()
        connection.transact(initialDatomicSchema).get()
        then:
        LOG.info('Schema created:\n{}', initialDatomicSchema.join('\n'))

        when:
        def query = "" +
                "[:find (pull ?e [*]), ?type, ?cardinality" +
                " :where\n" +
                " [?e :db/ident ?ident]\n" +
                " [?e :db/valueType ?eType]\n" +
                " [?eType :db/ident ?type]\n" +
                " [?e :db/cardinality ?eCardinality]\n" +
                " [?eCardinality :db/ident ?cardinality]\n" +
                " [?e :db/doc ?doc]\n" +
                " [_ :db.install/attribute ?e]\n" +
                " [(.toString ?ident) ?val]\n" +
                " [(.startsWith ?val \":$entity\")]" +
                "]"
        LOG.info("Executing query:\n{}", query)
        then:
        def initialSchemaResults = Peer.q(query, connection.db())
        LOG.info('Initial schema results:\n{}', initialSchemaResults.join('\n'))

        when:
        connection.transact(initialDatomicSchema).get()
        then:
        def reInitialSchemaResults = Peer.q(query, connection.db())
        LOG.info('Re initial schema results:\n{}', reInitialSchemaResults.join('\n'))
        reInitialSchemaResults.join('\n') == initialSchemaResults.join('\n')

        when:
        def revisedSchema = initialSchema.builder()
                .addField("copyright", Field.Type.STRING, Field.Cardinality.ONE, "The copyright of the book")
                .build()
        def revisedDatomicSchema = revisedSchema.render()
        connection.transact(revisedDatomicSchema).get()
        then:
        def revisedSchemaResults = Peer.q(query, connection.db())
        LOG.info('Revised schema results:\n{}', revisedSchemaResults.join('\n'))
        revisedSchemaResults.join('\n') != initialSchemaResults.join('\n')
    }

    @Ignore
    def "fiddle around with datomic"() {
        given:
        def dataClass = Movie
        def baseSchema = Schema.fromDataClass(dataClass)
        def entity = baseSchema.entity
        def schema = baseSchema.builder()
                .addField('genre', Field.Type.STRING, Field.Cardinality.ONE, 'Genre of movie')
                .build()
        connection.transact(schema.render()).get()

        def titleKey = ":$entity/title".toString()
        def releaseYearKey = ":$entity/releaseYear".toString()
        def genreKey = ":$entity/genre".toString()
        def data = Util.list(
                Util.map(
                        titleKey, 'The Matrix',
                        releaseYearKey, 1999,
                        genreKey, "SciFi".toString(),
                ),
                Util.map(
                        titleKey, "Inception".toString(),
                        releaseYearKey, 2010,
                        genreKey, "Action".toString()
                ),
                Util.map(
                        titleKey, "Commando".toString(),
                        releaseYearKey, 1985,
                        genreKey, "Adventure".toString()
                )
        )
        connection.transact(data).get()

        when:
        def db = connection.db()
        def label = 'allMovies'
        def query = "" +
                "[:find ?e\n" +
                " :where\n" +
                "   [?e $titleKey]\n" +
                "]"
        then:
        executeQuery(label, query, db)

        when:
        label = 'allTitles'
        query = "" +
                "[:find ?title\n" +
                " :where\n" +
                "   [_ $titleKey ?title]\n" +
                "]"
        then:
        executeQuery(label, query, db)

        when:
        def releaseYear = 1985
        label = "titlesFrom$releaseYear"
        query = "" +
                "[:find ?title\n" +
                " :where\n" +
                "   [?e $titleKey ?title]\n" +
                "   [?e $releaseYearKey $releaseYear]\n" +
                "]"
        then:
        executeQuery(label, query, db)

        when:
        label = "all-data-from-$releaseYear"
        query = "" +
                "[:find ?title ?year ?genre\n" +
                " :where" +
                "   [?e $titleKey ?title]\n" +
                "   [?e $releaseYearKey ?year]\n" +
                "   [?e $genreKey ?genre]\n" +
                "   [?e $releaseYearKey $releaseYear]\n" +
                "]"
        then:
        executeQuery(label, query, db)

        when:
        label = 'find-commando-id'
        query = "" +
                "[:find ?e\n" +
                " :where" +
                "   [?e $titleKey \"Commando\"]\n" +
                "]"
        def commandoId = executeQuery(label, query, db).first().first()
        LOG.info('Found commandoId = {}', commandoId)

        def newGenre = 'future governor'
        def commandoGenreChange = Util.list(
                Util.map(
                        ':db/id', commandoId,
                        genreKey, newGenre
                )
        )

        def txResult = connection.transact(commandoGenreChange).get()
        LOG.info('Commando genre change transaction result: {}', txResult)

        label = 'all-data-from-1985'
        query = "" +
                "[:find ?title ?year ?genre\n" +
                " :where" +
                "   [?e $titleKey ?title]\n" +
                "   [?e $releaseYearKey ?year]\n" +
                "   [?e $genreKey ?genre]\n" +
                "   [?e $releaseYearKey $releaseYear]\n" +
                "]"
        then:
        executeQuery(label, query, db)

        when:
        def oldDb = db
        db = connection.db()
        label = 'all-data-from-1985'
        query = "" +
                "[:find ?title ?year ?genre\n" +
                " :where" +
                "   [?e $titleKey ?title]\n" +
                "   [?e $releaseYearKey ?year]\n" +
                "   [?e $genreKey ?genre]\n" +
                "   [?e $releaseYearKey $releaseYear]\n" +
                "]"
        then:
        executeQuery(label, query, db)
        and:
        executeQuery(label+':prior', query, oldDb)

        when:
        label = 'commando-genre-history'
        query = "" +
                "[:find ?genre\n" +
                " :where" +
                "   [?e $titleKey \"Commando\"]\n" +
                "   [?e $genreKey ?genre]\n" +
                "   [?e $releaseYearKey $releaseYear]\n" +
                "]"
        then:
        executeQuery(label, query, db.history())

        when:
        label = 'find-commando-id-again'
        query = "" +
                "[:find ?e\n" +
                " :where" +
                "   [?e $titleKey \"Commando\"]\n" +
                "]"
        then:
        def commandoIdAgain = executeQuery(label, query, db).first().first()
        LOG.info('Found commandoIdAgain = {}', commandoIdAgain)
        and:
        commandoIdAgain == commandoId

        when:
        //{:tx-data [[:db/retract [:Movie/title \"Commando\"] :Movie/genre \"$newGenre\"]]}

        def retraction = Util.list(
                Util.list(":db/retract", commandoId, ":Movie/genre", newGenre)
        )
//        // Retract the data
//        List<?> txData = Util.list(Util.list(":db/retract", entityId, attribute, value));

        then:
        def retractionResults = connection.transact(retraction).get()
        LOG.info('\nExecuted retraction:\nretraction = {}\nresults:\n{}\n', retraction, retractionResults.join('\n'))

        when:
        db = connection.db()
        label = 'retracted-genre'
//        query = "" +
//                "[:find ?title ?year ?genre\n" +
//                " :where" +
//                "   [?e $titleKey ?title]\n" +
//                "   [?e $releaseYearKey ?year]\n" +
//                "   [?e $genreKey ?genre]\n" +
//                "   [?e $releaseYearKey $releaseYear]\n" +
//                "]"
        query = "" +
                "[:find ?title ?year\n" +
                " :where" +
                "   [?e $titleKey ?title]\n" +
                "   [?e $releaseYearKey ?year]\n" +
                "   [?e $releaseYearKey $releaseYear]\n" +
                "]"
        then:
        executeQuery(label, query, db)

        when:
        label = 'commando-genre-history-again'
        query = "" +
                "[:find ?genre\n" +
                " :where" +
                "   [?e $titleKey \"Commando\"]\n" +
                "   [?e $genreKey ?genre]\n" +
                "   [?e $releaseYearKey $releaseYear]\n" +
                "]"
        then:
        executeQuery(label, query, db.history())
    }

    def executeQuery(label, query, db) {
        def results = Peer.q(query, db)
        LOG.info('\nExecuted query:\nlabel = {}\nquery = {}\nresults:\n{}\n', label, query, results.join('\n'))
        results
    }
}
