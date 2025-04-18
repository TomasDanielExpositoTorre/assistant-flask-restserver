"""
Rest API module.
"""

import json
from flask import Flask, request, jsonify
from dotenv import load_dotenv
import os
from flask_cors import CORS
from devices.device import Device
from devices.light import Light
import requests

load_dotenv()

BASE_URL = f"{os.getenv('BASE_URL')}"
HEADERS = {
    "Content-Type": "application/json",
    "Authorization": f"Bearer {os.getenv('TOKEN')}",
}
app = Flask(__name__)
cors = CORS(app, resources={r"/*": {"origins": "*"}})


@app.route("/devices", methods=["GET"])
def get_devices():
    """
    Get all available (supported) devices from the Home Assistant endpoint.
    """
    devices = Device.get(BASE_URL, HEADERS)
    supported_devices = {"light": Light}
    data = []
    for d in devices:
        dtype, _, _ = d["entity_id"].partition(".")
        if supported_devices.get(dtype):
            dev: Device = supported_devices[dtype](d)
            data.append(dev.data())

    return jsonify(data)


@app.route("/devices", methods=["POST"])
def post_devices():
    """
    Petition wrapper to modify the state of a device.
    """
    data: dict = json.loads(request.data)
    supported_devices = {"light": Light}
    dtype, _, _ = data["entity_id"].partition(".")
    return supported_devices[dtype].post(data, BASE_URL, HEADERS)


@app.route("/profiles", methods=["GET"])
def get_profiles():
    """
    Returns a set of pre-defined profile titles that can be applied to a device
    set.
    """
    global profiles

    return jsonify(
        [
            {"name": key, "devices": [device.get("name", "X") for device in devices]}
            for key, devices in profiles.items()
        ]
    )


@app.route("/profiles", methods=["POST"])
def post_profiles():
    """
    Applies the received a pre-defined profile to the setup.
    """
    global profiles
    data: dict = json.loads(request.data)
    supported_devices = {"light": Light}

    devices = profiles.get(data.get("profile", ""), [])
    for dev in devices:
        dtype, _, _ = dev["entity_id"].partition(".")
        supported_devices[dtype].post(
            {key: value for key, value in dev.items() if key != "name"},
            BASE_URL,
            HEADERS,
        )

    return jsonify("Profile applied!")


with open("data/profiles.json", "r") as file:
    profiles: dict = json.load(file)
    devices = Device.get(BASE_URL, HEADERS)


app.run(host="0.0.0.0", port=8000, ssl_context=("data/server.crt", "data/server.key"))
