package com.github.aakumykov.simple_audio_recorder.recorder_service;

import android.app.Service;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.MediaRecorder;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.github.aakumykov.simple_audio_recorder.utils.ArgumentChecker;
import com.gitlab.aakumykov.exception_utils_module.ExceptionUtils;

import java.io.File;
import java.io.IOException;

import omrecorder.AudioChunk;
import omrecorder.AudioRecordConfig;
import omrecorder.OmRecorder;
import omrecorder.PullTransport;
import omrecorder.PullableSource;
import omrecorder.Recorder;

public class RecorderService extends Service implements iRecorderService {

    public static final String EXTRA_FILE_PATH = "FILE_PATH";
    private static final String TAG = "service_debug";

    private Recorder mRecorder;
    @Nullable private iRecorderServiceCallbacks mCallbacks;
    private boolean mIsRecordingNow = false;
    private File mRecordedFile;


    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        String filePath = intent.getStringExtra(EXTRA_FILE_PATH);
        if (null == filePath)
            throw new IllegalArgumentException("Service start Intent must contains file path data as EXTRA_FILE_PATH.");

        mRecordedFile = new File(filePath);

        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // Log.d(TAG, "*** onBind()");
        return new RecorderServiceBinder(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Log.d(TAG, "onDestroy() ###");

        if (!mIsRecordingNow)
            if (null != mCallbacks)
                mCallbacks.onRecorderServiceReleased();
    }


    @Override
    public void startRecording() {

        if (mIsRecordingNow)
            return;

        prepareRecorder();

        mRecorder.startRecording();
        mIsRecordingNow = true;

        if (null != mCallbacks)
            mCallbacks.onRecordingStarted();
    }

    @Override
    public void stopRecording() {
        try {
            mRecorder.stopRecording();
            mIsRecordingNow = false;
            mRecorder = null;

            if (null != mCallbacks)
                mCallbacks.onRecordingFinished(mRecordedFile.getAbsolutePath());
        }
        catch (IOException e) {
            if (null != mCallbacks)
                mCallbacks.onRecordingError(ExceptionUtils.getErrorMessage(e));
            Log.e(TAG, ExceptionUtils.getErrorMessage(e), e);
        }

        stopSelf();
    }

    @Override
    public boolean isRecordingNow() {
        return mIsRecordingNow;
    }

    @Override
    public void setCallbacks(@NonNull iRecorderServiceCallbacks callbacks) {
        ArgumentChecker.checkNotNull(callbacks);
        mCallbacks = callbacks;
    }

    @Override
    public void unsetCallbacks() {
        mCallbacks = null;
    }


    private void prepareRecorder() {
        mRecorder = OmRecorder.wav(
                new PullTransport.Default(mic(), new PullTransport.OnAudioChunkPulledListener() {
                    @Override public void onAudioChunkPulled(AudioChunk audioChunk) {
                        double amplitude = audioChunk.maxAmplitude();
                        if (null != mCallbacks)
                            mCallbacks.onAmplitude(amplitude);
                    }
                }),
                mRecordedFile
        );
    }

    @NonNull private PullableSource.Default mic() {
        return new PullableSource.Default(
                new AudioRecordConfig.Default(
                        MediaRecorder.AudioSource.MIC, AudioFormat.ENCODING_PCM_16BIT,
                        AudioFormat.CHANNEL_IN_MONO, 44100
                )
        );
    }
}