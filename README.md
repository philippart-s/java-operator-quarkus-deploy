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
2023-06-13 15:33:52,746 INFO  [io.qua.ope.run.ConfigurationServiceRecorder] (Quarkus Main Thread) Leader election deactivated for dev profile

2023-06-13 15:33:53,615 INFO  [io.qua.ope.run.OperatorProducer] (Quarkus Main Thread) Quarkus Java Operator SDK extension 5.1.0 (commit: 232db56 on branch: 232db566edf4120b3dc7d5ec724104d5336b8e23) built on Thu Feb 23 15:42:39 UTC 2023
2023-06-13 15:33:53,621 WARN  [io.qua.ope.run.AppEventListener] (Quarkus Main Thread) No Reconciler implementation was found so the Operator was not started.
2023-06-13 15:33:53,755 INFO  [io.quarkus] (Quarkus Main Thread) java-operator-quarkus-deploy 0.0.1-SNAPSHOT on JVM (powered by Quarkus 2.16.3.Final) started in 8.738s. Listening on: http://localhost:8080
2023-06-13 15:33:53,758 INFO  [io.quarkus] (Quarkus Main Thread) Profile dev activated. Live Coding activated.
2023-06-13 15:33:53,759 INFO  [io.quarkus] (Quarkus Main Thread) Installed features: [cdi, kubernetes, kubernetes-client, micrometer, openshift-client, operator-sdk, smallrye-context-propagation, smallrye-health, vertx]
```

## 📄 CRD generation
 - la branche `02-crd-generation` contient le résultat de cette étape
 - création de l'API : `operator-sdk create api --version v1 --kind QuarkusOperator`
 - cette commande a créé les 4 classes nécessaires pour créer l'opérateur:
```bash
src
└── main
    ├── java
    │   └── fr
    │       └── wilda
    │           ├── QuarkusOperator.java
    │           ├── QuarkusOperatorReconciler.java
    │           ├── QuarkusOperatorSpec.java
    │           └── QuarkusOperatorStatus.java
```
  - désactiver, pour l'instant, la création de l'image :
```properties
quarkus.container-image.build=false
#quarkus.container-image.group=
quarkus.container-image.name=java-operator-quarkus-deploy-operator
# set to true to automatically apply CRDs to the cluster when they get regenerated
quarkus.operator-sdk.crd.apply=false
```
  - tester que tout compile que la CRD se génère bien: `mvn clean package` (ou restez en mode `mvn quarkus:dev` pour voir la magie opérer en direct :wink:)
  - la CRD doit être générée dans le target, `target/kubernetes/quarkusoperators.wilda.fr-v1.yml`:
  - elle doit aussi être installée sur le cluster:
```bash
$ kubectl get crds quarkusoperators.wilda.fr
NAME                        CREATED AT

quarkusoperators.wilda.fr   2022-08-26T15:40:19Z
```

## 👋 Hello, World!
 - la branche `03-hello-world` contient le résultat de cette étape
 - modifier la partie _spec_ de la CRD en modifiant la classe `QuarkusOperatorSpec` :
```java
public class QuarkusOperatorSpec {

    private String version;
    private int nodePort;
    public String getVersion() {
        return version;
    }
    public void setVersion(String version) {
        this.version = version;
    }
    public int getNodePort() {
        return nodePort;
    }
    public void setNodePort(int nodePort) {
        this.nodePort = nodePort;
    }
}
```
 - modifier le reconciler `QuarkusOperatorReconciler.java` : 
```java
public class QuarkusOperatorReconciler
    implements Reconciler<QuarkusOperator>, Cleaner<QuarkusOperator> {
  private static final Logger log = LoggerFactory.getLogger(QuarkusOperatorReconciler.class);
  private final KubernetesClient client;

  public QuarkusOperatorReconciler(KubernetesClient client) {
    this.client = client;
  }

  @Override
  public UpdateControl<QuarkusOperator> reconcile(QuarkusOperator resource, Context context) {
    log.info("⚡️ Event occurs ! Reconcile called.");

    String namespace = resource.getMetadata().getNamespace();

    // Create Deployment
    log.info("🚀 Deploy the application!");
    Deployment deployment = makeDeployment(resource);
    client.apps().deployments().inNamespace(namespace).resource(deployment).create();

    // Create service
    log.info("✨ Create the service!");
    Service service = makeService(resource);
    Service existingService = client.services().inNamespace(resource.getMetadata().getNamespace())
        .withName(service.getMetadata().getName()).get();
    if (existingService == null) {
      client.services().inNamespace(namespace).resource(service).create();
    }


    return UpdateControl.noUpdate();
  }

  @Override
  public DeleteControl cleanup(QuarkusOperator resource, Context<QuarkusOperator> context) {
    log.info("🗑 Undeploy the application");

    return DeleteControl.defaultDelete();
  }

  /**
   * Generate the Kubernetes deployment resource.
   * 
   * @param resource The created custom resource
   * @return The created deployment
   */
  private Deployment makeDeployment(QuarkusOperator resource) {
    Deployment deployment = new DeploymentBuilder()
    .withNewMetadata()
      .withName("quarkus-deployment")
      .addToLabels("app", "quarkus")
    .endMetadata()
    .withNewSpec()
      .withReplicas(1)
      .withNewSelector()
        .withMatchLabels(Map.of("app", "quarkus"))
      .endSelector()
      .withNewTemplate()
        .withNewMetadata()
          .addToLabels("app", "quarkus")
        .endMetadata()
        .withNewSpec()
          .addNewContainer()
            .withName("quarkus")
            .withImage("wilda/hello-world-from-quarkus:" + resource.getSpec().getVersion())
            .addNewPort()
              .withContainerPort(80)
            .endPort()
          .endContainer()
        .endSpec()
      .endTemplate()
    .endSpec()
    .build();

    deployment.addOwnerReference(resource);

    log.info("Generated deployment {}", Serialization.asYaml(deployment));

    return deployment;
  }

  /**
   * Generate the Kubernetes service resource.
   * 
   * @param resource The custom resource
   * @return The service.
   */
  private Service makeService(QuarkusOperator resource) {
    Service service = new ServiceBuilder()
    .withNewMetadata()
      .withName("quarkus-service")
      .addToLabels("app", "quarkus")
    .endMetadata()
    .withNewSpec()
      .withType("NodePort")
      .withSelector(Map.of("app", "quarkus"))
      .addNewPort()
        .withPort(80)
        .withTargetPort(new IntOrString(8080))
        .withNodePort(resource.getSpec().getNodePort())
      .endPort()
    .endSpec()
    .build();

    service.addOwnerReference(resource);

    log.info("Generated service {}", Serialization.asYaml(service));

    return service;
  }
}
```
 - créer la CR de tests `./src/test/resources/cr-test-deploy-quarkus.yml`: 
```yaml
apiVersion: "wilda.fr/v1"
kind: QuarkusOperator
metadata:
  name: quarkus-app
spec:
  version: "1.0.0"
  nodePort: 30080
```
 - créer le namespace `test-java-operator` : `kubectl create ns test-java-operator`
 - appliquer la CR : `kubectl apply -f ./src/test/resources/cr-test-deploy-quarkus.yml -n test-java-operator`
 - vérifier que l'application a bien été déployée par l'opérateur :
```bash
$ kubectl get pod,svc  -n test-java-operator                                            

NAME                                      READY   STATUS    RESTARTS   AGE
pod/quarkus-deployment-5f8c85d587-g445p   1/1     Running   0          2m2s

NAME                      TYPE       CLUSTER-IP    EXTERNAL-IP   PORT(S)        AGE
service/quarkus-service   NodePort   XX.XX.XX.XXX   <none>        80:30080/TCP   51s
```
 - tester l'application déployée : 
```bash
$ curl http://<cluster node URL>:30080/hello

👋  Hello, World ! 🌍
```
 - supprimer la CR : `kubectl delete quarkusoperator/quarkus-app -n test-java-operator`
 - vérifier que tout a été supprimé : 
```bash
$ kubectl get pod,svc  -n test-java-operator              

No resources found in test-java-operator namespace.
```
