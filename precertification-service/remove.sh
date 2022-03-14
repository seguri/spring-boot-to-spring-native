#!/bin/bash

# Ensure target cluster is up
exec 2>/dev/null && tmp=$(kubectl cluster-info)
errorCode=$?
if [ $errorCode -ne 0 ]; then
  echo "Check that your current kubectl context is pointing to a running k8s cluster, and try again"
  exit
fi

# Uninstall precertification service
if [ -n "$(helm list | sed -n '/precertification-service/p')" ]; then
  helm uninstall precertification-service
fi


# Delete Okta secrets
if [ -n "$(kubectl get secret okta-domain | sed -n '/okta-domain/p')" ]; then
  kubectl delete secret okta-domain
fi

if [ -n "$(kubectl get secret okta-client-id | sed -n '/okta-client-id/p')" ]; then
  kubectl delete secret okta-client-id
fi

if [ -n "$(kubectl get secret okta-client-secret | sed -n '/okta-client-secret/p')" ]; then
  kubectl delete secret okta-client-secret
fi

# Delete configmap
if [ -n "$(kubectl get configmap precertification-service-config | sed -n '/precertification-service-config/p')" ]; then
  kubectl delete configmap precertification-service-config
fi

