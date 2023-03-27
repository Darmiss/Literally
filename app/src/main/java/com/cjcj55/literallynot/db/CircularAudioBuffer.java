package com.cjcj55.literallynot.db;

public class CircularAudioBuffer {
    private final byte[] buffer;
    private int writeIndex;
    private int readIndex;
    private int availableData;

    public CircularAudioBuffer(int capacity) {
        buffer = new byte[capacity];
        writeIndex = 0;
        readIndex = 0;
        availableData = 0;
    }

    public synchronized void write(byte[] data, int length) {
        for (int i = 0; i < length; i++) {
            buffer[writeIndex] = data[i];
            writeIndex = (writeIndex + 1) % buffer.length;
            availableData = Math.min(availableData + 1, buffer.length);
        }
    }

    public synchronized byte[] read(int length) {
        if (length > availableData) {
            length = availableData;
        }
        byte[] result = new byte[length];

        for (int i = 0; i < length; i++) {
            result[i] = buffer[readIndex];
            readIndex = (readIndex + 1) % buffer.length;
            availableData--;
        }

        return result;
    }

    public byte[] toByteArray() {
        byte[] audioData = new byte[availableData];

        int tempReadIndex = readIndex;
        for (int i = 0; i < availableData; i++) {
            audioData[i] = buffer[tempReadIndex];
            tempReadIndex = (tempReadIndex + 1) % buffer.length;
        }

        return audioData;
    }
}
