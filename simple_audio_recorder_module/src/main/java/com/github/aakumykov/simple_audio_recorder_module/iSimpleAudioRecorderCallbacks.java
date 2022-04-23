package com.github.aakumykov.simple_audio_recorder_module;

// TODO: коллбеки-то общие со службой (или классом рекодера в службе?)...

import androidx.annotation.NonNull;

public interface iSimpleAudioRecorderCallbacks {
    void onRecordingStarted();
    void onRecordingFinished(@NonNull String filePath);
    void onRecordingError(@NonNull String errorMsg);

    /**
     * Коллбек, вызывающийся при изменении амплитуды входящего сигнала микрофона.
     * @param value Значение амплитуды от 0 до 100.
     *              Иногда проскакивает большое отрицательное значение (-2.147483648E9).
     */
    void onAmplitudeChanged(double value);
}
