import pyaudio
import ipywidgets as widgets
import IPython.display as display
from pydub import AudioSegment
from threading import Thread
from queue import Queue
import subprocess
import json
from vosk import Model, KaldiRecognizer



model = Model(model_name="vosk-model-en-us-0.22")
rec = KaldiRecognizer(model, 16000)
rec.SetWords(True)


messages = Queue()
recordings = Queue()

p = pyaudio.PyAudio()
for i in range(p.get_device_count()):
    print(p.get_device_info_by_index(i))
p.terminate()

microphone_index = 1

def record_microphone(chunk = 1024):
    p = pyaudio.PyAudio()
    
    stream = p.open(format=pyaudio.paInt16,
                    channels=1,
                    rate=16000,
                    input=True,
                    input_device_index=2,
                    frames_per_buffer=1024)
    frames = []
    while not messages.empty():
        data = stream.read(chunk)
        if len(frames) >= (16000*20)/chunk:
            recordings.put(frames.copy())
            frames = []
    stream.stop_stream()
    stream.close()
    p.terminate()

def speech_recognition(output):
    while not messages.empty():
        frames = recordings.get()
        
        rec.AcceptWaveform(b''.join(frames))
        result = rec.Result()
        text = json.loads(result)["text"]
        
        cased = subprocess.check_output("python recasepunc/recasepunc.py predict recasepunc/checkpoint", shell=True, text=True, input=text)
        output.append_stdout(cased)
output = widgets.Output()

def start_recording(data):
        messages.put(True)
        with output:
            display("Starting")
            record = Thread(target=record_microphone)
            record.start()
            
            transcribe = Thread(target=speech_recognition, args=(output))
            transcribe.start()
def stop_recording():
    with output:
        messages.get()
        display("Stopping")

def main():
    start_recording()