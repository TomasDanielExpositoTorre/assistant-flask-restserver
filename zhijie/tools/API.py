from io import BytesIO
import numpy as np
import requests
from enum import Enum
from PIL import Image
from .states import *

import os
from dotenv import load_dotenv
load_dotenv()



BASE_URL = f"{os.getenv('URL')}/api"
HEADERS = {
  "Content-Type": "application/json",
  "Authorization": f"Bearer {os.getenv('TOKEN')}"
}

class Option(Enum):
  ON = "on"
  OFF = "off"

  def __str__(self):
    return self.value

class LAMP(Enum):
  LEFT = "light.lampara_izquierda"
  RIGHT = "light.lampara_derecha"
  READING = "light.lampara_de_lectura"

  def __str__(self):
    return self.value

def control_lamp(action, lamp, state=None):
  url = f"{BASE_URL}/services/light/turn_{action}"
  data = {"entity_id": lamp.value}
  if action == Option.ON:
    state.load(data)

  response = requests.post(url, headers=HEADERS, json=data)
  return response

def get_image():
  url = f"{BASE_URL}/camera_proxy/camera.amicam"
  try:
    response = requests.get(url, headers=HEADERS, stream=True) #Stream to handle large images.
    response.raise_for_status()  # Raise an exception for bad status codes (4xx or 5xx)

    image_data = BytesIO(response.content)
    img = Image.open(image_data)
    img_rgb = img.convert("RGB") #Ensure image is RGB
    img_np = np.array(img_rgb)
    return img_np
  except requests.exceptions.RequestException as e:
    print(f"Error fetching image: {e}")
    return None
  except Exception as e: # Catch other potential errors like invalid image format.
    print(f"Error processing image: {e}")
    return None

# if __name__ == "__main__":
#   image = get_image()
#   if image is not None:
#     cv2.imwrite("image.jpg", image)