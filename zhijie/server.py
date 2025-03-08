import multiprocessing
import threading
from flask import Flask
import signal
from tools import Listener, Watcher
import os


app = Flask(__name__)

# Global flag to signal threads to stop
stop_flag = threading.Event()

@app.route('/')
def home():
    return "Spanish speech recognition service is running!"

def signal_handler(sig, frame):
    print("Ctrl+C pressed. Stopping...")
    stop_flag.set()
    os._exit(0)

if __name__ == '__main__':
    # Set up signal handler for Ctrl+C
    signal.signal(signal.SIGINT, signal_handler)

    
    whisper = Listener()
    audio_process = multiprocessing.Process(target=whisper.process(stop_flag))
    
    yolo = Watcher()
    video_process = multiprocessing.Process(target=yolo.process(stop_flag))


    # Run Flask app
    app.run(host='0.0.0.0', port=5000, debug=False)
    audio_process.start()
    video_process.start()

    audio_process.join()
    video_process.join()