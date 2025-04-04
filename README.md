# poc-datomic

Proof of concept for use of datomic database

## Set the datomic uri in application.properties
    A. For in-memory case, like:
        datomic.uri=datomic:mem:/example-db
    B. For remote case, like: 
        datomic.uri=datomic:dev://localhost:4334/example-db

## Prerequisites for remote
    A. Obtain datomic transactor executable if necessary:
        1. % mkdir ~/datomic
        2. % cd ~/datomic
        3. % curl https://datomic-pro-downloads.s3.amazonaws.com/1.0.7277/datomic-pro-1.0.7277.zip -O
        4. % unzip datomic-pro-1.0.7277.zip
    B. Run transactor if not already running:
        1. % cd ~/datomic/datomic-pro-1.0.7277
        2. % ./bin/transactor config/samples/dev-transactor-template.properties
