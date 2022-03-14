import os
import threading
from base64 import b64encode

from locust import task, HttpUser
from queue import LifoQueue


class PrecertificationRequestLoadTest(HttpUser):
    lock = threading.Lock()
    next_member_id = 0
    avail_member_ids = LifoQueue()

    def __init__(self, *args, **kwargs):
        super().__init__(*args, **kwargs)
        self.bearer_token = ""
        self.member_id = 1

    def on_start(self):
        url = "https://" + os.environ.get("OKTA_DOMAIN") + "/oauth2/default/v1/token"
        basic_auth = "Basic " + b64encode(
            (os.environ.get("OKTA_CLIENT_ID") + ":" + os.environ.get("OKTA_CLIENT_SECRET")).encode("utf-8")).decode(
            "utf-8")
        headers = {"accept": "application/json", "authorization": basic_auth,
                   "content-type": "application/x-www-form-urlencoded"}
        body = "grant_type=password&username=" + os.environ.get("INTAKE_USER") + "&password=" + os.environ.get(
            "INTAKE_PASSWORD") + "&scope=openid"
        self.client.headers = headers
        self.bearer_token = self.client.post(url, body).json()['access_token']
        PrecertificationRequestLoadTest.lock.acquire()
        if PrecertificationRequestLoadTest.avail_member_ids.empty():
            PrecertificationRequestLoadTest.next_member_id += 1
            self.member_id = PrecertificationRequestLoadTest.next_member_id
            print("test started, member id assigned: " + str(self.member_id))
            PrecertificationRequestLoadTest.next_member_id += 1
            PrecertificationRequestLoadTest.avail_member_ids.put(PrecertificationRequestLoadTest.next_member_id)
            print("member id added to queue: " + str(PrecertificationRequestLoadTest.next_member_id))
        else:
            self.member_id = PrecertificationRequestLoadTest.avail_member_ids.get()
            print("test started, member id checked out from queue and assigned: " + str(self.member_id))
        PrecertificationRequestLoadTest.lock.release()

    @task
    def create_and_void_drug_precertification_request(self):
        self.client.headers = {"content-type": "application/json", "Authorization": "Bearer " + self.bearer_token}

        cert_id = ""
        with self.client.post("/precertificationrequests", json={
            "diagnosis": {
                "id": 1
            },
            "drug": {
                "id": 1
            },
            "requestingProvider": {
                "id": 1
            },
            "dueDate": "2021-06-21",
            "member": {
                "id": self.member_id
            },
            "requestDate": "2021-07-19",
            "units": 1
        }, catch_response=True) as response:
            if response.status_code == 422:
                response.success()
                cert_id = "1"
            elif response.status_code == 201:
                n = response.json()['precertNumber']
                cert_id = self.client.get("/precertificationrequests/" + n).json()['id']

        with self.client.put("/precertificationrequests/" + str(cert_id) + "/void", catch_response=True) as response:
            if response.status_code == 422:
                response.success()

    def on_stop(self):
        PrecertificationRequestLoadTest.lock.acquire()
        PrecertificationRequestLoadTest.avail_member_ids.put(self.member_id)
        print("test completed, member id returned to queue: " + str(self.member_id))
        PrecertificationRequestLoadTest.lock.release()
