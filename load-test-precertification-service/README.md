# Locust Load Test for the Precertification Requests Service

## Purpose

Provides a containerized instance of a load test suite for the Precertification Requests REST API. The load test suite
is based on the Locust framework (https://locust.io/)

## Deploy

**Note**: Before deploying this load test service, ensure that you have deployed the Precertification Requests Service and
verified that it is functioning correctly.

1. Set the environment variables INTAKE_USER and INTAKE_PASSWORD. These should correspond to the user account that you
   added to the "Intake" group when setting up the Okta Development Account as part of the deployment of the
   Precertification Requests Service.
2. Ensure that the target Kubernetes cluster is up and running and is configured in the current-context of Kubectl.
3. Execute "./deploy.sh"
4. Execute "kubectl get pods -w". Wait until the locust master and worker pods for the load test service are in running state.

## Run

### Test Description
The load test script packaged in this service performs the following steps in each test iteration for each user thread it spawns:
1. Authenticate with Okta to obtain a JWT token.
2. Invoke the Precertication Requests REST API to create a drug precertification request for one of the members in the database. The database contains a dummy drug record, which is used for the load test.
3. Invoke the Precertication Requests REST API to void this newly created request.

Steps 2 and 3 are repeated in the user thread until the test is stopped. Load is generated on the target RESP API by spawning multiple concurrent user threads.

### Test Execution Steps
1. Access the Web UI for the load test at http://localhost/precertificationloadtest/ (the trailing slash is important). If you see a "503" error, wait for a couple of minutes for the Locust master to fully start up.
2. Enter the following test parameters:
   * Number of total users to simulate = 5. (The sample DB is set up with 5 members. You could add more members to the DB and increase the count here if you'd like to test with a higher concurrent user count).
   * Spawn rate = 1 (Don't exceed this, since it avoids exceeding the rate limit on the Okta login API on the free account).
   * Host = http://precertification-service (The Locust service will be able to reach the precertification service via this URL internally within the Kubernetes cluster)
3. Click "Start Swarming" and observe the test as it proceeds. You should observe Grafana dashboards that render metrics for the precertification service while the load test is running to observe how the service is performing under load.
4. Let the test run for 2-3 minutes. This should be sufficient to gather necessary metrics. 
5. Click on the "Stop Test" button to terminate the test.

In order to repeat the test, it might be best to reset the database to its pre-test state before re-executing the test. 

## Remove
Simply execute the remove.sh script to uninstall the load test service from the cluster.