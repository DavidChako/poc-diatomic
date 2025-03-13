package com.icosahedron.datomic.test.local

import com.icosahedron.datomic.dataset.MovieDataset
import datomic.Peer
import org.slf4j.LoggerFactory
import spock.lang.Specification

class LocalSpec extends Specification {
    static LOG = LoggerFactory.getLogger(LocalSpec)

    def "build db, then setup schema, then add data, and then query local datomic database"() {
        given:
        def uri = 'datomic:mem:/example-db'
        def dataset = new MovieDataset()

        when:
        Peer.createDatabase(uri)
        LOG.info('Database created: {}', uri)

        def connection = Peer.connect(uri)
        LOG.info('Connected established:\n{}', connection)

        def schema = dataset.schema()
        connection.transact(dataset.schema()).get()
        LOG.info('Schema created:\n{}', schema.join('\n'))

        def data = dataset.data()
        connection.transact(data).get()
        LOG.info('Data added:\n{}', data.join('\n'))

        def query = dataset.sampleQuery()
        def results = Peer.q(query, connection.db())

        then:
        LOG.info("Executed query:\n{}", query)
        LOG.info('Query results:\n{}', results.join('\n'))

        cleanup:
        connection.release()
    }
}
