package com.icosahedron.datomic.test.remote

import com.icosahedron.datomic.DatomicConfiguration
import com.icosahedron.datomic.dataset.Dataset
import com.icosahedron.datomic.dataset.MovieDataset
import datomic.Connection
import datomic.Peer
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

@ContextConfiguration(classes = DatomicConfiguration)
class RemoteSpec extends Specification {
    static LOG = LoggerFactory.getLogger(RemoteSpec)

    @Autowired Connection connection

    def "connect to and query from remote db"() {
        given:
        def dataset = new MovieDataset()
        def query = dataset.sampleQuery()

        when:
        def results = Peer.q(query, connection.db())

        then:
        LOG.info('Executed query:\n{}', query)
        LOG.info('Query results:\n{}', results.join('\n'))

        and:
        LOG.info(connection.db())
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
