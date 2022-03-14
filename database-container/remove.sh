#!/bin/bash

# Ensure target cluster is up
exec 2>/dev/null && tmp=$(kubectl cluster-info)
errorCode=$?
if [ $errorCode -ne 0 ]; then
  echo "Check that your current kubectl context is pointing to a running k8s cluster, and try again"
  exit
fi

# Uninstall database-container
if [ -z $(helm list | sed -n '/database-container/p') ]; then
  echo "database-container not deployed, nothing to uninstall"
else
  helm uninstall database-container
fi

# Remove database secret
if [ -n "$(kubectl get secret postgres-secret | sed -n '/postgres-secret/p')" ]; then
  kubectl delete secret postgres-secret
fi







