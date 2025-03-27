package com.icosahedron.datomic

import datomic.Connection
import datomic.Peer
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.PropertySource

@Configuration
@PropertySource("classpath:application.properties")
class DatomicConfiguration {
    @Bean
    fun connection(@Value("\${datomic.uri}") uri: String): Connection {
        if (Peer.createDatabase(uri)) {
            LOG.info("Database created: {}", uri)

        }

        val connection = Peer.connect(uri)
        LOG.info("Connection established: {}", connection)
        return connection
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(DatomicConfiguration.javaClass)
    }
}