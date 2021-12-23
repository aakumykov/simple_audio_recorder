package com.github.aakumykov.simple_audio_recorder_module.recorder_service;

import androidx.annotation.NonNull;

public interface iRecorderServiceCallbacks {

    void onRecordingStarted();
    void onRecordingFinished(@NonNull String filePath);
    void onRecordingError(@NonNull String errorMsg);

    void onAmplitude(double amplitudeValue);
    void onRecorderServiceReleased();
}
