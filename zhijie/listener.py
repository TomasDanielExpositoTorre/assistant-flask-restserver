import numpy as np
import pyaudio
from faster_whisper import BatchedInferencePipeline, WhisperModel
import threading
from rules import Rules
import subprocess as sp
import os

# Configuration
FORMAT = pyaudio.paInt16  # Audio format (16-bit)
CHANNELS = 1              # Number of audio channels (mono)
RATE = 16000              # Sampling rate (16 kHz)
CHUNK = 2048              # Buffer size (number of samples per chunk)
SILENCE_THRESHOLD = 500   # Silence threshold (adjust as needed)
MIN_AUDIO_LENGTH = 1.0    # Minimum audio length in seconds to process
BATCH_SIZE = 8            # Batch size for parallel processing

class Listener:
    def __init__(self, stream_url):
        self.model = WhisperModel("large-v3", device="cuda", compute_type="int8")
        self.batched_model = BatchedInferencePipeline(model=self.model)
        self.audio = pyaudio.PyAudio()
        self.rules = Rules()
        self.stream_url = stream_url

    def process(self, stop_flag=threading.Event()):
        if self.audio is None:
            return

        # FFmpeg command to stream audio from URL
        ffmpeg_command = [
            'ffmpeg',
            '-i', self.stream_url,  # Input URL
            '-f', 's16le',        # Output format (16-bit PCM)
            '-ac', str(CHANNELS),  # Number of channels
            '-ar', str(RATE),     # Sampling rate
            '-loglevel', 'quiet', # Suppress logs
            '-'                   # Output to stdout
        ]

        # Start FFmpeg process
        ffmpeg_process = sp.Popen(ffmpeg_command, stdout=sp.PIPE, stderr=sp.PIPE)

        # Open PyAudio stream
        stream = self.audio.open(
            format=FORMAT,
            channels=CHANNELS,
            rate=RATE,
            input=True,
            frames_per_buffer=CHUNK
        )

        print("Listening...")

        try:
            while True:
                # Read audio data from FFmpeg stdout
                raw_audio = ffmpeg_process.stdout.read(CHUNK * CHANNELS * 2)  # 2 bytes per sample
                if not raw_audio:
                    break

                # Convert raw audio to numpy array
                audio_np = np.frombuffer(raw_audio, dtype=np.int16)

                # Check if the audio level is above the silence threshold
                if np.abs(audio_np).mean() > SILENCE_THRESHOLD:
                    print("Processing...")
                    frames = [raw_audio]

                    # Continue recording until silence is detected
                    while True:
                        raw_audio = ffmpeg_process.stdout.read(CHUNK * CHANNELS * 2)
                        if not raw_audio:
                            break
                        audio_np = np.frombuffer(raw_audio, dtype=np.int16)
                        if np.abs(audio_np).mean() < SILENCE_THRESHOLD:
                            break
                        frames.append(raw_audio)

                    # Combine the recorded frames into a single audio buffer
                    audio_buffer = b''.join(frames)

                    # Convert the audio buffer to a numpy array
                    audio_np = np.frombuffer(audio_buffer, dtype=np.int16).astype(np.float32) / 32768.0

                    # Transcribe the audio using Faster Whisper
                    segments, _ = self.batched_model.transcribe(
                        audio_np,
                        language="es",
                        batch_size=BATCH_SIZE
                    )

                    # Print the recognized text in real-time
                    sentence = "".join([seg.text for seg in segments])
                    self.rules.analyze(sentence)

        except KeyboardInterrupt:
            print("Stopping...")

        finally:
            # Clean up
            ffmpeg_process.terminate()
            stream.stop_stream()
            stream.close()
            self.audio.terminate()


if __name__ == "__main__":
    # Replace with your streaming URL
    stream_url = "http://192.168.138.11:8080/audio.wav"
    Listener(stream_url).process()