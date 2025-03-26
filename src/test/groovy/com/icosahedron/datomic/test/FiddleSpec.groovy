package com.icosahedron.datomic.test

import datomic.Peer
import datomic.Util
import org.slf4j.LoggerFactory
import spock.lang.Ignore
import spock.lang.Specification

@Ignore
class FiddleSpec extends Specification {
    static LOG = LoggerFactory.getLogger(FiddleSpec)

    def "fiddle around with datomic"() {
        given:
        def uri = 'datomic:dev://localhost:4334/movie-db'
        def connection = Peer.connect(uri)
        def db = connection.db()

        when:
        def label = 'allMovies'
        def query = '' +
                '[:find ?e\n' +
                ' :where\n' +
                '   [?e :movie/title]\n' +
                ']'
        then:
        executeQuery(label, query, db)

        when:
        label = 'allTitles'
        query = '' +
                '[:find ?movie-title\n' +
                ' :where\n' +
                '   [_ :movie/title ?movie-title]\n' +
                ']'
        then:
        executeQuery(label, query, db)

        when:
        label = 'titlesFrom1985'
        query = '' +
                '[:find ?title\n' +
                ' :where\n' +
                '   [?e :movie/title ?title]\n' +
                '   [?e :movie/release-year 1985]\n' +
                ']'
        then:
        executeQuery(label, query, db)

        when:
        label = 'all-data-from-1985'
        query = '' +
                '[:find ?title ?year ?genre\n' +
                ' :where' +
                '   [?e :movie/title ?title]\n' +
                '   [?e :movie/release-year ?year]\n' +
                '   [?e :movie/genre ?genre]\n' +
                '   [?e :movie/release-year 1985]\n' +
                ']'
        then:
        executeQuery(label, query, db)

        when:
        label = 'find-commando-id'
        query = '' +
                '[:find ?e\n' +
                ' :where' +
                '   [?e :movie/title "Commando"]\n' +
                ']'
        def commandoId = executeQuery(label, query, db).first().first()
        LOG.info('Found commandoId = {}', commandoId)

        def commandoGenreChange = Util.list(
                Util.map(
                        ':db/id', commandoId,
                        ':movie/genre', 'future governor'
                )
        )

        def txResult = connection.transact(commandoGenreChange).get()
        LOG.info('Commando genre change transaction result: {}', txResult)

        label = 'all-data-from-1985'
        query = '' +
                '[:find ?title ?year ?genre\n' +
                ' :where' +
                '   [?e :movie/title ?title]\n' +
                '   [?e :movie/release-year ?year]\n' +
                '   [?e :movie/genre ?genre]\n' +
                '   [?e :movie/release-year 1985]\n' +
                ']'
        then:
        executeQuery(label, query, db)

        when:
        def oldDb = db
        db = connection.db()
        label = 'all-data-from-1985'
        query = '' +
                '[:find ?title ?year ?genre\n' +
                ' :where' +
                '   [?e :movie/title ?title]\n' +
                '   [?e :movie/release-year ?year]\n' +
                '   [?e :movie/genre ?genre]\n' +
                '   [?e :movie/release-year 1985]\n' +
                ']'
        then:
        executeQuery(label, query, db)

        and:
        executeQuery(label+':prior', query, oldDb)

        when:
        label = 'commando-genre-history'
        query = '' +
                '[:find ?genre\n' +
                ' :where' +
                '   [?e :movie/title "Commando"]\n' +
                '   [?e :movie/genre ?genre]\n' +
                '   [?e :movie/release-year 1985]\n' +
                ']'
        then:
        executeQuery(label, query, db.history())

        cleanup:
        connection.release()
    }

    def executeQuery(label, query, db) {
        def results = Peer.q(query, db)
        LOG.info('\nExecuted query:\nlabel = {}\nquery = {}\nresults:\n{}', label, query, results.join('\n'))
        results
    }
}
