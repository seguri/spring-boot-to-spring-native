# Database Container

## Purpose

The purpose of this project is to provide a containerized PostGreSQL database instance to be used with the MilleniumCare
microservices.The deployed instance contains the "precertification" and the "claims" databases needed by the
microservices. Some configuration data is also pre-loaded into these databases. In a Kubernetes deployment, the database
container is deployed in the same cluster as the application microservices. The applications can access the database as
an in-cluster service. the application microservices

## Pre-requisites

Prior to building and deploying this service, you will need to install the following software packages and libraries on
your local development machine. While you should use the specified versions as much as possible, you could experiment
with higher versions.

* Docker (Container runtime): v20.10.7
* Kind (Local Kubernetes cluster): v0.11.1 (Kubernetes v1.21.1). If you wish, you may use a compatible remote Kubernetes
  cluster.
* Kubectl (Kubernetes CLI tool): v1.21.1
* Helm (Kubernetes package manager and deployment tool): v3.6.3

You can use the software package manager (brew, apt, snap, etc.) for your OS platform to install this software. The
scripts included with this project run in the bash shell. Windows users might need to install a bash shell program, or
otherwise install WSL (Windows Subsystem for Linux) for best results.

### Environment Variables

The following environment variables must be populated with valid values prior to attempting to deploy this service:

* POSTGRES_USER (default value "postgres")
* POSTGRES_PASSWORD (default value "postgres")
* PRECERT_DB (default value "precertification")
* PRECERT_USER (default value "precert")
* PRECERT_PASSWORD (default value "precert")
* CLAIMS_DB (default value "claims")
* CLAIMS_USER (default value "claims")
* CLAIMS_PASSWORD (default value "claims")

## Deploy

* Set the environment variables listed in the pre-requisites section. You may skip this step if you are OK using the
  default values.
* Install/reinstall the Kind cluster, if you wish to deploy in a local Kind Kubernetes cluster by executing the script "
  install-kind-cluster.sh".
* To deploy the database container, execute "./deploy.sh". Note that the current
  kubectl context must be configured to point to the target cluster.
* Wait until the database container pod is in a healthy state.

## Remove

To remove the database-container, simply execute the script "remove.sh".