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
    client.apps().deployments().inNamespace(namespace).createOrReplace(deployment);

    // Create service
    log.info("✨ Create the service!");
    Service service = makeService(resource);
    Service existingService = client.services().inNamespace(resource.getMetadata().getNamespace())
        .withName(service.getMetadata().getName()).get();
    if (existingService == null) {
      client.services().inNamespace(namespace).createOrReplace(service);
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

    try {
      log.info("Generated deployment {}", SerializationUtils.dumpAsYaml(deployment));
    } catch (JsonProcessingException e) {
      log.error("Unable to get YML");
      e.printStackTrace();
    }

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

    try {
      log.info("Generated service {}", SerializationUtils.dumpAsYaml(service));
    } catch (JsonProcessingException e) {
      log.error("Unable to get YML");
      e.printStackTrace();
    }

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
$ curl http://ptgtl8.nodes.c1.gra7.k8s.ovh.net:30080/hello

👋  Hello, World ! 🌍
```
 - supprimer la CR : `kubectl delete quarkusoperator/quarkus-app -n test-java-operator`
 - vérifier que tout a été supprimé : 
```bash
$ kubectl get pod,svc  -n test-java-operator              

No resources found in test-java-operator namespace.
```

## 🐳 Packaging & déploiement dans K8s
 - la branche `04-package-deploy` contient le résultat de cette étape
 - arrêter le mode dev de Quarkus
 - ajouter un fichier `src/main/kubernetes/kubernetes.yml` contenant la définition des _ClusterRole_ / _ClusterRoleBinding_ spécifiques à l'opérateur:
```yaml
apiVersion: rbac.authorization.k8s.io/v1
kind: ClusterRole
metadata:
    name: service-deployment-cluster-role
    namespace: java-operator-quarkus-deploy
rules:
  - apiGroups:
    - ""
    resources:
    - secrets
    - serviceaccounts
    - services  
    verbs:
    - "*"
  - apiGroups:
    - "apps"
    verbs:
        - "*"
    resources:
    - deployments
---
apiVersion: rbac.authorization.k8s.io/v1
kind: ClusterRoleBinding
metadata:
  name: service-deployment-cluster-role-binding
  namespace: java-operator-quarkus-deploy
roleRef:
  kind: ClusterRole
  apiGroup: rbac.authorization.k8s.io
  name: service-deployment-cluster-role
subjects:
  - kind: ServiceAccount
    name: java-operator-quarkus-deploy-operator
    namespace: java-operator-quarkus-deploy
---
```
 - modifier le fichier `application.properties`:
```propertie
quarkus.container-image.build=true
#quarkus.container-image.group=wilda
quarkus.container-image.name=java-operator-quarkus-deploy-operator
# set to true to automatically apply CRDs to the cluster when they get regenerated
quarkus.operator-sdk.crd.apply=false
# Kubernetes options
quarkus.kubernetes.namespace=java-operator-quarkus-deploy
```
 - lancer le packaging : `mvn clean package`
 - vérifier que l'image a bien été générée : 
```bash
$ docker images | grep java-operator-quarkus-deploy-operator

wilda/java-operator-quarkus-deploy-operator    0.0.1-SNAPSHOT   988250eed234   12 seconds ago       412MB
```
 - push de l'image : `docker login` && `docker push wilda/java-operator-quarkus-deploy-operator:0.0.1-SNAPSHOT`
 - créer le namespace `java-operator-quarkus-deploy`: `kubectl create ns java-operator-quarkus-deploy`
 - appliquer le manifest créé : `kubectl apply -f ./target/kubernetes/kubernetes.yml`
 - vérifier que tout va bien:
```bash
$ kubectl get pod -n java-operator-quarkus-deploy

NAME                                             READY   STATUS    RESTARTS   AGE
java-operator-quarkus-deploy-8b9cf6766-q6mns      1/1     Running   0          42s   
```
 - appliquer la CR de test : `kubectl apply -f ./src/test/resources/cr-test-deploy-quarkus.yml -n test-java-operator`
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
$ curl http://ptgtl8.nodes.c1.gra7.k8s.ovh.net:30080/hello

👋  Hello, World ! 🌍
```
 - supprimer la CR : `kubectl delete quarkusoperator/quarkus-app -n test-java-operator`
 - vérifier que tout a été supprimé : 
```bash
$ kubectl get pod,svc  -n test-java-operator              

No resources found in test-java-operator namespace.
```
 - supprimer l'opérateur : `kubectl delete -f ./target/kubernetes/kubernetes.yml`
 - supprimer les namespaces: `kubectl delete ns test-java-operator java-operator-quarkus-deploy`
 - supprimer la crd: `kubectl delete crds/quarkusoperators.wilda.fr`