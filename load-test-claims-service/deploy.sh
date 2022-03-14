#!/bin/bash

# Ensure target cluster is up
exec 2>/dev/null && tmp=$(kubectl cluster-info)
errorCode=$?
if [ $errorCode -ne 0 ]; then
  echo "Check that your current kubectl context is pointing to a running k8s cluster, and try again"
  exit
fi

# Ensure required environment variables are populated
if [ "$INTAKE_USER" = "" ] || [ "$INTAKE_PASSWORD" = "" ]; then
  echo "Check that INTAKE_USER and INTAKE_PASSWORD are all set"
  exit
fi

# Load secrets for the test user
if [ -n "$(kubectl get secret okta-intake-user | sed -n '/okta-intake-user/p')" ]; then
  kubectl delete secret okta-intake-user
fi
kubectl create secret generic okta-intake-user --from-literal=INTAKE_USER="$INTAKE_USER"
if [ -n "$(kubectl get secret okta-intake-password | sed -n '/okta-intake-password/p')" ]; then
  kubectl delete secret okta-intake-password
fi
kubectl create secret generic okta-intake-password --from-literal=INTAKE_PASSWORD="$INTAKE_PASSWORD"

# Load the main.py as a config map
if [ -n "$(kubectl get configmap claims-service-loadtest | sed -n '/claims-service-loadtest/p')" ]; then
  kubectl delete configmap claims-service-loadtest
fi
kubectl create configmap claims-service-loadtest --from-file main.py

# Add the deliveryhero repo to local Helm repos, if not already done
if [ -z $(helm repo list | sed -n '/deliveryhero/p') ]; then
  echo "Adding deliveryhero to Helm repos"
  helm repo add deliveryhero https://charts.deliveryhero.io/
else
  echo "deliveryhero already added to Helm repos"
fi

# Install or upgrade the Locust chart into the k8s cluster
if [ -z $(helm list | sed -n '/claims-loadtest/p') ]; then
  echo "Updating Helm repos"
  helm repo update
  echo "Installing claims-loadtest"
  helm install claims-loadtest deliveryhero/locust -f values.yaml --version 0.19.23
else
  echo "Upgrading precertification-loadtest"
  helm upgrade claims-loadtest deliveryhero/locust -f values.yaml --version 0.19.23
fi
