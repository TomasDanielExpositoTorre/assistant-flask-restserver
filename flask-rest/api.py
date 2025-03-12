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
    data: dict = json.loads(request.data)
    supported_devices = {"light": Light}
    dtype, _, _ = data["entity_id"].partition(".")
    return supported_devices[dtype].post(data, BASE_URL, HEADERS)


@app.route("/profiles", methods=["GET"])
def get_profiles():
    return jsonify(
        {
            "Perfil Sexy": ["Lampara derecha"],
            "Perfil Luz": ["Lampara izquierda", "Lampara de lectura"],
            "Perfil Lectura": [
                "Lampara derecha",
                "Lampara izquierda",
                "Lampara de lectura",
            ],
        }
    )


@app.route("/profiles", methods=["POST"])
def post_profiles():
    data: dict = json.loads(request.data)
    supported_devices = {"light": Light}
    profiles = {
        "Perfil Sexy": [
            {
                "entity_id": "light.lampara_derecha",
                "off": False,
                "Brightness": 255,
                "rgb": [240, 117, 223],
            },
            {
                "entity_id": "light.lampara_de_lectura",
                "off": True,
            },
            {
                "entity_id": "light.lampara_izquierda",
                "off": True,
            },
        ],
        "Perfil Luz": [
            {
                "entity_id": "light.lampara_derecha",
                "off": True,
            },
            {
                "entity_id": "light.lampara_de_lectura",
                "off": False,
                "Brightness": 255,
                "Temperature": 10000,
            },
            {
                "entity_id": "light.lampara_izquierda",
                "off": False,
                "Brightness": 255,
                "Temperature": 10000,
            },
        ],
        "Perfil Lectura": [
            {
                "entity_id": "light.lampara_de_lectura",
                "off": False,
                "Brightness": 150,
                "Temperature": 10,
            },
            {
                "entity_id": "light.lampara_derecha",
                "off": False,
                "Brightness": 150,
                "Temperature": 10,
            },
            {
                "entity_id": "light.lampara_izquierda",
                "off": False,
                "Brightness": 150,
                "Temperature": 10,
            },
        ],
    }

    devices = profiles.get(data.get("profile", ""), [])
    for dev in devices:
        dtype, _, _ = dev["entity_id"].partition(".")
        supported_devices[dtype].post(dev, BASE_URL, HEADERS)

    return jsonify("Subido crack")

app.run(host="0.0.0.0", port=8000, ssl_context=("server.crt", "server.key"))
