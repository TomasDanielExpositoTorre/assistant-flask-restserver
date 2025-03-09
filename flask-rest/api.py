import requests
import json
from flask import Flask, jsonify, request

app = Flask(__name__)


@app.route("/devices", methods=["GET"])
def get_devices():
    lights = fetch_lights(fetch_devices())
    data = []

    for l in lights:
        attrs = l["attributes"]
        # Default attributes
        dev = {
            "id": l["entity_id"],
            "name": attrs["friendly_name"],
            "type": "light",
            "attributes": {
                "Brightness": [
                    0,
                    255,
                    attrs["brightness"] if attrs["brightness"] is not None else 0,
                ]
            },
        }

        # RGB color mode
        if "hs" in attrs["supported_color_modes"]:
            dev["attributes"]["Color"] = (
                attrs["rgb_color"] if attrs["rgb_color"] is not None else [0, 0, 0]
            )

        # Temperature color mode
        if "color_temp" in attrs["supported_color_modes"]:
            dev["attributes"]["Temperature"] = [
                attrs["min_color_temp_kelvin"],
                attrs["max_color_temp_kelvin"],
                (
                    attrs["color_temp_kelvin"]
                    if attrs["color_temp_kelvin"] is not None
                    else attrs["min_color_temp_kelvin"]
                ),
            ]

        data.append(dev)
    return jsonify(data)


@app.route("/devices", methods=["POST"])
def post_devices():
    # Convert byte string to a regular string
    data: dict = json.loads(request.data)
    mapping = {
        "Brightness": "brightness",
        "Temperature": "kelvin",
        "rgb": "rgb_color",
        "entity_id": "entity_id",
    }
    off = data.pop("off")
    headers = {
        "Authorization": "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiI0NDdlODRkYTExNmI0Yzk2YjIxM2ExMDdhMTY2NmVmMCIsImlhdCI6MTY3ODI3MTU4OSwiZXhwIjoxOTkzNjMxNTg5fQ.-jUvyVsmzQMxrkRd1kLNM_PIH1HTGnzsOzESDWlPukE",
        "Content-Type": "application/json",
    }
    l = fetch_light(data["entity_id"])["attributes"]

    if off == False:
        req = {mapping[key]: value for key, value in data.items()}
        color = req.get("rgb_color", False)
        temp = req.get("kelvin", False)

        # Remove non-changing values when clashing but prioritize RGB
        if color:
            if temp == l["min_color_temp_kelvin"] or temp == l["color_temp_kelvin"]:
                req.pop("kelvin")
            elif color == l["rgb_color"]:
                req.pop("rgb_color")
            else:
                req.pop("kelvin")
        requests.post(
            f"https://danubio.ii.uam.es/api/services/light/turn_on",
            headers=headers,
            json=req,
        )
    else:
        req = {"entity_id": data["entity_id"]}
        requests.post(
            f"https://danubio.ii.uam.es/api/services/light/turn_off",
            headers=headers,
            json=req,
        )

    return jsonify("Hola reloj!")


def fetch_devices():
    url = "https://danubio.ii.uam.es/api/states"
    headers = {
        "Authorization": "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiI0NDdlODRkYTExNmI0Yzk2YjIxM2ExMDdhMTY2NmVmMCIsImlhdCI6MTY3ODI3MTU4OSwiZXhwIjoxOTkzNjMxNTg5fQ.-jUvyVsmzQMxrkRd1kLNM_PIH1HTGnzsOzESDWlPukE",
        "Content-Type": "application/json",
    }
    response = requests.get(url, headers=headers)
    return json.loads(response.text)


def fetch_light(id):
    url = f"https://danubio.ii.uam.es/api/states/{id}"
    headers = {
        "Authorization": "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiI0NDdlODRkYTExNmI0Yzk2YjIxM2ExMDdhMTY2NmVmMCIsImlhdCI6MTY3ODI3MTU4OSwiZXhwIjoxOTkzNjMxNTg5fQ.-jUvyVsmzQMxrkRd1kLNM_PIH1HTGnzsOzESDWlPukE",
        "Content-Type": "application/json",
    }
    response = requests.get(url, headers=headers)
    return json.loads(response.text)


def fetch_lights(devices: list):
    return [d for d in devices if "light." in d["entity_id"]]


app.run(host="0.0.0.0", port=8000, ssl_context=("server.crt", "server.key"))
