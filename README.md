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
__  ____  __  _____   ___  __ ____  ______ 
 --/ __ \/ / / / _ | / _ \/ //_/ / / / __/ 
 -/ /_/ / /_/ / __ |/ , _/ ,< / /_/ /\ \   
--\___\_\____/_/ |_/_/|_/_/|_|\____/___/   
2023-02-10 13:55:54,283 WARN  [io.fab.kub.cli.Config] (Quarkus Main Thread) Found multiple Kubernetes config files [[/home/ubuntu/config/k8s/knative.yml, /home/ubuntu/config/k8s/kubeconfig-example2.yml]], using the first one: [/home/ubuntu/config/k8s/knative.yml]. If not desired file, please change it by doing `export KUBECONFIG=/path/to/kubeconfig` on Unix systems or `$Env:KUBECONFIG=/path/to/kubeconfig` on Windows.

2023-02-10 13:55:54,426 WARN  [io.fab.kub.cli.Config] (Quarkus Main Thread) Found multiple Kubernetes config files [[/home/ubuntu/config/k8s/knative.yml, /home/ubuntu/config/k8s/kubeconfig-example2.yml]], using the first one: [/home/ubuntu/config/k8s/knative.yml]. If not desired file, please change it by doing `export KUBECONFIG=/path/to/kubeconfig` on Unix systems or `$Env:KUBECONFIG=/path/to/kubeconfig` on Windows.
2023-02-10 13:55:54,667 INFO  [io.qua.ope.run.OperatorProducer] (Quarkus Main Thread) Quarkus Java Operator SDK extension 4.0.3 (commit: d88d41d on branch: d88d41d78baf198fa4e69d1205f9d19ee04d8c60) built on Thu Oct 06 20:26:39 UTC 2022
2023-02-10 13:55:54,674 WARN  [io.qua.ope.run.AppEventListener] (Quarkus Main Thread) No Reconciler implementation was found so the Operator was not started.
2023-02-10 13:55:54,795 INFO  [io.quarkus] (Quarkus Main Thread) java-operator-quarkus-deploy 0.0.1-SNAPSHOT on JVM (powered by Quarkus 2.13.1.Final) started in 5.591s. Listening on: http://localhost:8080
2023-02-10 13:55:54,800 INFO  [io.quarkus] (Quarkus Main Thread) Profile dev activated. Live Coding activated.
2023-02-10 13:55:54,801 INFO  [io.quarkus] (Quarkus Main Thread) Installed features: [cdi, kubernetes, kubernetes-client, micrometer, openshift-client, operator-sdk, smallrye-context-propagation, smallrye-health, vertx]
```