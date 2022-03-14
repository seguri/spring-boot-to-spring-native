# Community Prometheus and Grafana Stack

## Purpose

The purpose of this project is to deploy the community Prometheus and Grafana stack into the target Kubernetes cluster.
The performance and load characteristics of the deployed Spring Boot services can be observed and recorded using this
stack.

## Pre-requisites

* Software requirements: Kubectl and Helm
* The target Kubernetes cluster must be running and reachable. The local kubectl current context must be pointing to
  this cluster.
* The target Kubernetes cluster must have an NGINX or another Ingress Controller installed.

## Deployment

1. Ensure that the target cluster is up and reachable ("kubectl cluster-info").
2. Adjust the domain, local root and ingress parameters in the "values.yaml" file, if needed.
3. Execute the script "deploy.sh".
4. Wait until the pods in the stack are up and running ("kubectl get pods -w").
5. Verify the availability of the ingress for the stack ("kubectl get ingress kube-prometheus-stack-grafana").
6. Log onto Grafana http://localhost/grafana (User: admin, Password: prom-operator). Adjust the URL if you are deploying
   to a remote cluster.
7. Navigate to Manage -> Dashboards. Import the Spring Boot Statistics dashboard with ID 12464.

## Removal

1. Ensure that the target cluster is up and reachable ("kubectl cluster-info").
2. Execute the script "remove.sh".