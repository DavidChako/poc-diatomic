package com.icosahedron.datomic.test.remote

import com.icosahedron.datomic.dataset.Dataset
import com.icosahedron.datomic.dataset.MovieDataset
import datomic.Connection
import datomic.Peer
import org.slf4j.LoggerFactory
import spock.lang.Specification

class RemoteSpec extends Specification {
    static LOG = LoggerFactory.getLogger(RemoteSpec)

    def "connect to (and setup if necessary), and then query remote datomic database"() {
        given:
        def uri = 'datomic:dev://localhost:4334/movie-db'
        def dataset = new MovieDataset()
        def connection = connectToDatabase(uri, dataset)
        def query = dataset.sampleQuery()

        when:
        def results = Peer.q(query, connection.db())

        then:
        LOG.info('Executed query:\n{}', query)
        LOG.info('Query results:\n{}', results.join('\n'))

        cleanup:
        connection.release()
    }

    static Connection connectToDatabase(String uri, Dataset dataset) {
        if (Peer.createDatabase(uri)) {
            def connection = Peer.connect(uri)
            connection.transact(dataset.schema()).get()
            connection.transact(dataset.data()).get()
            return connection
        }

        return Peer.connect(uri)
    }
}
