package com.github.aakumykov.simple_audio_recorder;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;

import com.github.aakumykov.simple_audio_recorder.recorder_service.RecorderService;
import com.github.aakumykov.simple_audio_recorder.recorder_service.RecorderServiceBinder;
import com.github.aakumykov.simple_audio_recorder.recorder_service.iRecorderServiceCallbacks;

// TODO: логгирование ошибки, если проигрывателю передаётся пустой путь

/*
Пути дальнейшего развития:
1) многократный запуск рекодера без остановки службы;
2) вызов коллбека "завершено" по месту завершения записи, а не
разрушения службы.
 */

public class SimpleAudioRecorder
        implements iSimpleAudioRecorder, DefaultLifecycleObserver
{
    public static final String TAG = "service_debug";

    @NonNull private final Intent mRecorderServiceIntent;
    @NonNull private final ServiceConnection mRecorderServiceConnection;
    @NonNull private final iRecorderServiceCallbacks mRecorderServiceCallbacks;
    @Nullable private RecorderService mRecorderService;

    @NonNull private final Context mContext;
    @NonNull private final iSimpleAudioRecorderCallbacks mCallbacks;
    @Nullable private String mFilePath;


    // TODO: идентичность коллбеков...
    public SimpleAudioRecorder(@NonNull Context context,
                               @NonNull iSimpleAudioRecorderCallbacks callbacks) {

        mContext = context;
        mCallbacks = callbacks;

        mRecorderServiceIntent = new Intent(context, RecorderService.class);

        mRecorderServiceCallbacks = new iRecorderServiceCallbacks() {
            @Override
            public void onAmplitude(double amplitudeValue) {
                mCallbacks.onAmplitudeChanged(amplitudeValue);
            }

            @Override
            public void onRecordingStarted() {
                mCallbacks.onRecordingStarted();
            }

            @Override
            public void onRecordingFinished(@NonNull String filePath) {
                mFilePath = filePath;
            }

            @Override
            public void onRecordingError(@NonNull String errorMsg) {
                mCallbacks.onRecordingError(errorMsg);
            }

            @Override
            public void onRecorderServiceReleased() {
                /*SimpleAudioRecorder говорит, что отработал,
                когда сама служба остановилась, а не когда
                в ней остановилась запись. Это хак против
                позднего прихода значения амплитуды.*/
                mCallbacks.onRecordingFinished(mFilePath);
            }
        };

        mRecorderServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                mRecorderService = ((RecorderServiceBinder) service).getRecorderService();
                mRecorderService.setCallbacks(mRecorderServiceCallbacks);

                if (!mRecorderService.isRecordingNow())
                    mRecorderService.startRecording();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                /* Здесь не нужно убирать коллбеки
                 * из службы, так как служба
                 * самоуничтожается. А в процессе этого
                 * уничтожения, их нужно ещё дёрнуть. */
//                Log.d(TAG, "onServiceDisconnected()");
                mRecorderService = null;
            }
        };
    }


    // TODO: а сейчас же он внутри ViewModel...
    // Жизненный цикл Activity
    @Override
    public void onStart(@NonNull LifecycleOwner owner) {
        bindToService();
    }

    @Override
    public void onStop(@NonNull LifecycleOwner owner) {
        unbindFromService();
    }


    // Главные методы
    @Override
    public void startRecording(@NonNull String filePath) {
        mRecorderServiceIntent.putExtra(RecorderService.EXTRA_FILE_PATH, filePath);
        bindToService();
        mContext.startService(mRecorderServiceIntent);
    }

    @Override
    public void stopRecording() {
        if (null != mRecorderService)
            mRecorderService.stopRecording();
    }

    @Override
    public boolean isRecordingNow() {
        if (null == mRecorderService)
            return false;
        else
            return mRecorderService.isRecordingNow();
    }


    // Внутренние методы
    private void bindToService() {
//        Log.d(TAG, "bindToService()");
        mContext.bindService(mRecorderServiceIntent,
                mRecorderServiceConnection,
                0/*Context.BIND_IMPORTANT*/);
    }

    private void unbindFromService() {
//        Log.d(TAG, "unbindFromService()");
        mContext.unbindService(mRecorderServiceConnection);
    }

}