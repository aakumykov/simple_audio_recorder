package com.github.aakumykov.simple_audio_recorder;

import androidx.annotation.NonNull;

public interface iSimpleAudioRecorder {
    void startRecording(@NonNull String filePath);
    void stopRecording();
    boolean isRecordingNow();
}
