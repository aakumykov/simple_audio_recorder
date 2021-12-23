package com.github.aakumykov.simple_audio_recorder;

import androidx.annotation.NonNull;

public interface iSimpleAudioRecorderCallbacks {
    void onRecordingStarted();
    void onRecordingFinished(@NonNull String filePath);
    void onRecordingError(@NonNull String errorMsg);
    void onAmplitudeChanged(double value);
}
