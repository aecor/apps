

# Aecor Apps

## (app) Example using Aecor in an Akka Cluster deployment

 * #todo finish documenting functionality, design & deployment.

#### Functionality

Allows opening of bank accounts and executing transactions between them.
Both account and transaction entities are implemented persistent actors.

#### Deployment

##### Setup Cassandra for managing actor persistent state

Initial container creation
```
docker pull cassandra
docker run --name cassandra-dev  -p 9042:9042  cassandra:latest
```

Subsequent
```
docker start cassandra-dev
docker stop cassandra-dev
docker rm cassandra-dev
```

##### Start example app

```
runMain aecor.example.App
```

#### Use

##### `AccountRoute` - open account

```
curl -d '{"accountId": {"value":"ASEAN_123mnk"}, "checkBalance":true}' -H 'Content-Type: application/json' http://localhost:9000/accounts
```

First time
```
23/11/2018 02:14:38.872 INFO  aecor.runtime.akkapersistence.AkkaPersistenceRuntimeActor - [Account-ASEAN_123mnk] Command [open(true)] produced reply [Right(())] and events [Chain(Enriched(Timestamp(2018-11-23T10:14:38.871Z),AccountOpened(true)))]
```

Second time
```
23/11/2018 02:18:44.036 DEBUG akka.cluster.sharding.Shard - Starting entity [ASEAN_123mnk] in shard [11]
23/11/2018 02:18:44.037 INFO  aecor.runtime.akkapersistence.AkkaPersistenceRuntimeActor - [Account-ASEAN_123mnk] Starting...
23/11/2018 02:18:44.063 DEBUG akka.persistence.cassandra.query.EventsByPersistenceIdStage - EventsByPersistenceId [Account-ASEAN_123mnk] Query from seqNr [1] in partition [0]
23/11/2018 02:18:44.068 DEBUG akka.persistence.cassandra.query.EventsByPersistenceIdStage - EventsByPersistenceId [Account-ASEAN_123mnk] Query took [5] ms
23/11/2018 02:18:44.073 INFO  aecor.runtime.akkapersistence.AkkaPersistenceRuntimeActor - [Account-ASEAN_123mnk] Recovery to version [1] completed in [36 ms]
23/11/2018 02:18:44.073 DEBUG aecor.runtime.akkapersistence.AkkaPersistenceRuntimeActor - [Account-ASEAN_123mnk] Setting idle timeout to [60000 milliseconds]
23/11/2018 02:18:44.073 INFO  aecor.runtime.akkapersistence.AkkaPersistenceRuntimeActor - [Account-ASEAN_123mnk] Command [open(true)] produced reply [Right(())] and events [Chain()]
```

Create second account for transfer

```
curl -d '{"accountId": {"value":"ASEAN_124mnk"}, "checkBalance":true}' -H 'Content-Type: application/json' http://localhost:9000/accounts
```

```
25/11/2018 05:02:21.914 INFO  org.http4s.blaze.channel.nio1.NIO1SocketServerGroup - Accepted connection from /0:0:0:0:0:0:0:1:37688
25/11/2018 05:02:21.923 DEBUG akka.cluster.ddata.Replicator - Received Update for key [AccountCoordinatorState]
25/11/2018 05:02:21.924 DEBUG akka.cluster.sharding.DDataShardCoordinator - The coordinator state was successfully updated with ShardHomeAllocated(12,Actor[akka://test/system/sharding/Account#32281375])
25/11/2018 05:02:21.924 DEBUG akka.cluster.sharding.DDataShardCoordinator - Shard [12] allocated at [Actor[akka://test/system/sharding/Account#32281375]]
25/11/2018 05:02:21.925 DEBUG akka.cluster.sharding.Shard - Starting entity [ASEAN_124mnk] in shard [12]
25/11/2018 05:02:21.928 INFO  aecor.runtime.akkapersistence.AkkaPersistenceRuntimeActor - [Account-ASEAN_124mnk] Starting...
25/11/2018 05:02:21.965 INFO  aecor.runtime.akkapersistence.AkkaPersistenceRuntimeActor - [Account-ASEAN_124mnk] Recovery to version [0] completed in [38 ms]
25/11/2018 05:02:21.965 DEBUG aecor.runtime.akkapersistence.AkkaPersistenceRuntimeActor - [Account-ASEAN_124mnk] Setting idle timeout to [60000 milliseconds]
25/11/2018 05:02:21.967 INFO  aecor.runtime.akkapersistence.AkkaPersistenceRuntimeActor - [Account-ASEAN_124mnk] Command [open(true)] produced reply [Right(())] and events [Chain(Enriched(Timestamp(2018-11-25T13:02:21.966Z),AccountOpened(true)))]
25/11/2018 05:02:21.978 DEBUG org.http4s.server.blaze.Http1ServerStage$$anon$1 - Websocket key: None
Request headers: Headers(Host: localhost:9000, User-Agent: curl/7.47.0, Accept: */*, Content-Type: application/json, Content-Length: 60)
25/11/2018 05:02:21.978 INFO  akka.actor.RepointableActorRef - Message [java.lang.String] from Actor[akka://test/system/cassandra-journal#-1734847803] to Actor[akka://test/system/distributedPubSubMediator#1123252500] was not delivered. [4] dead letters encountered. If this is not an expected behavior, then [Actor[akka://test/system/distributedPubSubMediator#1123252500]] may have terminated unexpectedly, This logging can be turned off or adjusted with configuration settings 'akka.log-dead-letters' and 'akka.log-dead-letters-during-shutdown'.

```

##### `TransactionRoute` - authorize payment


```
curl -0 -v -X PUT http://localhost:9000/transactions/0000001 \
-H "Expect:" \
-H 'Content-Type: text/json; charset=utf-8' \
-d @- << EOF

{
  "from": {
    "value": {
      "value": "Account-ASEAN_123mnk"
    }
  },
  "to": {
    "value": {
      "value": "Account-ASEAN_124mnk"
    }
  },
  "amount": {
    "value": 100.1
  }
}
EOF
```

###### `TransactionRoute` - test

```
curl -d '{}' -H 'Content-Type: application/json' http://localhost:9000/test
```

## (app) Implement `business process`/`saga` functionality using Baker/Kagera

 [Baker](https://github.com/ing-bank/baker)
  * _" reduces the effort to orchestrate (micro)service-based process flows"_
  * _"Developers declare the orchestration logic in a recipe."_

 [Kagera](https://github.com/nikolakasev/kagera)
 * _"functions acting on the data in the tokens."_
 * _"An event sourcing function which updates the state using the emitted event / output object."_


## (app) Implement distributed data ingestion with Spark/Hoodie

 * #todo Show how `Spark job server` and `Mist` are not the best paradigm (not _"process flows"_)

