package com.github.aakumykov.simple_audio_recorder.recorder_service;

import androidx.annotation.NonNull;

public interface iRecorderService {

    void startRecording();
    void stopRecording();

    boolean isRecordingNow();

    void setCallbacks(@NonNull iRecorderServiceCallbacks callbacks);
    void unsetCallbacks();
}
