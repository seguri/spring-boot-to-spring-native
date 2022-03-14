#!/bin/bash
if [ -n "$(kind get clusters | sed -n '/kind/p')" ]; then
  echo "Removing existing Kind cluster"
  kind delete cluster
fi
echo "Creating fresh Kind cluster"
kind create cluster --config=kind-config.yml
echo "install NGINX Ingress Controller in the cluster"
VERSION=$(curl https://raw.githubusercontent.com/kubernetes/ingress-nginx/master/stable.txt)
kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/${VERSION}/deploy/static/provider/kind/deploy.yaml