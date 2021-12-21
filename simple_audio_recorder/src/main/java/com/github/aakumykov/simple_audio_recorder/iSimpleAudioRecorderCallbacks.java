package com.github.aakumykov.simple_audio_recorder;

// TODO: коллбеки-то общие со службой (или классом рекодера в службе?)...

import androidx.annotation.NonNull;

public interface iSimpleAudioRecorderCallbacks {
    void onRecordingStarted();
    void onRecordingFinished(@NonNull String filePath);
    void onRecordingError(@NonNull String errorMsg);
    void onAmplitudeChanged(double value);
}
