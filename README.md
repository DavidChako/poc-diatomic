Example codes for datomic database.

Local works out of box:
https://github.com/DavidChako/poc-diatomic/blob/master/src/main/kotlin/com/icosahedron/datomic/main/LocalDatomicMain.kt

Remote has two parts, and requires setup and startup of running datomic transactor.

Datomic transactor setup/startup:
https://docs.datomic.com/setup/pro-setup.html

Remote db setup:
https://github.com/DavidChako/poc-diatomic/blob/master/src/main/kotlin/com/icosahedron/datomic/main/RemoteBuildDatomicMain.kt

Remote db query:
https://github.com/DavidChako/poc-diatomic/blob/master/src/main/kotlin/com/icosahedron/datomic/main/RemoteQueryDatomicMain.kt
