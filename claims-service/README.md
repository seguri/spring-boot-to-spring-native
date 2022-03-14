# Claims Service

This is a Spring Boot RESTful API that is used to create, manage and adjudicate claims in the MilleniumCare system. It
uses a PotstgreSQL database for persistence. The API requires authentication, and access to individual endpoints is
controlled via RBAC (Role Based Access Control). Authentication and authorization is managed via Oauth2 integration with
Okta.

This application is meant to be built and deployed as a containerized microservice.

## Prerequisites

### Required Software

Prior to building and deploying this service, you will need to install the following software packages and libraries on
your local development machine. While you should use the specified versions as much as possible, you could experiment
with higher versions.

* Docker (Container runtime): v20.10.7
* Kind (Local Kubernetes cluster): v0.11.1 (Kubernetes v1.21.1)
* Kubectl (Kubernetes CLI tool): v1.21.1
* Helm (Kubernetes package manager and deployment tool): v3.6.3
* Curl (HTTP Client CLI tool): v7.68.0
* Java (OpenJDK): v11.0.11
* Maven (Java build tool): v3.6.3
* jq (CLI tool for JSON parsing): v1.5-1

You can use the software package manager (brew, apt, snap, etc.) for your OS platform to install this software. The
scripts included with this project run in the bash shell. Windows users might need to install a bash shell program, or
otherwise install WSL (Windows Subsystem for Linux) for best results.

### Okta Development Account

*NOTE*: You may skip this step if you have already set up the Okta Development Account as part of the deployment of the
Precertification Requests Service.

The API endpoints are protected by Spring Security authentication and authorization features. The application uses Okta
as the OpenID Connect 1.0 and Oauth 2.0 provider. Therefore, prior to deploying and using the API, you would need to
create and configure a (free) Okta developer account. Following are the high level step. Note that the exact steps in
the Okta UI might be a little different from these due to changes to the Okta UI. Please refer to Okta documentation.

1. Create an Okta developer account at https://developer.okta.com/signup/
2. Set up your application in your Okta account using instructions
   at https://developer.okta.com/docs/guides/implement-password/setup-app/. You may name the application "Millenium Care
   Inc. API".
3. Navigate to Directory -> People. Add two users: Joe Intake (jintake@milleniumcare.com) and Joe Adjudicator (
   jadjudictor@milleniumcare.com). Use "Set by Admin" option for creating the passwords. Make sure to take note of the
   usernames and passwords from this step.
4. Navigate to Directory -> Groups. Add two groups: "Adjudicators" and "Intake". Add both users Joe Intake and Joe
   Adjudicator to the Intake group. Add user Joe Adjudicator alone to the Adjudicators group.
5. Navigate to the "Manage Apps" option for each group, and add the "Millenium Care Inc. API" application to each group.
6. Navigate to Security -> API. Under "Authorization Servers", select the "default" authorization server. Select the "
   Claims" tab. Add a new claim with the following values:
    * Name: groups
    * Included in token type: Access Token
    * Value Type: Groups
    * Filter: Matches regex .*
    * Disable claim: unchecked
    * Include in: Any scope

7. Navigate to Applications -> Millenium Care Inc. API. Take note of the Client ID, Client secret and Okta domain.

### Kubernetes Cluster and Database

Before attempting to deploy the service, make sure the target Kubernetes cluster is up, and the PostGreSQL database
container that it depends on is deployed. Instructions for this are provided in the README for the "Database Container"
project.

### Prometheus and Grafana

This service deploys a service monitor that requires Prometheus and Grafana services in the cluster. You can deploy
these via the kube-prometheus-stack project. Instructions are provided in the README file of that project. You may
already have this in place if you have already successfully deployed the Precertification Requests Service.

### Environment Variables

The following environment variables must be populated with valid values prior to attempting to deploy this service:

* POSTGRES_USER (default value "postgres")
* POSTGRES_PASSWORD (default value "postgres")
* CLAIMS_DB (default value "claims")
* CLAIMS_USER (default value "claims")
* CLAIMS_PASSWORD (default value "claims")
* OKTA_DOMAIN (e.g., dev-8885869.okta.com. No default value)
* OKTA_CLIENT_ID= (no default value)
* OKTA_CLIENT_SECRET (no default value)

## Build

You can build a container image for this Spring Boot service by executing the script build.sh. This script relies on the
Maven plugin "build-image" from Spring Boot to compile the app and build it as a Docker image based on an Ubuntu Linux
distro. The resulting Docker image will be built with the name "claims-service" with the tag "0.0.1-SNAPSHOT"
and saved in the local Docker registry.

## Deploy

Once you have met all the pre-requisites, and have successfully built the container image for the service, you may
deploy it in the target Kubernetes cluster by running the supplied script deploy.sh. This script uses the supplied Helm
chart to deploy the service. Verify that the service pod(s) are running successfully before proceeding to test the
service.

## Test

Once you have successfully deployed the service and confirmed that it is live and ready, you can use the embedded
Swagger UI to view the available REST endpoints in the service. You can view the same UI to test the API as well.

1. Run the script getjwttoken.sh, supplying the username and password as the first and second arguments respectively.
   Make sure the environment variables from the pre-requisites section above are populated prior to running the script.
   Use either the jintake@milleniumcare.com user, or the jadjudicator@milleniumcare.com user, depending on which API
   endpoints you plan to test.
2. Access http://localhost/claims/swagger-ui.html.
3. Click on the Authorize padlock, paste the JWT token, and submit.
4. Select the POST endpoint and use the following JSON to create a new claim:

```json
{
  "diagnosis": {
    "id": 1
  },
  "drug": {
    "id": 2
  },
  "submittingProvider": {
    "id": 1
  },
  "precertNumber": "DR-00000000001",
  "claimDate": "2021-06-21",
  "submittedAmount": "10.00",
  "member": {
    "id": 1
  },
  "units": 1
}
```

5. Note the returned claim number. Use that to test the GET endpoint that requires the claim number.
6. You can test one of the adjudication endpoints to approve or deny this newly created claim (you will need to get and
   use a JWT token for the adjudicator user.)

## Remove

Simply execute the remove.sh script. This will uninstall the claim-service and its associated secrets and config map.
