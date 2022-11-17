package com.gitlab.aakumykov.simple_audio_recorder;

import static com.github.aakumykov.argument_utils.ArgumentUtils.checkNotNull;

import android.media.AudioFormat;
import android.media.MediaRecorder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.gitlab.aakumykov.exception_utils_module.ExceptionUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import omrecorder.AudioChunk;
import omrecorder.AudioRecordConfig;
import omrecorder.OmRecorder;
import omrecorder.PullTransport;
import omrecorder.PullableSource;
import omrecorder.Recorder;

public class SimpleAudioRecorder implements AudioRecorder {

    private static final String TAG = SimpleAudioRecorder.class.getSimpleName();
    private final Recorder mRecorder;
    private final AtomicBoolean mIsRecordingNow = new AtomicBoolean(false);
    @Nullable private File mTargetFile;
    @Nullable private Callbacks mCallbacks;


    public SimpleAudioRecorder() {
        mRecorder = OmRecorder.wav(
                new PullTransport.Default(mic(), new PullTransport.OnAudioChunkPulledListener() {
                    @Override public void onAudioChunkPulled(AudioChunk audioChunk) {
                        double amplitude = audioChunk.maxAmplitude();
                        if (null != mCallbacks)
                            mCallbacks.onSoundAmplitudeChanged(amplitude);
                    }
                }),
                mTargetFile
        );
    }


    @Override
    public void setCallbacks(@NonNull Callbacks callbacks) {
        mCallbacks = checkNotNull(callbacks);
    }

    @Override
    public void startRecording(@NonNull File targetFile) throws NullPointerException, FileNotFoundException {

        if (null == targetFile)
            throw new NullPointerException("Аргумент не может быть null");

        if (!targetFile.exists())
            throw new FileNotFoundException("Файл не найден: "+targetFile.getAbsolutePath());

        if (mIsRecordingNow.get()) {
            Log.w(TAG, "startRecording: запись уже идёт");
            return;
        }

        mTargetFile = targetFile;
        mRecorder.startRecording();
        mIsRecordingNow.set(true);

        if (null != mCallbacks)
            mCallbacks.onRecordingStarted();
    }

    @Override
    public void stopRecording() {
        if (!mIsRecordingNow.get())
            return;

        try {
            mRecorder.stopRecording();
            mIsRecordingNow.set(false);
            if (null != mCallbacks)
                mCallbacks.onRecordingFinished(mTargetFile);
        }
        catch (IOException e) {
            Log.e(TAG, ExceptionUtils.getErrorMessage(e), e);
            if (null != mCallbacks)
                mCallbacks.onRecordingError(e);
        }
    }

    @Override
    public boolean isRecordingNow() {
        return mIsRecordingNow.get();
    }


    @NonNull
    private PullableSource.Default mic() {
        return new PullableSource.Default(
                new AudioRecordConfig.Default(
                        MediaRecorder.AudioSource.MIC, AudioFormat.ENCODING_PCM_16BIT,
                        AudioFormat.CHANNEL_IN_MONO, 44100
                )
        );
    }
}
