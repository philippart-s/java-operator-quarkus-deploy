package fr.wilda;

import io.fabric8.kubernetes.client.KubernetesClient;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.api.reconciler.Reconciler;
import io.javaoperatorsdk.operator.api.reconciler.UpdateControl;

public class QuarkusOperatorReconciler implements Reconciler<QuarkusOperator> { 
  private final KubernetesClient client;

  public QuarkusOperatorReconciler(KubernetesClient client) {
    this.client = client;
  }

  // TODO Fill in the rest of the reconciler

  @Override
  public UpdateControl<QuarkusOperator> reconcile(QuarkusOperator resource, Context context) {
    // TODO: fill in logic

    return UpdateControl.noUpdate();
  }
}

