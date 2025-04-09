"""
Base class for supported devices.
"""

import json
import requests


class Device:
    def __init__(self, data: dict):
        """
        Constructor method for this class
        """
        self.id = data["entity_id"]
        self.name = data["attributes"]["friendly_name"]

    def data(self):
        """
        Returns a json-serializable object with device data.
        """
        return {"id": self.id, "name": self.name}

    @classmethod
    def get(self, url, headers):
        """
        Gets all devices configured in a Home Assistant setup.
        """
        url = f"{url}/api/states"
        response = requests.get(url, headers=headers)
        return json.loads(response.text)
