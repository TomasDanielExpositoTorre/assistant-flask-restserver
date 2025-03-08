class State:
  def __init__(self, rgb_color=None, kelvin=None, brightness_pct=None):
    self.rgb_color = rgb_color
    self.kelvin = kelvin
    self.brightness_pct = brightness_pct

    if self.kelvin is not None and self.rgb_color is not None:
      self.rgb_color = None

  def load(self, data):
    for attr, value in vars(self).items():
      if value is not None:
        data[attr] = value

class Default(State):
  def __init__(self):
    super().__init__(
      brightness_pct=15,
      kelvin=5000
    )

class Happy(State):
  def __init__(self):
    super().__init__(
      rgb_color=(255, 255, 0), 
      brightness_pct=15
    )

class Sad(State):
  def __init__(self):
    super().__init__(
      brightness_pct=15,
      kelvin=5000
    )

class Red(State):
  def __init__(self):
    super().__init__(
      rgb_color=(255, 0, 0),
      brightness_pct=15
    )

class Blue(State):
  def __init__(self):
    super().__init__(
      rgb_color=(0, 0, 255),
      brightness_pct=15
    )

class Green(State):
  def __init__(self):
    super().__init__(
      rgb_color=(0, 255, 0),
      brightness_pct=15
    )