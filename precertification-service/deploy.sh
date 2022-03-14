#!/bin/bash

# Ensure target cluster is up
exec 2>/dev/null && tmp=$(kubectl cluster-info)
errorCode=$?
if [ $errorCode -ne 0 ]; then
  echo "Check that your current kubectl context is pointing to a running k8s cluster, and try again"
  exit
fi

# Ensure required environment variables are populated
if [ "$OKTA_DOMAIN" = "" ] || [ "$OKTA_CLIENT_ID" = "" ] || [ "$OKTA_CLIENT_SECRET" = "" ]; then
  echo "Check that OKTA_DOMAIN, OKTA_CLIENT_ID and OKTA_CLIENT_SECRET are all set"
  exit
fi

# Load the application.properties file as a ConfigMap
if [ -n "$(kubectl get configmap precertification-service-config | sed -n '/precertification-service-config/p')" ]; then
  kubectl delete configmap precertification-service-config
fi
kubectl create configmap precertification-service-config --from-file=src/main/resources/application.properties

# Load Okta secrets
if [ -n "$(kubectl get secret okta-domain | sed -n '/okta-domain/p')" ]; then
  kubectl delete secret okta-domain
fi
kubectl create secret generic okta-domain --from-literal=OKTA_DOMAIN="$OKTA_DOMAIN"

if [ -n "$(kubectl get secret okta-client-id | sed -n '/okta-client-id/p')" ]; then
  kubectl delete secret okta-client-id
fi
kubectl create secret generic okta-client-id --from-literal=OKTA_CLIENT_ID="$OKTA_CLIENT_ID"

if [ -n "$(kubectl get secret okta-client-secret | sed -n '/okta-client-secret/p')" ]; then
  kubectl delete secret okta-client-secret
fi
kubectl create secret generic okta-client-secret --from-literal=OKTA_CLIENT_SECRET="$OKTA_CLIENT_SECRET"

# Install or upgrade the Precertification Service Helm Chart
kind load docker-image precertification-service:0.0.1-SNAPSHOT

#helm install precertification-service precertification-service/ --values precertification-service/values.yaml
if [ -z $(helm list | sed -n '/precertification-service/p') ]; then
  echo "Installing precertification-service"
  helm install precertification-service precertification-service/ --values precertification-service/values.yaml
else
  echo "Upgrading precertification-service"
  helm upgrade precertification-service precertification-service/ --values precertification-service/values.yaml
fi
