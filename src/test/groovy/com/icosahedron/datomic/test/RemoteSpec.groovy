package com.icosahedron.datomic.test

import com.icosahedron.datomic.DatomicConfiguration
import com.icosahedron.datomic.entity.Movie
import com.icosahedron.datomic.schema.Schema
import datomic.Connection
import datomic.Peer
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import spock.lang.Ignore
import spock.lang.Specification

@Ignore
@ContextConfiguration(classes = DatomicConfiguration)
class RemoteSpec extends Specification {
    static LOG = LoggerFactory.getLogger(RemoteSpec)

    @Autowired Connection connection

    def "connect to and query from remote db"() {
        given:
        def entity = 'movie'
        def dataClass = Movie

        def schema = Schema.fromDataClass(entity, dataClass)
        def datomicSchema = schema.render()
        connection.transact(datomicSchema).get()

        def data = [
                new Movie("The Matrix", 1999),
                new Movie("Inception", 2010)
        ]
        def datomicData = schema.renderData(data)
        connection.transact(datomicData).get()

        def query = "" +
                "[:find ?title ?year\n" +
                " :where\n" +
                " [?movie :movie/title ?title]\n" +
                " [?movie :movie/releaseYear ?year]\n" +
                "]"

        when:
        def results = Peer.q(query, connection.db())

        then:
        LOG.info('Executed query:\n{}', query)
        LOG.info('Query results:\n{}', results.join('\n'))
    }
}
