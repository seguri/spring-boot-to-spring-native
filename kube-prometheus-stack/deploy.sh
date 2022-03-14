#!/bin/bash
# Ensure target cluster is up
exec 2> /dev/null && tmp=$(kubectl cluster-info)
errorCode=$?
if [ $errorCode -ne 0 ]; then
  echo "Check that your current kubectl context is pointing to a running k8s cluster, and try again"
  exit
fi

# Add the prometheus-community repo to local Helm repos, if not already done
if [ -z $(helm repo list | sed -n '/prometheus-community/p') ]
then
  echo "Adding prometheus-community to Helm repos"
  helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
else
  echo "prometheus-repo already added to Helm repos"
fi

# Install or upgrade the kube-prometheus-stack into the k8s cluster
if [ -z $(helm list | sed -n '/kube-prometheus-stack/p') ]; then
  echo "Updating Helm repos"
  helm repo update
  echo "Installing kube-prometheus-stack"
  helm install kube-prometheus-stack prometheus-community/kube-prometheus-stack --values values.yml --version 17.1.0
else
  echo "Upgrading kube-prometheus-stack"
  helm upgrade kube-prometheus-stack prometheus-community/kube-prometheus-stack --values values.yml --version 17.1.0
fi