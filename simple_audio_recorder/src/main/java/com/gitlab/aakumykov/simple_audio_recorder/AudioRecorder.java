package com.gitlab.aakumykov.simple_audio_recorder;

import androidx.annotation.NonNull;

import java.io.File;

public interface AudioRecorder {

    void setCallbacks(@NonNull Callbacks callbacks);
    void startRecording(@NonNull File targetFile) throws NullPointerException;
    void stopRecording();
    boolean isRecordingNow();

    interface Callbacks {
        void onRecordingStarted();
        void onRecordingFinished(@NonNull File recordedFile);
        void onRecordingError(@NonNull Exception e);
        void onSoundAmplitudeChanged(double amplitudeValue);
    }
}
