"""
Module that defines the ruleset for the listener service.
"""
import re
import webcolors
import requests
import os
from dotenv import load_dotenv

load_dotenv('../.env')

class Rules:
    def __init__(self):

        self.api = os.getenv("API_URL", "https://glados.local:8000")
        self.lights = re.compile(
            r"(l[aá]mpara|lapara|luz|lus|lustra|luces)", re.IGNORECASE
        )

        self.patterns = {
            "light.lampara_izquierda": re.compile(
                r"(izquierda|izquierdo)", re.IGNORECASE
            ),
            "light.lampara_derecha": re.compile(r"(derecha|derecho)", re.IGNORECASE),
            "light.lampara_de_lectura": re.compile(r"(lectura)", re.IGNORECASE),
        }

        self.on = re.compile(
            r"(abre|abrir|hable|habre|habla|habre|enciende|encender|prender)", re.IGNORECASE
        )
        self.off = re.compile(r"(cierra|cerrar|cierre|apaga)", re.IGNORECASE)

        self.color = re.compile(r"(?:color|colo)\s+([a-z]+)", re.IGNORECASE)
        self.color2 = re.compile(
            r"(?:color|colo)\s+(\d+)\s+(\d+)\s+(\d+)", re.IGNORECASE
        )
        self.kelvin = re.compile(r"(?:kelvin|calvin)\s+(\d+)", re.IGNORECASE)
        self.brightness = re.compile(r"(?:brillo)\s+(\d+)", re.IGNORECASE)
        self.profile = re.compile(r"(?:perfil|prefil|profile|porfile|porfavor)\s+(\d+)", re.IGNORECASE)
        self.color_translations = {
            "rojo": "red",
            "naranja": "orange",
            "amarillo": "yellow",
            "verde": "green",
            "azul": "blue",
            "morado": "purple",
            "violeta": "violet",
            "rosa": "pink",
            "marrón": "brown",
            "cafe": "brown",
            "negro": "black",
            "blanco": "white",
            "gris": "gray",
            "plateado": "silver",
            "dorado": "gold",
            "cian": "cyan",
            "magenta": "magenta",
            "lima": "lime",
            "turquesa": "turquoise",
            "beige": "beige",
        }

    def get_lights(self, sentence):
        lights = [
            light
            for light, pattern in self.patterns.items()
            if re.search(pattern, sentence)
        ]

        if not len(lights):
            lights = [light for light in self.patterns]

        return lights

    def color_name_to_rgb(self, color_name):
        try:
            if self.color_translations[color_name] is not None:
                color_name = self.color_translations[color_name]
            rgb = webcolors.name_to_rgb(color_name)
            return [rgb.red, rgb.green, rgb.blue]
        except ValueError:
            return [0, 0, 0]

    def get_parameters(self, sentence):
        color, kelvin, brightness = None, None, None

        if _ := re.search(self.color, sentence):
            color = self.color_name_to_rgb(_.group(1))
        elif _ := re.search(self.color2, sentence):
            color = [int(_.group(1)), int(_.group(2)), int(_.group(3))]
        kelvin = int(_.group(1)) if (_ := re.search(self.kelvin, sentence)) else None
        brightness = (
            int(_.group(1)) if (_ := re.search(self.brightness, sentence)) else None
        )

        # Prioritize temperature over RGB
        if kelvin and color:
            color = None

        return color, kelvin, brightness

    def analyze(self, sentence):
        print(sentence)

        if _ :=  re.search(self.profile, sentence):
            requests.post(
                url=f"{self.api}/profiles",
                json={"profile": f"Perfil {_.group(1)}"},
                verify="../server.pem",
            )
            return True
        if not re.search(self.lights, sentence):
            return False
        
        sentence = sentence[:-1] if sentence[-1] == "." else sentence
        lights = self.get_lights(sentence)

        if re.search(self.off, sentence):
            self.turn_off(lights)
        elif re.search(self.on, sentence):
            color, kelvin, brightness = self.get_parameters(sentence)
            self.turn_on(lights, color, kelvin, brightness)

    def turn_off(self, lights):
        for light in lights:
            requests.post(
                url=f"{self.api}/devices",
                json={"entity_id": light, "off": True},
                verify="../server.pem",
            )

    def turn_on(self, lights, color, kelvin, brightness):
        request = {}

        # Add parameters
        if color:
            request["rgb"] = color
        if kelvin:
            request["kelvin"] = kelvin
        if brightness:
            request["brightness"] = brightness

        # Use default when no parameters are provided
        if not (color or kelvin or brightness):
            request = {"brightness": 150, "kelvin": 4000}

        for light in lights:
            requests.post(
                url=f"{self.api}/devices",
                json={**request, "entity_id": light, "off": False},
                verify="../server.pem",
            )


if __name__ == "__main__":
    sentence = "enciende luz derecha color rojo"
    Rules().analyze(sentence)
