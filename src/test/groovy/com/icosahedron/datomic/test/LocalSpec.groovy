package com.icosahedron.datomic.test

import com.icosahedron.datomic.DatomicConfiguration
import com.icosahedron.datomic.entity.Movie
import com.icosahedron.datomic.schema.Schema
import datomic.Connection
import datomic.Peer
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import spock.lang.Ignore
import spock.lang.Specification

@Ignore
@ContextConfiguration(classes = DatomicConfiguration)
@TestPropertySource("classpath:application-test.properties")
class LocalSpec extends Specification {
    static LOG = LoggerFactory.getLogger(LocalSpec)

    @Autowired Connection connection

    def "create in-memory db, setup schema, add data, query"() {
        given:
        def entity = 'movie'
        def dataClass = Movie
        def schema = Schema.fromDataClass(entity, dataClass)

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
        def datomicData = schema.renderData(data)
        connection.transact(datomicData).get()
        then:
        LOG.info('Data added:\n{}', datomicData.join('\n'))

        when:
        def query = "" +
                "[:find ?title ?year\n" +
                " :where\n" +
                " [?movie :movie/title ?title]\n" +
                " [?movie :movie/releaseYear ?year]\n" +
                "]"
        def results = Peer.q(query, connection.db())
        then:
        LOG.info("Executed query:\n{}", query)
        LOG.info('Query results:\n{}', results.join('\n'))
    }
}
