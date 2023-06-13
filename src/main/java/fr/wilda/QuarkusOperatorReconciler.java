package fr.wilda;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.fabric8.kubernetes.api.model.IntOrString;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceBuilder;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.utils.Serialization;
import io.javaoperatorsdk.operator.api.reconciler.Cleaner;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.api.reconciler.DeleteControl;
import io.javaoperatorsdk.operator.api.reconciler.Reconciler;
import io.javaoperatorsdk.operator.api.reconciler.UpdateControl;

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