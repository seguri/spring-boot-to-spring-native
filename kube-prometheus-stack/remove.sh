#!/bin/bash
# Ensure target cluster is up
exec 2> /dev/null && tmp=$(kubectl cluster-info)
errorCode=$?
if [ $errorCode -ne 0 ]; then
  echo "Check that your current kubectl context is pointing to a running k8s cluster, and try again"
  exit
fi

# Uninstall the kube-prometheus-stack from the k8s cluster, if present
if [ -z $(helm list | sed -n '/kube-prometheus-stack/p') ]
then
  echo "kube-prometheus-stack not installed, nothing to uninstall"
else
  helm uninstall kube-prometheus-stack
fi

# Remove the prometheus-community repo from the local Helm repos, if present
if [ -z $(helm repo list | sed -n '/prometheus-community/p') ]
then
  echo "prometheus-community repo not present locally, nothing to remove"
else
  helm repo remove prometheus-community
fi