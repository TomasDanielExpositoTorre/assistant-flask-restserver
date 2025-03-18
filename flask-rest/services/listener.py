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
NORMALIZER = 32768.0
STREAM_URL = "http://192.168.138.11:8080/audio.wav"

class Listener:
    def __init__(self, stream_url):
        self.whisper = WhisperModel("large-v3", device="cuda", compute_type="int8")
        self.model = BatchedInferencePipeline(model=self.whisper)
        self.rules = Rules()
        self.ffmpeg = self.open_ffmpeg(stream_url)
        self.audio, self.stream = self.open_stream()
        
    def open_ffmpeg(self, stream_url):
        # FFmpeg command to stream audio from URL
        ffmpeg_command = [
            'ffmpeg',
            '-i', stream_url,     # Input URL
            '-f', 's16le',        # Output format (16-bit PCM)
            '-ac', str(CHANNELS), # Number of channels
            '-ar', str(RATE),     # Sampling rate
            '-loglevel', 'quiet', # Suppress logs
            '-'                   # Output to stdout
        ]
        # Start FFmpeg process
        return sp.Popen(ffmpeg_command, stdout=sp.PIPE, stderr=sp.PIPE)
    
    def open_stream(self):
        # Open PyAudio stream
        audio = pyaudio.PyAudio()
        return audio, audio.open(
            format=FORMAT,
            channels=CHANNELS,
            rate=RATE,
            input=True,
            frames_per_buffer=CHUNK
        )
    
    def process(self, audio):
        """
        Wrapper function to record and process audio until silence is detected.

        Args:
            audio (bytes): Initial audio section

        Returns:
            NDarray: Raw numpy array representation of the recorded audio buffer.
        """
        frames = [audio]
        while True:
            audio = self.ffmpeg.stdout.read(CHUNK * CHANNELS * 2)
            audio_np = np.frombuffer(audio, dtype=np.int16)
            
            if not audio or np.abs(audio_np).mean() < SILENCE_THRESHOLD:
                return np.frombuffer(b''.join(frames), dtype=np.int16).astype(np.float32) / 32768.0

            frames.append(audio)


    def start(self):
        """
        Main loop for the process, where audio is captured from the microphone
        until interrupted.
        """
        if self.audio is None:
            return

        print("Listening...")
        try:
            while True:
                # Read audio data from FFmpeg stdout
                audio = self.ffmpeg.stdout.read(CHUNK * CHANNELS * 2)  # 2 bytes per sample
                if not audio:
                    break

                # Convert raw audio to numpy array
                audio_np = np.frombuffer(audio, dtype=np.int16)

                if np.abs(audio_np).mean() <= SILENCE_THRESHOLD:
                    continue

                print("Processing...")
                raw = self.process(audio)

                # Transcribe the audio using Faster Whisper
                segments, _ = self.model.transcribe(raw, "es", batch_size=BATCH_SIZE)

                # Analyze and print the recognized text in real-time
                self.rules.analyze("".join([seg.text for seg in segments]))

        except KeyboardInterrupt:
            print("Stopping...")

        self.stop()
    
    def stop(self):
        """
        Perform cleanup of the listener process on user interrupt.
        """
        self.ffmpeg.terminate()
        self.stream.stop_stream()
        self.stream.close()
        self.audio.terminate()

if __name__ == "__main__":
    Listener(STREAM_URL).start()