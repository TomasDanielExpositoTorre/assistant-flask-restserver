import pyaudio
import numpy as np
from faster_whisper import BatchedInferencePipeline, WhisperModel
import threading
from .rules import Rules

# Configuration
FORMAT = pyaudio.paInt16  # Audio format (16-bit)
CHANNELS = 1              # Number of audio channels (mono)
RATE = 16000              # Sampling rate (16 kHz)
CHUNK = 2048              # Buffer size (number of samples per chunk)
SILENCE_THRESHOLD = 500   # Silence threshold (adjust as needed)
MIN_AUDIO_LENGTH = 1.0    # Minimum audio length in seconds to process
BATCH_SIZE = 8            # Batch size for parallel processing

class Listener:
  def __init__(self):
    self.model = WhisperModel("large-v3", device="cuda", compute_type="int8")
    self.batched_model = BatchedInferencePipeline(model=self.model)
    self.audio = pyaudio.PyAudio()
    self.rules = Rules()
  
  def process(self, stop_flag = threading.Event()):
    if self.audio is None:
      return

    stream = self.audio.open(
      format=FORMAT,
      channels=CHANNELS,
      rate=RATE,
      input=True,
      input_device_index=None,
      frames_per_buffer=CHUNK
    )

    print("Listening...")

    try:
      while not stop_flag.is_set():
        # Read audio data from the microphone
        audio_data = stream.read(CHUNK, exception_on_overflow=False)
        audio_np = np.frombuffer(audio_data, dtype=np.int16)

        # Check if the audio level is above the silence threshold
        if np.abs(audio_np).mean() > SILENCE_THRESHOLD:
          print("Processing...")
          frames = [audio_data]

          # Continue recording until silence is detected
          while True:
              audio_data = stream.read(CHUNK, exception_on_overflow=False)
              audio_np = np.frombuffer(audio_data, dtype=np.int16)
              if np.abs(audio_np).mean() < SILENCE_THRESHOLD:
                  break
              frames.append(audio_data)

          # Combine the recorded frames into a single audio buffer
          audio_buffer = b''.join(frames)

          # Convert the audio buffer to a numpy array
          audio_np = np.frombuffer(audio_buffer, dtype=np.int16).astype(np.float32) / 32768.0

          # Transcribe the audio using Faster Whisper
          segments, _ = self.batched_model.transcribe(
              audio_np, 
              language="es", 
              batch_size=8
          )

          # Print the recognized text in real-time
          sentence = "".join([seg.text for seg in segments])
          self.rules.analyze(sentence)

    except KeyboardInterrupt:
      print("Stopping...")

    finally:
      # Clean up
      stream.stop_stream()
      stream.close()
      self.audio.terminate()

if __name__ == "__main__":
  Listener().process()