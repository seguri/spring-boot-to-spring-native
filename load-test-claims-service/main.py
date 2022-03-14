import os
import threading
from base64 import b64encode

from locust import task, HttpUser
from queue import LifoQueue


class ClaimsRequestLoadTest(HttpUser):
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
        ClaimsRequestLoadTest.lock.acquire()
        if ClaimsRequestLoadTest.avail_member_ids.empty():
            ClaimsRequestLoadTest.next_member_id += 1
            self.member_id = ClaimsRequestLoadTest.next_member_id
            print("test started, member id assigned: " + str(self.member_id))
            ClaimsRequestLoadTest.next_member_id += 1
            ClaimsRequestLoadTest.avail_member_ids.put(ClaimsRequestLoadTest.next_member_id)
            print("member id added to queue: " + str(ClaimsRequestLoadTest.next_member_id))
        else:
            self.member_id = ClaimsRequestLoadTest.avail_member_ids.get()
            print("test started, member id checked out from queue and assigned: " + str(self.member_id))
        ClaimsRequestLoadTest.lock.release()

    @task
    def create_and_void_drug_claim(self):
        self.client.headers = {"content-type": "application/json", "Authorization": "Bearer " + self.bearer_token}

        cert_id = ""
        with self.client.post("/claims", json={
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
        }, catch_response=True) as response:
            if response.status_code == 422:
                response.success()
                cert_id = "1"
            elif response.status_code == 201:
                n = response.json()['claimNumber']
                cert_id = self.client.get("/claims/" + n).json()['id']

        with self.client.put("/claims/" + str(cert_id) + "/void", catch_response=True) as response:
            if response.status_code == 422:
                response.success()

    def on_stop(self):
        ClaimsRequestLoadTest.lock.acquire()
        ClaimsRequestLoadTest.avail_member_ids.put(self.member_id)
        print("test completed, member id returned to queue: " + str(self.member_id))
        ClaimsRequestLoadTest.lock.release()
