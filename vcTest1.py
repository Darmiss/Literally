import speech_recognition as sr

recognizer = sr.Recognizer()

while True:
    try:
        with sr.Microphone() as mic:
            recognizer.adjust_for_ambient_noise(mic, duration=0.2)
            audio = recognizer.listen(mic)
            
            text = recognizer.recognize_google(audio)
            text = text.lower()
            
            print(f"Recognized {text}")
            #Breaks if inside the tuple it finds the word "literally"
            if text.find("literally") != -1:
                break
    except sr.UnknownValueError:
        recognizer = sr.Recognizer()
        continue
