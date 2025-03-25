package com.icosahedron.datomic.test.local

import com.icosahedron.datomic.DatomicConfiguration
import com.icosahedron.datomic.dataset.MovieDataset
import datomic.Connection
import datomic.Peer
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import spock.lang.Specification

@ContextConfiguration(classes = DatomicConfiguration)
@TestPropertySource("classpath:application-test.properties")
class LocalSpec extends Specification {
    static LOG = LoggerFactory.getLogger(LocalSpec)

    @Autowired Connection connection

    def "create in-memory db, setup schema, add data, query"() {
        given:
        def dataset = new MovieDataset()
        def schema = dataset.schema()

        when:
        def datomicSchema = schema.toDatomic()
        connection.transact(datomicSchema).get()
        then:
        LOG.info('Schema created:\n{}', datomicSchema.join('\n'))

        when:
        def data = dataset.data()
        connection.transact(data).get()
        then:
        LOG.info('Data added:\n{}', data.join('\n'))

        when:
        def query = dataset.sampleQuery()
        def results = Peer.q(query, connection.db())

        then:
        LOG.info("Executed query:\n{}", query)
        LOG.info('Query results:\n{}', results.join('\n'))

//        cleanup: <-- appears unnecessary when using datomic
//        connection.release()
    }
}
