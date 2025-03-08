import time
import threading
from ultralytics import YOLO
from .rules import Rules
from .API import *

class Watcher:
  def __init__(self):
    self.model = YOLO('./best.pt')
    self.rules = Rules()
  
  def process(self, stop_flag = threading.Event()):
    if self.model is None:
      return

    print("Watching...")

    try:
      while not stop_flag.is_set():
        print("Watching...")
        image = get_image()
        if image is None:
          continue

        for r in self.model(image):
          for box in r.boxes:
              confidence = float(box.conf[0].item())
              if confidence < 0.5:
                continue
                  
              label = box.cls[0].item()
              if label == 0:
                continue

        time.sleep(1)

    except KeyboardInterrupt:
      print("Stopping...")

if __name__ == "__main__":
  Watcher().process()