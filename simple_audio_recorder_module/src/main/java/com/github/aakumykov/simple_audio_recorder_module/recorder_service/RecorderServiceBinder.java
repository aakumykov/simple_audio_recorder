package com.github.aakumykov.simple_audio_recorder_module.recorder_service;

import android.os.Binder;

public class RecorderServiceBinder extends Binder {

    private final RecorderService mRecorderService;

    public RecorderServiceBinder(RecorderService recorderService) {
        mRecorderService = recorderService;
    }

    public RecorderService getRecorderService() {
        return mRecorderService;
    }
}
