"""
This module specifies support for light-emitting devices that can be
regulated from brightness, temperature and/or RGB values.
"""
import json
from .device import Device
import requests
from flask import jsonify


class Light(Device):
    def __init__(self, data: dict):
        """
        Constructor method for this class
        """
        super().__init__(data)

        attrs = data["attributes"]
        self.type = "light"
        self.attributes = {"brightness": [0, 255, attrs["brightness"] or 0]}

        # RGB color mode
        if "hs" in attrs["supported_color_modes"]:
            color = attrs["rgb_color"]
            self.attributes["rgb_color"] = color if color is not None else [0, 0, 0]

        # Temperature color mode
        if "color_temp" in attrs["supported_color_modes"]:
            curr = attrs["color_temp_kelvin"]
            self.attributes["kelvin"] = [
                attrs["min_color_temp_kelvin"],
                attrs["max_color_temp_kelvin"],
                curr if curr is not None else attrs["min_color_temp_kelvin"],
            ]

    def data(self):
        """
        Returns a json-serializable object with device data.
        """
        mapper = {
            "kelvin": "Temperature",
            "brightness": "Brightness",
            "rgb_color": "Color",
        }

        return {
            "id": self.id,
            "name": self.name,
            "type": "light",
            "attributes": {
                mapper[key]: value for key, value in self.attributes.items()
            },
        }
    
    @classmethod
    def get(self, entity_id, url, headers):
        """
        Gets specific device configured in a Home Assistant setup.
        """
        url = f"{url}/api/states"
        response = requests.get(url, headers=headers)
        devices = json.loads(response.text)
        supported_devices = {"light": Light}
        print(entity_id)
        for d in devices:
            dtype, _, _ = d["entity_id"].partition(".")
            if supported_devices.get(dtype) and d["entity_id"] == entity_id: 
                return d
        return None

    @classmethod
    def post(cls, request: dict, url: str, headers: dict):
        """
        Wrapper method to post new light attributes
        """
        mapper = {
            "Brightness": "brightness",
            "Temperature": "kelvin",
            "brightness": "brightness",
            "kelvin": "kelvin",
            "rgb": "rgb_color",
            "entity_id": "entity_id",
        }
        turn_off = request.pop("off")
        # Turn the light off
        if turn_off:
            request = {"entity_id": request["entity_id"]}
            requests.post(
                f"{url}/api/services/light/turn_off",
                headers=headers,
                json=request,
            )
            return jsonify("Turning off!")
        
        # Turn the light on
        attrs = Light.get(request["entity_id"], url, headers)["attributes"]
        request = {mapper[key]: value for key, value in request.items()}
        color = request.get("rgb_color", False)
        temp = request.get("kelvin", False)

        # Remove non-changing values when clashing but prioritize RGB
        if color and temp:
            if (
                temp == attrs["min_color_temp_kelvin"]
                or temp == attrs["color_temp_kelvin"]
            ):
                request.pop("kelvin")
            elif color == attrs["rgb_color"]:
                request.pop("rgb_color")
            else:
                request.pop("kelvin")

        requests.post(
            f"{url}/api/services/light/turn_on",
            headers=headers,
            json=request,
        )
        return jsonify("Turning on!")
