import json
import requests


class Device:
    def __init__(self, data: dict):
        self.id = data["entity_id"]
        self.name = data["attributes"]["friendly_name"]

    def data(self):
        return {"id": self.id, "name": self.name}

    @classmethod
    def get(self, url, headers):
        url = f"{url}/api/states"
        response = requests.get(url, headers=headers)
        return json.loads(response.text)
