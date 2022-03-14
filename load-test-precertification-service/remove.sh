#!/bin/bash

# Ensure target cluster is up
exec 2>/dev/null && tmp=$(kubectl cluster-info)
errorCode=$?
if [ $errorCode -ne 0 ]; then
  echo "Check that your current kubectl context is pointing to a running k8s cluster, and try again"
  exit
fi

# Remove the Locust chart into the k8s cluster
if [ -n "$(helm list | sed -n '/precertification-loadtest/p')" ]; then
  echo "Uninstalling precertification-loadtest"
  helm uninstall precertification-loadtest
fi

# Delete secrets for the test user
if [ -n "$(kubectl get secret okta-intake-user | sed -n '/okta-intake-user/p')" ]; then
  kubectl delete secret okta-intake-user
fi
if [ -n "$(kubectl get secret okta-intake-password | sed -n '/okta-intake-password/p')" ]; then
  kubectl delete secret okta-intake-password
fi

# Delete the main.py config map
if [ -n "$(kubectl get configmap precertification-service-loadtest | sed -n '/precertification-service-loadtest/p')" ]; then
  kubectl delete configmap precertification-service-loadtest
fi



