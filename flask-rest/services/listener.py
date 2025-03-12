import pyaudio
import numpy as np
from faster_whisper import BatchedInferencePipeline, WhisperModel
from rules import Rules

""" Listener Model configuration """
FORMAT = pyaudio.paInt16
CHANNELS = 1
SAMPLE_RATE = 16000
CHUNK = 2048  # Number of samples per chunk
SILENCE_THRESHOLD = 1000
MIN_AUDIO_LENGTH = 1.0
BATCH_SIZE = 8  # Batch size for parallel processing
NORMALIZER = 32768.0

class Listener:
    def __init__(self):
        """
        Constructor method for this class.
        """
        self.whisper = WhisperModel("large-v3", device="cpu", compute_type="int8")
        self.model = BatchedInferencePipeline(model=self.whisper)
        self.audio = pyaudio.PyAudio()
        self.rules = Rules()
        self.stream = self.audio.open(
            format=FORMAT,
            channels=CHANNELS,
            rate=SAMPLE_RATE,
            input=True,
            input_device_index=None,
            frames_per_buffer=CHUNK,
        )
        self.running = True

    def stop(self):
        """
        Perform cleanup of the listener process on user interrupt.
        """
        self.stream.stop_stream()
        self.stream.close()
        self.audio.terminate()

    def get_audio(self):
        """Wrapper to extract audio data from the process stream.

        Returns:
            tuple: Extracted audio and its raw numpy representation.
        """        
        audio = self.stream.read(CHUNK, exception_on_overflow=False)
        return audio, np.frombuffer(audio, dtype=np.int16)

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
            audio, raw = self.get_audio()

            if np.abs(raw).mean() < SILENCE_THRESHOLD:
                return (
                    np.frombuffer(b"".join(frames), dtype=np.int16).astype(np.float32)
                    / NORMALIZER
                )

            frames.append(audio)

    def start(self):
        """
        Main loop for the process, where audio is captured from the microphone
        until interrupted.
        """
        print("Listening...")
        try:
            while True:
                # Record microphone input
                audio, raw = self.get_audio()

                if np.abs(raw).mean() < SILENCE_THRESHOLD:
                    continue

                print("Processing...")
                raw = self.process(audio)

                # Transcribe the audio using Faster Whisper
                segments, _ = self.model.transcribe(raw, "es", batch_size=8)

                # Analyze and print the recognized text in real-time
                self.rules.analyze(sentence="".join([seg.text for seg in segments]))

        except KeyboardInterrupt:
            print("Stopping...")

        self.stop()


if __name__ == "__main__":
    Listener().start()
