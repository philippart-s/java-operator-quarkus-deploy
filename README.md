# java-operator-quarkus-deploy
Simple Kubernetes operator written in Java to deploy a Quarkus application

## 🎉 Init project
 - la branche `01-init-project` contient le résultat de cette étape
 - [installer / mettre](https://sdk.operatorframework.io/docs/installation/) à jour la dernière version du [Operator SDK](https://sdk.operatorframework.io/) 
 - créer le répertoire `java-operator-quarkus-deploy`
 - dans le répertoire `java-operator-quarkus-deploy `, scaffolding du projet avec Quarkus : `operator-sdk init --plugins quarkus --domain wilda.fr --project-name java-operator-quarkus-deploy`
 - l'arborescence générée est la suivante:
```bash
.
├── Makefile
├── PROJECT
├── README.md
├── pom.xml
├── src
│   └── main
│       ├── java
│       └── resources
│           └── application.properties
```
 - vérification que cela compile : `mvn clean compile`
 - tester le lancement: `mvn quarkus:dev`:
```bash
 --/ __ \/ / / / _ | / _ \/ //_/ / / / __/ 
 -/ /_/ / /_/ / __ |/ , _/ ,< / /_/ /\ \   
--\___\_\____/_/ |_/_/|_/_/|_|\____/___/   
2023-06-13 15:33:52,746 INFO  [io.qua.ope.run.ConfigurationServiceRecorder] (Quarkus Main Thread) Leader election deactivated for dev profile

2023-06-13 15:33:53,615 INFO  [io.qua.ope.run.OperatorProducer] (Quarkus Main Thread) Quarkus Java Operator SDK extension 5.1.0 (commit: 232db56 on branch: 232db566edf4120b3dc7d5ec724104d5336b8e23) built on Thu Feb 23 15:42:39 UTC 2023
2023-06-13 15:33:53,621 WARN  [io.qua.ope.run.AppEventListener] (Quarkus Main Thread) No Reconciler implementation was found so the Operator was not started.
2023-06-13 15:33:53,755 INFO  [io.quarkus] (Quarkus Main Thread) java-operator-quarkus-deploy 0.0.1-SNAPSHOT on JVM (powered by Quarkus 2.16.3.Final) started in 8.738s. Listening on: http://localhost:8080
2023-06-13 15:33:53,758 INFO  [io.quarkus] (Quarkus Main Thread) Profile dev activated. Live Coding activated.
2023-06-13 15:33:53,759 INFO  [io.quarkus] (Quarkus Main Thread) Installed features: [cdi, kubernetes, kubernetes-client, micrometer, openshift-client, operator-sdk, smallrye-context-propagation, smallrye-health, vertx]
```