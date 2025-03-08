import re
import webcolors
from .states import *
from .API import *

class Rules:
  def __init__(self):
    self.lamp = re.compile(r"(l[aá]mpara|lapara|luz|lus|lustra|luces)", re.IGNORECASE)
    self.left = re.compile(r"(izquierda|izquierdo)", re.IGNORECASE)
    self.right = re.compile(r"(derecha|derecho)", re.IGNORECASE)
    self.reading = re.compile(r"(lectura)", re.IGNORECASE)

    self.open = re.compile(r"(abre|abrir|hable|habre|habla|habre|enciende)", re.IGNORECASE)
    self.close = re.compile(r"(cierra|cerrar|cierre|apaga)", re.IGNORECASE)

    self.color = re.compile(r"(?:color|colo)\s+([a-z]+)", re.IGNORECASE)
    self.color2 = re.compile(r"(?:color|colo)\s+(\d+)\s+(\d+)\s+(\d+)", re.IGNORECASE)
    self.kelvin = re.compile(r"(?:kelvin|calvin)\s+(\d+)", re.IGNORECASE)
    self.brightness = re.compile(r"(?:brillo)\s+(\d+)", re.IGNORECASE)

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

  def analyze(self, sentence):
    print(sentence)

    # Check if the sentence contains the word "lamp"
    if re.search(self.lamp, sentence) is None:
      return False
    
    if sentence[-1] == ".":
      sentence = sentence[:-1]

    list_id = self.check_lamp(sentence)

    # Close lamp
    if re.search(self.close, sentence) is not None:
      self.close_lamp(list_id)
      return
    
    # Get configuration
    color, kelvin, brightness = self.check_parameters(sentence)
    
    # Open lamp
    self.open_lamp(list_id, color, kelvin, brightness)

  def check_lamp(self, sentence):
    list_id = []
    if re.search(self.left, sentence) is not None:
      list_id.append(LAMP.LEFT)
    if re.search(self.right, sentence) is not None:
      list_id.append(LAMP.RIGHT)
    if re.search(self.reading, sentence) is not None:
      list_id.append(LAMP.READING)
    
    if len(list_id) == 0:
      list_id = [LAMP.LEFT, LAMP.RIGHT, LAMP.READING]
    
    return list_id

  def close_lamp(self, list_id):
    for lamp in list_id:
      control_lamp(Option.OFF, lamp)
  
  def check_parameters(self, sentence):
    # Check color
    color = None
    match = re.search(self.color, sentence)
    if match is not None:
      color_str = match.group(1)
      color = self.color_name_to_rgb(color_str)
    
    match = re.search(self.color2, sentence)
    if match is not None:
      color = (int(match.group(1)), int(match.group(2)), int(match.group(3)))
    
    # Check kelvin, if there is a rgb, rgb is removed
    kelvin = None
    match = re.search(self.kelvin, sentence)
    if match is not None:
      kelvin = int(match.group(1))
      if color is not None:
        color = None
    
    # Check brightness
    brightness = None
    match = re.search(self.brightness, sentence)
    if match is not None:
      brightness = int(match.group(1))
    
    return color, kelvin, brightness

  def color_name_to_rgb(self, color_name):
    try:
        if self.color_translations[color_name] is not None:
          color_name = self.color_translations[color_name]
        rgb = webcolors.name_to_rgb(color_name)
        return (rgb.red, rgb.green, rgb.blue)
    except ValueError:
        return None  # Handle invalid color names

  def open_lamp(self, list_id, color, kelvin, brightness):
    if color is not None or kelvin is not None or brightness is not None:
      for lamp in list_id:
        state = State(rgb_color=color, kelvin=kelvin, brightness_pct=brightness)
        control_lamp(Option.ON, lamp, state)
    else:
      for lamp in list_id:
        control_lamp(Option.ON, lamp, Default())


if __name__ == "__main__":
  sentence = "luz derecha color verde"
  Rules().analyze(sentence)