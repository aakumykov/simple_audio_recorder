package com.github.aakumykov.simple_audio_recorder;

import androidx.annotation.NonNull;

public interface iSimpleRecorder {
    void startRecording(@NonNull String filePath);
    void stopRecording();
    boolean isRecordingNow();
}
