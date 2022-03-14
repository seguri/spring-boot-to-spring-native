#!/bin/bash

# Check that the environment variables are set before executing the script. Set defaults for any missing
# variables
if [ "$POSTGRES_USER" = "" ]; then
  echo "Environment variable POSTGRES_USER is not set. Default set to postgres"
  POSTGRES_USER=postgres
fi
if [ "$POSTGRES_PASSWORD" = "" ]; then
  echo "Environment variable POSTGRES_PASSWORD is not set. Default set to postgres"
  POSTGRES_PASSWORD=postgres
fi
if [ "$PRECERT_DB" = "" ]; then
  echo "Environment variable PRECERT_DB is not set. Default set to pecertification"
  PRECERT_DB=pecertification
fi
if [ "$PRECERT_USER" = "" ]; then
  echo "Environment variable PRECERT_USER is not set. Default set to precert"
  PRECERT_USER=precert
fi
if [ "$PRECERT_PASSWORD" = "" ]; then
  echo "Environment variable PRECERT_PASSWORD is not set. Default set to precert"
  PRECERT_PASSWORD=precert
fi
if [ "$CLAIMS_DB" = "" ]; then
  echo "Environment variable CLAIMS_DB is not set. Default set to claims"
  CLAIMS_DB=claims
fi
if [ "$CLAIMS_USER" = "" ]; then
  echo "Environment variable CLAIMS_USER is not set. Default set to claims"
  CLAIMS_USER=claims
fi
if [ "$CLAIMS_PASSWORD" = "" ]; then
  echo "Environment variable CLAIMS_PASSWORD is not set. Default set to claims"
  CLAIMS_PASSWORD=claims
fi

# Ensure target cluster is up
exec 2>/dev/null && tmp=$(kubectl cluster-info)
errorCode=$?
if [ $errorCode -ne 0 ]; then
  echo "Check that your current kubectl context is pointing to a running k8s cluster, and try again"
  exit
fi

# Load/re-load database secret
if [ -n "$(kubectl get secret postgres-secret | sed -n '/postgres-secret/p')" ]; then
  kubectl delete secret postgres-secret
fi
kubectl create secret generic postgres-secret \
  --from-literal=postgres-user="$POSTGRES_USER" \
  --from-literal=postgres-password="$POSTGRES_PASSWORD" \
  --from-literal=precert-db="$PRECERT_DB" \
  --from-literal=precert-user="$PRECERT_USER" \
  --from-literal=precert-password="$PRECERT_PASSWORD" \
  --from-literal=claims-db="$CLAIMS_DB" \
  --from-literal=claims-user="$CLAIMS_USER" \
  --from-literal=claims-password="$CLAIMS_PASSWORD"

# Install/upgrade database-container
if [ -z $(helm list | sed -n '/database-container/p') ]; then
  helm install database-container database-container/ --values database-container/values.yaml
else
  helm uninstall database-container
  helm install database-container database-container/ --values database-container/values.yaml
fi

