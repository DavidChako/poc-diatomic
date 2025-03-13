package com.icosahedron.datomic.test

import com.icosahedron.datomic.dataset.Dataset
import com.icosahedron.datomic.dataset.MovieDataset
import datomic.Connection
import datomic.Peer
import org.slf4j.LoggerFactory
import spock.lang.Specification

class RemoteDatomicSpec extends Specification {
    static LOG = LoggerFactory.getLogger(RemoteDatomicSpec)

    def "connect to (and setup if necessary), and then query remote datomic database"() {
        given:
        def uri = 'datomic:dev://localhost:4334/movie-db'
        def dataset = new MovieDataset()

        when:
        def connection = connectToDatabase(uri, dataset)
        def query = dataset.sampleQuery()
        def results = Peer.q(query, connection.db())

        then:
        LOG.info('Executed query:\n{}', query)
        LOG.info('Query results:\n{}', results.join('\n'))

        cleanup:
        connection.release()
    }

    Connection connectToDatabase(String uri, Dataset dataset) {
        if (Peer.createDatabase(uri)) {
            def connection = Peer.connect(uri)
            connection.transact(dataset.schema()).get()
            connection.transact(dataset.data()).get()
            return connection
        }

        return Peer.connect(uri)
    }
}
